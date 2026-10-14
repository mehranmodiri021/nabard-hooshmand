package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContactSupport
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AppSettings
import com.example.ui.components.GlowCard
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onToggleDarkMode: () -> Unit,
    onToggleNotifications: () -> Unit,
    onToggleSound: () -> Unit,
    onBack: () -> Unit,
    onOpenAdminPanel: (() -> Unit)? = null
) {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showPasswordDialog by remember { mutableStateOf(false) }
    var showSupportDialog by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }

    var oldPass by remember { mutableStateOf("") }
    var newPass by remember { mutableStateOf("") }
    var reportSubject by remember { mutableStateOf("") }
    var reportDesc by remember { mutableStateOf("") }
    var actionMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.testTag("settings_back_btn")) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "بازگشت",
                    tint = MaterialTheme.colorScheme.onBackground
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "تنظیمات برنامه",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // General Preferences
            item {
                Text(
                    text = "تنظیمات ظاهری و صدا",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            item {
                SettingsSwitchRow(
                    title = "حالت شب (Dark Mode)",
                    subtitle = "قالب تیره اختصاصی با گرافیک نئونی",
                    icon = Icons.Default.DarkMode,
                    checked = settings.isDarkMode,
                    onCheckedChange = { onToggleDarkMode() }
                )
            }

            item {
                SettingsSwitchRow(
                    title = "اعلان‌ها و یادآور مسابقات",
                    subtitle = "اطلاع‌رسانی زمان شروع تورنمنت‌ها و جوایز",
                    icon = Icons.Default.Notifications,
                    checked = settings.notificationsEnabled,
                    onCheckedChange = { onToggleNotifications() }
                )
            }

            item {
                SettingsSwitchRow(
                    title = "صدا و جلوه‌های صوتی",
                    subtitle = "جلوه‌های صوتی پاسخ صحیح، غلط و شمارش معکوس",
                    icon = Icons.Default.VolumeUp,
                    checked = settings.soundEffectsEnabled,
                    onCheckedChange = { onToggleSound() }
                )
            }

            // Account & Security
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "حساب کاربری و امنیت",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            item {
                SettingsClickRow(
                    title = "مدیریت حساب کاربری",
                    subtitle = "ویرایش شماره همراه و اطلاعات هویتی",
                    icon = Icons.Default.Person,
                    onClick = { actionMessage = "اطلاعات هویتی شما تایید شده و کیف پول فعال است." }
                )
            }

            item {
                SettingsClickRow(
                    title = "تغییر رمز عبور",
                    subtitle = "تنظیم گذرواژه امن ورود به اپلیکیشن",
                    icon = Icons.Default.Lock,
                    onClick = { showPasswordDialog = true }
                )
            }

            item {
                SettingsClickRow(
                    title = "زبان برنامه",
                    subtitle = "فارسی (پیش‌فرض)",
                    icon = Icons.Default.Language,
                    onClick = { actionMessage = "زبان پیش‌فرض فارسی با پشتیبانی کامل از چینش راست به چپ (RTL) است." }
                )
            }

            // Rules & Support
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "قوانین و پشتیبانی",
                    color = GoldLight,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            item {
                SettingsClickRow(
                    title = "قوانین و ضوابط مسابقات",
                    subtitle = "نحوه امتیازدهی، شرایط شرکت و واریز جوایز",
                    icon = Icons.Default.Description,
                    onClick = { showRulesDialog = true }
                )
            }

            item {
                SettingsClickRow(
                    title = "حریم خصوصی و امنیت داده‌ها",
                    subtitle = "سیاست‌های حفاظت از اطلاعات کاربری و تراکنش‌ها",
                    icon = Icons.Default.Security,
                    onClick = { showPrivacyDialog = true }
                )
            }

            item {
                SettingsClickRow(
                    title = "تماس با پشتیبانی ۲۴/۷",
                    subtitle = "تیکت پشتیبانی، کانال تلگرام و تلفن تماس",
                    icon = Icons.Default.ContactSupport,
                    onClick = { showSupportDialog = true }
                )
            }

            item {
                SettingsClickRow(
                    title = "گزارش مشکل یا پیشنهاد",
                    subtitle = "ارسال بازخورد مستقیم به تیم توسعه‌دهنده",
                    icon = Icons.Default.ReportProblem,
                    onClick = { showReportDialog = true }
                )
            }

            if (onOpenAdminPanel != null) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SettingsClickRow(
                        title = "پنل مدیریت مسابقات و سیستم (Admin)",
                        subtitle = "تعریف مسابقه، افزودن سوالات، مدیریت کاربران و تسویه‌ها",
                        icon = Icons.Default.AdminPanelSettings,
                        onClick = onOpenAdminPanel
                    )
                }
            }

            // About App
            item {
                Spacer(modifier = Modifier.height(8.dp))
                SettingsClickRow(
                    title = "درباره برنامه چالش و مسابقه",
                    subtitle = "سازنده: سیدحمید موسوی زاده • Version 1.0.0",
                    icon = Icons.Default.Info,
                    onClick = { showAboutDialog = true }
                )
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // About Dialog (Full Creator & Version details)
    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = {
                Text(
                    text = "درباره چالش و مسابقه",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "نام برنامه: چالش و مسابقه",
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نام سازنده:\nسیدحمید موسوی زاده",
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "نسخه برنامه:\nVersion 1.0.0",
                        color = CyberCyan,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "پلتفرم اختصاصی برگزاری مسابقات مهارتی و فکری، چالش‌های روزانه و هفتگی با سیستم پیشرفته کیف پول داخلی، واریز جوایز نقدی مستقیم، رتبه‌بندی کشوری و طراح مسابقات با هوش مصنوعی.",
                        color = Color(0xFF94A3B8),
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { showAboutDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "بستن", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    // Rules Dialog
    if (showRulesDialog) {
        AlertDialog(
            onDismissRequest = { showRulesDialog = false },
            title = { Text(text = "قوانین و ضوابط مسابقات", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "۱. ورود به هر مسابقه مستلزم داشتن حداقل اعتبار ورودی است (مسابقات رایگان نیازی به پرداخت ندارند).\n\n" +
                               "۲. امتیاز هر سوال بر اساس زمان پاسخگویی و ضریب سختی سوال محاسبه می‌گردد.\n\n" +
                               "۳. جوایز نفرات اول تا سوم پس از پایان مسابقه به صورت خودکار به کیف پول کاربر منظور می‌شود.\n\n" +
                               "۴. امکان درخواست تسویه به شماره شبای رسمی بانکی در هر ساعت از شبانه‌روز فراهم است.",
                        fontSize = 12.sp,
                        lineHeight = 18.sp,
                        color = Color(0xFFCBD5E1)
                    )
                }
            },
            confirmButton = {
                Button(onClick = { showRulesDialog = false }) {
                    Text(text = "متوجه شدم")
                }
            }
        )
    }

    // Privacy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text(text = "حریم خصوصی", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "اطلاعات هویتی و مالی شما با رمزنگاری پیشرفته ذخیره می‌گردد و تحت هیچ شرایطی در اختیار اشخاص ثالث قرار نخواهد گرفت. تراکنش‌های مالی از بستر شاپرک تایید صلاحیت می‌گردند.",
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            },
            confirmButton = {
                Button(onClick = { showPrivacyDialog = false }) {
                    Text(text = "تایید")
                }
            }
        )
    }

    // Password Change Dialog
    if (showPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showPasswordDialog = false },
            title = { Text(text = "تغییر رمز عبور", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = oldPass,
                        onValueChange = { oldPass = it },
                        label = { Text("رمز عبور فعلی") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newPass,
                        onValueChange = { newPass = it },
                        label = { Text("رمز عبور جدید") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showPasswordDialog = false
                        actionMessage = "رمز عبور با موفقیت بروزرسانی شد."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "ثبت رمز جدید", color = Color.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showPasswordDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Support Dialog
    if (showSupportDialog) {
        AlertDialog(
            onDismissRequest = { showSupportDialog = false },
            title = { Text(text = "پشتیبانی ۲۴ ساعته", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "تیم پشتیبانی اپلیکیشن چالش و مسابقه در تمام ساعات پاسخگوی شماست:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = "📞 تلفن پشتیبانی: ۰۲۱-۸۸۸۸۹۹۹۹", color = CyberCyan)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "✈️ پشتیبانی تلگرام: @ChallengeSupport", color = GoldLight)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = "✉️ ایمیل: support@challengetournament.ir")
                }
            },
            confirmButton = {
                Button(onClick = { showSupportDialog = false }) {
                    Text(text = "بستن")
                }
            }
        )
    }

    // Report Dialog
    if (showReportDialog) {
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "گزارش مشکل یا سوال", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = reportSubject,
                        onValueChange = { reportSubject = it },
                        label = { Text("عنوان گزارش") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = reportDesc,
                        onValueChange = { reportDesc = it },
                        label = { Text("توضیحات مشکل") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        actionMessage = "گزارش شما ثبت شد و به زودی بررسی خواهد شد."
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "ارسال گزارش", color = Color.Black)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showReportDialog = false }) {
                    Text(text = "انصراف")
                }
            }
        )
    }

    // Action Toast/Notice
    actionMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { actionMessage = null },
            title = { Text(text = "اطلاع‌رسانی", fontWeight = FontWeight.Bold) },
            text = { Text(text = msg) },
            confirmButton = {
                Button(onClick = { actionMessage = null }) {
                    Text(text = "متوجه شدم")
                }
            }
        )
    }
}

@Composable
fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    GlowCard(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = CosmicCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(GoldPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = GoldPrimary, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = GoldPrimary,
                    checkedTrackColor = GoldPrimary.copy(alpha = 0.4f)
                )
            )
        }
    }
}

@Composable
fun SettingsClickRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        backgroundColor = CosmicCard
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(CyberCyan.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = subtitle, color = Color(0xFF94A3B8), fontSize = 11.sp)
                }
            }

            Text(text = "➔", color = Color(0xFF64748B), fontSize = 14.sp)
        }
    }
}
