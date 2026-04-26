package com.example.bodyfit.view

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.fitness.FitnessOptions
import com.google.android.gms.fitness.data.DataType


object GoogleFitAuth {

    val fitnessOptions: FitnessOptions = FitnessOptions.builder()
        .addDataType(DataType.TYPE_STEP_COUNT_DELTA,      FitnessOptions.ACCESS_READ)
        .addDataType(DataType.AGGREGATE_STEP_COUNT_DELTA, FitnessOptions.ACCESS_READ)
        .build()

    fun hasAllPermissions(activity: Activity): Boolean {
        val runtimeGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            ContextCompat.checkSelfPermission(
                activity, Manifest.permission.ACTIVITY_RECOGNITION
            ) == PackageManager.PERMISSION_GRANTED
        } else true

        val fitGranted = GoogleSignIn.hasPermissions(
            GoogleSignIn.getAccountForExtension(activity, fitnessOptions),
            fitnessOptions
        )

        return runtimeGranted && fitGranted
    }
    const val FIT_REQUEST_CODE = 1001

    fun requestFitPermissions(activity: Activity) {
        val account = GoogleSignIn.getAccountForExtension(activity, fitnessOptions)
        GoogleSignIn.requestPermissions(activity, FIT_REQUEST_CODE, account, fitnessOptions)
    }
}