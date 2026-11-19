package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class BillingCadence(val displayLabel: String, val shortLabel: String) {
    MONTHLY("Monthly", "/mo"),
    ANNUAL("Annual", "/yr"),
    WEEKLY("Weekly", "/wk");

    fun toMonthlyAmount(amount: Double): Double = when (this) {
        MONTHLY -> amount
        ANNUAL -> amount / 12.0
        WEEKLY -> amount * 52.0 / 12.0
    }

    companion object {
        fun fromRaw(raw: String): BillingCadence =
            entries.firstOrNull { it.name == raw } ?: MONTHLY
    }
}

enum class SubscriptionCategory(val label: String) {
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    CLOUD_AI("Cloud & AI"),
    FITNESS("Fitness"),
    UTILITIES("Utilities"),
    OTHER("Other");

    companion object {
        fun fromRaw(raw: String): SubscriptionCategory =
            entries.firstOrNull { it.label == raw || it.name == raw } ?: OTHER
    }
}

enum class SubscriptionStatus(val label: String) {
    ACTIVE("Active"),
    TRIAL("Trial"),
    PAUSED("Paused"),
    CANCELLED("Cancelled");

    companion object {
        fun fromRaw(raw: String): SubscriptionStatus =
            entries.firstOrNull { it.name == raw } ?: ACTIVE
    }
}

@Keep
@IgnoreExtraProperties
data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val monthlyBudget: Double = 200.0,
    val renewalAlertsEnabled: Boolean = true,
    val trialWarningsEnabled: Boolean = true,
    val weeklyDigestEnabled: Boolean = false,
    val currencySymbol: String = "$",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class Subscription(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val monthlyCost: Double = 0.0,
    val amount: Double = monthlyCost,
    val cadence: String = BillingCadence.MONTHLY.name,
    val category: String = SubscriptionCategory.ENTERTAINMENT.label,
    val status: String = SubscriptionStatus.ACTIVE.name,
    val nextBillingDate: String = formatEpochDay(currentEpochDay()),
    val nextBillingEpochDay: Long = currentEpochDay(),
    val brandColorHex: String = "#2563EB",
    val iconKey: String = "card",
    val paymentLabel: String = "",
    val notes: String = "",
    val reminderEnabled: Boolean = true,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
) {
    fun cadenceEnum(): BillingCadence = BillingCadence.fromRaw(cadence)
    fun categoryEnum(): SubscriptionCategory = SubscriptionCategory.fromRaw(category)
    fun statusEnum(): SubscriptionStatus = SubscriptionStatus.fromRaw(status)

    fun monthlyNormalizedAmount(): Double =
        if (amount > 0.0) cadenceEnum().toMonthlyAmount(amount) else monthlyCost

    fun isActiveOrTrial(): Boolean {
        val s = statusEnum()
        return s == SubscriptionStatus.ACTIVE || s == SubscriptionStatus.TRIAL
    }

    fun daysUntilRenewal(todayEpochDay: Long = currentEpochDay()): Long =
        nextBillingEpochDay - todayEpochDay

    fun formattedNextBillingDate(): String =
        nextBillingDate.ifBlank { formatEpochDay(nextBillingEpochDay) }
}

data class SubscriptionPreset(
    val name: String,
    val defaultAmount: Double,
    val cadence: BillingCadence,
    val category: SubscriptionCategory,
    val brandColorHex: String,
    val iconKey: String
)

object SubscriptionCatalog {
    val presets: List<SubscriptionPreset> = listOf(
        SubscriptionPreset("Netflix", 22.99, BillingCadence.MONTHLY, SubscriptionCategory.ENTERTAINMENT, "#E50914", "movie"),
        SubscriptionPreset("Spotify", 11.99, BillingCadence.MONTHLY, SubscriptionCategory.ENTERTAINMENT, "#1DB954", "music"),
        SubscriptionPreset("iCloud+", 2.99, BillingCadence.MONTHLY, SubscriptionCategory.CLOUD_AI, "#3B82F6", "cloud"),
        SubscriptionPreset("ChatGPT Plus", 20.00, BillingCadence.MONTHLY, SubscriptionCategory.CLOUD_AI, "#10A37F", "spark"),
        SubscriptionPreset("YouTube Premium", 13.99, BillingCadence.MONTHLY, SubscriptionCategory.ENTERTAINMENT, "#FF0000", "play"),
        SubscriptionPreset("Figma Pro", 15.00, BillingCadence.MONTHLY, SubscriptionCategory.PRODUCTIVITY, "#A259FF", "design"),
        SubscriptionPreset("Adobe CC", 59.99, BillingCadence.MONTHLY, SubscriptionCategory.PRODUCTIVITY, "#ED2224", "palette"),
        SubscriptionPreset("Equinox Digital", 39.99, BillingCadence.MONTHLY, SubscriptionCategory.FITNESS, "#0F172A", "fitness"),
        SubscriptionPreset("Amazon Prime", 139.00, BillingCadence.ANNUAL, SubscriptionCategory.UTILITIES, "#00A8E1", "bag")
    )

    val brandSwatches: List<Pair<String, String>> = listOf(
        "#2563EB" to "Royal Blue",
        "#10B981" to "Emerald",
        "#E50914" to "Crimson",
        "#1DB954" to "Spotify Green",
        "#A259FF" to "Violet",
        "#F59E0B" to "Amber",
        "#0F172A" to "Slate",
        "#EC4899" to "Rose"
    )

    val iconKeys: List<Pair<String, String>> = listOf(
        "movie" to "Stream",
        "music" to "Music",
        "cloud" to "Cloud",
        "spark" to "AI",
        "design" to "Work",
        "fitness" to "Fitness",
        "bolt" to "Utility",
        "card" to "Card"
    )
}

fun currentEpochDay(): Long {
    val cal = Calendar.getInstance()
    cal.set(Calendar.HOUR_OF_DAY, 0)
    cal.set(Calendar.MINUTE, 0)
    cal.set(Calendar.SECOND, 0)
    cal.set(Calendar.MILLISECOND, 0)
    val localOffsetMillis = cal.timeZone.getOffset(cal.timeInMillis)
    return TimeUnit.MILLISECONDS.toDays(cal.timeInMillis + localOffsetMillis)
}

fun formatEpochDay(epochDay: Long): String {
    val cal = Calendar.getInstance()
    cal.timeInMillis = TimeUnit.DAYS.toMillis(epochDay)
    val offset = cal.timeZone.getOffset(cal.timeInMillis)
    val date = Date(TimeUnit.DAYS.toMillis(epochDay) - offset)
    return SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(date)
}

fun formatCurrency(amount: Double, symbol: String = "$"): String {
    return "$symbol${String.format(Locale.US, "%.2f", amount)}"
}
