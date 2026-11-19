package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.AutoGraph
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.auth.authStateFlow
import com.example.data.SubStopRepository
import com.example.data.Subscription
import com.example.ui.SubStopDashboardData
import com.example.ui.SubStopViewModel
import com.example.ui.UiState
import com.example.ui.components.FloatingBottomNavBar
import com.example.ui.components.SubStopNavItem
import com.example.ui.components.SubStopPrimaryButton
import com.example.ui.screens.AddEditSubscriptionSheet
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.EditBudgetDialog
import com.example.ui.screens.OverviewScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SubscriptionDetailSheet
import com.example.ui.screens.SubscriptionsScreen
import com.example.ui.theme.Slate500
import com.example.ui.theme.Slate900
import com.example.ui.theme.SubStopBackground
import com.example.ui.theme.SubStopPrimary
import com.example.ui.theme.SubStopPrimaryTint
import com.example.ui.theme.SubStopSurface
import com.example.ui.theme.SubStopTheme
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

const val ROUTE_OVERVIEW = "overview"
const val ROUTE_SUBSCRIPTIONS = "subscriptions"
const val ROUTE_ANALYTICS = "analytics"
const val ROUTE_SETTINGS = "settings"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            SubStopTheme {
                AppNavigation()
            }
        }
    }
}

@Composable
fun AppNavigation(auth: FirebaseAuth = Firebase.auth) {
    val currentUser by auth.authStateFlow().collectAsStateWithLifecycle(initialValue = auth.currentUser)
    val user = currentUser
    if (user == null) {
        AuthScreen(onAuthSuccess = {})
    } else {
        key(user.uid) {
            SubStopAuthenticatedRoot(
                currentUserId = user.uid,
                displayName = user.displayName ?: "SubStop Member",
                email = user.email
            )
        }
    }
}

@Composable
fun SubStopAuthenticatedRoot(
    currentUserId: String,
    displayName: String,
    email: String?
) {
    val viewModel: SubStopViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                SubStopViewModel(
                    repository = SubStopRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = displayName,
                    initialEmail = email
                )
            }
        }
    )

    val uiState by viewModel.dashboardState.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val statusFilter by viewModel.statusFilter.collectAsStateWithLifecycle()
    val actionMessage by viewModel.actionMessage.collectAsStateWithLifecycle()
    val isSaving by viewModel.isSaving.collectAsStateWithLifecycle()

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: ROUTE_OVERVIEW
    val snackbarHostState = remember { SnackbarHostState() }

    var detailSubscriptionId by remember { mutableStateOf<String?>(null) }
    var showAddEditSheet by remember { mutableStateOf(false) }
    var editingSubscription by remember { mutableStateOf<Subscription?>(null) }
    var showBudgetDialog by remember { mutableStateOf(false) }

    LaunchedEffect(actionMessage) {
        val msg = actionMessage
        if (msg != null) {
            snackbarHostState.showSnackbar(msg)
            viewModel.clearActionMessage()
        }
    }

    val navigateToTab: (String) -> Unit = { route ->
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    val upcomingCount = (uiState as? UiState.Success<SubStopDashboardData>)?.data?.upcomingRenewals?.size ?: 0
    val navItems = remember(upcomingCount) {
        listOf(
            SubStopNavItem(ROUTE_OVERVIEW, "Home", Icons.Rounded.Home),
            SubStopNavItem(ROUTE_SUBSCRIPTIONS, "Plans", Icons.Rounded.CreditCard),
            SubStopNavItem(ROUTE_ANALYTICS, "Analytics", Icons.Rounded.AutoGraph),
            SubStopNavItem(ROUTE_SETTINGS, "Alerts", Icons.Rounded.AccountCircle, badgeCount = upcomingCount)
        )
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(SubStopBackground)
    ) {
        val isExpandedScreen = maxWidth >= 840.dp

        Scaffold(
            containerColor = SubStopBackground,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackbarHostState,
                    modifier = Modifier.padding(bottom = if (isExpandedScreen) 16.dp else 88.dp)
                ) { data ->
                    Snackbar(
                        snackbarData = data,
                        shape = RoundedCornerShape(16.dp),
                        containerColor = Slate900,
                        contentColor = Color.White
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = innerPadding.calculateTopPadding())
            ) {
                if (isExpandedScreen) {
                    NavigationRail(
                        containerColor = SubStopSurface,
                        modifier = Modifier.fillMaxHeight()
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))
                        navItems.forEach { item ->
                            val selected = item.route == currentRoute
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(item.route) },
                                icon = { Icon(item.icon, contentDescription = item.label) },
                                label = { Text(item.label) },
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = SubStopPrimary,
                                    selectedTextColor = SubStopPrimary,
                                    indicatorColor = SubStopPrimaryTint,
                                    unselectedIconColor = Slate500,
                                    unselectedTextColor = Slate500
                                )
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    when (val state = uiState) {
                        UiState.Loading -> {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = SubStopPrimary)
                            }
                        }

                        is UiState.Error -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.weight(1f))
                                Text(
                                    text = "Couldn't sync subscriptions",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = Slate900
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = state.message,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate500,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(modifier = Modifier.height(18.dp))
                                SubStopPrimaryButton(
                                    text = "Retry",
                                    onClick = { navigateToTab(ROUTE_OVERVIEW) }
                                )
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }

                        is UiState.Success -> {
                            val dashboardData = state.data
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .widthIn(max = 680.dp)
                            ) {
                                NavHost(
                                    navController = navController,
                                    startDestination = ROUTE_OVERVIEW,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    composable(ROUTE_OVERVIEW) {
                                        OverviewScreen(
                                            data = dashboardData,
                                            selectedCategory = selectedCategory,
                                            onSelectCategory = viewModel::selectCategory,
                                            onSubscriptionClick = { detailSubscriptionId = it.id },
                                            onAddClick = {
                                                editingSubscription = null
                                                showAddEditSheet = true
                                            },
                                            onSeedSamplePack = viewModel::loadStarterPack,
                                            onNavigateToAnalytics = { navigateToTab(ROUTE_ANALYTICS) },
                                            onNavigateToSettings = { navigateToTab(ROUTE_SETTINGS) }
                                        )
                                    }
                                    composable(ROUTE_SUBSCRIPTIONS) {
                                        SubscriptionsScreen(
                                            data = dashboardData,
                                            searchQuery = searchQuery,
                                            statusFilter = statusFilter,
                                            onSearchQueryChange = viewModel::updateSearchQuery,
                                            onSelectStatusFilter = viewModel::selectStatusFilter,
                                            onSubscriptionClick = { detailSubscriptionId = it.id },
                                            onAddClick = {
                                                editingSubscription = null
                                                showAddEditSheet = true
                                            }
                                        )
                                    }
                                    composable(ROUTE_ANALYTICS) {
                                        AnalyticsScreen(
                                            data = dashboardData,
                                            onEditBudgetClick = { showBudgetDialog = true },
                                            onSubscriptionClick = { detailSubscriptionId = it.id }
                                        )
                                    }
                                    composable(ROUTE_SETTINGS) {
                                        SettingsScreen(
                                            data = dashboardData,
                                            onUpdateToggles = { alerts, trials, digest ->
                                                viewModel.updateSettings(
                                                    monthlyBudget = dashboardData.profile.monthlyBudget,
                                                    renewalAlertsEnabled = alerts,
                                                    trialWarningsEnabled = trials,
                                                    weeklyDigestEnabled = digest
                                                )
                                            },
                                            onEditBudgetClick = { showBudgetDialog = true },
                                            onLoadSamplePack = viewModel::loadStarterPack
                                        )
                                    }
                                }
                            }

                            val detailSub = detailSubscriptionId?.let { id ->
                                dashboardData.subscriptions.firstOrNull { it.id == id }
                            }
                            if (detailSub != null) {
                                SubscriptionDetailSheet(
                                    subscription = detailSub,
                                    currencySymbol = dashboardData.profile.currencySymbol,
                                    isSaving = isSaving,
                                    onDismiss = { detailSubscriptionId = null },
                                    onEdit = {
                                        editingSubscription = detailSub
                                        detailSubscriptionId = null
                                        showAddEditSheet = true
                                    },
                                    onToggleReminder = { enabled ->
                                        viewModel.toggleReminder(detailSub, enabled)
                                    },
                                    onChangeStatus = { newStatus ->
                                        viewModel.updateStatus(detailSub, newStatus)
                                        detailSubscriptionId = null
                                    },
                                    onDelete = {
                                        viewModel.deleteSubscription(detailSub) {
                                            detailSubscriptionId = null
                                        }
                                    }
                                )
                            }

                            if (showAddEditSheet) {
                                AddEditSubscriptionSheet(
                                    existing = editingSubscription,
                                    currencySymbol = dashboardData.profile.currencySymbol,
                                    isSaving = isSaving,
                                    onDismiss = {
                                        showAddEditSheet = false
                                        editingSubscription = null
                                    },
                                    onSave = { name, amount, cadence, category, status, epochDay, colorHex, iconKey, payment, notes, reminder ->
                                        val existing = editingSubscription
                                        if (existing == null) {
                                            viewModel.addSubscription(
                                                name = name,
                                                amount = amount,
                                                cadence = cadence,
                                                category = category,
                                                status = status,
                                                nextBillingEpochDay = epochDay,
                                                brandColorHex = colorHex,
                                                iconKey = iconKey,
                                                paymentLabel = payment,
                                                notes = notes,
                                                reminderEnabled = reminder,
                                                onComplete = {
                                                    showAddEditSheet = false
                                                }
                                            )
                                        } else {
                                            viewModel.updateSubscription(
                                                subscriptionId = existing.id,
                                                name = name,
                                                amount = amount,
                                                cadence = cadence,
                                                category = category,
                                                status = status,
                                                nextBillingEpochDay = epochDay,
                                                brandColorHex = colorHex,
                                                iconKey = iconKey,
                                                paymentLabel = payment,
                                                notes = notes,
                                                reminderEnabled = reminder,
                                                onComplete = {
                                                    showAddEditSheet = false
                                                    editingSubscription = null
                                                }
                                            )
                                        }
                                    }
                                )
                            }

                            if (showBudgetDialog) {
                                EditBudgetDialog(
                                    currentBudget = dashboardData.profile.monthlyBudget,
                                    currencySymbol = dashboardData.profile.currencySymbol,
                                    onDismiss = { showBudgetDialog = false },
                                    onConfirm = { newBudget ->
                                        viewModel.updateSettings(
                                            monthlyBudget = newBudget,
                                            renewalAlertsEnabled = dashboardData.profile.renewalAlertsEnabled,
                                            trialWarningsEnabled = dashboardData.profile.trialWarningsEnabled,
                                            weeklyDigestEnabled = dashboardData.profile.weeklyDigestEnabled
                                        )
                                        showBudgetDialog = false
                                    }
                                )
                            }
                        }
                    }

                    if (!isExpandedScreen) {
                        FloatingBottomNavBar(
                            items = navItems,
                            currentRoute = currentRoute,
                            onSelect = navigateToTab,
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .widthIn(max = 520.dp)
                        )
                    }
                }
            }
        }
    }
}
