package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddCard
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowOutward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.TransactionStatus
import com.example.data.model.TransactionType
import com.example.data.model.WalletTransaction
import com.example.ui.components.GlowCard
import com.example.ui.components.GradientButton
import com.example.ui.components.formatPersianNumber
import com.example.ui.theme.CosmicBorder
import com.example.ui.theme.CosmicCard
import com.example.ui.theme.CosmicCardElevated
import com.example.ui.theme.CrimsonDanger
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.EmeraldLight
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.GoldDark
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldPrimary

enum class TransactionFilter(val labelFa: String) {
    ALL("همه"),
    DEPOSITS("واریزها"),
    WITHDRAWALS("برداشت‌ها"),
    PRIZES("جوایز مسابقات"),
    FEES("هزینه ورودی")
}

@Composable
fun WalletScreen(
    balanceToman: Long,
    totalWonToman: Long,
    transactions: List<WalletTransaction>,
    onDepositRequest: (Long) -> Unit,
    onSubmitWithdrawal: (Long, String, String, String) -> Boolean
) {
    var showDepositDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf(TransactionFilter.ALL) }

    var depositAmountInput by remember { mutableStateOf("50000") }
    var withdrawAmountInput by remember { mutableStateOf("100000") }
    var shabaInput by remember { mutableStateOf("IR820170000000123456789012") }
    var cardInput by remember { mutableStateOf("6037-9975-1234-5678") }
    var accountHolderInput by remember { mutableStateOf("سیدحمید موسوی زاده") }
    var alertNoticeMessage by remember { mutableStateOf<String?>(null) }
    var isWithdrawError by remember { mutableStateOf(false) }

    val filteredTransactions = remember(transactions, selectedFilter) {
        when (selectedFilter) {
            TransactionFilter.ALL -> transactions
            TransactionFilter.DEPOSITS -> transactions.filter { it.type == TransactionType.DEPOSIT }
            TransactionFilter.WITHDRAWALS -> transactions.filter { it.type == TransactionType.WITHDRAWAL }
            TransactionFilter.PRIZES -> transactions.filter { it.type == TransactionType.PRIZE_WON }
            TransactionFilter.FEES -> transactions.filter { it.type == TransactionType.ENTRY_FEE }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("wallet_screen_container")
            .padding(top = 16.dp)
    ) {
        // Top Title
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "کیف پول و جوایز نقدی",
                color = MaterialTheme.colorScheme.onBackground,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                modifier = Modifier.testTag("wallet_screen_title")
            )

            // Security Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(EmeraldSuccess.copy(alpha = 0.12f))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "امنیت شاپرک",
                    color = EmeraldSuccess,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("transaction_history_list")
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Main Balance Card (Credit Card Aesthetic)
            item {
                WalletBalanceCard(
                    balanceToman = balanceToman,
                    totalWonToman = totalWonToman
                )
            }

            // Primary Action Buttons: Deposit & Withdrawal
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Deposit Button
                    Button(
                        onClick = { showDepositDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("deposit_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = GoldPrimary,
                            contentColor = Color.Black
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AddCard,
                                contentDescription = "واریز",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "افزایش موجودی",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Withdrawal Button
                    Button(
                        onClick = { showWithdrawDialog = true },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .testTag("withdraw_button"),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = EmeraldSuccess,
                            contentColor = Color.White
                        )
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AccountBalance,
                                contentDescription = "برداشت",
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "درخواست تسویه",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Payment Gateway Info Banner
            item {
                GatewayReadinessBanner()
            }

            // Transaction History Section Header & Filter Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.ReceiptLong,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "گردش حساب و سوابق مالی",
                                color = MaterialTheme.colorScheme.onBackground,
                                fontWeight = FontWeight.Bold,
                                fontSize = 17.sp
                            )
                        }

                        Text(
                            text = "${formatPersianNumber(filteredTransactions.size.toLong())} مورد",
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Transaction Category Filter Chips
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 2.dp)
                    ) {
                        items(TransactionFilter.values()) { filter ->
                            val isSelected = filter == selectedFilter
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedFilter = filter },
                                label = {
                                    Text(
                                        text = filter.labelFa,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 12.sp
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = GoldPrimary,
                                    selectedLabelColor = Color.Black,
                                    containerColor = CosmicCard,
                                    labelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) GoldLight else CosmicBorder,
                                    selectedBorderColor = GoldLight,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.testTag("filter_chip_${filter.name.lowercase()}")
                            )
                        }
                    }
                }
            }

            // Empty State Handling
            if (filteredTransactions.isEmpty()) {
                item {
                    EmptyTransactionsState()
                }
            } else {
                // Transactions List Items
                items(filteredTransactions, key = { it.id }) { tx ->
                    TransactionItemRow(transaction = tx)
                }
            }

            // Bottom insets spacing
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }

    // Deposit Dialog with Preset Options (Simulating Shetab / Shaparak Banking Gateway)
    if (showDepositDialog) {
        DepositModalDialog(
            depositAmountInput = depositAmountInput,
            onAmountChange = { depositAmountInput = it },
            onConfirm = { amount ->
                showDepositDialog = false
                onDepositRequest(amount)
                alertNoticeMessage = "مبلغ ${formatPersianNumber(amount)} تومان با موفقیت به کیف پول شما افزوده شد."
                isWithdrawError = false
            },
            onDismiss = { showDepositDialog = false }
        )
    }

    // Withdrawal Modal Dialog
    if (showWithdrawDialog) {
        WithdrawalModalDialog(
            balanceToman = balanceToman,
            withdrawAmountInput = withdrawAmountInput,
            shabaInput = shabaInput,
            cardInput = cardInput,
            accountHolderInput = accountHolderInput,
            onAmountChange = { withdrawAmountInput = it },
            onShabaChange = { shabaInput = it },
            onCardChange = { cardInput = it },
            onHolderChange = { accountHolderInput = it },
            onConfirm = { amount, shaba, card, holder ->
                if (amount <= 0 || amount > balanceToman) {
                    alertNoticeMessage = "مبلغ درخواستی نمی‌تواند بیشتر از موجودی کیف پول باشد."
                    isWithdrawError = true
                } else {
                    val success = onSubmitWithdrawal(amount, shaba, card, holder)
                    showWithdrawDialog = false
                    if (success) {
                        alertNoticeMessage = "درخواست تسویه مبلغ ${formatPersianNumber(amount)} تومان با موفقیت ثبت شد و ظرف ۲۴ ساعت کاری به شبای شما واریز می‌شود."
                        isWithdrawError = false
                    }
                }
            },
            onDismiss = { showWithdrawDialog = false }
        )
    }

    // Feedback Notice Dialog
    alertNoticeMessage?.let { msg ->
        AlertDialog(
            onDismissRequest = { alertNoticeMessage = null },
            title = {
                Text(
                    text = if (isWithdrawError) "خطا در عملیات" else "عملیات موفق",
                    fontWeight = FontWeight.Bold,
                    color = if (isWithdrawError) CrimsonDanger else EmeraldSuccess
                )
            },
            text = { Text(text = msg, fontSize = 13.sp, lineHeight = 20.sp) },
            confirmButton = {
                Button(
                    onClick = { alertNoticeMessage = null },
                    colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary)
                ) {
                    Text(text = "متوجه شدم", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
fun WalletBalanceCard(
    balanceToman: Long,
    totalWonToman: Long
) {
    ElevatedCard(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .testTag("balance_card"),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF1E1B4B),
                            Color(0xFF311042),
                            Color(0xFF0F172A)
                        )
                    )
                )
                .border(
                    width = 1.5.dp,
                    brush = Brush.linearGradient(listOf(GoldPrimary, CyberCyan)),
                    shape = RoundedCornerShape(24.dp)
                )
                .padding(20.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp, 24.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Brush.horizontalGradient(listOf(GoldPrimary, GoldDark)))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "کیف پول اختصاصی VIP",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(CyberCyan.copy(alpha = 0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "شبکه شتاب / شاپرک",
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }
                }

                // Balance Section
                Column {
                    Text(
                        text = "موجودی فعلی قابل برداشت:",
                        color = Color(0xFFCBD5E1),
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Row(
                        verticalAlignment = Alignment.Bottom,
                        modifier = Modifier.testTag("wallet_balance_value")
                    ) {
                        Text(
                            text = formatPersianNumber(balanceToman),
                            color = GoldLight,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 30.sp
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "تومان",
                            color = Color.White,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 5.dp)
                        )
                    }
                }

                // Bottom Stats Row: Owner Name + Cash Won
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "سیدحمید موسوی زاده",
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "مجموع برد: ${formatPersianNumber(totalWonToman / 1000000)} میلیون ت",
                            color = EmeraldSuccess,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GatewayReadinessBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberPurple.copy(alpha = 0.12f))
            .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(CyberCyan.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Security,
                    contentDescription = null,
                    tint = CyberCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "آماده اتصال به درگاه‌های پرداخت شتاب و شاپرک",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "پشتیبانی از زرین‌پال، به‌پرداخت ملت و سداد با تسویه خودکار پایا و رمز پویا.",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp,
                    lineHeight = 16.sp
                )
            }
        }
    }
}

@Composable
fun DepositModalDialog(
    depositAmountInput: String,
    onAmountChange: (String) -> Unit,
    onConfirm: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val presets = listOf(20000L, 50000L, 100000L, 200000L)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AddCard,
                    contentDescription = null,
                    tint = GoldPrimary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "افزایش موجودی (درگاه شاپرک)",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "مبلغ مورد نظر خود را به تومان انتخاب یا وارد نمایید:",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Preset Buttons Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    presets.forEach { amount ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(CosmicCardElevated)
                                .border(1.dp, CosmicBorder, RoundedCornerShape(8.dp))
                                .clickable { onAmountChange(amount.toString()) }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${formatPersianNumber(amount / 1000)}k",
                                color = GoldLight,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = depositAmountInput,
                    onValueChange = { onAmountChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("مبلغ دلخواه (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("deposit_amount_field"),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = GoldPrimary,
                        unfocusedBorderColor = CosmicBorder
                    ),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = depositAmountInput.toLongOrNull() ?: 50000L
                    onConfirm(amount)
                },
                colors = ButtonDefaults.buttonColors(containerColor = GoldPrimary),
                modifier = Modifier.testTag("confirm_deposit_button")
            ) {
                Text(text = "پرداخت و افزایش آنی", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}

@Composable
fun WithdrawalModalDialog(
    balanceToman: Long,
    withdrawAmountInput: String,
    shabaInput: String,
    cardInput: String,
    accountHolderInput: String,
    onAmountChange: (String) -> Unit,
    onShabaChange: (String) -> Unit,
    onCardChange: (String) -> Unit,
    onHolderChange: (String) -> Unit,
    onConfirm: (Long, String, String, String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.AccountBalance,
                    contentDescription = null,
                    tint = EmeraldSuccess,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "درخواست تسویه جایزه نقدی",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
            }
        },
        text = {
            Column {
                Text(
                    text = "موجودی قابل برداشت: ${formatPersianNumber(balanceToman)} تومان",
                    fontSize = 12.sp,
                    color = EmeraldLight,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = withdrawAmountInput,
                    onValueChange = { onAmountChange(it.filter { c -> c.isDigit() }) },
                    label = { Text("مبلغ برداشت (تومان)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("withdraw_amount_field"),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = shabaInput,
                    onValueChange = onShabaChange,
                    label = { Text("شماره شبا (با پیشوند IR)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = accountHolderInput,
                    onValueChange = onHolderChange,
                    label = { Text("نام و نام خانوادگی صاحب حساب") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val amount = withdrawAmountInput.toLongOrNull() ?: 0L
                    onConfirm(amount, shabaInput, cardInput, accountHolderInput)
                },
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess),
                modifier = Modifier.testTag("confirm_withdraw_button")
            ) {
                Text(text = "ثبت درخواست تسویه", color = Color.White, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text(text = "انصراف")
            }
        }
    )
}

@Composable
fun EmptyTransactionsState() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF334155).copy(alpha = 0.5f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = Color(0xFF94A3B8),
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "هیچ تراکنشی در این دسته‌بندی یافت نشد",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "تراکنش‌های جدید پس از شرکت در مسابقه یا شارژ حساب اضافه می‌شوند.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun TransactionItemRow(transaction: WalletTransaction) {
    val isIncome = transaction.amountToman > 0
    val icon = when (transaction.type) {
        TransactionType.DEPOSIT -> Icons.Default.ArrowDownward
        TransactionType.PRIZE_WON -> Icons.Default.EmojiEvents
        TransactionType.ENTRY_FEE -> Icons.Default.ArrowOutward
        TransactionType.WITHDRAWAL -> Icons.Default.ArrowUpward
    }
    val iconColor = if (isIncome) EmeraldSuccess else CrimsonDanger

    GlowCard(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("transaction_row_${transaction.id}"),
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
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconColor.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = transaction.type.labelFa,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = transaction.title,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "${transaction.datePersian} • کد: ${transaction.trackingCode}",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = (if (isIncome) "+" else "") + "${formatPersianNumber(transaction.amountToman)} تومان",
                    color = if (isIncome) EmeraldSuccess else Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(
                            when (transaction.status) {
                                TransactionStatus.SUCCESS -> EmeraldSuccess.copy(alpha = 0.15f)
                                TransactionStatus.PENDING -> GoldPrimary.copy(alpha = 0.15f)
                                TransactionStatus.FAILED -> CrimsonDanger.copy(alpha = 0.15f)
                            }
                        )
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = transaction.status.labelFa,
                        color = when (transaction.status) {
                            TransactionStatus.SUCCESS -> EmeraldSuccess
                            TransactionStatus.PENDING -> GoldLight
                            TransactionStatus.FAILED -> CrimsonDanger
                        },
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}
