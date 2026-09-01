package com.example.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.model.TransferState
import com.example.ui.components.ConnectionRequestModal
import com.example.ui.screens.AboutScreen
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.NearbyDevicesScreen
import com.example.ui.screens.PrivacyPolicyScreen
import com.example.ui.screens.ReceiveWaitingScreen
import com.example.ui.screens.SelectedFilesScreen
import com.example.ui.screens.SendFilePickerScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.screens.TransferCompleteScreen
import com.example.ui.screens.TransferScreen
import com.example.ui.viewmodels.QuickDropViewModel

@Composable
fun QuickDropNavGraph(
    navController: NavHostController = rememberNavController(),
    viewModel: QuickDropViewModel = viewModel()
) {
    val categoryFiles by viewModel.categoryFiles.collectAsState()
    val selectedFiles by viewModel.selectedFiles.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoadingFiles by viewModel.isLoadingFiles.collectAsState()
    val discoveredDevices by viewModel.discoveredDevices.collectAsState()
    val transferState by viewModel.transferState.collectAsState()
    val allHistory by viewModel.allHistory.collectAsState()
    val sentHistory by viewModel.sentHistory.collectAsState()
    val receivedHistory by viewModel.receivedHistory.collectAsState()
    val deviceName by viewModel.deviceName.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val hapticsEnabled by viewModel.hapticsEnabled.collectAsState()
    val storageUsage by viewModel.storageUsage.collectAsState()

    // Tracks whether gallery/media read access was denied so the picker screen
    // can show a clear, actionable message instead of silently looking empty.
    var mediaPermissionDenied by remember { mutableStateOf(false) }

    val mediaPermissions = remember {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            listOf(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.READ_MEDIA_VIDEO,
                Manifest.permission.READ_MEDIA_AUDIO
            )
        } else {
            listOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Permissions requester for modern Android
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        mediaPermissionDenied = mediaPermissions.any { results[it] == false }
        if (!mediaPermissionDenied) {
            viewModel.loadFilesForCategory(selectedCategory)
        }
    }

    LaunchedEffect(Unit) {
        val permissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.NEARBY_WIFI_DEVICES)
            permissions.add(Manifest.permission.BLUETOOTH_SCAN)
            permissions.add(Manifest.permission.BLUETOOTH_ADVERTISE)
            permissions.add(Manifest.permission.BLUETOOTH_CONNECT)
            permissions.add(Manifest.permission.READ_MEDIA_IMAGES)
            permissions.add(Manifest.permission.READ_MEDIA_VIDEO)
            permissions.add(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)
            permissions.add(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        permissionLauncher.launch(permissions.toTypedArray())
    }

    // Auto-navigate to Transfer & TransferComplete based on state
    LaunchedEffect(transferState) {
        when (transferState) {
            is TransferState.InProgress -> {
                if (navController.currentDestination?.route != Screen.Transfer.route) {
                    navController.navigate(Screen.Transfer.route)
                }
            }
            is TransferState.Completed -> {
                if (navController.currentDestination?.route != Screen.TransferComplete.route) {
                    navController.navigate(Screen.TransferComplete.route)
                }
            }
            is TransferState.Cancelled -> {
                navController.popBackStack(Screen.Home.route, false)
            }
            else -> {}
        }
    }

    // Connection Request Modal (if another device requests connection)
    val requestedState = transferState as? TransferState.ConnectionRequested
    if (requestedState != null) {
        ConnectionRequestModal(
            deviceName = requestedState.deviceName,
            fileCount = requestedState.fileCount,
            totalFormattedSize = viewModel.selectedFilesFormattedSize,
            onAccept = {
                viewModel.acceptIncomingConnection(requestedState.endpointId)
            },
            onDecline = {
                viewModel.declineIncomingConnection(requestedState.endpointId)
            }
        )
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        enterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(animationSpec = tween(280)) { it / 6 }
        },
        exitTransition = {
            fadeOut(animationSpec = tween(180))
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(220)) +
                slideInHorizontally(animationSpec = tween(280)) { -it / 6 }
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(180)) +
                slideOutHorizontally(animationSpec = tween(220)) { it / 6 }
        }
    ) {
        // Splash Screen
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // Home Dashboard
        composable(Screen.Home.route) {
            HomeScreen(
                onSendClick = {
                    viewModel.loadFilesForCategory(selectedCategory)
                    navController.navigate(Screen.SendPicker.route)
                },
                onReceiveClick = {
                    viewModel.startReceiveMode()
                    navController.navigate(Screen.ReceiveWaiting.route)
                },
                onHistoryClick = {
                    navController.navigate(Screen.History.route)
                },
                onSettingsClick = {
                    navController.navigate(Screen.Settings.route)
                }
            )
        }

        // Step 1: Send File Picker
        composable(Screen.SendPicker.route) {
            SendFilePickerScreen(
                files = categoryFiles,
                selectedFiles = selectedFiles,
                selectedCategory = selectedCategory,
                isLoading = isLoadingFiles,
                mediaPermissionDenied = mediaPermissionDenied,
                onCategorySelected = { cat -> viewModel.loadFilesForCategory(cat) },
                onFileToggled = { file -> viewModel.toggleFileSelection(file) },
                onUrisPicked = { uris -> viewModel.addPickedUris(uris) },
                onRequestPermission = { permissionLauncher.launch(mediaPermissions.toTypedArray()) },
                onBackClick = { navController.popBackStack() },
                onNextClick = { navController.navigate(Screen.SelectedFiles.route) }
            )
        }

        // Step 2: Selected Files Review
        composable(Screen.SelectedFiles.route) {
            SelectedFilesScreen(
                selectedFiles = selectedFiles,
                formattedTotalSize = viewModel.selectedFilesFormattedSize,
                onRemoveFile = { file -> viewModel.removeSelectedFile(file) },
                onClearAll = { viewModel.clearSelection() },
                onBackClick = { navController.popBackStack() },
                onSendClick = {
                    viewModel.startSendDiscovery()
                    navController.navigate(Screen.NearbyDevices.route)
                }
            )
        }

        // Step 3: Nearby Devices Discovery
        composable(Screen.NearbyDevices.route) {
            NearbyDevicesScreen(
                devices = discoveredDevices,
                onDeviceSelected = { device ->
                    viewModel.connectAndSend(device)
                    navController.navigate(Screen.Transfer.route)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Transfer In Progress Screen
        composable(Screen.Transfer.route) {
            TransferScreen(
                transferState = transferState,
                onCancelClick = {
                    viewModel.cancelTransfer()
                    navController.popBackStack(Screen.Home.route, false)
                }
            )
        }

        // Transfer Complete Screen
        composable(Screen.TransferComplete.route) {
            TransferCompleteScreen(
                transferState = transferState,
                onDoneClick = {
                    viewModel.resetState()
                    viewModel.clearSelection()
                    navController.popBackStack(Screen.Home.route, false)
                },
                onViewHistoryClick = {
                    viewModel.resetState()
                    viewModel.clearSelection()
                    navController.navigate(Screen.History.route) {
                        popUpTo(Screen.Home.route)
                    }
                }
            )
        }

        // Receive Waiting Screen
        composable(Screen.ReceiveWaiting.route) {
            ReceiveWaitingScreen(
                localDeviceName = deviceName,
                onBackClick = {
                    viewModel.resetState()
                    navController.popBackStack()
                }
            )
        }

        // History Screen
        composable(Screen.History.route) {
            HistoryScreen(
                allTransfers = allHistory,
                sentTransfers = sentHistory,
                receivedTransfers = receivedHistory,
                onDeleteItem = { id -> viewModel.deleteHistoryItem(id) },
                onClearAll = { viewModel.clearAllHistory() },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Settings Screen
        composable(Screen.Settings.route) {
            SettingsScreen(
                deviceName = deviceName,
                themeMode = themeMode,
                hapticsEnabled = hapticsEnabled,
                storageUsage = storageUsage,
                onDeviceNameChanged = { viewModel.setDeviceName(it) },
                onThemeModeChanged = { viewModel.setThemeMode(it) },
                onHapticsChanged = { viewModel.setHapticsEnabled(it) },
                onPrivacyPolicyClick = { navController.navigate(Screen.PrivacyPolicy.route) },
                onAboutClick = { navController.navigate(Screen.About.route) },
                onBackClick = { navController.popBackStack() }
            )
        }

        // Privacy Policy Screen
        composable(Screen.PrivacyPolicy.route) {
            PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        // About Screen
        composable(Screen.About.route) {
            AboutScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
