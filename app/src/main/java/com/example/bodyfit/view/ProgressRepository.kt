package com.example.bodyfit.view

import com.google.firebase.firestore.FirebaseFirestore

class ProgressRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getProgress(userId: String, onResult: (ProgressData?) -> Unit) {
        db.collection("users")
            .document(userId)
            .collection("goals")
            .document("userGoals")
            .get()
            .addOnSuccessListener { doc ->
                val data = doc.toObject(ProgressData::class.java)
                onResult(data)
            }
            .addOnFailureListener {
                onResult(null)
            }
    }
}