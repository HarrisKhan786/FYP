package com.example.bodyfit.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProgressViewModel : ViewModel() {

    private val repository = ProgressRepository()

    var progressData by mutableStateOf<ProgressData?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)


    fun loadProgress(userId: String) {
        isLoading = true
        repository.getProgress(userId) { data ->
            progressData = data
            isLoading = false
        }
    }

    fun logWorkoutSession(userId: String) {
        val current = progressData?.workoutsProgress ?: return
        val goalMax = progressData?.WeeklyWorkoutsGoal ?: return

        repository.incrementWorkouts(userId, current, goalMax) {
            exceeded -> if (exceeded){
            feedbackMessage = "🎉 Weekly workout goal already reached!"
            } else {
            feedbackMessage = null
            loadProgress(userId)
            }
        }
    }

    fun addCalories(userId: String, additionalCalories: Int) {
        val goalMax = progressData?.dailyCaloriesGoal ?: return

        repository.addCalories(userId, additionalCalories, goalMax) { newTotal, exceeded ->
            feedbackMessage = if (exceeded)
                "⚠️ Daily calorie goal already reached! Total capped at your set goal $newTotal kcal"
            else
                null
            loadProgress(userId)
        }
    }

    fun clearFeedback() { feedbackMessage = null }
}