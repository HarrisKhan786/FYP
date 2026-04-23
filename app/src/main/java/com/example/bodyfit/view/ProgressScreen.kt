package com.example.bodyfit.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.auth.FirebaseAuth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = viewModel()) {

//    getting data from firebase database
    val data = viewModel.progressData

    LaunchedEffect(Unit) {
        viewModel.loadProgress(FirebaseAuth.getInstance().currentUser?.uid ?: return@LaunchedEffect)
    }

    if (data == null) {
        Text("Loading...")
        return
    }

    val stepsProgress = data.stepsProgress.toFloat() / data.dailyStepsGoal
    val workoutProgress = data.workoutsProgress.toFloat() / data.WeeklyWorkoutsGoal
    val calorieProgress = data.caloriesProgress.toFloat() / data.dailyCaloriesGoal

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Analytics") },
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

            ProgressCard(
                title = "Daily Steps",
                progress = stepsProgress,
                valueText = "${data.stepsProgress} of ${data.dailyStepsGoal} Steps"
            )

            ProgressCard(
                title = "Weekly Workouts",
                progress = workoutProgress,
                valueText = "${data.workoutsProgress} of ${data.WeeklyWorkoutsGoal} Sessions"
            )

            ProgressCard(
                title = "Calories Burned",
                progress = calorieProgress,
                valueText = "${data.caloriesProgress} of ${data.dailyCaloriesGoal} kcal burned"
            )

            WeeklySummaryCard()
        }
    }
}

// Reusable progress card
@Composable
fun ProgressCard(
    title: String,
    progress: Float,
    valueText: String
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
                text = title,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = valueText,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

// weekly score card
@Composable
    fun WeeklySummaryCard() {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            ),
            shape = RoundedCornerShape(12.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {

                Text(
                    text = "Weekly Summary",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "✔ Goals met: 2 / 3\n✔ Active days: 5\n✔ Consistency: Good",
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
}

