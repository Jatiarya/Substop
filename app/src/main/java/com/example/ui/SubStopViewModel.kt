package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.BillingCadence
import com.example.data.SubStopRepository
import com.example.data.Subscription
import com.example.data.SubscriptionCategory
import com.example.data.SubscriptionStatus
import com.example.data.UserProfile
import com.example.data.currentEpochDay
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

data class CategorySpend(
    val category: SubscriptionCategory,
    val monthlyAmount: Double,
    val percentageOfTotal: Float,
    val count: Int
)

data class SubStopDashboardData(
    val profile: UserProfile,
    val subscriptions: List<Subscription>,
    val totalMonthlySpend: Double,
    val totalAnnualProjection: Double,
    val monthlySavingsFromStopped: Double,
    val activeCount: Int,
    val trialCount: Int,
    val pausedOrCancelledCount: Int,
    val upcomingRenewals: List<Subscription>,
    val budgetRatio: Float,
    val categoryBreakdown: List<CategorySpend>
)

class SubStopViewModel(
    private val repository: SubStopRepository,
    private val currentUserId: String,
    private val initialDisplayName: String,
    private val initialEmail: String?
) : ViewModel() {

    private val _selectedCategory = MutableStateFlow<SubscriptionCategory?>(null)
    val selectedCategory: StateFlow<SubscriptionCategory?> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _statusFilter = MutableStateFlow("ACTIVE")
    val statusFilter: StateFlow<String> = _statusFilter.asStateFlow()

    private val _actionMessage = MutableStateFlow<String?>(null)
    val actionMessage: StateFlow<String?> = _actionMessage.asStateFlow()

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    init {
        viewModelScope.launch {
            repository.ensureUserProfile(
                userId = currentUserId,
                displayName = initialDisplayName,
                email = initialEmail
            )
        }
    }

    val dashboardState: StateFlow<UiState<SubStopDashboardData>> = combine(
        repository.observeUserProfile(currentUserId),
        repository.observeSubscriptions(currentUserId)
    ) { profileOrNull, subscriptions ->
        val profile = profileOrNull ?: UserProfile(
            userId = currentUserId,
            displayName = initialDisplayName.ifBlank { "SubStop Member" },
            email = initialEmail.orEmpty(),
            monthlyBudget = 200.0
        )

        val today = currentEpochDay()
        val activeOrTrial = subscriptions.filter { it.isActiveOrTrial() }
        val activeCount = subscriptions.count { it.statusEnum() == SubscriptionStatus.ACTIVE }
        val trialCount = subscriptions.count { it.statusEnum() == SubscriptionStatus.TRIAL }
        val pausedOrCancelled = subscriptions.filter { !it.isActiveOrTrial() }

        val totalMonthly = activeOrTrial.sumOf { it.monthlyNormalizedAmount() }
        val totalAnnual = totalMonthly * 12.0
        val monthlySavings = pausedOrCancelled.sumOf { it.monthlyNormalizedAmount() }

        val upcoming = activeOrTrial
            .filter { it.daysUntilRenewal(today) in -1L..7L }
            .sortedBy { it.nextBillingEpochDay }

        val budget = profile.monthlyBudget.coerceAtLeast(1.0)
        val budgetRatio = (totalMonthly / budget).toFloat().coerceIn(0f, 1.5f)

        val categoryGroups = activeOrTrial.groupBy { it.categoryEnum() }
        val breakdown = categoryGroups.map { (cat, list) ->
            val catMonthly = list.sumOf { it.monthlyNormalizedAmount() }
            val pct = if (totalMonthly > 0.0) (catMonthly / totalMonthly).toFloat() else 0f
            CategorySpend(
                category = cat,
                monthlyAmount = catMonthly,
                percentageOfTotal = pct,
                count = list.size
            )
        }.sortedByDescending { it.monthlyAmount }

        UiState.Success(
            SubStopDashboardData(
                profile = profile,
                subscriptions = subscriptions,
                totalMonthlySpend = totalMonthly,
                totalAnnualProjection = totalAnnual,
                monthlySavingsFromStopped = monthlySavings,
                activeCount = activeCount,
                trialCount = trialCount,
                pausedOrCancelledCount = pausedOrCancelled.size,
                upcomingRenewals = upcoming,
                budgetRatio = budgetRatio,
                categoryBreakdown = breakdown
            )
        ) as UiState<SubStopDashboardData>
    }.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing SubStop data", rootError)
        emit(UiState.Error(rootError.message ?: "Failed to load subscriptions"))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = UiState.Loading
    )

    val subscriptions: StateFlow<UiState<List<Subscription>>> =
        repository.observeSubscriptions(currentUserId)
            .map<List<Subscription>, UiState<List<Subscription>>> { UiState.Success(it) }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val rootError = firestoreError ?: error
                Log.w(TAG, "Error observing subscriptions", rootError)
                emit(UiState.Error(rootError.message ?: "Failed to observe subscriptions"))
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
                initialValue = UiState.Loading
            )

    fun createSubscription(subscription: Subscription, onComplete: () -> Unit = {}) {
        addSubscription(
            name = subscription.name,
            amount = if (subscription.amount > 0.0) subscription.amount else subscription.monthlyCost,
            cadence = subscription.cadenceEnum(),
            category = subscription.categoryEnum(),
            status = subscription.statusEnum(),
            nextBillingEpochDay = subscription.nextBillingEpochDay,
            brandColorHex = subscription.brandColorHex,
            iconKey = subscription.iconKey,
            paymentLabel = subscription.paymentLabel,
            notes = subscription.notes,
            reminderEnabled = subscription.reminderEnabled,
            onComplete = onComplete
        )
    }

    fun updateSubscription(subscription: Subscription, onComplete: () -> Unit = {}) {
        updateSubscription(
            subscriptionId = subscription.id,
            name = subscription.name,
            amount = if (subscription.amount > 0.0) subscription.amount else subscription.monthlyCost,
            cadence = subscription.cadenceEnum(),
            category = subscription.categoryEnum(),
            status = subscription.statusEnum(),
            nextBillingEpochDay = subscription.nextBillingEpochDay,
            brandColorHex = subscription.brandColorHex,
            iconKey = subscription.iconKey,
            paymentLabel = subscription.paymentLabel,
            notes = subscription.notes,
            reminderEnabled = subscription.reminderEnabled,
            onComplete = onComplete
        )
    }

    fun deleteSubscription(subscriptionId: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.deleteSubscription(currentUserId, subscriptionId)
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "Subscription removed"
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not remove subscription"
            }
        }
    }

    fun selectCategory(category: SubscriptionCategory?) {
        _selectedCategory.value = category
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectStatusFilter(filter: String) {
        _statusFilter.value = filter
    }

    fun clearActionMessage() {
        _actionMessage.value = null
    }

    fun addSubscription(
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
        reminderEnabled: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.addSubscription(
                userId = currentUserId,
                name = name,
                amount = amount,
                cadence = cadence,
                category = category,
                status = status,
                nextBillingEpochDay = nextBillingEpochDay,
                brandColorHex = brandColorHex,
                iconKey = iconKey,
                paymentLabel = paymentLabel,
                notes = notes,
                reminderEnabled = reminderEnabled
            )
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "$name added to SubStop"
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not save subscription"
            }
        }
    }

    fun updateSubscription(
        subscriptionId: String,
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
        reminderEnabled: Boolean,
        onComplete: () -> Unit
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateSubscription(
                userId = currentUserId,
                subscriptionId = subscriptionId,
                name = name,
                amount = amount,
                cadence = cadence,
                category = category,
                status = status,
                nextBillingEpochDay = nextBillingEpochDay,
                brandColorHex = brandColorHex,
                iconKey = iconKey,
                paymentLabel = paymentLabel,
                notes = notes,
                reminderEnabled = reminderEnabled
            )
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "$name updated"
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update subscription"
            }
        }
    }

    fun updateStatus(subscription: Subscription, newStatus: SubscriptionStatus) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateSubscriptionStatus(
                userId = currentUserId,
                subscriptionId = subscription.id,
                newStatus = newStatus
            )
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = when (newStatus) {
                    SubscriptionStatus.CANCELLED -> "${subscription.name} stopped — savings unlocked"
                    SubscriptionStatus.PAUSED -> "${subscription.name} paused"
                    SubscriptionStatus.ACTIVE -> "${subscription.name} resumed"
                    SubscriptionStatus.TRIAL -> "${subscription.name} marked as trial"
                }
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Status update failed"
            }
        }
    }

    fun toggleReminder(subscription: Subscription, enabled: Boolean) {
        viewModelScope.launch {
            val result = repository.toggleSubscriptionReminder(
                userId = currentUserId,
                subscriptionId = subscription.id,
                enabled = enabled
            )
            result.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update reminder"
            }
        }
    }

    fun deleteSubscription(subscription: Subscription, onComplete: () -> Unit) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.deleteSubscription(currentUserId, subscription.id)
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "${subscription.name} removed"
                onComplete()
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not remove subscription"
            }
        }
    }

    fun updateSettings(
        monthlyBudget: Double,
        renewalAlertsEnabled: Boolean,
        trialWarningsEnabled: Boolean,
        weeklyDigestEnabled: Boolean
    ) {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.updateUserSettings(
                userId = currentUserId,
                monthlyBudget = monthlyBudget,
                renewalAlertsEnabled = renewalAlertsEnabled,
                trialWarningsEnabled = trialWarningsEnabled,
                weeklyDigestEnabled = weeklyDigestEnabled
            )
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "Preferences saved"
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Could not update settings"
            }
        }
    }

    fun loadStarterPack() {
        viewModelScope.launch {
            _isSaving.value = true
            val result = repository.seedStarterSubscriptions(currentUserId)
            _isSaving.value = false
            result.onSuccess {
                _actionMessage.value = "Sample subscriptions loaded"
            }.onFailure { err ->
                _actionMessage.value = err.localizedMessage ?: "Failed to load sample pack"
            }
        }
    }

    private companion object {
        const val TAG = "SubStopVM"
        const val STOP_TIMEOUT_MILLIS = 5000L
    }
}
