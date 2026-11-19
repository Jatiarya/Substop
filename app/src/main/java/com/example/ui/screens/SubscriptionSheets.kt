package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.CalendarToday
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.PauseCircleOutline
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.StopCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.data.BillingCadence
import com.example.data.Subscription
import com.example.data.SubscriptionCatalog
import com.example.data.SubscriptionCategory
import com.example.data.SubscriptionStatus
import com.example.data.currentEpochDay
import com.example.data.formatCurrency
import com.example.data.formatEpochDay
import com.example.ui.components.IosToggleSwitch
import com.example.ui.components.ServiceSquircleAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.components.SubStopDestructiveButton
import com.example.ui.components.SubStopInputField
import com.example.ui.components.SubStopPrimaryButton
import com.example.ui.components.SubStopSecondaryButton
import com.example.ui.components.iconForKey
import com.example.ui.components.parseHexColor
import com.example.ui.theme.CurrencyStatTextStyle
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopError
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopPrimaryTint
import com.example.ui.theme.SubStopSurface

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubscriptionDetailSheet(
    subscription: Subscription,
    currencySymbol: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onToggleReminder: (Boolean) -> Unit,
    onChangeStatus: (SubscriptionStatus) -> Unit,
    onDelete: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var confirmDelete by remember { mutableStateOf(false) }
    val status = subscription.statusEnum()
    val cadence = subscription.cadenceEnum()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SubStopSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                ServiceSquircleAvatar(
                    brandColorHex = subscription.brandColorHex,
                    iconKey = subscription.iconKey,
                    serviceName = subscription.name,
                    size = 56.dp
                )
                Spacer(modifier = Modifier.width(14.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = subscription.name,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StatusBadge(
                            status = status,
                            daysUntilRenewal = subscription.daysUntilRenewal()
                        )
                        Text(
                            text = subscription.category,
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }

            // Pricing Summary Well
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Slate100)
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BILLING AMOUNT",
                        style = LabelSmUppercaseStyle,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${formatCurrency(subscription.amount, currencySymbol)} ${cadence.shortLabel}",
                        style = CurrencyStatTextStyle.copy(fontSize = androidx.compose.ui.unit.TextUnit(26f, androidx.compose.ui.unit.TextUnitType.Sp)),
                        color = Slate900
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "MONTHLY EQUIVALENT",
                        style = LabelSmUppercaseStyle,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = formatCurrency(subscription.monthlyNormalizedAmount(), currencySymbol) + "/mo",
                        style = MaterialTheme.typography.titleMedium,
                        color = SubStopPrimary
                    )
                }
            }

            // Metadata Details
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailMetaRow("Next Billing Date", subscription.formattedNextBillingDate())
                if (subscription.paymentLabel.isNotBlank()) {
                    DetailMetaRow("Payment Method", subscription.paymentLabel)
                }
                if (subscription.notes.isNotBlank()) {
                    DetailMetaRow("Notes", subscription.notes)
                }
            }

            HorizontalDivider(color = Slate200.copy(alpha = 0.7f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Renewal Reminder",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900
                    )
                    Text(
                        text = "Alert me 3 days before charge",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }
                IosToggleSwitch(
                    checked = subscription.reminderEnabled,
                    onCheckedChange = onToggleReminder,
                    contentDescription = "Toggle renewal reminder",
                    modifier = Modifier.testTag("detail_reminder_switch")
                )
            }

            // Actions
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SubStopPrimaryButton(
                    text = "Edit Subscription",
                    icon = Icons.Rounded.Edit,
                    enabled = !isSaving,
                    onClick = onEdit,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("detail_edit_button")
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (subscription.isActiveOrTrial()) {
                        SubStopSecondaryButton(
                            text = "Pause",
                            icon = Icons.Rounded.PauseCircleOutline,
                            onClick = { onChangeStatus(SubscriptionStatus.PAUSED) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_pause_button")
                        )
                        SubStopDestructiveButton(
                            text = "Stop Plan",
                            icon = Icons.Rounded.StopCircle,
                            onClick = { onChangeStatus(SubscriptionStatus.CANCELLED) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_stop_button")
                        )
                    } else {
                        SubStopSecondaryButton(
                            text = "Resume Plan",
                            icon = Icons.Rounded.PlayArrow,
                            onClick = { onChangeStatus(SubscriptionStatus.ACTIVE) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_resume_button")
                        )
                        SubStopDestructiveButton(
                            text = "Delete",
                            icon = Icons.Rounded.DeleteOutline,
                            onClick = { confirmDelete = true },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("detail_delete_button")
                        )
                    }
                }
            }
        }
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            containerColor = SubStopSurface,
            title = { Text("Delete ${subscription.name}?", color = Slate900) },
            text = {
                Text(
                    "This permanently removes the subscription record and its savings history.",
                    color = Slate500
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        onDelete()
                    }
                ) {
                    Text("Delete", color = SubStopError, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text("Keep", color = Slate500)
                }
            }
        )
    }
}

@Composable
private fun DetailMetaRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = Slate500
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.SemiBold),
            color = Slate900
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditSubscriptionSheet(
    existing: Subscription?,
    currencySymbol: String,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        amount: Double,
        cadence: BillingCadence,
        category: SubscriptionCategory,
        status: SubscriptionStatus,
        nextBillingEpochDay: Long,
        brandColorHex: String,
        iconKey: String,
        paymentLabel: String,
        notes: String,
        reminderEnabled: Boolean
    ) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var name by remember(existing) { mutableStateOf(existing?.name.orEmpty()) }
    var amountText by remember(existing) {
        mutableStateOf(if (existing != null) String.format(java.util.Locale.US, "%.2f", existing.amount) else "")
    }
    var cadence by remember(existing) { mutableStateOf(existing?.cadenceEnum() ?: BillingCadence.MONTHLY) }
    var category by remember(existing) { mutableStateOf(existing?.categoryEnum() ?: SubscriptionCategory.ENTERTAINMENT) }
    var status by remember(existing) { mutableStateOf(existing?.statusEnum() ?: SubscriptionStatus.ACTIVE) }
    var nextBillingEpochDay by remember(existing) {
        mutableLongStateOf(existing?.nextBillingEpochDay ?: (currentEpochDay() + 14L))
    }
    var brandColorHex by remember(existing) { mutableStateOf(existing?.brandColorHex ?: "#2563EB") }
    var iconKey by remember(existing) { mutableStateOf(existing?.iconKey ?: "card") }
    var paymentLabel by remember(existing) { mutableStateOf(existing?.paymentLabel.orEmpty()) }
    var notes by remember(existing) { mutableStateOf(existing?.notes.orEmpty()) }
    var reminderEnabled by remember(existing) { mutableStateOf(existing?.reminderEnabled ?: true) }
    var validationError by remember { mutableStateOf<String?>(null) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SubStopSurface,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (existing == null) "Add Subscription" else "Edit Subscription",
                style = MaterialTheme.typography.headlineMedium,
                color = Slate900
            )

            // Quick Service Presets when adding new
            if (existing == null) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "QUICK PRESETS",
                        style = LabelSmUppercaseStyle,
                        color = Slate500
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SubscriptionCatalog.presets.forEach { preset ->
                            val selected = name.equals(preset.name, ignoreCase = true)
                            Row(
                                modifier = Modifier
                                    .minimumInteractiveComponentSize()
                                    .clip(CircleShape)
                                    .background(if (selected) SubStopPrimaryTint else Slate100)
                                    .border(
                                        1.dp,
                                        if (selected) SubStopPrimary else Color.Transparent,
                                        CircleShape
                                    )
                                    .clickable {
                                        name = preset.name
                                        amountText = String.format(java.util.Locale.US, "%.2f", preset.defaultAmount)
                                        cadence = preset.cadence
                                        category = preset.category
                                        brandColorHex = preset.brandColorHex
                                        iconKey = preset.iconKey
                                    }
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = iconForKey(preset.iconKey),
                                    contentDescription = null,
                                    tint = parseHexColor(preset.brandColorHex),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Slate900
                                )
                            }
                        }
                    }
                }
            }

            // Service Name
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Service Name", style = MaterialTheme.typography.labelMedium, color = Slate500)
                SubStopInputField(
                    value = name,
                    onValueChange = {
                        name = it.take(60)
                        validationError = null
                    },
                    placeholder = "e.g., Netflix, iCloud+, Equinox",
                    modifier = Modifier.testTag("form_name_input")
                )
            }

            // Amount + Billing Cadence
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Price", style = MaterialTheme.typography.labelMedium, color = Slate500)
                    SubStopInputField(
                        value = amountText,
                        onValueChange = { raw ->
                            val filtered = raw.filter { it.isDigit() || it == '.' }
                            if (filtered.count { it == '.' } <= 1) {
                                amountText = filtered
                                validationError = null
                            }
                        },
                        placeholder = "15.99",
                        prefixText = currencySymbol,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.testTag("form_amount_input")
                    )
                }
            }

            // Cadence Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Billing Cadence", style = MaterialTheme.typography.labelMedium, color = Slate500)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BillingCadence.entries.forEach { option ->
                        SelectPill(
                            label = option.displayLabel,
                            selected = cadence == option,
                            onClick = { cadence = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Status Selector
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Plan Status", style = MaterialTheme.typography.labelMedium, color = Slate500)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL, SubscriptionStatus.PAUSED).forEach { option ->
                        SelectPill(
                            label = option.label,
                            selected = status == option,
                            onClick = { status = option },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Category Chips
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Category", style = MaterialTheme.typography.labelMedium, color = Slate500)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SubscriptionCategory.entries.forEach { cat ->
                        SelectPill(
                            label = cat.label,
                            selected = category == cat,
                            onClick = { category = cat }
                        )
                    }
                }
            }

            // Next Billing Date Picker Strip
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Next Billing / Trial End Date", style = MaterialTheme.typography.labelMedium, color = Slate500)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate100)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.CalendarToday,
                            contentDescription = null,
                            tint = SubStopPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = formatEpochDay(nextBillingEpochDay),
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { nextBillingEpochDay = (nextBillingEpochDay - 1L).coerceAtLeast(0L) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Remove, contentDescription = "Previous day", tint = Slate900)
                        }
                        IconButton(
                            onClick = { nextBillingEpochDay = (nextBillingEpochDay + 1L).coerceAtMost(100000L) },
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(Icons.Rounded.Add, contentDescription = "Next day", tint = Slate900)
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val today = currentEpochDay()
                    listOf(2L to "In 2 days", 7L to "In 1 wk", 30L to "In 1 mo").forEach { (offset, label) ->
                        SelectPill(
                            label = label,
                            selected = nextBillingEpochDay == today + offset,
                            onClick = { nextBillingEpochDay = today + offset },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Brand Color Swatches
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Brand Color", style = MaterialTheme.typography.labelMedium, color = Slate500)
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    SubscriptionCatalog.brandSwatches.forEach { (hex, label) ->
                        val color = parseHexColor(hex)
                        val selected = brandColorHex.equals(hex, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(color)
                                .then(
                                    if (selected) Modifier.border(3.dp, Slate900, CircleShape)
                                    else Modifier
                                )
                                .clickable { brandColorHex = hex }
                        )
                    }
                }
            }

            // Optional Payment Label & Notes
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Payment Method (Optional)", style = MaterialTheme.typography.labelMedium, color = Slate500)
                SubStopInputField(
                    value = paymentLabel,
                    onValueChange = { paymentLabel = it.take(40) },
                    placeholder = "e.g., Apple Card •• 8821"
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Notes (Optional)", style = MaterialTheme.typography.labelMedium, color = Slate500)
                SubStopInputField(
                    value = notes,
                    onValueChange = { notes = it.take(240) },
                    placeholder = "Plan tier or cancellation link reminder",
                    singleLine = false,
                    maxLines = 3
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Renewal Alert (3 days prior)", style = MaterialTheme.typography.titleMedium, color = Slate900)
                IosToggleSwitch(
                    checked = reminderEnabled,
                    onCheckedChange = { reminderEnabled = it },
                    contentDescription = "Renewal alert toggle"
                )
            }

            if (validationError != null) {
                Text(
                    text = validationError!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = SubStopError
                )
            }

            SubStopPrimaryButton(
                text = if (isSaving) "Saving..." else "Save Subscription",
                enabled = !isSaving,
                onClick = {
                    val parsedAmount = amountText.toDoubleOrNull()
                    when {
                        name.isBlank() -> validationError = "Please enter a service name."
                        parsedAmount == null || parsedAmount < 0.0 || parsedAmount > 50000.0 -> {
                            validationError = "Please enter a valid price between 0 and 50,000."
                        }
                        else -> onSave(
                            name,
                            parsedAmount,
                            cadence,
                            category,
                            status,
                            nextBillingEpochDay,
                            brandColorHex,
                            iconKey,
                            paymentLabel,
                            notes,
                            reminderEnabled
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("form_save_button")
            )
        }
    }
}

@Composable
private fun SelectPill(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clip(CircleShape)
            .background(if (selected) SubStopPrimary else Slate100)
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = if (selected) Color.White else Slate500,
            maxLines = 1
        )
    }
}

@Composable
fun EditBudgetDialog(
    currentBudget: Double,
    currencySymbol: String,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit
) {
    var budgetText by remember(currentBudget) {
        mutableStateOf(String.format(java.util.Locale.US, "%.0f", currentBudget))
    }
    var error by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SubStopSurface,
        title = {
            Text(
                text = "Monthly Budget Goal",
                style = MaterialTheme.typography.titleLarge,
                color = Slate900
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Set a comfortable monthly ceiling for all active subscriptions.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                SubStopInputField(
                    value = budgetText,
                    onValueChange = { raw ->
                        val filtered = raw.filter { it.isDigit() || it == '.' }
                        if (filtered.count { it == '.' } <= 1) {
                            budgetText = filtered
                            error = null
                        }
                    },
                    placeholder = "200",
                    prefixText = currencySymbol,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.testTag("budget_dialog_input")
                )
                if (error != null) {
                    Text(text = error!!, style = MaterialTheme.typography.bodySmall, color = SubStopError)
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val value = budgetText.toDoubleOrNull()
                    if (value == null || value <= 0.0 || value > 100000.0) {
                        error = "Enter an amount between 1 and 100,000"
                    } else {
                        onConfirm(value)
                    }
                },
                modifier = Modifier.testTag("budget_dialog_save")
            ) {
                Text("Save", color = SubStopPrimary, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = Slate500)
            }
        }
    )
}
