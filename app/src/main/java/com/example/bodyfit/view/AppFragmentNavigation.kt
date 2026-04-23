package com.example.bodyfit.view

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.compose.currentBackStackEntryAsState



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
                DashboardScreen(
                )
            }
            composable(Screen.Goals.route) {
                GoalSettingScreen(navController)
            }
            composable(Screen.Progress.route) {
                ProgressScreen()
            }
            composable(Screen.Profile.route) {
                ProfileScreen(onLogout = {
                    navController.navigate(Screen.Login.route)
                })
            }
            composable(Screen.Notifications.route) {
                NotificationsScreen()
            }
        }
    }
}
