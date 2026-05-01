package com.example.bodyfit.view

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.charts.RadarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import com.google.firebase.auth.FirebaseAuth
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import kotlinx.coroutines.launch

//Progress screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgressScreen(viewModel: ProgressViewModel = viewModel()) {

    val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    val data = viewModel.progressData
    val loading = viewModel.isLoading

    var showCalorieDialog by remember { mutableStateOf(false) }
    var showWorkoutConfirm by remember { mutableStateOf(false) }

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(viewModel.feedbackMessage) {
        viewModel.feedbackMessage?.let { msg ->
            scope.launch { snackBarHostState.showSnackbar(msg) }
            viewModel.clearFeedback()
        }
    }

    LaunchedEffect(Unit) {
        if (uid.isNotBlank()) viewModel.loadProgress(uid)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progress & Analytics") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackBarHostState) }
    ) { padding ->

        when {
            loading -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }

            data != null && !data.goalsExist -> {
                EmptyProgressState(Modifier.padding(padding))
            }

            data != null -> {
                val stepsRatio = if (data.dailyStepsGoal > 0)
                    (data.stepsProgress.toFloat() / data.dailyStepsGoal).coerceIn(0f, 1f) else 0f
                val workoutRatio = if (data.WeeklyWorkoutsGoal > 0)
                    (data.workoutsProgress.toFloat() / data.WeeklyWorkoutsGoal).coerceIn(
                        0f,
                        1f
                    ) else 0f
                val calorieRatio = if (data.dailyCaloriesGoal > 0)
                    (data.caloriesProgress.toFloat() / data.dailyCaloriesGoal).coerceIn(
                        0f,
                        1f
                    ) else 0f

                val goalsMet = listOf(stepsRatio, workoutRatio, calorieRatio).count { it >= 1f }
                val consistency = when (goalsMet) {
                    3 -> "Excellent 🏆"
                    2 -> "Good 💪"
                    1 -> "Keep going 🔥"
                    else -> "Just getting started"
                }

                Column(
                    modifier = Modifier
                        .padding(padding)
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Steps bar chart
                    GoalBarChartCard(
                        title = "Daily Steps",
                        progress = stepsRatio,
                        current = data.stepsProgress,
                        goal = data.dailyStepsGoal,
                        unit = "steps",
                        barColor = AndroidColor.rgb(76, 175, 80)
                    )

                    // Workouts bar chart + log button
                    GoalBarChartCard(
                        title = "Weekly Workouts",
                        progress = workoutRatio,
                        current = data.workoutsProgress,
                        goal = data.WeeklyWorkoutsGoal,
                        unit = "sessions",
                        barColor = AndroidColor.rgb(33, 150, 243),
                        actionLabel = "Log Session",
                        actionIcon = Icons.Default.FitnessCenter,
                        actionEnabled = data.workoutsProgress < data.WeeklyWorkoutsGoal,
                        onAction = { showWorkoutConfirm = true }


                    )

                    // Calories bar chart + update button
                    GoalBarChartCard(
                        title = "Calories Burned",
                        progress = calorieRatio,
                        current = data.caloriesProgress,
                        goal = data.dailyCaloriesGoal,
                        unit = "kcal",
                        barColor = AndroidColor.rgb(255, 152, 0),
                        actionLabel = "Update",
                        actionIcon = Icons.Default.LocalFireDepartment,
                        actionEnabled = data.caloriesProgress < data.dailyCaloriesGoal,
                        onAction = { showCalorieDialog = true }
                    )

                    // Radar overview + summary
                    OverviewRadarCard(
                        stepsRatio = stepsRatio,
                        workoutRatio = workoutRatio,
                        calorieRatio = calorieRatio,
                        goalsMet = goalsMet,
                        consistency = consistency
                    )
                }
            }

            else -> {
                Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Could not load progress.")
                        Spacer(Modifier.height(8.dp))
                        Button(onClick = { viewModel.loadProgress(uid) }) { Text("Retry") }
                    }
                }
            }
        }
    }

    // Dialogs
    if (showWorkoutConfirm) {
        AlertDialog(
            onDismissRequest = { showWorkoutConfirm = false },
            icon = { Icon(Icons.Default.FitnessCenter, null) },
            title = { Text("Log Workout Session") },
            text = { Text("Add 1 workout session to your weekly count?") },
            confirmButton = {
                TextButton(onClick = {
                    showWorkoutConfirm = false
                    viewModel.logWorkoutSession(uid)
                }) { Text("Log it") }
            },
            dismissButton = {
                TextButton(onClick = { showWorkoutConfirm = false }) { Text("Cancel") }
            }
        )
    }

    if (showCalorieDialog) {
        val remaining = (data?.dailyCaloriesGoal ?: 0) - (data?.caloriesProgress ?: 0)
        if (showCalorieDialog) {
            val remaining = (data?.dailyCaloriesGoal ?: 0) - (data?.caloriesProgress ?: 0)
            CumulativeCalorieDialog(
                remainingCalories = remaining,
                onConfirm = { additional ->
                    showCalorieDialog = false
                    viewModel.addCalories(uid, additional)
                },
                onDismiss = { showCalorieDialog = false }
            )
        }
    }
}

private fun ratio(progress: Int, goal: Int): Float =
    if (goal > 0) (progress.toFloat() / goal).coerceIn(0f, 1f) else 0f

@Composable
fun CumulativeCalorieDialog(
    remainingCalories: Int,
    onConfirm: (Int) -> Unit,
    onDismiss: () -> Unit
) {
    var input by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Icon(Icons.Default.LocalFireDepartment, null) },
        title   = { Text("Add Calories Burned") },
        text    = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // ✅ Show remaining calories so user knows how much room is left
                Text(
                    text  = "Remaining today: $remainingCalories kcal",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text  = "Enter calories to add to today's total:",
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value           = input,
                    onValueChange   = { input = it; error = "" },
                    label           = { Text("Calories to add (kcal)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    isError         = error.isNotEmpty(),
                    supportingText  = if (error.isNotEmpty()) {{ Text(error) }} else null,
                    singleLine      = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = input.toIntOrNull()
                when {
                    v == null || v <= 0 -> error = "Enter a number greater than 0"
                    // Warn but still allow — repository will cap at goal
                    v > remainingCalories -> error = "Exceeds remaining budget ($remainingCalories kcal). Will be capped."
                    else -> onConfirm(v)
                }
                // If only the calories warning, allow confirm on second tap
                if (error.contains("capped")) {
                    val v2 = input.toIntOrNull()
                    if (v2 != null && v2 > 0) { error = ""; onConfirm(v2) }
                }
            }) { Text("Add") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

//Handling horizontal chart for each goal
@Composable
fun GoalBarChartCard(
    title: String,
    progress: Float,
    current: Int,
    goal: Int,
    unit: String,
    barColor: Int,
    actionLabel: String? = null,
    actionIcon: androidx.compose.ui.graphics.vector.ImageVector? = null,
    actionEnabled: Boolean = true,
    onAction: (() -> Unit)? = null
) {
    val textArgb = MaterialTheme.colorScheme.onSurfaceVariant.toArgb()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Title row + optional action button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

                if (actionLabel != null && actionIcon != null && onAction != null) {
                    FilledTonalButton(
                        onClick = onAction,
                        enabled = actionEnabled,
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(actionIcon, null, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(actionLabel, style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            Spacer(Modifier.height(4.dp))

            Text(
                text  = "${(progress * 100).toInt()}% complete  •  $current / $goal $unit",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(Modifier.height(12.dp))
            // When at 100%, show goal reached
            if (progress >= 1f) {
                Text(
                    text  = "Goal reached! 🎉  $current / $goal $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Text(
                    text  = "${(progress * 100).toInt()}% complete  •  $current / $goal $unit",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))

            // The chart — tall enough to look substantial, short enough to stay scannable
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(90.dp)
                    .clip(RoundedCornerShape(8.dp)),
                factory  = { ctx ->
                    HorizontalBarChart(ctx).apply {
                        description.isEnabled = false
                        legend.isEnabled      = false
                        setTouchEnabled(false)
                        setDrawBarShadow(true)
                        setDrawValueAboveBar(false)

                        xAxis.apply {
                            position        = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                            granularity     = 1f
                            // Hide the x label — we already have text above
                            valueFormatter  = IndexAxisValueFormatter(listOf(""))
                            textColor       = AndroidColor.TRANSPARENT
                        }

                        axisLeft.apply {
                            axisMinimum     = 0f
                            axisMaximum     = 100f
                            setDrawLabels(false)
                            setDrawGridLines(false)
                            setDrawAxisLine(false)
                        }

                        axisRight.isEnabled = false
                        setExtraOffsets(8f, 8f, 20f, 8f)
                    }
                },
                update = { chart ->
                    val percent = (progress * 100f).coerceIn(0f, 100f)

                    val entry = BarEntry(0f, percent)
                    val dataSet = BarDataSet(listOf(entry), title).apply {
                        color = barColor
                        barShadowColor = AndroidColor.argb(30, 0, 0, 0)
                        valueTextSize  = 13f
                        valueTextColor = textArgb
                        valueFormatter = PercentFormatter()
                        setDrawValues(true)
                    }

                    chart.data = BarData(dataSet).apply { barWidth = 0.55f }
                    chart.animateY(900)
                    chart.invalidate()
                }
            )
        }
    }
}

//Overview card for radar chart
@Composable
fun OverviewRadarCard(
    stepsRatio: Float,
    workoutRatio: Float,
    calorieRatio: Float,
    goalsMet: Int,
    consistency: String
) {
    val primaryArgb = MaterialTheme.colorScheme.primary.toArgb()
    val onPrimArgb  = MaterialTheme.colorScheme.onPrimaryContainer.toArgb()

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape    = RoundedCornerShape(16.dp),
        colors   = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            Text(
                "Weekly Overview",
                style      = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(4.dp))

            Text(
                "✔ Goals met: $goalsMet / 3\n✔ Consistency: $consistency",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )

            Spacer(Modifier.height(8.dp))

            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp),
                factory  = { ctx ->
                    RadarChart(ctx).apply {
                        description.isEnabled = false
                        webLineWidth          = 1.5f
                        webColor              = AndroidColor.LTGRAY
                        webLineWidthInner     = 1f
                        webColorInner         = AndroidColor.LTGRAY
                        webAlpha              = 100
                        isRotationEnabled     = false

                        xAxis.apply {
                            textSize       = 13f
                            textColor      = onPrimArgb
                            valueFormatter = IndexAxisValueFormatter(
                                listOf("Steps", "Workouts", "Calories")
                            )
                        }

                        yAxis.apply {
                            axisMinimum = 0f
                            axisMaximum = 100f
                            setDrawLabels(false)
                            setLabelCount(5, true)
                        }

                        legend.isEnabled = false
                        setBackgroundColor(AndroidColor.TRANSPARENT)
                    }
                },
                update = { chart ->
                    val entries = listOf(
                        RadarEntry(stepsRatio   * 100f),
                        RadarEntry(workoutRatio * 100f),
                        RadarEntry(calorieRatio * 100f)
                    )

                    val dataSet = RadarDataSet(entries, "Progress").apply {
                        color         = primaryArgb
                        fillColor     = primaryArgb
                        setDrawFilled(true)
                        fillAlpha     = 80
                        lineWidth     = 2f
                        isDrawHighlightCircleEnabled = true
                        setDrawHighlightIndicators(false)
                        // Hide per-point value labels — they clutter the radar
                        valueTextSize = 0f
                    }

                    chart.data = RadarData(dataSet)
                    chart.animateXY(1000, 1000)
                    chart.invalidate()
                }
            )
        }
    }
}

//function to handle a case where everything is empty

@Composable
fun EmptyProgressState(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(32.dp)
        ) {
            Text("📊", style = MaterialTheme.typography.displayMedium)
            Text("No progress yet", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "Set your goals first and then come back here to track your progress!",
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

//Input dialogue for calories
@Composable
fun CalorieInputDialog(currentCalories: Int, onConfirm: (Int) -> Unit, onDismiss: () -> Unit) {
    var input by remember { mutableStateOf(currentCalories.toString()) }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        icon    = { Icon(Icons.Default.LocalFireDepartment, null) },
        title   = { Text("Update Calories Burned") },
        text    = {
            Column {
                Text("Enter your total calories burned today:")
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value           = input,
                    onValueChange   = { input = it; error = "" },
                    label           = { Text("Calories (kcal)") },
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                    ),
                    isError         = error.isNotEmpty(),
                    supportingText  = if (error.isNotEmpty()) {{ Text(error) }} else null,
                    singleLine      = true
                )
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val v = input.toIntOrNull()
                when {
                    v == null -> error = "Enter a valid number"
                    v < 0    -> error = "Cannot be negative"
                    else     -> onConfirm(v)
                }
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}