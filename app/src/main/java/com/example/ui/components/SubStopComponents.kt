package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Bolt
import androidx.compose.material.icons.rounded.Brush
import androidx.compose.material.icons.rounded.CloudQueue
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.DesignServices
import androidx.compose.material.icons.rounded.FitnessCenter
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Subscription
import com.example.data.SubscriptionStatus
import com.example.data.formatCurrency
import com.example.ui.theme.LabelSmUppercaseStyle
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopError
import com.example.ui.theme.SubStopErrorTint
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopPrimaryTint
import com.example.ui.theme.SubStopSecondary
import com.example.ui.theme.SubStopSecondaryText
import com.example.ui.theme.SubStopSecondaryTint
import com.example.ui.theme.SubStopShadowBlue
import com.example.ui.theme.SubStopShadowSlate
import com.example.ui.theme.SubStopSurface
import com.example.ui.theme.SubStopTertiary
import com.example.ui.theme.SubStopTertiaryText
import com.example.ui.theme.SubStopTertiaryTint

fun parseHexColor(hex: String, fallback: Color = SubStopPrimary): Color {
    return try {
        val cleaned = hex.removePrefix("#")
        val value = cleaned.toLong(16)
        when (cleaned.length) {
            6 -> Color((0xFF000000L or value).toInt())
            8 -> Color(value.toInt())
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}

fun iconForKey(iconKey: String): ImageVector = when (iconKey.lowercase()) {
    "movie" -> Icons.Rounded.Movie
    "music" -> Icons.Rounded.MusicNote
    "cloud" -> Icons.Rounded.CloudQueue
    "spark" -> Icons.Rounded.AutoAwesome
    "play" -> Icons.Rounded.PlayCircle
    "design" -> Icons.Rounded.DesignServices
    "palette" -> Icons.Rounded.Brush
    "fitness" -> Icons.Rounded.FitnessCenter
    "bolt" -> Icons.Rounded.Bolt
    "bag" -> Icons.Rounded.ShoppingBag
    else -> Icons.Rounded.CreditCard
}

@Composable
fun SubStopCard(
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 24.dp,
    onClick: (() -> Unit)? = null,
    contentPadding: PaddingValues = PaddingValues(20.dp),
    content: @Composable ColumnScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (onClick != null && isPressed) 0.985f else 1f,
        animationSpec = spring(stiffness = 500f),
        label = "cardScale"
    )
    val shape = RoundedCornerShape(cornerRadius)
    val shadowElevation = if (isPressed) 6.dp else 14.dp

    Surface(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = shadowElevation,
                shape = shape,
                ambientColor = SubStopShadowBlue.copy(alpha = 0.14f),
                spotColor = SubStopShadowBlue.copy(alpha = 0.14f)
            )
            .clip(shape)
            .then(
                if (onClick != null) {
                    Modifier.clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = onClick
                    )
                } else Modifier
            ),
        shape = shape,
        color = SubStopSurface,
        border = BorderStroke(1.dp, Slate200.copy(alpha = 0.45f))
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

@Composable
fun ServiceSquircleAvatar(
    brandColorHex: String,
    iconKey: String,
    serviceName: String,
    size: Dp = 44.dp
) {
    val brandColor = parseHexColor(brandColorHex)
    Box(
        modifier = Modifier
            .size(size)
            .clip(RoundedCornerShape(16.dp))
            .background(brandColor.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = iconForKey(iconKey),
            contentDescription = "$serviceName icon",
            tint = brandColor,
            modifier = Modifier.size(size * 0.52f)
        )
    }
}

@Composable
fun StatusBadge(
    status: SubscriptionStatus,
    daysUntilRenewal: Long? = null
) {
    val isDueSoon = (status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL) &&
        daysUntilRenewal != null && daysUntilRenewal in 0L..3L

    val (bg, fg, dotColor, label) = when {
        status == SubscriptionStatus.TRIAL -> Quad(
            SubStopTertiaryTint,
            SubStopTertiaryText,
            SubStopTertiary,
            if (daysUntilRenewal != null && daysUntilRenewal in 0L..7L) {
                "TRIAL • ${daysUntilRenewal}D"
            } else "TRIAL"
        )
        isDueSoon -> Quad(
            SubStopTertiaryTint,
            SubStopTertiaryText,
            SubStopTertiary,
            if (daysUntilRenewal == 0L) "RENEWS TODAY" else "IN ${daysUntilRenewal}D"
        )
        status == SubscriptionStatus.ACTIVE -> Quad(
            SubStopSecondaryTint,
            SubStopSecondaryText,
            SubStopSecondary,
            "ACTIVE"
        )
        status == SubscriptionStatus.PAUSED -> Quad(
            Slate100,
            Slate500,
            Slate400,
            "PAUSED"
        )
        else -> Quad(
            SubStopErrorTint,
            SubStopError,
            SubStopError,
            "STOPPED"
        )
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = label,
            style = LabelSmUppercaseStyle,
            color = fg,
            maxLines = 1
        )
    }
}

private data class Quad<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)

@Composable
fun SubscriptionRowItem(
    subscription: Subscription,
    currencySymbol: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val days = subscription.daysUntilRenewal()
    val status = subscription.statusEnum()
    val cadence = subscription.cadenceEnum()
    val subtitle = when {
        status == SubscriptionStatus.CANCELLED -> "Stopped • ${subscription.category}"
        status == SubscriptionStatus.PAUSED -> "Paused • ${subscription.category}"
        days == 0L -> "Renews today • ${subscription.category}"
        days == 1L -> "Renews tomorrow • ${subscription.category}"
        days in 2L..5L -> "Renews in $days days"
        else -> "Next: ${subscription.formattedNextBillingDate()}"
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .minimumInteractiveComponentSize()
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 12.dp)
            .testTag("subscription_row_${subscription.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ServiceSquircleAvatar(
            brandColorHex = subscription.brandColorHex,
            iconKey = subscription.iconKey,
            serviceName = subscription.name,
            size = 44.dp
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = subscription.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = Slate900,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                if (status != SubscriptionStatus.ACTIVE || days in 0L..3L) {
                    StatusBadge(status = status, daysUntilRenewal = days)
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = if ((status == SubscriptionStatus.ACTIVE || status == SubscriptionStatus.TRIAL) && days in 0L..3L) {
                    SubStopTertiaryText
                } else {
                    Slate500
                },
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = formatCurrency(subscription.amount, currencySymbol),
                style = MaterialTheme.typography.titleMedium,
                color = if (subscription.isActiveOrTrial()) Slate900 else Slate500
            )
            Text(
                text = cadence.name,
                style = LabelSmUppercaseStyle,
                color = Slate400
            )
        }
    }
}

@Composable
fun CircularSpendingRing(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 88.dp,
    strokeWidth: Dp = 9.dp,
    centerLabel: String,
    centerSubLabel: String? = null
) {
    val clamped = progress.coerceIn(0f, 1f)
    val animatedSweep by animateFloatAsState(
        targetValue = clamped * 360f,
        animationSpec = spring(stiffness = 180f, dampingRatio = 0.8f),
        label = "ringSweep"
    )
    val activeColor by animateColorAsState(
        targetValue = when {
            progress > 1.0f -> SubStopError
            progress >= 0.85f -> SubStopTertiary
            else -> SubStopPrimary
        },
        label = "ringColor"
    )

    Box(modifier = modifier.size(size), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(this.size.width - strokePx, this.size.height - strokePx)
            drawArc(
                color = Slate200,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animatedSweep > 0f) {
                drawArc(
                    color = activeColor,
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = centerLabel,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.ExtraBold),
                color = Slate900
            )
            if (centerSubLabel != null) {
                Text(
                    text = centerSubLabel,
                    style = LabelSmUppercaseStyle.copy(fontSize = 9.sp),
                    color = Slate500
                )
            }
        }
    }
}

@Composable
fun SubStopPrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: ImageVector? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = 600f),
        label = "btnScale"
    )
    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SubStopPrimary,
            contentColor = Color.White,
            disabledContainerColor = SubStopPrimary.copy(alpha = 0.45f),
            disabledContentColor = Color.White
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 14.dp),
        modifier = modifier
            .scale(scale)
            .height(52.dp)
            .minimumInteractiveComponentSize()
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = Color.White)
    }
}

@Composable
fun SubStopSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SubStopPrimaryTint,
            contentColor = SubStopPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .height(50.dp)
            .minimumInteractiveComponentSize()
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = SubStopPrimary)
    }
}

@Composable
fun SubStopDestructiveButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null
) {
    Button(
        onClick = onClick,
        shape = CircleShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = SubStopErrorTint,
            contentColor = SubStopError
        ),
        elevation = ButtonDefaults.buttonElevation(0.dp, 0.dp),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
        modifier = modifier
            .height(50.dp)
            .minimumInteractiveComponentSize()
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text, style = MaterialTheme.typography.titleMedium, color = SubStopError)
    }
}

@Composable
fun SubStopInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
    prefixText: String? = null,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    singleLine: Boolean = true,
    maxLines: Int = if (singleLine) 1 else 3
) {
    var isFocused by remember { mutableStateOf(false) }
    val bgColor by animateColorAsState(
        targetValue = if (isFocused) SubStopSurface else Slate100,
        label = "inputBg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isFocused) SubStopPrimary else Color.Transparent,
        label = "inputBorder"
    )

    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (singleLine) Modifier.height(48.dp) else Modifier.padding(vertical = 2.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(bgColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 14.dp, vertical = if (singleLine) 0.dp else 12.dp),
        verticalAlignment = if (singleLine) Alignment.CenterVertically else Alignment.Top
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = if (isFocused) SubStopPrimary else Slate400,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
        }
        if (prefixText != null) {
            Text(
                text = prefixText,
                style = MaterialTheme.typography.titleMedium,
                color = Slate500
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Box(modifier = Modifier.weight(1f)) {
            if (value.isEmpty()) {
                Text(
                    text = placeholder,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate400
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Slate900),
                singleLine = singleLine,
                maxLines = maxLines,
                keyboardOptions = keyboardOptions,
                cursorBrush = SolidColor(SubStopPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .onFocusChanged { isFocused = it.isFocused }
            )
        }
    }
}

@Composable
fun IosToggleSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) SubStopPrimary else Slate200,
        label = "switchTrack"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 22.dp else 3.dp,
        animationSpec = spring(stiffness = 500f, dampingRatio = 0.78f),
        label = "switchThumb"
    )

    Box(
        modifier = modifier
            .minimumInteractiveComponentSize()
            .clickable(
                role = Role.Switch,
                onClickLabel = contentDescription
            ) { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .width(50.dp)
                .height(30.dp)
                .clip(CircleShape)
                .background(trackColor),
            contentAlignment = Alignment.CenterStart
        ) {
            Box(
                modifier = Modifier
                    .offset(x = thumbOffset)
                    .size(24.dp)
                    .shadow(3.dp, CircleShape, ambientColor = SubStopShadowSlate.copy(alpha = 0.25f))
                    .clip(CircleShape)
                    .background(Color.White)
            )
        }
    }
}

data class SubStopNavItem(
    val route: String,
    val label: String,
    val icon: ImageVector,
    val badgeCount: Int = 0
)

@Composable
fun FloatingBottomNavBar(
    items: List<SubStopNavItem>,
    currentRoute: String,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 20.dp,
                    shape = CircleShape,
                    ambientColor = SubStopShadowSlate.copy(alpha = 0.14f),
                    spotColor = SubStopShadowBlue.copy(alpha = 0.14f)
                ),
            shape = CircleShape,
            color = SubStopSurface.copy(alpha = 0.98f),
            border = BorderStroke(1.dp, Slate200.copy(alpha = 0.6f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { item ->
                    val selected = item.route == currentRoute
                    val bg by animateColorAsState(
                        targetValue = if (selected) SubStopPrimaryTint else Color.Transparent,
                        label = "navPillBg"
                    )
                    val tint by animateColorAsState(
                        targetValue = if (selected) SubStopPrimary else Slate500,
                        label = "navTint"
                    )
                    Row(
                        modifier = Modifier
                            .weight(1f)
                            .minimumInteractiveComponentSize()
                            .clip(CircleShape)
                            .background(bg)
                            .clickable { onSelect(item.route) }
                            .padding(horizontal = 10.dp, vertical = 10.dp)
                            .testTag("nav_${item.route}"),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = tint,
                                modifier = Modifier.size(20.dp)
                            )
                            if (item.badgeCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .offset(x = 3.dp, y = (-2).dp)
                                        .size(7.dp)
                                        .clip(CircleShape)
                                        .background(SubStopTertiary)
                                )
                            }
                        }
                        if (selected) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = item.label,
                                style = MaterialTheme.typography.labelMedium,
                                color = SubStopPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }
        }
    }
}
