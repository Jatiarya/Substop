package com.example.ui.screens

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.Subscription
import com.example.data.SubscriptionCategory
import com.example.data.formatCurrency
import com.example.ui.SubStopDashboardData
import com.example.ui.components.CircularSpendingRing
import com.example.ui.components.ServiceSquircleAvatar
import com.example.ui.components.StatusBadge
import com.example.ui.components.SubStopCard
import com.example.ui.components.SubStopPrimaryButton
import com.example.ui.components.SubStopSecondaryButton
import com.example.ui.components.SubscriptionRowItem
import com.example.ui.theme.CurrencyStatTextStyle
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopPrimaryTint
import com.example.ui.theme.SubStopSecondary
import com.example.ui.theme.SubStopSecondaryText
import com.example.ui.theme.SubStopSecondaryTint
import com.example.ui.theme.SubStopShadowBlue
import com.example.ui.theme.SubStopSurface
import com.example.ui.theme.SubStopTertiary
import com.example.ui.theme.SubStopTertiaryText
import com.example.ui.theme.SubStopTertiaryTint

@Composable
fun OverviewScreen(
    data: SubStopDashboardData,
    selectedCategory: SubscriptionCategory?,
    onSelectCategory: (SubscriptionCategory?) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onAddClick: () -> Unit,
    onSeedSamplePack: () -> Unit,
    onNavigateToAnalytics: () -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = data.profile.currencySymbol
    val firstName = data.profile.displayName.split(" ").firstOrNull().orEmpty().ifBlank { "there" }
    val activeSubs = data.subscriptions.filter { sub ->
        sub.isActiveOrTrial() && (selectedCategory == null || sub.categoryEnum() == selectedCategory)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Top Header Bar
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SUBSTOP CONTROL",
                        style = LabelSmUppercaseStyle,
                        color = SubStopPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Hi, $firstName",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Slate900
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = SubStopSurface,
                        modifier = Modifier.shadow(
                            6.dp,
                            CircleShape,
                            ambientColor = SubStopShadowBlue.copy(alpha = 0.12f)
                        )
                    ) {
                        IconButton(
                            onClick = onNavigateToSettings,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("header_alerts_button")
                        ) {
                            Box {
                                Icon(
                                    imageVector = Icons.Rounded.NotificationsNone,
                                    contentDescription = "Reminder preferences",
                                    tint = Slate900
                                )
                                if (data.upcomingRenewals.isNotEmpty()) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .size(8.dp)
                                            .clip(CircleShape)
                                            .background(SubStopTertiary)
                                    )
                                }
                            }
                        }
                    }

                    Surface(
                        shape = CircleShape,
                        color = SubStopPrimary,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .clickable(onClick = onAddClick)
                            .testTag("header_add_button")
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Rounded.Add,
                                contentDescription = "Add subscription",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }

        // Total Monthly Spend Hero Summary Card
        item {
            val pct = (data.budgetRatio * 100).toInt().coerceAtMost(999)
            SubStopCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("monthly_spend_card"),
                onClick = onNavigateToAnalytics,
                contentPadding = PaddingValues(22.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Total Monthly Spend",
                                style = MaterialTheme.typography.titleMedium,
                                color = Slate500
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = formatCurrency(data.totalMonthlySpend, currency),
                            style = CurrencyStatTextStyle,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${formatCurrency(data.totalAnnualProjection, currency)} projected /yr",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(SubStopSecondaryTint)
                                    .padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(SubStopSecondary)
                                )
                                Text(
                                    text = "${data.activeCount + data.trialCount} ACTIVE",
                                    style = LabelSmUppercaseStyle,
                                    color = SubStopSecondaryText
                                )
                            }
                            if (data.monthlySavingsFromStopped > 0.0) {
                                Row(
                                    modifier = Modifier
                                        .clip(CircleShape)
                                        .background(SubStopPrimaryTint)
                                        .padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Savings,
                                        contentDescription = null,
                                        tint = SubStopPrimary,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = "${formatCurrency(data.monthlySavingsFromStopped, currency)} SAVED",
                                        style = LabelSmUppercaseStyle,
                                        color = SubStopPrimary
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    CircularSpendingRing(
                        progress = data.budgetRatio,
                        size = 92.dp,
                        strokeWidth = 9.dp,
                        centerLabel = "${pct}%",
                        centerSubLabel = "OF ${formatCurrency(data.profile.monthlyBudget, currency).substringBefore(".")}"
                    )
                }
            }
        }

        // Empty State Onboarding Card when user has 0 subscriptions
        if (data.subscriptions.isEmpty()) {
            item {
                SubStopCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(SubStopPrimaryTint),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.AutoAwesome,
                                contentDescription = null,
                                tint = SubStopPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Start your subscription radar",
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Add your streaming, cloud, or fitness plans to monitor renewals and cut unwanted charges.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        SubStopPrimaryButton(
                            text = "Add First Subscription",
                            icon = Icons.Rounded.Add,
                            onClick = onAddClick,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("empty_add_subscription_button")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        SubStopSecondaryButton(
                            text = "Load Sample Pack (6 Services)",
                            onClick = onSeedSamplePack,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("load_sample_pack_button")
                        )
                    }
                }
            }
        }

        // Upcoming Renewals Strip ("Due Soon")
        if (data.upcomingRenewals.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Upcoming Renewals",
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate900
                        )
                        Row(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(SubStopTertiaryTint)
                                .padding(horizontal = 10.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${data.upcomingRenewals.size} DUE SOON",
                                style = LabelSmUppercaseStyle,
                                color = SubStopTertiaryText
                            )
                        }
                    }

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(data.upcomingRenewals, key = { "upcoming_${it.id}" }) { sub ->
                            val days = sub.daysUntilRenewal()
                            SubStopCard(
                                modifier = Modifier
                                    .width(196.dp)
                                    .testTag("upcoming_card_${sub.id}"),
                                cornerRadius = 20.dp,
                                onClick = { onSubscriptionClick(sub) },
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    ServiceSquircleAvatar(
                                        brandColorHex = sub.brandColorHex,
                                        iconKey = sub.iconKey,
                                        serviceName = sub.name,
                                        size = 40.dp
                                    )
                                    StatusBadge(
                                        status = sub.statusEnum(),
                                        daysUntilRenewal = days
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = sub.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = Slate900,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = formatCurrency(sub.amount, currency) + sub.cadenceEnum().shortLabel,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = SubStopPrimary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Category Filter Chips + Active Subscriptions Grouping Container
        if (data.subscriptions.isNotEmpty()) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Active Subscriptions",
                            style = MaterialTheme.typography.titleLarge,
                            color = Slate900
                        )
                        Text(
                            text = "${activeSubs.size} plans",
                            style = MaterialTheme.typography.labelMedium,
                            color = Slate500
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        CategoryFilterChip(
                            label = "All",
                            selected = selectedCategory == null,
                            onClick = { onSelectCategory(null) }
                        )
                        SubscriptionCategory.entries.forEach { category ->
                            CategoryFilterChip(
                                label = category.label,
                                selected = selectedCategory == category,
                                onClick = {
                                    onSelectCategory(if (selectedCategory == category) null else category)
                                }
                            )
                        }
                    }
                }
            }

            item {
                if (activeSubs.isEmpty()) {
                    SubStopCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(20.dp)
                    ) {
                        Text(
                            text = "No active subscriptions in this category.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate500
                        )
                    }
                } else {
                    SubStopCard(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                    ) {
                        activeSubs.forEachIndexed { index, sub ->
                            SubscriptionRowItem(
                                subscription = sub,
                                currencySymbol = currency,
                                onClick = { onSubscriptionClick(sub) }
                            )
                            if (index < activeSubs.lastIndex) {
                                HorizontalDivider(
                                    modifier = Modifier.padding(horizontal = 16.dp),
                                    thickness = 1.dp,
                                    color = Slate200.copy(alpha = 0.65f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CategoryFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val bg by animateColorAsState(
        targetValue = if (selected) SubStopSurface else Color.Transparent,
        label = "chipBg"
    )
    val textColor by animateColorAsState(
        targetValue = if (selected) Slate900 else Slate500,
        label = "chipText"
    )
    Box(
        modifier = Modifier
            .minimumInteractiveComponentSize()
            .then(
                if (selected) {
                    Modifier.shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = SubStopShadowBlue.copy(alpha = 0.14f),
                        spotColor = SubStopShadowBlue.copy(alpha = 0.14f)
                    )
                } else Modifier
            )
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = textColor
        )
    }
}
