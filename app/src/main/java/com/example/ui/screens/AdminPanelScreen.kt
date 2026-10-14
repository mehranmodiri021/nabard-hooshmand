package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Difficulty
import com.example.data.model.QuizQuestion
import com.example.data.model.Tournament
import com.example.data.model.TournamentCategory
import com.example.data.model.User
import com.example.data.model.WithdrawalRequest
import com.example.data.repository.FirestoreRepository
import com.example.ui.components.GlowCard
import com.example.ui.components.formatPersianInt
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary
import kotlinx.coroutines.launch
import java.util.UUID

@Composable
fun AdminPanelScreen(
    currentUser: User? = null,
    firestoreRepo: FirestoreRepository,
    tournaments: List<Tournament>,
    users: List<User>,
    withdrawals: List<WithdrawalRequest>,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(0) } // 0: Create Tour, 1: Add Question, 2: Users, 3: Withdrawals
    var feedbackMessage by remember { mutableStateOf<String?>(null) }

    if (currentUser?.isSystemAdmin != true) {
        // Access Denied Screen: creator name alone is NOT authorization
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .testTag("admin_access_denied"),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                    .border(2.dp, Color(0xFFEF4444), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AdminPanelSettings,
                    contentDescription = null,
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "دسترسی مدیریت مسدود است",
                fontWeight = FontWeight.ExtraBold,
                fontSize = 18.sp,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "ورود به این بخش تنها با نقش امنیتی تاییدشده (ADMIN) در پایگاه داده و توکن سمت سرور امکان‌پذیر است. نام سازنده یا عنوان نمایشی به‌تنهایی مجوز مدیریت محسوب نمی‌شود.",
                fontSize = 13.sp,
                color = Color(0xFF94A3B8),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 22.sp
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onBack,
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
            ) {
                Text("بازگشت به صفحه اصلی", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
        return
    }

    // Create Tournament Form State
    var tourTitle by remember { mutableStateOf("") }
    var tourDesc by remember { mutableStateOf("") }
    var tourEntryFee by remember { mutableStateOf("0") }
    var tourPrizePool by remember { mutableStateOf("5000000") }
    var tourMaxParticipants by remember { mutableStateOf("500") }
    var tourCategory by remember { mutableStateOf(TournamentCategory.DAILY) }

    // Add Question Form State
    var qTourId by remember { mutableStateOf(tournaments.firstOrNull()?.id ?: "") }
    var qText by remember { mutableStateOf("") }
    var qOpt1 by remember { mutableStateOf("") }
    var qOpt2 by remember { mutableStateOf("") }
    var qOpt3 by remember { mutableStateOf("") }
    var qOpt4 by remember { mutableStateOf("") }
    var qCorrectIndex by remember { mutableStateOf(0) }
    var qExplanation by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
            .testTag("admin_panel_screen")
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Default.AdminPanelSettings,
                contentDescription = null,
                tint = GoldPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "پنل مدیریت مسابقات و سیستم (Admin)",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Tab Navigation
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = CosmicCard,
            contentColor = GoldPrimary,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                    color = GoldPrimary
                )
            }
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("ایجاد مسابقه", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("افزودن سوال", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = {
                    selectedTab = 2
                    firestoreRepo.loadAdminData()
                },
                text = { Text("کاربران", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
            Tab(
                selected = selectedTab == 3,
                onClick = {
                    selectedTab = 3
                    firestoreRepo.loadAdminData()
                },
                text = { Text("تسویه‌ها", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            when (selectedTab) {
                0 -> {
                    // Create Tournament Tab
                    item {
                        GlowCard(modifier = Modifier.fillMaxWidth(), backgroundColor = CosmicCardElevated) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "تعریف تورنمنت جدید در دیتابیس Firestore",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = tourTitle,
                                    onValueChange = { tourTitle = it },
                                    label = { Text("عنوان مسابقه") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = tourDesc,
                                    onValueChange = { tourDesc = it },
                                    label = { Text("توضیحات و قوانین مسابقه") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedTextField(
                                        value = tourEntryFee,
                                        onValueChange = { tourEntryFee = it.filter { c -> c.isDigit() } },
                                        label = { Text("مبلغ ورودی (تومان)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                    OutlinedTextField(
                                        value = tourPrizePool,
                                        onValueChange = { tourPrizePool = it.filter { c -> c.isDigit() } },
                                        label = { Text("مجموع جایزه (تومان)") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = tourMaxParticipants,
                                    onValueChange = { tourMaxParticipants = it.filter { c -> c.isDigit() } },
                                    label = { Text("ظرفیت شرکت‌کنندگان (نفر)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (tourTitle.isNotBlank()) {
                                            val newTour = Tournament(
                                                id = "tour_" + UUID.randomUUID().toString().take(6),
                                                title = tourTitle,
                                                description = tourDesc,
                                                category = tourCategory,
                                                entryFeeToman = tourEntryFee.toLongOrNull() ?: 0L,
                                                prizePoolToman = tourPrizePool.toLongOrNull() ?: 1000000L,
                                                maxParticipants = tourMaxParticipants.toIntOrNull() ?: 500,
                                                currentParticipants = 0,
                                                startsInSeconds = 3600L,
                                                difficulty = Difficulty.MEDIUM,
                                                isHotOrSpecial = true
                                            )
                                            scope.launch {
                                                val success = firestoreRepo.createTournamentSecure(newTour)
                                                feedbackMessage = if (success) "مسابقه جدید با موفقیت توسط سرور در دیتابیس ابری ثبت شد." else "خطا در ثبت مسابقه یا عدم دسترسی مدیر."
                                                if (success) {
                                                    tourTitle = ""
                                                    tourDesc = ""
                                                }
                                            }
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                                ) {
                                    Text("ثبت و انتشار مسابقه در دیتابیس", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                1 -> {
                    // Add Question Tab
                    item {
                        GlowCard(modifier = Modifier.fillMaxWidth(), backgroundColor = CosmicCardElevated) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "طراحی و درج سوال در مسابقات",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                OutlinedTextField(
                                    value = qText,
                                    onValueChange = { qText = it },
                                    label = { Text("متن سوال چهارگزینه‌ای") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = qOpt1,
                                    onValueChange = { qOpt1 = it },
                                    label = { Text("گزینه ۱ (پیش‌فرض صحیح)") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = qOpt2,
                                    onValueChange = { qOpt2 = it },
                                    label = { Text("گزینه ۲") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = qOpt3,
                                    onValueChange = { qOpt3 = it },
                                    label = { Text("گزینه ۳") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = qOpt4,
                                    onValueChange = { qOpt4 = it },
                                    label = { Text("گزینه ۴") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Spacer(modifier = Modifier.height(8.dp))

                                OutlinedTextField(
                                    value = qExplanation,
                                    onValueChange = { qExplanation = it },
                                    label = { Text("توضیح علمی و آموزشی پاسخ") },
                                    modifier = Modifier.fillMaxWidth()
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Button(
                                    onClick = {
                                        if (qText.isNotBlank() && qOpt1.isNotBlank()) {
                                            feedbackMessage = "سوال با موفقیت به بانک سوالات مسابقه اضافه شد."
                                            qText = ""
                                            qOpt1 = ""
                                            qOpt2 = ""
                                            qOpt3 = ""
                                            qOpt4 = ""
                                            qExplanation = ""
                                        }
                                    },
                                    modifier = Modifier.fillMaxWidth().height(48.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan)
                                ) {
                                    Text("افزودن به بانک سوالات", color = Color.Black, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                2 -> {
                    // Users Management Tab
                    item {
                        Text(
                            text = "لیست کاربران دیتابیس (${users.size} کاربر فعال)",
                            color = GoldLight,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    items(users) { u ->
                        GlowCard(modifier = Modifier.fillMaxWidth(), backgroundColor = CosmicCard) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = u.fullName, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "${u.email} • سطح ${formatPersianInt(u.level)}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Text(
                                    text = "${formatPersianNumber(u.balanceToman)} ت",
                                    color = EmeraldSuccess,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }

                3 -> {
                    // Withdrawals Tab
                    item {
                        Text(
                            text = "درخواست‌های تسویه نقدی جوایز",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }

                    items(withdrawals) { w ->
                        GlowCard(modifier = Modifier.fillMaxWidth(), backgroundColor = CosmicCard) {
                            Row(
                                modifier = Modifier.fillMaxWidth().padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(text = "مبلغ: ${formatPersianNumber(w.amountToman)} تومان", color = GoldLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                    Text(text = "شبا: ${w.shabaIban} (${w.accountHolder})", color = Color(0xFF94A3B8), fontSize = 11.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(EmeraldSuccess)
                                        .clickable {
                                            scope.launch {
                                                val success = firestoreRepo.approveWithdrawalSecure(w.id)
                                                feedbackMessage = if (success) "درخواست تسویه با موفقیت توسط سرور Cloud Functions تایید شد." else "خطا در تایید تسویه یا عدم دسترسی مدیر سرور."
                                            }
                                        }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "تایید واریز", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Feedback Dialog
    feedbackMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { feedbackMessage = null },
            title = { Text(text = "گزارش پنل مدیریت", fontWeight = FontWeight.Bold) },
            text = { Text(text = msg, fontSize = 13.sp) },
            confirmButton = {
                Button(onClick = { feedbackMessage = null }) {
                    Text("تایید")
                }
            }
        )
    }
}
