package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class SubStopRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    fun observeUserProfile(userId: String): Flow<UserProfile?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val docRef = db.collection("users").document(userId)
        emitAll(
            docRef.snapshots()
                .map { snapshot ->
                    if (snapshot.exists()) {
                        snapshot.toObject(
                            UserProfile::class.java,
                            DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                        )
                    } else {
                        null
                    }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                    }
                    throw error
                }
        )
    }

    fun observeSubscriptions(userId: String): Flow<List<Subscription>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Signed-in user mismatch" }
        val colRef = db.collection("users").document(userId).collection("subscriptions")
        emitAll(
            colRef.snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        Subscription::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedBy { it.nextBillingEpochDay }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, colRef.path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserProfile(
        userId: String,
        displayName: String,
        email: String?
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val docRef = db.collection("users").document(userId)
            val existing = docRef.get().await()
            if (!existing.exists()) {
                val safeName = displayName.trim().ifBlank { "SubStop Member" }.take(80)
                val payload = buildMap<String, Any> {
                    put("userId", uid)
                    put("displayName", safeName)
                    if (!email.isNullOrBlank()) {
                        put("email", email.trim().take(120))
                    }
                    put("monthlyBudget", 200.0)
                    put("renewalAlertsEnabled", true)
                    put("trialWarningsEnabled", true)
                    put("weeklyDigestEnabled", false)
                    put("currencySymbol", "$")
                    put("createdAt", FieldValue.serverTimestamp())
                    put("updatedAt", FieldValue.serverTimestamp())
                }
                docRef.set(payload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(
        userId: String,
        monthlyBudget: Double,
        renewalAlertsEnabled: Boolean,
        trialWarningsEnabled: Boolean,
        weeklyDigestEnabled: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            require(monthlyBudget > 0.0 && monthlyBudget <= 100000.0) {
                "Monthly budget must be between $1 and $100,000"
            }
            val docRef = db.collection("users").document(userId)
            docRef.update(
                mapOf(
                    "monthlyBudget" to monthlyBudget,
                    "renewalAlertsEnabled" to renewalAlertsEnabled,
                    "trialWarningsEnabled" to trialWarningsEnabled,
                    "weeklyDigestEnabled" to weeklyDigestEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addSubscription(
        userId: String,
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
    ): Result<Unit> {
        var docPath = "users/$userId/subscriptions"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val trimmedName = name.trim()
            require(trimmedName.isNotEmpty() && trimmedName.length <= 60) {
                "Service name must be 1 to 60 characters"
            }
            require(amount >= 0.0 && amount <= 50000.0) {
                "Amount must be between 0 and 50,000"
            }
            require(nextBillingEpochDay in 0L..100000L) {
                "Billing date is out of supported range"
            }

            val docRef = db.collection("users").document(userId).collection("subscriptions").document()
            docPath = docRef.path
            val normalizedMonthly = cadence.toMonthlyAmount(amount)
            val formattedDate = formatEpochDay(nextBillingEpochDay)
            val payload = buildMap<String, Any> {
                put("userId", uid)
                put("name", trimmedName)
                put("amount", amount)
                put("monthlyCost", normalizedMonthly)
                put("cadence", cadence.name)
                put("category", category.label)
                put("status", status.name)
                put("nextBillingDate", formattedDate)
                put("nextBillingEpochDay", nextBillingEpochDay)
                put("brandColorHex", brandColorHex.trim().take(16))
                put("iconKey", iconKey.trim().take(32))
                val cleanPayment = paymentLabel.trim().take(40)
                if (cleanPayment.isNotEmpty()) {
                    put("paymentLabel", cleanPayment)
                }
                val cleanNotes = notes.trim().take(240)
                if (cleanNotes.isNotEmpty()) {
                    put("notes", cleanNotes)
                }
                put("reminderEnabled", reminderEnabled)
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            docRef.set(payload).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateSubscription(
        userId: String,
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
        reminderEnabled: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/subscriptions/$subscriptionId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            require(subscriptionId.isNotBlank()) { "Subscription ID must not be blank" }
            val trimmedName = name.trim()
            require(trimmedName.isNotEmpty() && trimmedName.length <= 60) {
                "Service name must be 1 to 60 characters"
            }
            require(amount >= 0.0 && amount <= 50000.0) {
                "Amount must be between 0 and 50,000"
            }
            require(nextBillingEpochDay in 0L..100000L) {
                "Billing date is out of supported range"
            }

            val cleanPayment = paymentLabel.trim().take(40)
            val cleanNotes = notes.trim().take(240)
            val normalizedMonthly = cadence.toMonthlyAmount(amount)
            val formattedDate = formatEpochDay(nextBillingEpochDay)
            val docRef = db.collection("users").document(userId)
                .collection("subscriptions").document(subscriptionId)

            docRef.update(
                mapOf(
                    "name" to trimmedName,
                    "amount" to amount,
                    "monthlyCost" to normalizedMonthly,
                    "cadence" to cadence.name,
                    "category" to category.label,
                    "status" to status.name,
                    "nextBillingDate" to formattedDate,
                    "nextBillingEpochDay" to nextBillingEpochDay,
                    "brandColorHex" to brandColorHex.trim().take(16),
                    "iconKey" to iconKey.trim().take(32),
                    "paymentLabel" to cleanPayment,
                    "notes" to cleanNotes,
                    "reminderEnabled" to reminderEnabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateSubscriptionStatus(
        userId: String,
        subscriptionId: String,
        newStatus: SubscriptionStatus
    ): Result<Unit> {
        val docPath = "users/$userId/subscriptions/$subscriptionId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            require(subscriptionId.isNotBlank()) { "Subscription ID must not be blank" }
            val docRef = db.collection("users").document(userId)
                .collection("subscriptions").document(subscriptionId)
            docRef.update(
                mapOf(
                    "status" to newStatus.name,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun toggleSubscriptionReminder(
        userId: String,
        subscriptionId: String,
        enabled: Boolean
    ): Result<Unit> {
        val docPath = "users/$userId/subscriptions/$subscriptionId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            require(subscriptionId.isNotBlank()) { "Subscription ID must not be blank" }
            val docRef = db.collection("users").document(userId)
                .collection("subscriptions").document(subscriptionId)
            docRef.update(
                mapOf(
                    "reminderEnabled" to enabled,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteSubscription(
        userId: String,
        subscriptionId: String
    ): Result<Unit> {
        val docPath = "users/$userId/subscriptions/$subscriptionId"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            require(subscriptionId.isNotBlank()) { "Subscription ID must not be blank" }
            val docRef = db.collection("users").document(userId)
                .collection("subscriptions").document(subscriptionId)
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }

    suspend fun seedStarterSubscriptions(userId: String): Result<Unit> {
        val basePath = "users/$userId/subscriptions"
        return try {
            val uid = requireUserId()
            check(uid == userId) { "Signed-in user changed; refusing cross-user write." }
            val today = currentEpochDay()
            val batch = db.batch()
            val colRef = db.collection("users").document(userId).collection("subscriptions")

            val starterPayloads = listOf(
                mapOf(
                    "userId" to uid,
                    "name" to "Netflix",
                    "amount" to 22.99,
                    "monthlyCost" to 22.99,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.ENTERTAINMENT.label,
                    "status" to SubscriptionStatus.ACTIVE.name,
                    "nextBillingDate" to formatEpochDay(today + 2L),
                    "nextBillingEpochDay" to (today + 2L),
                    "brandColorHex" to "#E50914",
                    "iconKey" to "movie",
                    "paymentLabel" to "Apple Card •• 8821",
                    "notes" to "4K Ultra HD family plan",
                    "reminderEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "name" to "ChatGPT Plus",
                    "amount" to 20.00,
                    "monthlyCost" to 20.00,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.CLOUD_AI.label,
                    "status" to SubscriptionStatus.ACTIVE.name,
                    "nextBillingDate" to formatEpochDay(today + 9L),
                    "nextBillingEpochDay" to (today + 9L),
                    "brandColorHex" to "#10A37F",
                    "iconKey" to "spark",
                    "paymentLabel" to "Visa •• 4242",
                    "reminderEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "name" to "Spotify Duo",
                    "amount" to 16.99,
                    "monthlyCost" to 16.99,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.ENTERTAINMENT.label,
                    "status" to SubscriptionStatus.ACTIVE.name,
                    "nextBillingDate" to formatEpochDay(today + 14L),
                    "nextBillingEpochDay" to (today + 14L),
                    "brandColorHex" to "#1DB954",
                    "iconKey" to "music",
                    "paymentLabel" to "Apple Card •• 8821",
                    "reminderEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "name" to "Figma Pro",
                    "amount" to 15.00,
                    "monthlyCost" to 15.00,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.PRODUCTIVITY.label,
                    "status" to SubscriptionStatus.TRIAL.name,
                    "nextBillingDate" to formatEpochDay(today + 3L),
                    "nextBillingEpochDay" to (today + 3L),
                    "brandColorHex" to "#A259FF",
                    "iconKey" to "design",
                    "paymentLabel" to "Visa •• 4242",
                    "notes" to "Cancel before trial converts if unused",
                    "reminderEnabled" to true,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "name" to "iCloud+ 2TB",
                    "amount" to 9.99,
                    "monthlyCost" to 9.99,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.CLOUD_AI.label,
                    "status" to SubscriptionStatus.ACTIVE.name,
                    "nextBillingDate" to formatEpochDay(today + 19L),
                    "nextBillingEpochDay" to (today + 19L),
                    "brandColorHex" to "#3B82F6",
                    "iconKey" to "cloud",
                    "paymentLabel" to "Apple Pay",
                    "reminderEnabled" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                ),
                mapOf(
                    "userId" to uid,
                    "name" to "Adobe Creative Cloud",
                    "amount" to 59.99,
                    "monthlyCost" to 59.99,
                    "cadence" to BillingCadence.MONTHLY.name,
                    "category" to SubscriptionCategory.PRODUCTIVITY.label,
                    "status" to SubscriptionStatus.CANCELLED.name,
                    "nextBillingDate" to formatEpochDay(today + 25L),
                    "nextBillingEpochDay" to (today + 25L),
                    "brandColorHex" to "#ED2224",
                    "iconKey" to "palette",
                    "notes" to "Stopped to save $719.88/yr",
                    "reminderEnabled" to false,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            )

            starterPayloads.forEach { payload ->
                batch.set(colRef.document(), payload)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, basePath)
            Result.failure(e)
        }
    }
}
