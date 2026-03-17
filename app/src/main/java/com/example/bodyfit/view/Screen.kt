package com.example.bodyfit.view

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Goals : Screen("goals")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Login : Screen("login")
    object Register : Screen("register")
}
