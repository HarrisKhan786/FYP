package com.example.bodyfit.view

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.Fitness
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType
import com.google.android.gms.fitness.request.DataReadRequest
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.concurrent.TimeUnit

object GoogleFitManager {

//    reads the current steps from google fit API and then stores them in firestore
    fun readAndSyncSteps(
        context: Context,
        onResult: (Int) -> Unit = {}
    ) {
        val fitnessOptions = FitnessOptions.builder()
            .addDataType(DataType.TYPE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
            .build()

        val account = GoogleSignIn.getAccountForExtension(context, fitnessOptions)

        val endTime   = System.currentTimeMillis()
        val startTime = endTime - TimeUnit.DAYS.toMillis(1)

        val request = DataReadRequest.Builder()
            .read(DataType.TYPE_STEP_COUNT_DELTA)
            .setTimeRange(startTime, endTime, TimeUnit.MILLISECONDS)
            .build()

        Fitness.getHistoryClient(context, account)
            .readData(request)
            .addOnSuccessListener { response ->
                var totalSteps = 0
                response.dataSets.forEach { dataSet ->
                    dataSet.dataPoints.forEach { dp ->
                        totalSteps += dp.getValue(dp.dataType.fields[0]).asInt()
                    }
                }
                syncStepsToFirestore(totalSteps)
                onResult(totalSteps)
            }
            .addOnFailureListener {
                onResult(0)
            }
    }

    /** Kept for backward-compat – delegates to readAndSyncSteps. */
    fun readSteps(context: Context, onResult: (Int) -> Unit) =
        readAndSyncSteps(context, onResult)

    // Private helper functions

    private fun syncStepsToFirestore(steps: Int) {
        val uid = Firebase.auth.currentUser?.uid ?: return
        FirebaseFirestore.getInstance()
            .collection("users")
            .document(uid)
            .collection("goals")
            .document("userGoals")
            .update("stepsProgress", steps)
            // If the document does not yet exist, fall back to set-with-merge
            .addOnFailureListener {
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(uid)
                    .collection("goals")
                    .document("userGoals")
                    .set(mapOf("stepsProgress" to steps),
                        com.google.firebase.firestore.SetOptions.merge())
            }
    }
}