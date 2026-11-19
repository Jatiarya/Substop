package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import com.example.auth.signOut
import com.example.data.formatCurrency
import com.example.ui.SubStopDashboardData
import com.example.ui.components.IosToggleSwitch
import com.example.ui.components.SubStopCard
import com.example.ui.components.SubStopDestructiveButton
import com.example.ui.components.SubStopSecondaryButton
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopPrimaryTint

@Composable
fun SettingsScreen(
    data: SubStopDashboardData,
    onUpdateToggles: (renewalAlerts: Boolean, trialWarnings: Boolean, weeklyDigest: Boolean) -> Unit,
    onEditBudgetClick: () -> Unit,
    onLoadSamplePack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }
    val profile = data.profile
    val initials = profile.displayName
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "S" }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "PREFERENCES",
                    style = LabelSmUppercaseStyle,
                    color = SubStopPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Alerts & Account",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Slate900
                )
            }
        }

        // User Profile Card
        item {
            SubStopCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .clip(CircleShape)
                            .background(SubStopPrimaryTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = initials,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = SubStopPrimary
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = profile.displayName.ifBlank { "SubStop Member" },
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate900
                        )
                        if (profile.email.isNotBlank()) {
                            Text(
                                text = profile.email,
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate500
                            )
                        }
                    }
                }
            }
        }

        // Budget Goal Row
        item {
            SubStopCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .minimumInteractiveComponentSize()
                        .clickable(onClick = onEditBudgetClick)
                        .padding(vertical = 12.dp)
                        .testTag("settings_budget_row"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(SubStopPrimaryTint),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Savings,
                            contentDescription = null,
                            tint = SubStopPrimary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Monthly Budget Goal",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                        Text(
                            text = "Current target: ${formatCurrency(profile.monthlyBudget, profile.currencySymbol)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Edit monthly budget",
                        tint = Slate500
                    )
                }
            }
        }

        // Renewal Reminder Preferences (iOS Toggles)
        item {
            SubStopCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "Renewal Alerts",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Stay ahead of upcoming deductions",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(14.dp))

                ToggleSettingRow(
                    title = "3-Day Renewal Alerts",
                    subtitle = "Highlight charges arriving within 72 hours",
                    checked = profile.renewalAlertsEnabled,
                    onCheckedChange = {
                        onUpdateToggles(it, profile.trialWarningsEnabled, profile.weeklyDigestEnabled)
                    },
                    testTag = "toggle_renewal_alerts"
                )
                HorizontalDivider(color = Slate200.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
                ToggleSettingRow(
                    title = "Trial Expiration Warnings",
                    subtitle = "Surface free trials before auto-conversion",
                    checked = profile.trialWarningsEnabled,
                    onCheckedChange = {
                        onUpdateToggles(profile.renewalAlertsEnabled, it, profile.weeklyDigestEnabled)
                    },
                    testTag = "toggle_trial_warnings"
                )
                HorizontalDivider(color = Slate200.copy(alpha = 0.6f), modifier = Modifier.padding(vertical = 8.dp))
                ToggleSettingRow(
                    title = "Weekly Spend Digest",
                    subtitle = "Summarize monthly burn & unlocked savings",
                    checked = profile.weeklyDigestEnabled,
                    onCheckedChange = {
                        onUpdateToggles(profile.renewalAlertsEnabled, profile.trialWarningsEnabled, it)
                    },
                    testTag = "toggle_weekly_digest"
                )
            }
        }

        // Starter Pack & Sign Out
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                if (data.subscriptions.isEmpty()) {
                    SubStopSecondaryButton(
                        text = "Load Sample Subscriptions",
                        icon = Icons.Rounded.AutoAwesome,
                        onClick = onLoadSamplePack,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("settings_load_samples_button")
                    )
                }

                SubStopDestructiveButton(
                    text = "Sign Out",
                    icon = Icons.AutoMirrored.Rounded.Logout,
                    onClick = {
                        signOut(credentialManager = credentialManager, scope = scope)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sign_out_button")
                )
            }
        }
    }
}

@Composable
private fun ToggleSettingRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f).padding(end = 12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = Slate900
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = Slate500
            )
        }
        IosToggleSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            contentDescription = title,
            modifier = Modifier.testTag(testTag)
        )
    }
}
