package com.example.bodyfit.view

data class ProgressData(
    val stepsProgress: Int = 0,
    val dailyStepsGoal: Int = 0,
    val workoutsProgress: Int = 0,
    val WeeklyWorkoutsGoal: Int = 0,
    val dailyCaloriesGoal: Int = 0,
    val caloriesProgress: Int = 0,
    // True only when the Firestore document actually exists (user has set goals)
    val goalsExist: Boolean = false
)