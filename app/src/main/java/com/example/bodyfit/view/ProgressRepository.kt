package com.example.bodyfit.view

import com.google.firebase.firestore.FirebaseFirestore

// Data access layer for all progress related firestore reads and writes
// all methods are async and communicates through callbacks
class ProgressRepository {
    // Shared firestore instance for class operations
    private val db = FirebaseFirestore.getInstance()
    // fetches complete data snapshot from firestore
    fun getProgress(userId: String, onResult: (ProgressData?) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document("userGoals")
            .get()
            .addOnSuccessListener { doc ->
                if (!doc.exists()) {
                    // Document missing; user has never set goals
                    onResult(ProgressData(goalsExist = false))
                } else {
                    // match each firestore field to corresponding progress data property
                    val data = ProgressData(
                        stepsProgress    = (doc.getLong("stepsProgress")     ?: 0).toInt(),
                        dailyStepsGoal   = (doc.getLong("dailyStepsGoal")    ?: 0).toInt(),
                        workoutsProgress = (doc.getLong("workoutsProgress")  ?: 0).toInt(),
                        WeeklyWorkoutsGoal = (doc.getLong("WeeklyWorkOutGoal") ?: 0).toInt(),
                        dailyCaloriesGoal  = (doc.getLong("dailyCaloriesGoal") ?: 0).toInt(),
                        caloriesProgress   = (doc.getLong("caloriesProgress")  ?: 0).toInt(),
                        goalsExist = true
                    )
                    onResult(data)
                }
            }
            // network error or denied, return null object
            .addOnFailureListener { onResult(null) }
    }

    //Increment workout session count by 1
    fun incrementWorkouts(userId: String, current: Int, goalMax: Int, onDone: (exceeded: Boolean) -> Unit) {
        if (current >= goalMax) {
            onDone(true)
            return
        }
        db.collection("users").document(userId)
            .collection("goals").document("userGoals")
            .update("workoutsProgress", current + 1)
            .addOnCompleteListener { onDone(false) }
    }

    //Update calories burned
    fun addCalories(
        userId: String,
        additionalCalories: Int,
        goalMax: Int,
        onDone: (newTotal: Int, exceeded: Boolean) -> Unit
    ) {
        val docRef = db.collection("users").document(userId)
            .collection("goals").document("userGoals")

        // Read current value fresh from Firestore before writing
        docRef.get().addOnSuccessListener { doc ->
            val currentProgress = (doc.getLong("caloriesProgress") ?: 0).toInt()
            val newTotal        = (currentProgress + additionalCalories).coerceAtMost(goalMax)
            val exceeded        = (currentProgress + additionalCalories) > goalMax

            docRef.update("caloriesProgress", newTotal)
                .addOnCompleteListener { onDone(newTotal, exceeded) }
        }.addOnFailureListener {
            onDone(0, false)
        }
    }
}