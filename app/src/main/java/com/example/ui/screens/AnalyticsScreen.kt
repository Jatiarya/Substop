package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.BillingCadence
import com.example.data.Subscription
import com.example.data.SubscriptionCategory
import com.example.data.formatCurrency
import com.example.ui.SubStopDashboardData
import com.example.ui.components.CircularSpendingRing
import com.example.ui.components.SubStopCard
import com.example.ui.components.SubStopSecondaryButton
import com.example.ui.theme.CurrencyStatTextStyle
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopError
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopSecondary
import com.example.ui.theme.SubStopTertiary

@Composable
fun AnalyticsScreen(
    data: SubStopDashboardData,
    onEditBudgetClick: () -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = data.profile.currencySymbol
    val budget = data.profile.monthlyBudget
    val remaining = (budget - data.totalMonthlySpend).coerceAtLeast(0.0)
    val overBudget = (data.totalMonthlySpend - budget).coerceAtLeast(0.0)
    val pct = (data.budgetRatio * 100).toInt().coerceAtMost(999)

    val activeSubs = data.subscriptions.filter { it.isActiveOrTrial() }
    val monthlyCadenceSum = activeSubs.filter { it.cadenceEnum() == BillingCadence.MONTHLY }.sumOf { it.monthlyNormalizedAmount() }
    val annualCadenceSum = activeSubs.filter { it.cadenceEnum() == BillingCadence.ANNUAL }.sumOf { it.monthlyNormalizedAmount() }
    val weeklyCadenceSum = activeSubs.filter { it.cadenceEnum() == BillingCadence.WEEKLY }.sumOf { it.monthlyNormalizedAmount() }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text(
                    text = "INSIGHTS",
                    style = LabelSmUppercaseStyle,
                    color = SubStopPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Spend Analytics",
                    style = MaterialTheme.typography.headlineLarge,
                    color = Slate900
                )
            }
        }

        // Budget Ring Hero Card
        item {
            SubStopCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CircularSpendingRing(
                        progress = data.budgetRatio,
                        size = 118.dp,
                        strokeWidth = 11.dp,
                        centerLabel = "${pct}%",
                        centerSubLabel = "ALLOCATED"
                    )
                    Spacer(modifier = Modifier.width(18.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "MONTHLY BUDGET",
                            style = LabelSmUppercaseStyle,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatCurrency(budget, currency),
                            style = CurrencyStatTextStyle.copy(fontSize = androidx.compose.ui.unit.TextUnit(28f, androidx.compose.ui.unit.TextUnitType.Sp)),
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (overBudget > 0.0) {
                                "${formatCurrency(overBudget, currency)} over target"
                            } else {
                                "${formatCurrency(remaining, currency)} remaining"
                            },
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                            color = if (overBudget > 0.0) SubStopError else SubStopSecondary
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                SubStopSecondaryButton(
                    text = "Adjust Monthly Budget",
                    icon = Icons.Rounded.Edit,
                    onClick = onEditBudgetClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("adjust_budget_button")
                )
            }
        }

        // Category Breakdown Card
        item {
            SubStopCard(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(20.dp)
            ) {
                Text(
                    text = "Category Breakdown",
                    style = MaterialTheme.typography.titleLarge,
                    color = Slate900
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Normalized monthly recurring spend",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate500
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (data.categoryBreakdown.isEmpty()) {
                    Text(
                        text = "Add active subscriptions to see category distribution.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Slate500
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                        data.categoryBreakdown.forEach { item ->
                            val animatedFraction by animateFloatAsState(
                                targetValue = item.percentageOfTotal.coerceIn(0.04f, 1f),
                                animationSpec = spring(stiffness = 220f),
                                label = "catBar_${item.category.name}"
                            )
                            val barColor = categoryAccentColor(item.category)
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.category.label} (${item.count})",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = Slate900
                                    )
                                    Text(
                                        text = "${formatCurrency(item.monthlyAmount, currency)} • ${(item.percentageOfTotal * 100).toInt()}%",
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                                        color = Slate500
                                    )
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(10.dp)
                                        .clip(CircleShape)
                                        .background(Slate200.copy(alpha = 0.65f))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(animatedFraction)
                                            .fillMaxHeight()
                                            .clip(CircleShape)
                                            .background(barColor)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Billing Cadence Split & 12-Month Burn
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SubStopCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "ANNUAL PROJECTION",
                        style = LabelSmUppercaseStyle,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = formatCurrency(data.totalAnnualProjection, currency),
                        style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Next 12 months",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                }

                SubStopCard(
                    modifier = Modifier.weight(1f),
                    cornerRadius = 20.dp,
                    contentPadding = PaddingValues(18.dp)
                ) {
                    Text(
                        text = "CADENCE SPLIT",
                        style = LabelSmUppercaseStyle,
                        color = Slate500
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "${formatCurrency(monthlyCadenceSum, currency)}/mo",
                        style = MaterialTheme.typography.titleMedium,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Annualized: ${formatCurrency(annualCadenceSum, currency)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate500
                    )
                    if (weeklyCadenceSum > 0.0) {
                        Text(
                            text = "Weekly: ${formatCurrency(weeklyCadenceSum, currency)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                    }
                }
            }
        }

        // Next 3 Renewals Timeline
        if (activeSubs.isNotEmpty()) {
            item {
                SubStopCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    Text(
                        text = "Upcoming Billing Timeline",
                        style = MaterialTheme.typography.titleLarge,
                        color = Slate900
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    activeSubs.take(5).forEach { sub ->
                        val days = sub.daysUntilRenewal()
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Slate900
                                )
                                Text(
                                    text = "${sub.formattedNextBillingDate()} (${if (days <= 0) "Today" else "in $days days"})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Slate500
                                )
                            }
                            Text(
                                text = formatCurrency(sub.amount, currency),
                                style = MaterialTheme.typography.titleMedium,
                                color = SubStopPrimary
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun categoryAccentColor(category: SubscriptionCategory): Color = when (category) {
    SubscriptionCategory.ENTERTAINMENT -> SubStopPrimary
    SubscriptionCategory.CLOUD_AI -> SubStopSecondary
    SubscriptionCategory.PRODUCTIVITY -> Color(0xFF7C3AED)
    SubscriptionCategory.FITNESS -> SubStopTertiary
    SubscriptionCategory.UTILITIES -> Color(0xFF0EA5E9)
    SubscriptionCategory.OTHER -> Color(0xFF64748B)
}
