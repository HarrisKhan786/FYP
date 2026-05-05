package com.example.bodyfit.view

// Immutable data class that represents the current state of a user's progress, against their fitness goals.
// It is a single model shared between repository, viewmodel, and progress screen.
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