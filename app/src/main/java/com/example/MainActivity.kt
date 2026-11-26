package com.example

import android.app.Activity
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.Choreographer
import android.view.Display
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.MenuBook
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.CenterFocusStrong
import androidx.compose.material.icons.outlined.Devices
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.rounded.AdminPanelSettings
import androidx.compose.material.icons.rounded.CenterFocusStrong
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.credentials.CredentialManager
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.audio.ThunderSoundPlayer
import com.example.data.SessionRepository
import com.example.model.BoostState
import com.example.model.FirestoreUiState
import com.example.model.LoginPortalMode
import com.example.ui.components.FloatingFpsOverlayPill
import com.example.ui.components.StatusPill
import com.example.ui.screens.AccessBlockedScreen
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AuthLoadingScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.BoostHubScreen
import com.example.ui.screens.DevicesSensitivityScreen
import com.example.ui.screens.FpsGuideScreen
import com.example.ui.screens.TouchCalibratorScreen
import com.example.ui.screens.attemptAutoSignIn
import com.example.ui.screens.signOutUser
import com.example.ui.theme.ApexOrange
import com.example.ui.theme.CriticalRed
import com.example.ui.theme.CyanTelemetry
import com.example.ui.theme.ElevatedSurface
import com.example.ui.theme.FFApexBoostTheme
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.OptimalGreen
import com.example.ui.theme.TacticalCard
import com.example.ui.theme.TacticalCardBright
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.ApexBoostViewModel
import com.example.ui.viewmodel.SessionViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

const val ROUTE_BOOST_HUB = "boost_hub"
const val ROUTE_DEVICES = "devices_sensitivity"
const val ROUTE_CALIBRATOR = "touch_calibrator"
const val ROUTE_FPS_GUIDE = "fps_guide"
const val ROUTE_ADMIN = "admin_console"

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String,
)

private val customerDestinations = listOf(
    NavDestinationItem(
        route = ROUTE_BOOST_HUB,
        labelRes = R.string.tab_boost,
        selectedIcon = Icons.Rounded.Speed,
        unselectedIcon = Icons.Outlined.Speed,
        testTag = "nav_boost",
    ),
    NavDestinationItem(
        route = ROUTE_DEVICES,
        labelRes = R.string.tab_devices,
        selectedIcon = Icons.Rounded.Devices,
        unselectedIcon = Icons.Outlined.Devices,
        testTag = "nav_devices",
    ),
    NavDestinationItem(
        route = ROUTE_CALIBRATOR,
        labelRes = R.string.tab_calibrator,
        selectedIcon = Icons.Rounded.CenterFocusStrong,
        unselectedIcon = Icons.Outlined.CenterFocusStrong,
        testTag = "nav_calibrator",
    ),
    NavDestinationItem(
        route = ROUTE_FPS_GUIDE,
        labelRes = R.string.tab_guide,
        selectedIcon = Icons.AutoMirrored.Rounded.MenuBook,
        unselectedIcon = Icons.AutoMirrored.Outlined.MenuBook,
        testTag = "nav_guide",
    ),
)

private val adminDestination = NavDestinationItem(
    route = ROUTE_ADMIN,
    labelRes = R.string.tab_admin,
    selectedIcon = Icons.Rounded.AdminPanelSettings,
    unselectedIcon = Icons.Outlined.AdminPanelSettings,
    testTag = "nav_admin",
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        setContent {
            FFApexBoostTheme {
                AppContent()
            }
        }
    }
}

@Composable
fun AppContent() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val credentialManager = remember(context) { CredentialManager.create(context) }

    var currentUser by remember { mutableStateOf<FirebaseUser?>(Firebase.auth.currentUser) }
    var selectedPortalMode by remember { mutableStateOf(LoginPortalMode.CUSTOMER) }
    var authGateError by remember { mutableStateOf<String?>(null) }

    DisposableEffect(Unit) {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            currentUser = auth.currentUser
        }
        Firebase.auth.addAuthStateListener(listener)
        onDispose { Firebase.auth.removeAuthStateListener(listener) }
    }

    LaunchedEffect(Unit) {
        if (currentUser == null) {
            attemptAutoSignIn(
                context = context,
                credentialManager = credentialManager,
                onAuthSuccess = { authGateError = null },
                onUnauthenticated = {},
                scope = scope,
            )
        }
    }

    val user = currentUser
    if (user == null) {
        AuthScreen(
            selectedPortal = selectedPortalMode,
            externalError = authGateError,
            onSelectPortal = { selectedPortalMode = it },
            onClearError = { authGateError = null },
            onAuthSuccess = { authGateError = null },
        )
        return
    }

    val uid = user.uid
    key(uid) {
        val sessionViewModel: SessionViewModel = viewModel(
            key = uid,
            factory = viewModelFactory {
                initializer {
                    val app = checkNotNull(this[APPLICATION_KEY])
                    val repo = SessionRepository(app)
                    SessionViewModel(
                        repo = repo,
                        currentUserId = uid,
                        initialIsBootstrapAdmin = repo.isBootstrapAdmin(user),
                    )
                }
            },
        )

        val ownSessionState by sessionViewModel.ownSessionState.collectAsStateWithLifecycle()
        val appAccessConfig by sessionViewModel.appAccessConfig.collectAsStateWithLifecycle()
        val isAdmin by sessionViewModel.isAdmin.collectAsStateWithLifecycle()
        val portalSyncCompleted by sessionViewModel.portalSyncCompleted.collectAsStateWithLifecycle()
        var syncError by remember(uid) { mutableStateOf<String?>(null) }

        LaunchedEffect(uid) {
            val initialScreen = if (selectedPortalMode == LoginPortalMode.ADMIN) {
                "Admin Console"
            } else {
                "Boost Hub"
            }
            val result = sessionViewModel.syncOnSignIn(
                portalMode = selectedPortalMode,
                currentScreen = initialScreen,
                activePresetName = "Auto Preset",
            )
            result.onSuccess { portalAllowed ->
                if (!portalAllowed) {
                    authGateError = "Admin access denied for ${user.email ?: "this account"}. " +
                        "Please select Customer Login or sign in with an authorized Admin Google account."
                    signOutUser(credentialManager, scope)
                }
            }.onFailure { err ->
                syncError = err.localizedMessage ?: "Unable to verify account session."
            }
        }

        val performSignOut: () -> Unit = {
            signOutUser(
                credentialManager = credentialManager,
                scope = scope,
                beforeFirebaseSignOut = { sessionViewModel.markSignedOut() },
            )
        }

        if (syncError != null) {
            AccessBlockedScreen(
                userEmail = user.email.orEmpty(),
                userName = user.displayName.orEmpty(),
                reason = syncError.orEmpty(),
                isGlobalPause = false,
                onSignOutClick = performSignOut,
            )
        } else if (!portalSyncCompleted) {
            AuthLoadingScreen(onSignOutClick = performSignOut)
        } else {
            val ownSession = (ownSessionState as? FirestoreUiState.Success)?.data
            val isIndividuallyBlocked = !isAdmin && (ownSession?.accessEnabled == false)
            val isGloballyPaused = !isAdmin && !appAccessConfig.customersAllowed

            if (isIndividuallyBlocked || isGloballyPaused) {
                LaunchedEffect(uid, isIndividuallyBlocked, isGloballyPaused) {
                    sessionViewModel.updateLiveActivity(
                        currentScreen = "Access Restricted",
                        activePresetName = "",
                        sessionActive = false,
                    )
                }
                val reason = if (isIndividuallyBlocked) {
                    ownSession.blockedReason
                } else {
                    appAccessConfig.maintenanceMessage
                }
                AccessBlockedScreen(
                    userEmail = user.email.orEmpty(),
                    userName = user.displayName.orEmpty(),
                    reason = reason,
                    isGlobalPause = isGloballyPaused && !isIndividuallyBlocked,
                    onSignOutClick = performSignOut,
                )
            } else {
                ApexBoostRoot(
                    currentUserId = uid,
                    currentUserName = user.displayName?.takeIf { it.isNotBlank() }
                        ?: user.email?.substringBefore("@")
                        ?: "Player",
                    currentUserEmail = user.email.orEmpty(),
                    isAdmin = isAdmin,
                    startInAdminTab = selectedPortalMode == LoginPortalMode.ADMIN && isAdmin,
                    sessionViewModel = sessionViewModel,
                    onSignOut = performSignOut,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ApexBoostRoot(
    currentUserId: String,
    currentUserName: String,
    currentUserEmail: String,
    isAdmin: Boolean,
    startInAdminTab: Boolean,
    sessionViewModel: SessionViewModel,
    onSignOut: () -> Unit,
    viewModel: ApexBoostViewModel = viewModel(),
) {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination
    val currentRoute = currentDestination?.route ?: ROUTE_BOOST_HUB
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showAccountDialog by remember { mutableStateOf(false) }

    val startDestination = remember(startInAdminTab) {
        if (startInAdminTab) ROUTE_ADMIN else ROUTE_BOOST_HUB
    }
    val topLevelDestinations = remember(isAdmin) {
        if (isAdmin) customerDestinations + adminDestination else customerDestinations
    }

    val telemetry by viewModel.telemetry.collectAsStateWithLifecycle()
    val detectedProfile by viewModel.detectedDeviceProfile.collectAsStateWithLifecycle()
    val boostState by viewModel.boostState.collectAsStateWithLifecycle()
    val liveFps by viewModel.liveFps.collectAsStateWithLifecycle()
    val latestFrameTimeMs by viewModel.latestFrameTimeMs.collectAsStateWithLifecycle()
    val frameHistory by viewModel.frameTimeHistoryMs.collectAsStateWithLifecycle()

    val devices by viewModel.filteredDevices.collectAsStateWithLifecycle()
    val allDevicesCatalog by viewModel.allDevicesCatalog.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val selectedBrand by viewModel.selectedBrand.collectAsStateWithLifecycle()
    val selectedPlayStyle by viewModel.selectedPlayStyle.collectAsStateWithLifecycle()
    val favoriteIds by viewModel.favoriteDeviceIds.collectAsStateWithLifecycle()

    val targetDistance by viewModel.targetDistance.collectAsStateWithLifecycle()
    val dragAttempts by viewModel.dragAttempts.collectAsStateWithLifecycle()
    val calibrationSummary by viewModel.calibrationSummary.collectAsStateWithLifecycle()
    val savedCalibratedProfile by viewModel.savedCalibratedProfile.collectAsStateWithLifecycle()

    val dpiCalculation by viewModel.dpiCalculation.collectAsStateWithLifecycle()
    val completedChecklistIds by viewModel.completedChecklistIds.collectAsStateWithLifecycle()

    val forceMaxRefreshRate by viewModel.forceMaxRefreshRate.collectAsStateWithLifecycle()
    val adaptiveBatteryPause by viewModel.adaptiveBatteryPause.collectAsStateWithLifecycle()
    val floatingFpsOverlayEnabled by viewModel.floatingFpsOverlayEnabled.collectAsStateWithLifecycle()
    val thunderSoundEnabled by viewModel.thunderSoundEnabled.collectAsStateWithLifecycle()
    val smoothSuggestion by viewModel.smoothSuggestion.collectAsStateWithLifecycle()
    val activeAppliedProfile by viewModel.activeAppliedProfile.collectAsStateWithLifecycle()

    val allSessionsState by sessionViewModel.allSessionsState.collectAsStateWithLifecycle()
    val appAccessConfig by sessionViewModel.appAccessConfig.collectAsStateWithLifecycle()

    val currentScreenLabel = when (currentRoute) {
        ROUTE_BOOST_HUB -> "Boost Hub"
        ROUTE_DEVICES -> "Devices & Sensi"
        ROUTE_CALIBRATOR -> "Touch Calibrator"
        ROUTE_FPS_GUIDE -> "FPS & Config Lab"
        ROUTE_ADMIN -> "Admin Console"
        else -> "Boost Hub"
    }
    val latestScreenLabel by rememberUpdatedState(currentScreenLabel)
    val latestPresetName by rememberUpdatedState(activeAppliedProfile.sourceName)
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(currentUserId, currentScreenLabel, activeAppliedProfile.sourceName) {
        sessionViewModel.updateLiveActivity(
            currentScreen = currentScreenLabel,
            activePresetName = activeAppliedProfile.sourceName,
            sessionActive = true,
        )
    }

    LaunchedEffect(currentUserId) {
        while (isActive) {
            delay(25_000L)
            if (lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                sessionViewModel.updateLiveActivity(
                    currentScreen = latestScreenLabel,
                    activePresetName = latestPresetName,
                    sessionActive = true,
                )
            }
        }
    }

    DisposableEffect(lifecycleOwner, currentUserId) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_START -> {
                    sessionViewModel.updateLiveActivity(
                        currentScreen = latestScreenLabel,
                        activePresetName = latestPresetName,
                        sessionActive = true,
                    )
                }
                Lifecycle.Event.ON_STOP -> {
                    sessionViewModel.updateLiveActivity(
                        currentScreen = latestScreenLabel,
                        activePresetName = latestPresetName,
                        sessionActive = false,
                    )
                }
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val context = LocalContext.current
    val thunderSoundPlayer = remember(context) { ThunderSoundPlayer(context) }
    DisposableEffect(thunderSoundPlayer) {
        onDispose { thunderSoundPlayer.release() }
    }
    val latestThunderEnabled by rememberUpdatedState(thunderSoundEnabled)
    var lastThunderBoostTimestampMs by remember {
        mutableLongStateOf((boostState as? BoostState.Completed)?.timestampMs ?: 0L)
    }
    LaunchedEffect(boostState) {
        val completed = boostState as? BoostState.Completed ?: return@LaunchedEffect
        if (completed.timestampMs != lastThunderBoostTimestampMs) {
            lastThunderBoostTimestampMs = completed.timestampMs
            if (latestThunderEnabled) {
                thunderSoundPlayer.playThunder()
            }
        }
    }

    LaunchedEffect(forceMaxRefreshRate) {
        val activity = context as? Activity
        if (activity != null) {
            applyWindowMaxRefreshRate(activity, forceMaxRefreshRate)
            viewModel.refreshTelemetryNow()
        }
    }

    // Live root Choreographer sampler so FPS counter & frame pacing stay live across all booster tabs
    DisposableEffect(telemetry.maxSupportedRefreshRateHz) {
        val choreographer = Choreographer.getInstance()
        var lastFrameNs = 0L
        var frameCount = 0
        var windowStartNs = 0L
        val maxHzCap = telemetry.maxSupportedRefreshRateHz.coerceAtLeast(60)

        val callback = object : Choreographer.FrameCallback {
            override fun doFrame(frameTimeNanos: Long) {
                if (lastFrameNs != 0L) {
                    val deltaMs = (frameTimeNanos - lastFrameNs) / 1_000_000f
                    frameCount++
                    if (windowStartNs == 0L) windowStartNs = frameTimeNanos
                    val elapsedNs = frameTimeNanos - windowStartNs
                    if (elapsedNs >= 450_000_000L) {
                        val measuredFps = ((frameCount * 1_000_000_000f) / elapsedNs)
                            .roundToInt()
                            .coerceIn(15, maxHzCap)
                        viewModel.recordFrameTimeSample(deltaMs.coerceIn(2f, 45f), measuredFps)
                        frameCount = 0
                        windowStartNs = frameTimeNanos
                    }
                }
                lastFrameNs = frameTimeNanos
                choreographer.postFrameCallback(this)
            }
        }
        choreographer.postFrameCallback(callback)
        onDispose { choreographer.removeFrameCallback(callback) }
    }

    val copiedMsg = stringResource(R.string.copied_preset_toast)
    val savedCalMsg = stringResource(R.string.calibrator_saved_toast)

    fun navigateToTab(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val useNavigationRail = maxWidth >= 600.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
            topBar = {
                TopAppBar(
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = ElevatedSurface,
                        titleContentColor = TextPrimary,
                    ),
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            Surface(
                                modifier = Modifier.size(34.dp),
                                shape = CircleShape,
                                color = ObsidianBg,
                                border = BorderStroke(1.5.dp, ApexOrange),
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_apex_crosshair_logo),
                                    contentDescription = "Zenitsu_2.0 Logo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize().clip(CircleShape),
                                )
                            }
                            Text(
                                text = "ZENITSU_2.0",
                                style = MaterialTheme.typography.titleLarge,
                                color = ApexOrange,
                                fontWeight = FontWeight.ExtraBold,
                            )
                        }
                    },
                    actions = {
                        if (isAdmin) {
                            StatusPill(
                                text = "ADMIN",
                                color = CyanTelemetry,
                                modifier = Modifier.padding(end = 4.dp),
                            )
                        }
                        StatusPill(
                            text = "${telemetry.currentRefreshRateHz}Hz",
                            color = OptimalGreen,
                            modifier = Modifier.padding(end = 4.dp),
                        )
                        IconButton(
                            onClick = {
                                viewModel.refreshTelemetryNow()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Live hardware telemetry refreshed")
                                }
                            },
                            modifier = Modifier.testTag("refresh_telemetry_button"),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Refresh,
                                contentDescription = "Refresh telemetry",
                                tint = CyanTelemetry,
                            )
                        }
                        IconButton(
                            onClick = { showAccountDialog = true },
                            modifier = Modifier.testTag("sign_out_button"),
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Logout,
                                contentDescription = "Account and sign out",
                                tint = ApexOrange,
                            )
                        }
                    },
                )
            },
            bottomBar = {
                if (!useNavigationRail) {
                    NavigationBar(
                        containerColor = ElevatedSurface,
                        contentColor = TextPrimary,
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationBarItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag),
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ApexOrange,
                                    selectedTextColor = ApexOrange,
                                    indicatorColor = TacticalCardBright,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                ),
                            )
                        }
                    }
                }
            },
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            ) {
                if (useNavigationRail) {
                    NavigationRail(
                        containerColor = ElevatedSurface,
                        contentColor = TextPrimary,
                    ) {
                        topLevelDestinations.forEach { dest ->
                            val selected = currentDestination?.hierarchy?.any { it.route == dest.route } == true
                            NavigationRailItem(
                                selected = selected,
                                onClick = { navigateToTab(dest.route) },
                                icon = {
                                    Icon(
                                        imageVector = if (selected) dest.selectedIcon else dest.unselectedIcon,
                                        contentDescription = stringResource(dest.labelRes),
                                    )
                                },
                                label = {
                                    Text(
                                        text = stringResource(dest.labelRes),
                                        style = MaterialTheme.typography.labelSmall,
                                    )
                                },
                                modifier = Modifier.testTag(dest.testTag),
                                colors = NavigationRailItemDefaults.colors(
                                    selectedIconColor = ApexOrange,
                                    selectedTextColor = ApexOrange,
                                    indicatorColor = TacticalCardBright,
                                    unselectedIconColor = TextMuted,
                                    unselectedTextColor = TextMuted,
                                ),
                            )
                        }
                    }
                }

                NavHost(
                    navController = navController,
                    startDestination = startDestination,
                    modifier = Modifier.weight(1f),
                ) {
                    composable(ROUTE_BOOST_HUB) {
                        BoostHubScreen(
                            telemetry = telemetry,
                            detectedProfile = detectedProfile,
                            boostState = boostState,
                            liveFps = liveFps,
                            frameTimeHistoryMs = frameHistory,
                            forceMaxRefreshRate = forceMaxRefreshRate,
                            adaptiveBatteryPause = adaptiveBatteryPause,
                            floatingFpsOverlayEnabled = floatingFpsOverlayEnabled,
                            thunderSoundEnabled = thunderSoundEnabled,
                            smoothSuggestion = smoothSuggestion,
                            allDevices = allDevicesCatalog,
                            activeProfile = activeAppliedProfile,
                            onToggleForceMaxRefreshRate = { enabled ->
                                viewModel.setForceMaxRefreshRate(enabled)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (enabled) {
                                            "Max ${telemetry.maxSupportedRefreshRateHz}Hz refresh rate mode locked"
                                        } else {
                                            "Display refresh rate restored to system auto"
                                        },
                                    )
                                }
                            },
                            onToggleAdaptiveBatteryPause = { enabled ->
                                viewModel.setAdaptiveBatteryPause(enabled)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (enabled) {
                                            "Adaptive battery & auto-pause on game start enabled"
                                        } else {
                                            "Auto-pause on game start disabled"
                                        },
                                    )
                                }
                            },
                            onToggleFloatingFpsOverlay = { enabled ->
                                viewModel.setFloatingFpsOverlay(enabled)
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (enabled) {
                                            "Floating FPS counter overlay enabled"
                                        } else {
                                            "Floating FPS counter overlay hidden"
                                        },
                                    )
                                }
                            },
                            onToggleThunderSound = { enabled ->
                                viewModel.setThunderSoundEnabled(enabled)
                                if (enabled) {
                                    thunderSoundPlayer.playThunder()
                                }
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (enabled) {
                                            "Thunder ⚡ boost activation sound enabled"
                                        } else {
                                            "Thunder boost activation sound muted"
                                        },
                                    )
                                }
                            },
                            onPreviewThunderSound = {
                                thunderSoundPlayer.playThunder()
                            },
                            onSelectSmoothPriority = { priority ->
                                viewModel.selectSmoothPriority(priority)
                            },
                            onSelectSuggestionDevice = { deviceId ->
                                viewModel.selectSuggestionDevice(deviceId)
                            },
                            onApplyScribdPreset = { sourceName, sens, fpsPreset ->
                                viewModel.applyScribdPreset(sourceName, sens, fpsPreset, isCustomized = false)
                                scope.launch {
                                    snackbarHostState.showSnackbar("Applied Scribd preset: $sourceName")
                                }
                            },
                            onUpdateActiveSensitivity = { updated ->
                                viewModel.updateActiveSensitivity(updated)
                            },
                            onUpdateActiveFpsPreset = { updated ->
                                viewModel.updateActiveFpsPreset(updated)
                            },
                            onResetActiveToAuto = {
                                viewModel.resetActiveToScribdAuto()
                                scope.launch {
                                    snackbarHostState.showSnackbar("Reset active profile to Scribd device auto")
                                }
                            },
                            onActivateAllFeatures = {
                                viewModel.activateAllFeatures()
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        "All engine features active: ${telemetry.maxSupportedRefreshRateHz}Hz lock, BG pause, FPS overlay & Scribd preset",
                                    )
                                }
                            },
                            onOptimizeClick = { viewModel.runOptimization() },
                            onLaunchGame = { pkg ->
                                viewModel.prepareAndLaunchGame(pkg) { launched ->
                                    if (!launched) {
                                        scope.launch {
                                            snackbarHostState.showSnackbar("Unable to launch package $pkg")
                                        }
                                    }
                                }
                            },
                            onOpenSettings = { dest ->
                                if (!viewModel.openSystemSettings(dest)) {
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Unable to open system settings screen")
                                    }
                                }
                            },
                            onNavigateToDevices = { navigateToTab(ROUTE_DEVICES) },
                            onPresetCopied = {
                                scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                            },
                        )
                    }

                    composable(ROUTE_DEVICES) {
                        DevicesSensitivityScreen(
                            devices = devices,
                            searchQuery = searchQuery,
                            selectedBrand = selectedBrand,
                            selectedPlayStyle = selectedPlayStyle,
                            favoriteIds = favoriteIds,
                            onSearchChange = { viewModel.updateSearchQuery(it) },
                            onSelectBrand = { viewModel.selectBrand(it) },
                            onSelectPlayStyle = { viewModel.selectPlayStyle(it) },
                            onToggleFavorite = { viewModel.toggleFavoriteDevice(it) },
                            onApplyPreset = { device, mode ->
                                val tuned = device.baseSensitivity.tunedFor(mode)
                                viewModel.applyScribdPreset(
                                    sourceName = "${device.deviceName} (${mode.shortLabel})",
                                    sensitivity = tuned,
                                    fpsPreset = device.fpsPreset,
                                    isCustomized = false,
                                )
                                scope.launch {
                                    snackbarHostState.showSnackbar("Applied ${device.deviceName} Scribd preset")
                                }
                            },
                            onPresetCopied = {
                                scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                            },
                        )
                    }

                    composable(ROUTE_CALIBRATOR) {
                        TouchCalibratorScreen(
                            targetDistance = targetDistance,
                            attempts = dragAttempts,
                            summary = calibrationSummary,
                            onSelectDistance = { viewModel.selectTargetDistance(it) },
                            onRecordAttempt = { outcome, vel, angle, distDp, hz ->
                                viewModel.recordDragAttempt(outcome, vel, angle, distDp, hz)
                            },
                            onResetSession = { viewModel.resetCalibrationSession() },
                            onSaveProfile = {
                                viewModel.saveCalibratedProfile()
                                scope.launch { snackbarHostState.showSnackbar(savedCalMsg) }
                            },
                        )
                    }

                    composable(ROUTE_FPS_GUIDE) {
                        FpsGuideScreen(
                            dpiCalculation = dpiCalculation,
                            savedProfile = savedCalibratedProfile,
                            completedChecklistIds = completedChecklistIds,
                            onUpdateDpiCalc = { targetDpi, thumbSpeed ->
                                viewModel.updateDpiCalculator(targetDpi, thumbSpeed)
                            },
                            onToggleChecklist = { viewModel.toggleChecklistItem(it) },
                            onNavigateToCalibrator = { navigateToTab(ROUTE_CALIBRATOR) },
                            onPresetCopied = {
                                scope.launch { snackbarHostState.showSnackbar(copiedMsg) }
                            },
                        )
                    }

                    if (isAdmin) {
                        composable(ROUTE_ADMIN) {
                            AdminDashboardScreen(
                                currentUserId = currentUserId,
                                sessionsState = allSessionsState,
                                appAccessConfig = appAccessConfig,
                                onToggleUserAccess = { targetSession, enabled, reason ->
                                    val targetId = targetSession.userId.ifBlank { targetSession.id }
                                    sessionViewModel.setUserAccess(targetId, enabled, reason) { res ->
                                        scope.launch {
                                            res.onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    if (enabled) {
                                                        "Restored access for ${targetSession.displayName}"
                                                    } else {
                                                        "Suspended ${targetSession.displayName}"
                                                    },
                                                )
                                            }.onFailure { err ->
                                                snackbarHostState.showSnackbar(
                                                    err.localizedMessage ?: "Failed to update user access",
                                                )
                                            }
                                        }
                                    }
                                },
                                onToggleAdminRole = { targetSession, makeAdmin ->
                                    sessionViewModel.setUserAdminRole(targetSession, makeAdmin) { res ->
                                        scope.launch {
                                            res.onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    if (makeAdmin) {
                                                        "Promoted ${targetSession.displayName} to Admin"
                                                    } else {
                                                        "Changed ${targetSession.displayName} to Customer"
                                                    },
                                                )
                                            }.onFailure { err ->
                                                snackbarHostState.showSnackbar(
                                                    err.localizedMessage ?: "Failed to update user role",
                                                )
                                            }
                                        }
                                    }
                                },
                                onSaveGlobalPolicy = { allowed, message ->
                                    sessionViewModel.setGlobalCustomersAllowed(allowed, message) { res ->
                                        scope.launch {
                                            res.onSuccess {
                                                snackbarHostState.showSnackbar(
                                                    if (allowed) {
                                                        "Customer app access enabled"
                                                    } else {
                                                        "Customer app access paused globally"
                                                    },
                                                )
                                            }.onFailure { err ->
                                                snackbarHostState.showSnackbar(
                                                    err.localizedMessage ?: "Failed to update global policy",
                                                )
                                            }
                                        }
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }

        if (floatingFpsOverlayEnabled) {
            FloatingFpsOverlayPill(
                liveFps = liveFps,
                frameTimeMs = latestFrameTimeMs,
                refreshRateHz = telemetry.currentRefreshRateHz,
                onDismiss = { viewModel.setFloatingFpsOverlay(false) },
                modifier = Modifier.windowInsetsPadding(WindowInsets.safeDrawing),
            )
        }
    }

    if (showAccountDialog) {
        AlertDialog(
            onDismissRequest = { showAccountDialog = false },
            containerColor = TacticalCard,
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary,
            title = { Text("Account Session") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = currentUserName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                    )
                    if (currentUserEmail.isNotBlank()) {
                        Text(
                            text = currentUserEmail,
                            style = MaterialTheme.typography.bodyMedium,
                            color = CyanTelemetry,
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    StatusPill(
                        text = if (isAdmin) "ROLE: ADMIN" else "ROLE: CUSTOMER",
                        color = if (isAdmin) CyanTelemetry else ApexOrange,
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showAccountDialog = false
                        onSignOut()
                    },
                    modifier = Modifier.testTag("confirm_sign_out_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CriticalRed,
                        contentColor = TextPrimary,
                    ),
                ) {
                    Text("Sign Out")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAccountDialog = false }) {
                    Text("Close", color = CyanTelemetry)
                }
            },
        )
    }
}

private fun applyWindowMaxRefreshRate(activity: Activity, forceMax: Boolean) {
    val window = activity.window ?: return
    val params = window.attributes
    if (!forceMax) {
        params.preferredDisplayModeId = 0
        params.preferredRefreshRate = 0f
        window.attributes = params
        return
    }

    @Suppress("DEPRECATION")
    val display: Display? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        runCatching { activity.display }.getOrNull() ?: activity.windowManager.defaultDisplay
    } else {
        activity.windowManager.defaultDisplay
    }
    val currentMode = display?.mode
    val bestMode = display?.supportedModes
        ?.filter { mode ->
            currentMode == null ||
                (mode.physicalWidth == currentMode.physicalWidth &&
                    mode.physicalHeight == currentMode.physicalHeight)
        }
        ?.maxByOrNull { it.refreshRate }
        ?: display?.supportedModes?.maxByOrNull { it.refreshRate }

    if (bestMode != null) {
        params.preferredDisplayModeId = bestMode.modeId
        params.preferredRefreshRate = bestMode.refreshRate
        window.attributes = params
    }
}
