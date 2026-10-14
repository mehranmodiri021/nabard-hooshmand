package com.example.data.repository

import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class TournamentRepository {

    private val _tournaments = MutableStateFlow<List<Tournament>>(emptyList())
    val tournaments: StateFlow<List<Tournament>> = _tournaments.asStateFlow()

    init {
        _tournaments.value = generateInitialTournaments()
    }

    fun getTournamentById(id: String): Tournament? {
        return _tournaments.value.find { it.id == id }
    }

    fun joinTournament(tournamentId: String): Boolean {
        val current = _tournaments.value.toMutableList()
        val index = current.indexOfFirst { it.id == tournamentId }
        if (index != -1) {
            val item = current[index]
            if (item.currentParticipants < item.maxParticipants) {
                current[index] = item.copy(currentParticipants = item.currentParticipants + 1)
                _tournaments.value = current
                return true
            }
        }
        return false
    }

    fun addCustomAiTournament(
        title: String,
        category: String,
        questions: List<QuizQuestion>
    ): Tournament {
        val newTournament = Tournament(
            id = "ai_" + UUID.randomUUID().toString().take(6),
            title = title,
            description = "چالش تولید شده اختصاصی توسط هوش مصنوعی بر اساس حوزه $category",
            category = TournamentCategory.DAILY,
            entryFeeToman = 0,
            prizePoolToman = 2500000,
            maxParticipants = 50,
            currentParticipants = 1,
            startsInSeconds = 0,
            questions = questions,
            difficulty = Difficulty.HARD,
            isHotOrSpecial = true
        )
        _tournaments.value = listOf(newTournament) + _tournaments.value
        return newTournament
    }

    private fun generateInitialTournaments(): List<Tournament> {
        val qSetScience = listOf(
            QuizQuestion(
                id = "q1",
                question = "سریع‌ترین شیء شناخته شده در کل کیهان کدام است؟",
                options = listOf("صوت در خلاء", "فوتون‌های نور در خلاء", "امواج گرانشی مایع", "بادهای خورشیدی"),
                correctIndex = 1,
                explanation = "سرعت نور در خلاء حدود ۳۰۰ هزار کیلومتر بر ثانیه است و حداکثر سرعت انتقال اطلاعات در جهان است.",
                category = "فیزیک و نجوم",
                difficulty = Difficulty.MEDIUM,
                timeLimitSeconds = 15,
                basePoints = 120
            ),
            QuizQuestion(
                id = "q2",
                question = "کدام عنصر بیشترین درصد از حجم جو زمین را تشکیل می‌دهد؟",
                options = listOf("اکسیژن", "دی‌اکسید کربن", "نیتروژن (ازت)", "آرگون"),
                correctIndex = 2,
                explanation = "نیتروژن حدود ۷۸ درصد از حجم اتمسفر کره زمین را به خود اختصاص داده است.",
                category = "شیمی و زمین‌شناسی",
                difficulty = Difficulty.EASY,
                timeLimitSeconds = 15,
                basePoints = 100
            ),
            QuizQuestion(
                id = "q3",
                question = "کدام اندام در بدن انسان بیشترین مصرف انرژی (گلوکز) را دارد؟",
                options = listOf("مغز", "کبد", "قلب", "کلیه"),
                correctIndex = 0,
                explanation = "مغز انسان حدود ۲۰ درصد از کل کالری و گلوکز دریافتی روزانه بدن را مصرف می‌کند.",
                category = "زیست‌شناسی",
                difficulty = Difficulty.MEDIUM,
                timeLimitSeconds = 15,
                basePoints = 130
            ),
            QuizQuestion(
                id = "q4",
                question = "بزرگ‌ترین اقیانوس کره زمین کدام است؟",
                options = listOf("اقیانوس اطلس", "اقیانوس هند", "اقیانوس آرام (کبیر)", "اقیانوس منجمد شمالی"),
                correctIndex = 2,
                explanation = "اقیانوس آرام مساحتی بیش از کل مساحت خشکی‌های زمین دارد.",
                category = "جغرافیا",
                difficulty = Difficulty.EASY,
                timeLimitSeconds = 12,
                basePoints = 100
            ),
            QuizQuestion(
                id = "q5",
                question = "نخستین زبانی که اینترنت و صفحات وب با آن پایه‌گذاری شد چه بود؟",
                options = listOf("C++", "HTML", "Python", "Java"),
                correctIndex = 1,
                explanation = "زبان نشانه‌گذاری ابرمتن (HTML) توسط تیم برنرز لی برای نمایش صفحات وب طراحی شد.",
                category = "فناوری و وب",
                difficulty = Difficulty.EASY,
                timeLimitSeconds = 12,
                basePoints = 110
            )
        )

        val qSetLogic = listOf(
            QuizQuestion(
                id = "ql1",
                question = "اگر ۳ گربه در ۳ دقیقه ۳ موش بگیرند، ۱۰۰ گربه در ۱۰۰ دقیقه چند موش می‌گیرند؟",
                options = listOf("۱۰۰ موش", "۳۰۰ موش", "۱۰۰۰ موش", "۳۳۳۳ موش"),
                correctIndex = 0,
                explanation = "هر گربه در ۳ دقیقه ۱ موش می‌گیرد. در ۱۰۰ دقیقه هر گربه ۳۳ موش می‌گیرد، پس ۱۰۰ گربه ۳۳۳۳ موش می‌گیرند! (اما اگر زمان هر گربه مستقل باشد ۱۰۰ گربه ۱۰۰ موش در ۳ دقیقه می‌گیرند). در این مسئله استاندارد پاسخ ریاضی ۱۰۰ است چون نرخ یکسان است.",
                category = "هوش و ریاضی",
                difficulty = Difficulty.HARD,
                timeLimitSeconds = 18,
                basePoints = 180
            ),
            QuizQuestion(
                id = "ql2",
                question = "کدام عدد الگوی بعدی است؟ ۲، ۶، ۱۲، ۲۰، ۳۰، ؟",
                options = listOf("۳۸", "۴۰", "۴۲", "۴۸"),
                correctIndex = 2,
                explanation = "اختلاف اعداد: ۴، ۶، ۸، ۱۰، و بعدی ۱۲ است. ۳۰ + ۱۲ = ۴۲.",
                category = "الگو و معما",
                difficulty = Difficulty.MEDIUM,
                timeLimitSeconds = 15,
                basePoints = 140
            ),
            QuizQuestion(
                id = "ql3",
                question = "آن چیست که هرچه برداری بزرگ‌تر می‌شود؟",
                options = listOf("حفره (گودال)", "کوه", "درخت", "سایه"),
                correctIndex = 0,
                explanation = "با برداشتن خاک بیشتر، حفره و گودال عمیق‌تر و بزرگ‌تر می‌شود.",
                category = "معمای فکری",
                difficulty = Difficulty.EASY,
                timeLimitSeconds = 15,
                basePoints = 100
            ),
            QuizQuestion(
                id = "ql4",
                question = "اگر دیروز فردا بود، امروز شنبه بود! در واقع امروز چه روزی است؟",
                options = listOf("جمعه", "یکشنبه", "دوشنبه", "پنجشنبه"),
                correctIndex = 1,
                explanation = "اگر امروز یکشنبه باشد، دیروز شنبه بوده که همان فرض مسئله است.",
                category = "منطق زمان",
                difficulty = Difficulty.EXPERT,
                timeLimitSeconds = 20,
                basePoints = 200
            )
        )

        val qSetGrand = listOf(
            QuizQuestion(
                id = "qg1",
                question = "بزرگ‌ترین اختراع قرن ۲۱ که نحوه تعامل جهان با هوش محاسباتی را متحول کرد چیست؟",
                options = listOf("مدل‌های زبان بزرگ (LLM)", "فلاپی دیسک", "تلویزیون پلاسما", "فکس دیجیتال"),
                correctIndex = 0,
                explanation = "ترانسفورمرها و مدل‌های هوش مصنوعی بزرگ عصر جدیدی از محاسبات مولد را رقم زدند.",
                category = "هوش مصنوعی",
                difficulty = Difficulty.MEDIUM,
                timeLimitSeconds = 15,
                basePoints = 150
            ),
            QuizQuestion(
                id = "qg2",
                question = "واحد پول رسمی کشور ژاپن چیست؟",
                options = listOf("یوآن", "وون", "ین", "رینگیت"),
                correctIndex = 2,
                explanation = "ین (JPY) پول رسمی ژاپن است.",
                category = "اطلاعات عمومی",
                difficulty = Difficulty.EASY,
                timeLimitSeconds = 10,
                basePoints = 100
            ),
            QuizQuestion(
                id = "qg3",
                question = "سرعت چرخش زمین به دور خورشید تقریباً چقدر است؟",
                options = listOf("۱۰ کیلومتر بر ساعت", "۳۰ کیلومتر بر ثانیه", "۱۰۰ کیلومتر بر ثانیه", "۵۰۰ متر بر ثانیه"),
                correctIndex = 1,
                explanation = "زمین با سرعت تقریبی ۱۰۷ هزار کیلومتر بر ساعت (معادل ۳۰ کیلومتر بر ثانیه) به دور خورشید می‌گردد.",
                category = "نجوم",
                difficulty = Difficulty.HARD,
                timeLimitSeconds = 15,
                basePoints = 170
            )
        )

        return listOf(
            Tournament(
                id = "tour_today_01",
                title = "چالش بزرگ امروز: نبرد نخبگان",
                description = "مسابقه روزانه با ضرایب امتیازی دوبل و جوایز نقدی مستقیم واریزی به حساب بانکی",
                category = TournamentCategory.DAILY,
                entryFeeToman = 0, // Free daily entry!
                prizePoolToman = 5000000,
                maxParticipants = 500,
                currentParticipants = 384,
                startsInSeconds = 1800,
                questions = qSetScience,
                difficulty = Difficulty.MEDIUM,
                isHotOrSpecial = true
            ),
            Tournament(
                id = "tour_vip_grand",
                title = "گرند تورنمنت VIP کشوری (فصل پاییز)",
                description = "بزرگ‌ترین مسابقه مهارتی و اطلاعاتی فصل با جایزه بزرگ ۱۰۰ میلیون تومانی",
                category = TournamentCategory.GRAND,
                entryFeeToman = 50000,
                prizePoolToman = 100000000,
                maxParticipants = 2000,
                currentParticipants = 1720,
                startsInSeconds = 7200,
                questions = qSetGrand + qSetLogic,
                difficulty = Difficulty.EXPERT,
                isHotOrSpecial = true
            ),
            Tournament(
                id = "tour_weekly_league",
                title = "لیگ هفتگی هوش و محاسبات سریع",
                description = "مسابقه اختصاصی حل سریع معماهای منطقی، الگوها و سرعت عکس‌العمل",
                category = TournamentCategory.WEEKLY,
                entryFeeToman = 20000,
                prizePoolToman = 25000000,
                maxParticipants = 800,
                currentParticipants = 612,
                startsInSeconds = 14400,
                questions = qSetLogic,
                difficulty = Difficulty.HARD,
                isHotOrSpecial = false
            ),
            Tournament(
                id = "tour_free_speed",
                title = "میدان تمرین و رقابت آزاد (کاملاً رایگان)",
                description = "شرکت نامحدود بدون هزینه ورودی، کسب امتیاز رنکینگ و تجربه بازی در محیط رقابتی",
                category = TournamentCategory.FREE,
                entryFeeToman = 0,
                prizePoolToman = 1000000,
                maxParticipants = 300,
                currentParticipants = 245,
                startsInSeconds = 300,
                questions = qSetScience,
                difficulty = Difficulty.EASY,
                isHotOrSpecial = false
            ),
            Tournament(
                id = "tour_vip_master",
                title = "جام ویژه سلاطین معلومات عمومی",
                description = "مسابقه ویژه نخبگان با سوالات چالشی هنر، ادبیات، علوم تجربی و تکنولوژی",
                category = TournamentCategory.VIP,
                entryFeeToman = 30000,
                prizePoolToman = 40000000,
                maxParticipants = 600,
                currentParticipants = 480,
                startsInSeconds = 28800,
                questions = qSetScience + qSetGrand,
                difficulty = Difficulty.HARD,
                isHotOrSpecial = true
            )
        )
    }
}
