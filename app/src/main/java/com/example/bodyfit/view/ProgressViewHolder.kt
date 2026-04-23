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

    fun loadProgress(userId: String) {
        isLoading = true
        repository.getProgress(userId) { data ->
            progressData = data
            isLoading = false
        }
    }

    fun logWorkoutSession(userId: String) {
        val current = progressData?.workoutsProgress ?: return
        repository.incrementWorkouts(userId, current) {
            // Reload so all values stay in sync
            loadProgress(userId)
        }
    }

    fun updateCalories(userId: String, calories: Int) {
        repository.updateCalories(userId, calories) {
            loadProgress(userId)
        }
    }
}