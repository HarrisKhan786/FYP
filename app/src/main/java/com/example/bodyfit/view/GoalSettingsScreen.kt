package com.example.bodyfit.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    navController: NavController
) {
    var steps by remember { mutableStateOf("") }
    var workouts by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Set Your Goals") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {

            GoalInputCard(
                label = "Daily Steps Goal",
                value = steps,
                onValueChange = { steps = it },
                unit = "steps"
            )

            GoalInputCard(
                label = "Weekly Workouts",
                value = workouts,
                onValueChange = { workouts = it },
                unit = "sessions"
            )

            GoalInputCard(
                label = "Daily Calories Burn",
                value = calories,
                onValueChange = { calories = it },
                unit = "kcal"
            )

            Button(
                onClick = {
                    // The Logic for saving the goals to firebase
                    if (steps.isEmpty() || workouts.isEmpty() || calories.isEmpty()){
                        Toast.makeText(context, "All Fields are Required!", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val userID = Firebase.auth.currentUser?.uid
                    val userDB = FirebaseFirestore.getInstance()

                    val stepsValue = steps.toInt()
                    val workoutsValue = workouts.toInt()
                    val caloriesValue = calories.toInt()

                    if (stepsValue <= 0 || workoutsValue <= 0 || caloriesValue <= 0) {
                        Toast.makeText(context, "Values should be greater than 0", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val goalsData = hashMapOf(
                        "dailyStepsGoal" to stepsValue,
                        "WeeklyWorkOutGoal" to workoutsValue,
                        "dailyCaloriesGoal" to caloriesValue,
                        "stepsProgress" to 0,
                        "workoutsProgress" to 0,
                        "caloriesProgress" to 0,
                        "CreatedAt" to System.currentTimeMillis()
                    )

                    userID?.let {
                        userDB.collection("users")
                            .document(it)
                            .collection("goals")
                            .document("userGoals")
                            .set(goalsData)
                            .addOnSuccessListener {
                                Toast.makeText(context, "Goals saved Successfully", Toast.LENGTH_SHORT).show()
                                navController.navigate("dashboard") {
                                    popUpTo("goals") { inclusive = true }
                                }
                            }
                            .addOnFailureListener {
                                Toast.makeText(context, "Failed to save goals!", Toast.LENGTH_SHORT).show()
                            }
                    }

                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Goals")
            }
        }
    }
}

@Composable
fun GoalInputCard(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unit: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = value,
                onValueChange = onValueChange,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                trailingIcon = {
                    Text(unit, color = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}


