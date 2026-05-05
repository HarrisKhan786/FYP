package com.example.bodyfit.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

// It sits between UI and repository for holding observable state that survives recomposition and screen rotations
class ProgressViewModel : ViewModel() {
    // internal repository only this viewmodel talks to database directly
    private val repository = ProgressRepository()
    // The user current progress snapshot
    var progressData by mutableStateOf<ProgressData?>(null)
        private set // only this view model can write, UI can read

    var isLoading by mutableStateOf(false)
        private set

    var feedbackMessage by mutableStateOf<String?>(null)

    // fetches the latest progress snapshot for a given user
    fun loadProgress(userId: String) {
        isLoading = true
        repository.getProgress(userId) { data ->
            progressData = data
            isLoading = false
        }
    }
    // Logs one workout session for today
    // count already equals their goal, so the counter can never exceed the cap.
    //On success, reloads data so all charts reflect the new value immediately.

    fun logWorkoutSession(userId: String) {
        val current = progressData?.workoutsProgress ?: return
        val goalMax = progressData?.WeeklyWorkoutsGoal ?: return

        repository.incrementWorkouts(userId, current, goalMax) {
            exceeded -> if (exceeded){
                // goal already reached
            feedbackMessage = "🎉 Weekly workout goal already reached!"
            } else {
            feedbackMessage = null
            loadProgress(userId)
            }
        }
    }
    // adds calories on  top of the already logged out calories for the day
    fun addCalories(userId: String, additionalCalories: Int) {
        val goalMax = progressData?.dailyCaloriesGoal ?: return

        repository.addCalories(userId, additionalCalories, goalMax) { newTotal, exceeded ->
            // inform the user if their entry was trimmed to cater for the days goal
            feedbackMessage = if (exceeded)
                "⚠️ Daily calorie goal already reached! Total capped at your set goal $newTotal kcal"
            else
                null
            // reload to keep the UI in SYC
            loadProgress(userId)
        }
    }
    // clears the one shot after it has been displayed
    fun clearFeedback() { feedbackMessage = null }
}