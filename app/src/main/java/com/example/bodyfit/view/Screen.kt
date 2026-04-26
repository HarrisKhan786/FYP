package com.example.bodyfit.view

import java.net.URLDecoder
import java.net.URLEncoder

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object Goals : Screen("goals")
    object Progress : Screen("progress")
    object Profile : Screen("profile")
    object Notifications : Screen("notifications")
    object Login : Screen("login")
    object Register : Screen("register")
    object Workout : Screen("workout/{category}") {
        fun createRoute(category: String): String {
            val encoded = URLEncoder.encode(category, "UTF-8")
            return "workout/$encoded"
        }

        fun decodeCategory(raw: String): String =
            URLDecoder.decode(raw, "UTF-8")
    }
}
