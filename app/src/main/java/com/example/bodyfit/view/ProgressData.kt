package com.example.bodyfit.view

data class ProgressData(
    val stepsProgress: Int = 0,
    val dailyStepsGoal: Int = 10000,
    val workoutsProgress: Int = 0,
    val WeeklyWorkoutsGoal: Int = 6,
    val dailyCaloriesGoal: Int = 0,
    val caloriesProgress: Int = 2000
)
