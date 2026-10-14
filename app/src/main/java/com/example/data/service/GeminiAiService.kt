package com.example.data.service

import com.example.BuildConfig
import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiAiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    suspend fun generateQuizQuestions(
        topic: String,
        count: Int = 4
    ): List<QuizQuestion> = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    لطفاً $count سوال چهارگزینه‌ای چالشی، جذاب و علمی در حوزه "$topic" به زبان فارسی تولید کن.
                    پاسخ را دقیقاً در قالب یک آرایه JSON معتبر برگردان بدون هیچ متن اضافی:
                    [
                      {
                        "question": "متن سوال",
                        "options": ["گزینه ۱", "گزینه ۲", "گزینه ۳", "گزینه ۴"],
                        "correctIndex": 0,
                        "explanation": "توضیح کوتاه پاسخ صحیح",
                        "difficulty": "MEDIUM"
                      }
                    ]
                """.trimIndent()

                val jsonPayload = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                    val genConfig = JSONObject().apply {
                        put("responseMimeType", "application/json")
                    }
                    put("generationConfig", genConfig)
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val root = JSONObject(respBody)
                    val candidates = root.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val text = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")

                        val parsedArray = JSONArray(text)
                        val questionsList = mutableListOf<QuizQuestion>()
                        for (i in 0 until parsedArray.length()) {
                            val qObj = parsedArray.getJSONObject(i)
                            val optionsArray = qObj.getJSONArray("options")
                            val optionsList = mutableListOf<String>()
                            for (j in 0 until optionsArray.length()) {
                                optionsList.add(optionsArray.getString(j))
                            }
                            questionsList.add(
                                QuizQuestion(
                                    id = "ai_q_" + UUID.randomUUID().toString().take(6),
                                    question = qObj.getString("question"),
                                    options = optionsList,
                                    correctIndex = qObj.optInt("correctIndex", 0),
                                    explanation = qObj.optString("explanation", "پاسخ صحیح تایید شد."),
                                    category = topic,
                                    difficulty = Difficulty.HARD,
                                    timeLimitSeconds = 15,
                                    basePoints = 150
                                )
                            )
                        }
                        if (questionsList.isNotEmpty()) {
                            return@withContext questionsList
                        }
                    }
                }
            } catch (e: Exception) {
                // Fallback to offline curated AI questions
            }
        }
        return@withContext getOfflineAiQuestions(topic)
    }

    suspend fun analyzeUserPerformance(
        wins: Int,
        totalPlayed: Int,
        totalPoints: Long,
        winRate: Int
    ): String = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val prompt = """
                    کاربر با آمار زیر در پلتفرم چالش و مسابقه مسابقه داده است:
                    تعداد مسابقات: $totalPlayed
                    تعداد بردها: $wins
                    درصد پیروزی: $winRate%
                    امتیاز کل: $totalPoints
                    یک تحلیل هوشمند، روانشناسی عملکرد، نقاط قوت و ۳ استراتژی کلیدی برای رسیدن به رتبه ۱ در تورنمنت بزرگ کشوری به زبان فارسی جذاب بنویس.
                """.trimIndent()

                val jsonPayload = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)
                }

                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val respBody = response.body?.string() ?: ""
                    val root = JSONObject(respBody)
                    val text = root.getJSONArray("candidates")
                        .getJSONObject(0)
                        .getJSONObject("content")
                        .getJSONArray("parts")
                        .getJSONObject(0)
                        .getString("text")
                    return@withContext text
                }
            } catch (e: Exception) {
                // Fallback
            }
        }
        return@withContext getOfflinePerformanceAnalysis(wins, totalPlayed, winRate)
    }

    private fun getOfflineAiQuestions(topic: String): List<QuizQuestion> {
        return when {
            topic.contains("فناوری") || topic.contains("هوش مصنوعی") -> listOf(
                QuizQuestion(
                    id = "ai_tech_1",
                    question = "معماری پایه Transformer که مدل‌های زبانی مانند Gemini بر آن استوارند در چه سالی معرفی شد؟",
                    options = listOf("۲۰۱۵", "۲۰۱۷ (مقاله Attention is all you need)", "۲۰۱۹", "۲۰۲۱"),
                    correctIndex = 1,
                    explanation = "مقاله مشهور پژوهشگران گوگل در سال ۲۰۱۷ ترانسفورمر را به عنوان پایه هوش مصنوعی مدرن معرفی کرد.",
                    category = topic,
                    difficulty = Difficulty.HARD,
                    timeLimitSeconds = 15,
                    basePoints = 160
                ),
                QuizQuestion(
                    id = "ai_tech_2",
                    question = "کدام مفهوم در شبکه‌های کامپیوتری زمان تاخیر رفت و برگشت داده را اندازه می‌گیرد؟",
                    options = listOf("Ping / Latency", "Bandwidth", "Throughput", "Jitter"),
                    correctIndex = 0,
                    explanation = "پینگ مدت زمان رفت و برگشت یک بسته اطلاعاتی از دستگاه کاربر به سرور است.",
                    category = topic,
                    difficulty = Difficulty.MEDIUM,
                    timeLimitSeconds = 12,
                    basePoints = 120
                ),
                QuizQuestion(
                    id = "ai_tech_3",
                    question = "کدام رمزنگاری در معاملات بانکی و ارتباطات امن HTTPS به کار می‌رود؟",
                    options = listOf("AES و RSA", "ROT13", "Base64", "MD5"),
                    correctIndex = 0,
                    explanation = "پروتکل TLS از تلفیق رمزنگاری نامتقارن RSA/ECC و رمزنگاری متقارن AES برای امنیت داده‌ها استفاده می‌کند.",
                    category = topic,
                    difficulty = Difficulty.HARD,
                    timeLimitSeconds = 15,
                    basePoints = 150
                )
            )
            topic.contains("ریاضی") || topic.contains("هوش") -> listOf(
                QuizQuestion(
                    id = "ai_math_1",
                    question = "مجموع زوایای داخلی یک پنج‌ضلعی منتظم چند درجه است؟",
                    options = listOf("۳۶۰ درجه", "۵۴۰ درجه", "۷۲۰ درجه", "۹۰۰ درجه"),
                    correctIndex = 1,
                    explanation = "فرمول مجموع زوایای داخلی: (n-2) * 180 = (5-2) * 180 = 540 درجه.",
                    category = topic,
                    difficulty = Difficulty.MEDIUM,
                    timeLimitSeconds = 15,
                    basePoints = 130
                ),
                QuizQuestion(
                    id = "ai_math_2",
                    question = "تنها عدد اول زوج در جهان کدام است؟",
                    options = listOf("۰", "۱", "۲", "۴"),
                    correctIndex = 2,
                    explanation = "عدد ۲ تنها عدد اول زوج است زیرا سایر اعداد زوج بر ۲ بخش‌پذیرند.",
                    category = topic,
                    difficulty = Difficulty.EASY,
                    timeLimitSeconds = 10,
                    basePoints = 100
                ),
                QuizQuestion(
                    id = "ai_math_3",
                    question = "اگر تاس استانداردی پرتاب شود، احتمال آمدن عددی بزرگتر از ۴ چقدر است؟",
                    options = listOf("۱ به ۶", "۱ به ۳ (یا ۲ به ۶)", "۱ به ۲", "۲ به ۳"),
                    correctIndex = 1,
                    explanation = "اعداد ۵ و ۶ دو حالت مطلوب از شش حالت ممکن هستند: ۲/۶ = ۱/۳.",
                    category = topic,
                    difficulty = Difficulty.MEDIUM,
                    timeLimitSeconds = 15,
                    basePoints = 140
                )
            )
            else -> listOf(
                QuizQuestion(
                    id = "ai_gen_1",
                    question = "کهن‌ترین تمدن تاریخی که سیستم خط و قانون‌نویسی مکتوب را پدید آورد کدام است؟",
                    options = listOf("تمدن سومر و بین‌النهرین", "مایاها", "یونان باستان", "وایکینگ‌ها"),
                    correctIndex = 0,
                    explanation = "سومریان نخستین خط میخی مکتوب را در بین‌النهرین ابداع کردند.",
                    category = topic,
                    difficulty = Difficulty.MEDIUM,
                    timeLimitSeconds = 15,
                    basePoints = 130
                ),
                QuizQuestion(
                    id = "ai_gen_2",
                    question = "کدام سیاره در منظومه شمسی کوتاه‌ترین طول شبانه‌روز (سریع‌ترین چرخش وضعی) را دارد؟",
                    options = listOf("زمین", "مریخ", "مشتری (ژوپیتر)", "عطارد"),
                    correctIndex = 2,
                    explanation = "مشتری هر ۹ ساعت و ۵۵ دقیقه یک بار دور خود می‌چرخد و سریع‌ترین چرخش وضعی را دارد.",
                    category = topic,
                    difficulty = Difficulty.HARD,
                    timeLimitSeconds = 15,
                    basePoints = 150
                )
            )
        }
    }

    private fun getOfflinePerformanceAnalysis(wins: Int, totalPlayed: Int, winRate: Int): String {
        return """
            🎯 تحلیل اختصاصی هوش مصنوعی چالش و مسابقه:
            
            ⚡ قدرت تصمیم‌گیری و سرعت پاسخ:
            شما با نرخ پیروزی $winRate درصد از مجموع $totalPlayed مسابقه، جزو ۱۰٪ بازیکنان باهوش و باثبات در جدول کشوری قرار دارید.
            
            🧠 نقاط قوت شما:
            • سرعت استثنایی در پاسخ به سوالات ریاضی و حل معماهای منطقی
            • حفظ آرامش و کسب ضریب امتیاز (Streak Multiplier) در سوالات دارای زمان محدود
            • مدیریت سرمایه موثر در انتخاب مسابقات با سود نقدی بالا
            
            🚀 راهکارهای ارتقا به رتبه ۱ کشوری:
            ۱. شرکت در لیگ هفتگی: ضریب امتیاز دهی در لیگ هفتگی ۱.۵ برابر مسابقات عادی است.
            ۲. چالش هوش مصنوعی روزانه: انجام حداقل یک چالش اختصاصی پیش از ورود به گرند تورنمنت ۱۰۰ میلیونی تمرکز ذهنی را تا ۴۰٪ افزایش می‌دهد.
            ۳. تقویت حافظه تاریخی و ادبیات معاصر جهت پاسخگویی به سوالات درجه سختی Expert.
        """.trimIndent()
    }
}
