package com.example.bodyfit.view

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.app.NotificationCompat
import androidx.navigation.NavController
import com.example.bodyfit.R
import java.util.Calendar
import androidx.compose.material.icons.filled.Alarm


const val CHANNEL_ID   = "bodyfit_reminders"
const val CHANNEL_NAME = "Workout Reminders"
data class Reminder(
    val id: Int,
    val title: String,
    val hour: Int,
    val minute: Int,
    val enabled: Boolean = true
) {
    val timeLabel: String
        get() {
            val period = if (hour < 12) "AM" else "PM"
            val displayHour = when {
                hour == 0  -> 12
                hour > 12  -> hour - 12
                else       -> hour
            }
            return "%d:%02d %s".format(displayHour, minute, period)
        }
}

// broadcast deliver

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val title   = intent.getStringExtra("title") ?: "BodyFit Reminder"
        val message = intent.getStringExtra("message") ?: "Time for your workout!"
        val notificationId = intent.getIntExtra("notificationId", 0)

        ensureChannel(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setAutoCancel(true)
            .build()

        nm.notify(notificationId, notification)
    }
}

fun ensureChannel(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(
            CHANNEL_ID,
            CHANNEL_NAME,
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "BodyFit workout and goal reminders & Alerts"
            enableVibration(true)
            enableLights(true)
        }
        nm.createNotificationChannel(channel)
    }
}

//Alarm helper functions
private fun scheduleAlarm(context: Context, reminder: Reminder) {
    ensureChannel(context)
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S &&
        !alarmManager.canScheduleExactAlarms()
    ) {
        Toast.makeText(
            context,
            "Please allow exact alarms in Settings > Apps > BodyFit > Alarms & Reminders",
            Toast.LENGTH_LONG
        ).show()
        //Open system settings for permission request
        val intent = Intent(android.provider.Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        return
    }

    val intent = Intent(context, ReminderReceiver::class.java).apply {
        putExtra("title",   reminder.title)
        putExtra("message", "⏰ Time for: ${reminder.title}")
        putExtra("notificationId", reminder.id)
    }
    val pi = PendingIntent.getBroadcast(
        context, reminder.id, intent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    val cal = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, reminder.hour)
        set(Calendar.MINUTE,      reminder.minute)
        set(Calendar.SECOND,      0)
        // If the set time is already past today, schedule alert for tomorrow
        if (timeInMillis <= System.currentTimeMillis()) {
            add(Calendar.DAY_OF_YEAR, 1)
        }
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            cal.timeInMillis,
            pi
        )
    } else {
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }
}

private fun cancelAlarm(context: Context, reminderId: Int) {
    val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    val pi = PendingIntent.getBroadcast(
        context, reminderId,
        Intent(context, ReminderReceiver::class.java),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    alarmManager.cancel(pi)
}

// reminders screen design starts here
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsScreen(navController: NavController) {

    val context = LocalContext.current

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            Toast.makeText(context, "Notifications are disabled. You won't see reminders.", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(Unit) {
        ensureChannel(context)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    // Master toggle
    LaunchedEffect(Unit) { ensureChannel(context) }
    var notificationsEnabled by rememberSaveable { mutableStateOf(true) }

    // Reminder list, starts with two default reminders
    var reminders by remember {
        mutableStateOf(
            listOf(
                Reminder(id = 1001, title = "Morning Run",  hour = 6,  minute = 30),
                Reminder(id = 1002, title = "Evening Jog",  hour = 19, minute = 0)
            )
        )
    }

    // Added reminder dialog state
    var showAddDialog   by remember { mutableStateOf(false) }
    var showTimePicker  by remember { mutableStateOf(false) }
    var newTitle        by remember { mutableStateOf("") }
    var newTitleError   by remember { mutableStateOf("") }
    var pickedHour      by remember { mutableStateOf(8) }
    var pickedMinute    by remember { mutableStateOf(0) }
    val timePickerState = rememberTimePickerState(initialHour = pickedHour, initialMinute = pickedMinute)

    Scaffold(
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector        = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                title = { Text("Notifications & Reminders") },
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

            // master toggle
            NotificationToggle(
                enabled  = notificationsEnabled,
                onToggle = { enabled ->
                    notificationsEnabled = enabled
                    if (!enabled) {
                        // Cancel every scheduled alarm
                        reminders.forEach { cancelAlarm(context, it.id) }
                    } else {
                        // Re-enable all that were switched on
                        reminders.filter { it.enabled }.forEach { scheduleAlarm(context, it) }
                    }
                }
            )

            // Section header
            Text(
                text = "Scheduled Reminders",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            if (reminders.isEmpty()) {
                Text(
                    text = "No reminders yet. Tap + Add Reminder to create one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Reminders
            reminders.forEach { reminder ->
                ReminderCard(
                    reminder             = reminder,
                    masterEnabled        = notificationsEnabled,
                    onToggle             = { isOn ->
                        reminders = reminders.map {
                            if (it.id == reminder.id) it.copy(enabled = isOn) else it
                        }
                        if (isOn && notificationsEnabled)
                            scheduleAlarm(context, reminder)
                        else
                            cancelAlarm(context, reminder.id)
                    },
                    onDelete             = {
                        cancelAlarm(context, reminder.id)
                        reminders = reminders.filter { it.id != reminder.id }
                    }
                )
            }

            // Add Reminder Button
            OutlinedButton(
                onClick  = { showAddDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Reminder")
            }
        }
    }

    // Add reminder dialogue
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddDialog = false
                newTitle = ""
                newTitleError = ""
            },
            title = { Text("New Reminder") },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value         = newTitle,
                        onValueChange = { newTitle = it; newTitleError = "" },
                        label         = { Text("Reminder title") },
                        isError       = newTitleError.isNotEmpty(),
                        supportingText = if (newTitleError.isNotEmpty()) {{ Text(newTitleError) }} else null,
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    // Show selected time and let user change it
                    val displayTime = run {
                        val period = if (pickedHour < 12) "AM" else "PM"
                        val h = when { pickedHour == 0 -> 12; pickedHour > 12 -> pickedHour - 12; else -> pickedHour }
                        "%d:%02d %s".format(h, pickedMinute, period)
                    }

                    OutlinedButton(
                        onClick  = { showTimePicker = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Alarm, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Time: $displayTime")
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    if (newTitle.isBlank()) {
                        newTitleError = "Title is required"
                    } else {
                        val newId = System.currentTimeMillis().toInt()
                        val reminder = Reminder(
                            id      = newId,
                            title   = newTitle.trim(),
                            hour    = pickedHour,
                            minute  = pickedMinute,
                            enabled = true
                        )
                        reminders = reminders + reminder
                        if (notificationsEnabled) scheduleAlarm(context, reminder)
                        showAddDialog = false
                        newTitle = ""
                    }
                }) { Text("Add") }
            },
            dismissButton = {
                TextButton(onClick = {
                    showAddDialog = false
                    newTitle = ""
                    newTitleError = ""
                }) { Text("Cancel") }
            }
        )
    }

    // Time picker dialogue
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            title = { Text("Pick a time") },
            text  = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    pickedHour   = timePickerState.hour
                    pickedMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text("Cancel") }
            }
        )
    }
}

//Reminder card

@Composable
fun ReminderCard(
    reminder: Reminder,
    masterEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    Card(
        shape  = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector  = Icons.Default.Alarm,
                contentDescription = null,
                tint         = if (reminder.enabled && masterEnabled)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(reminder.title, style = MaterialTheme.typography.bodyLarge)
                Text(
                    reminder.timeLabel,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Switch(
                checked         = reminder.enabled && masterEnabled,
                onCheckedChange = onToggle,
                enabled         = masterEnabled
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete reminder",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

//Master toggle
@Composable
fun NotificationToggle(enabled: Boolean, onToggle: (Boolean) -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Enable Notifications", style = MaterialTheme.typography.bodyLarge)
                Text(
                    "Receive reminders for workouts and goals",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(checked = enabled, onCheckedChange = onToggle)
        }
    }
}