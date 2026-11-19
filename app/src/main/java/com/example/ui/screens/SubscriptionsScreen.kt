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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Savings
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.Subscription
import com.example.data.SubscriptionStatus
import com.example.data.formatCurrency
import com.example.ui.SubStopDashboardData
import com.example.ui.components.SubStopCard
import com.example.ui.components.SubStopInputField
import com.example.ui.components.SubStopPrimaryButton
import com.example.ui.components.SubscriptionRowItem
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopSecondary
import com.example.ui.theme.SubStopSecondaryText
import com.example.ui.theme.SubStopSecondaryTint
import com.example.ui.theme.SubStopShadowBlue
import com.example.ui.theme.SubStopSurface

@Composable
fun SubscriptionsScreen(
    data: SubStopDashboardData,
    searchQuery: String,
    statusFilter: String,
    onSearchQueryChange: (String) -> Unit,
    onSelectStatusFilter: (String) -> Unit,
    onSubscriptionClick: (Subscription) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currency = data.profile.currencySymbol
    val filtered = data.subscriptions.filter { sub ->
        val matchesStatus = when (statusFilter) {
            "ACTIVE" -> sub.statusEnum() == SubscriptionStatus.ACTIVE
            "TRIAL" -> sub.statusEnum() == SubscriptionStatus.TRIAL
            "STOPPED" -> sub.statusEnum() == SubscriptionStatus.PAUSED || sub.statusEnum() == SubscriptionStatus.CANCELLED
            else -> true
        }
        val q = searchQuery.trim().lowercase()
        val matchesQuery = q.isEmpty() ||
            sub.name.lowercase().contains(q) ||
            sub.category.lowercase().contains(q)
        matchesStatus && matchesQuery
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 112.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DIRECTORY",
                        style = LabelSmUppercaseStyle,
                        color = SubStopPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Subscriptions",
                        style = MaterialTheme.typography.headlineLarge,
                        color = Slate900
                    )
                }
                Surface(
                    shape = CircleShape,
                    color = SubStopPrimary,
                    modifier = Modifier
                        .minimumInteractiveComponentSize()
                        .clip(CircleShape)
                        .clickable(onClick = onAddClick)
                        .testTag("subs_add_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = "Add new subscription",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Add",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // Search Bar
        item {
            SubStopInputField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                placeholder = "Search Netflix, Spotify, Cloud...",
                leadingIcon = Icons.Rounded.Search,
                modifier = Modifier.testTag("subscriptions_search_input")
            )
        }

        // Status Tabs
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatusSegmentTab(
                    label = "Active (${data.activeCount})",
                    selected = statusFilter == "ACTIVE",
                    onClick = { onSelectStatusFilter("ACTIVE") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_active")
                )
                StatusSegmentTab(
                    label = "Trials (${data.trialCount})",
                    selected = statusFilter == "TRIAL",
                    onClick = { onSelectStatusFilter("TRIAL") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_trial")
                )
                StatusSegmentTab(
                    label = "Stopped (${data.pausedOrCancelledCount})",
                    selected = statusFilter == "STOPPED",
                    onClick = { onSelectStatusFilter("STOPPED") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("tab_stopped")
                )
            }
        }

        // Monthly Savings Unlocked Banner
        if (data.monthlySavingsFromStopped > 0.0) {
            item {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = SubStopSecondaryTint,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SubStopSecondary.copy(alpha = 0.16f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Savings,
                                contentDescription = null,
                                tint = SubStopSecondaryText,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "SAVINGS UNLOCKED",
                                style = LabelSmUppercaseStyle,
                                color = SubStopSecondaryText
                            )
                            Text(
                                text = "${formatCurrency(data.monthlySavingsFromStopped, currency)}/mo (${formatCurrency(data.monthlySavingsFromStopped * 12.0, currency)}/yr)",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Slate900
                            )
                        }
                    }
                }
            }
        }

        // Filtered List
        item {
            if (filtered.isEmpty()) {
                SubStopCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(24.dp)
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (searchQuery.isNotBlank()) "No matching subscriptions" else "No subscriptions here yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = Slate900
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (statusFilter == "STOPPED") {
                                "Paused or cancelled plans will appear here along with your monthly savings."
                            } else {
                                "Tap below to add a recurring plan or free trial."
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate500
                        )
                        if (statusFilter != "STOPPED") {
                            Spacer(modifier = Modifier.height(16.dp))
                            SubStopPrimaryButton(
                                text = "Add Subscription",
                                icon = Icons.Rounded.Add,
                                onClick = onAddClick,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            } else {
                SubStopCard(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(vertical = 6.dp, horizontal = 4.dp)
                ) {
                    filtered.forEachIndexed { index, sub ->
                        SubscriptionRowItem(
                            subscription = sub,
                            currencySymbol = currency,
                            onClick = { onSubscriptionClick(sub) }
                        )
                        if (index < filtered.lastIndex) {
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

@Composable
private fun StatusSegmentTab(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            .background(if (selected) SubStopSurface else Color.Transparent)
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (selected) FontWeight.Bold else FontWeight.SemiBold
            ),
            color = if (selected) Slate900 else Slate500,
            maxLines = 1
        )
    }
}
