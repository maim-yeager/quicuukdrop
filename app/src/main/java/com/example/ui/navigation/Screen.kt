package com.example.ui.navigation

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object SendPicker : Screen("send_picker")
    data object SelectedFiles : Screen("selected_files")
    data object NearbyDevices : Screen("nearby_devices")
    data object Transfer : Screen("transfer")
    data object TransferComplete : Screen("transfer_complete")
    data object ReceiveWaiting : Screen("receive_waiting")
    data object History : Screen("history")
    data object Settings : Screen("settings")
    data object PrivacyPolicy : Screen("privacy_policy")
    data object About : Screen("about")
}
