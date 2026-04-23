package com.example.bodyfit.view

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role.Companion.Button
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


//Display Saved Goals
data class SavedGoals(
    val dailyStepsGoal: Int = 0,
    val weeklyWorkoutGoal: Int = 0,
    val dailyCaloriesGoal: Int = 0
)

// The main screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalSettingScreen(
    navController: NavController
) {
    var steps by remember { mutableStateOf("") }
    var workouts by remember { mutableStateOf("") }
    var calories by remember { mutableStateOf("") }

//reading the existing goals
    var savedGoals by remember { mutableStateOf<SavedGoals?> (null)}
    var goalsLoaded by remember { mutableStateOf(false) }

    val context = LocalContext.current

    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid
        if (uid == null) {
            goalsLoaded = true
            savedGoals = SavedGoals()
            return@LaunchedEffect
        }
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .collection("goals").document("userGoals")
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    savedGoals = SavedGoals(
                        dailyStepsGoal = (documentSnapshot.getLong("dailyStepsGoal") ?:0).toInt(),
                        weeklyWorkoutGoal = (documentSnapshot.getLong("WeeklyWorkOutGoal") ?: 0).toInt(),
                        dailyCaloriesGoal = (documentSnapshot.getLong("dailyCaloriesGoal") ?: 0).toInt()
                    )
                } else {
                    SavedGoals()
                }

                goalsLoaded = true
            } .addOnFailureListener {
                savedGoals = SavedGoals()
                goalsLoaded = true
            }
    }

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
            //A card to display the existing goals
            CurrentGoalsCard(goals = savedGoals, loaded = goalsLoaded)

            Text(
                text = "Update Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

          //Goals input cards
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

//A card to display current goals
@Composable
fun CurrentGoalsCard(goals: SavedGoals?, loaded: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Current Goals",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(modifier = Modifier.height(12.dp))

            when {
                !loaded -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text("Loading...", color = MaterialTheme.colorScheme.onPrimaryContainer)
                    }
                }

                goals == null || (goals.dailyStepsGoal == 0 && goals.weeklyWorkoutGoal == 0 && goals.dailyCaloriesGoal == 0) -> {
                    Text(
                        text = "No goals set yet. Use the form below to get started!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                else -> {
                    GoalRow(
                        icon = Icons.Default.DirectionsRun,
                        label = "Daily Steps",
                        value = "${goals.dailyStepsGoal} steps"
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    GoalRow(
                        icon = Icons.Default.FitnessCenter,
                        label = "Weekly Workouts",
                        value = "${goals.weeklyWorkoutGoal} sessions"
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                    GoalRow(
                        icon = Icons.Default.LocalFireDepartment,
                        label = "Daily Calories",
                        value = "${goals.dailyCaloriesGoal} kcal"
                    )

                }
            }
        }
    }
}

@Composable
private fun GoalRow(icon: ImageVector, label: String, value: String){
    Row(verticalAlignment = Alignment.CenterVertically){
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(10.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.75f)

            )
            Text(
                text = value,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
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


