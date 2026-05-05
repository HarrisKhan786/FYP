package com.example.bodyfit.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument

// This is the central navigation host for the entire application
// all screen to screen navigation are declared here using navhost
// The start destination is login
@Composable
fun AppNavigator(paddingValues: PaddingValues) {
    // The single navController shared across the whole application
    val navController = rememberNavController()
    // observe the current back stack entry so that the app can react to route changes
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            // show the personalized top app bar on the dashboard screen only
            if (currentRoute == Screen.Dashboard.route) {
                DashboardTopBar(
                    onNotificationClick = {
                        // navigate to the full notification and reminders screen on the bell icon click
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }
        },
        bottomBar = {
            // shows the bottom navigation on the four main screens only
            // it is hidden on login, register, notifications, and workout details
            // The screen where it is hidden use their own back arrow button for back stack.
            if (currentRoute in listOf(
                    Screen.Dashboard.route,
                    Screen.Goals.route,
                    Screen.Progress.route,
                    Screen.Profile.route
                )
            ) {
                DashboardBottomBar(
                    navController = navController,
                    currentRoute = currentRoute
                )
            }
        }
    ) { padding ->

        // NavHost declares every screen in the app and maps them to their particular route string
        // the modifier ensures that no screen content is hidden behind the top or bottom bars
        NavHost(
            navController = navController,
            startDestination = Screen.Login.route, // Always start at the login screen
            modifier = Modifier.padding(padding)
        ) {
            // auth screen which is login and register
            composable(Screen.Login.route) {
                // NavController takes user to Dashboard after successful login
                LoginScreen(navController, paddingValues)
            }
            composable(Screen.Register.route) {
                // takes user to registration screen if they do not have an account
                SignUpScreen(navController, paddingValues)
            }
            // main authenticated screens
            composable(Screen.Dashboard.route) {
                // navigates to workouts screen from dashboard
                DashboardScreen(navController = navController)
            }
            composable(Screen.Goals.route) {
                GoalSettingScreen(navController)
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Notifications.route){ 
                NotificationsScreen(
                    navController = navController
                )
            }
            composable(Screen.Profile.route) {
                ProfileScreen(
                    // When user logs out the entire back stack is cleared
                    // user returns to authentication screen
                    navController = navController,
                    onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
            // Workout screen receives category as a url encoded string
            // navController is needed for back arrow at the top
            composable(
                route = Screen.Workout.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
                // the raw arguments are decoded for back to display name
            ) { backStackEntry ->
                val rawCategory = backStackEntry.arguments?.getString("category") ?: "Full body"
                val category = Screen.Workout.decodeCategory(rawCategory)

                WorkoutScreen(category = category, navController = navController)
            }
        }
    }
}
