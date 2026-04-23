package com.example.bodyfit.view

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel

class ProgressViewModel : ViewModel() {

    private val repository = ProgressRepository()

    var progressData by mutableStateOf<ProgressData?>(null)
        private set

    fun loadProgress(userId: String) {
        repository.getProgress(userId) { data ->
            progressData = data
        }
    }
}