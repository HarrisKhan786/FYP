package com.example.bodyfit.view

import java.net.URLDecoder
import java.net.URLEncoder
// This file defines all the navigation destinations in this app as a sealed class.
// each object in the sealed class represents one screen and holds that particular class navigation string.
// use of sealed class ensures that all routes are defined in one place for single source of truth property,
// route changes should be made here, and compiler flags typos at build time instead of runtime
sealed class Screen(val route: String) {
    //route for the main dashboard that is shown after login
    object Dashboard : Screen("dashboard")
    // Route for goal setting where users define their daily and weekly targets
    object Goals : Screen("goals")
    // Route for analytics screen where users can visualise their progress using MPAChart
    object Progress : Screen("progress")
    // Route for user profile, settings, and logout button
    object Profile : Screen("profile")
    // Route for Notifications  and reminders setting and editing
    object Notifications : Screen("notifications")
    // Route to login which is the entry point of the app
    object Login : Screen("login")
    // Route to register screen which is accessed through Login screen
    object Register : Screen("register")
    //Route to Workouts details screen which accepts ${category} and a parameter to guide the routing
    object Workout : Screen("workout/{category}") {
        // Build a safe navigation by use of URL encoding
        fun createRoute(category: String): String {
            val encoded = URLEncoder.encode(category, "UTF-8")
            return "workout/$encoded"
        }
        // decode the path back to the original arguments for back button.
        fun decodeCategory(raw: String): String =
            URLDecoder.decode(raw, "UTF-8")
    }
}
