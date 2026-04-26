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


@Composable
fun AppNavigator(paddingValues: PaddingValues) {

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    Scaffold(
        topBar = {
            if (currentRoute == Screen.Dashboard.route) {
                DashboardTopBar(
                    onNotificationClick = {
                        navController.navigate(Screen.Notifications.route)
                    }
                )
            }
        },
        bottomBar = {
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

        NavHost(
            navController = navController,
            startDestination = Screen.Login.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Login.route) {
                LoginScreen(navController, paddingValues)
            }
            composable(Screen.Register.route) {
                SignUpScreen(navController, paddingValues)
            }
            composable(Screen.Dashboard.route) {
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
                    navController = navController,
                    onLogout = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                })
            }
            composable(
                route = Screen.Workout.route,
                arguments = listOf(navArgument("category") { type = NavType.StringType })
            ) { backStackEntry ->
                val rawCategory = backStackEntry.arguments?.getString("category") ?: "Full body"
                val category = Screen.Workout.decodeCategory(rawCategory)

                WorkoutScreen(category = category, navController = navController)
            }
        }
    }
}
