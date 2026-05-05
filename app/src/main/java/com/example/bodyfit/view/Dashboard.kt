package com.example.bodyfit.view
import android.Manifest
import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.bodyfit.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import android.os.Build
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.text.style.TextAlign

// Contains all the composables that make the main authenticated screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    navController: NavController, // needed so that the click of workout category navigates to workout details screen
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as Activity
    // Google fit permission launcher
    val activityRecognitionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            // Both layers satisfied — safe to read step data now
            if (GoogleFitAuth.hasAllPermissions(activity)) {
                GoogleFitManager.readAndSyncSteps(context)
            } else {
                // ACTIVITY_RECOGNITION granted but Fit OAuth not yet — request it
                GoogleFitAuth.requestFitPermissions(activity)
               // Steps will be read on the next launch when hasAllPermissions() = true
            }
        }
        // If denied: degrade gracefully; steps simply show as 0
    }
    // when launched for the first time, decided which permission step is required
    LaunchedEffect(true) {
        when {
            GoogleFitAuth.hasAllPermissions(activity) -> {
                GoogleFitManager.readAndSyncSteps(context)
            }
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q -> {
                activityRecognitionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION)
            }
            else -> {
                GoogleFitAuth.requestFitPermissions(activity)
            }
        }
    }
    // rendering the main dashboard body
    DashboardContent(navController = navController, modifier = modifier.fillMaxSize())
}

// Top app bar that shows only on dashboard screen
// has profile photo, personalised greetings, and notification bell with a red badge
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    onNotificationClick: () -> Unit = {}
) {
    // state for the user display name and profile photo url
    var userName by remember { mutableStateOf("") }
    var photoUrl by remember { mutableStateOf<String?>(null) }

    val uid = Firebase.auth.currentUser?.uid
    val db  = FirebaseFirestore.getInstance()

    // fetch the user name and the profile photo url from firebase storage and firestore
    LaunchedEffect(true) {
        uid?.let {
            db.collection("users").document(it).get()
                .addOnSuccessListener { doc ->
                    userName = doc.getString("name")     ?: "User"
                    photoUrl = doc.getString("photoUrl")
                }
        }
    }

    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.padding(18.dp),
        title = {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .padding(horizontal = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // circular profile picture that loads from firestore storage, falls back to drawable
                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (photoUrl != null){
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(photoUrl).crossfade(true).build(),
                            contentDescription = "Profile picture",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(90.dp).clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Profile Picture",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.size(48.dp)
                        )
                    }

                }
                //Greetings text with user name in bold
                Text(
                    buildAnnotatedString {
                        append("Hello, ")
                        withStyle(
                            style = SpanStyle(
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp
                            )
                        ) {
                            var userName by remember { mutableStateOf("") }

                            val uid = Firebase.auth.currentUser?.uid
                            val db = FirebaseFirestore.getInstance()

                            LaunchedEffect(true) {
                                uid?.let {
                                    db.collection("users")
                                        .document(it)
                                        .get()
                                        .addOnSuccessListener { doc ->
                                            userName = doc.getString("name") ?: "User"
                                        }
                                }
                            }
                            append(userName)
                        }
                    },
                    modifier = Modifier.padding(start = 10.dp)
                )

                Spacer(modifier = Modifier.weight(1f))
                // Bell icon with red badge as a dot for navigating to notifications and reminders
                BadgedBox(
                    badge = {
                        Badge(
                            modifier = Modifier.clip(CircleShape)
                                .background(Color.Red)
                                .align(Alignment.BottomEnd)
                        )
                    }
                ) {
                    IconButton(onClick = onNotificationClick) { Icon(
                        imageVector = Icons.Filled.Notifications,
                        contentDescription = "Notifications"

                    )}
                }
            }
        }
    )
}

// Bottom bar shown on the four main authenticated screens
// hidden on login, register, work out details, and notifications
// It holds profile, dashboard, goals, and progress tabs
// The active tab is highlighted using the primary theme color
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardBottomBar(
    navController: NavHostController,
    currentRoute: String?
) {
    BottomAppBar(
        modifier = Modifier.clip(
            RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 5.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {

            BottomBarIcon(
                icon = Icons.Filled.PersonOutline,
                label = "Profile",
                selected = currentRoute == Screen.Profile.route
            ) {
                navController.navigate(Screen.Profile.route)
            }

            BottomBarIcon(
                icon = Icons.Outlined.GridView,
                label = "Dashboard",
                selected = currentRoute == Screen.Dashboard.route
            ) {
                navController.navigate(Screen.Dashboard.route)
            }

            BottomBarIcon(
                icon = Icons.Outlined.TrackChanges,
                label = "Goals",
                selected = currentRoute == Screen.Goals.route
            ) {
                navController.navigate(Screen.Goals.route)
            }

            BottomBarIcon(
                icon = Icons.AutoMirrored.Outlined.TrendingUp,
                label = "Progress",
                selected = currentRoute == Screen.Progress.route
            ) {
                navController.navigate(Screen.Progress.route)
            }
        }
    }
}

// bottom bar icon and label used in dashboard bottom bar
// it applies the primary colour when selected
@Composable
fun BottomBarIcon(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {

    Column(
        modifier = Modifier
            .clickable(onClick = onClick)
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            modifier = Modifier.size(26.dp),
            tint = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )

        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (selected)
                MaterialTheme.colorScheme.primary
            else
                MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// workout chip which has mutually exclusive selection using in workout category lazy row
// unselected has white background and selected has black background, depending on user theme
@Composable
fun WorkoutChip(text: String, selected: Boolean, onClick: () -> Unit) {
    AssistChip(
        onClick = onClick,
        label = {
            Text(text)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = if (selected) Color.Black else Color.White,
            labelColor = if (selected) Color.White else Color.Black
        ),
        border = BorderStroke(1.dp, Color.Black)
    )
}
// Scrollable body that contains workout category, featured workout, calories, and today's goals
@Composable
fun DashboardContent( navController: NavController,
    modifier: Modifier = Modifier
) {
    val workOutCategories = listOf("Full body", "Cardio", "Cross Fit", "Cyclist", "Glutes", "Power")

    // currently selected workout category defaults to the firt item
    var selectedCategory by remember { mutableStateOf(workOutCategories.first()) }
    // goals and current state progress loaded from firestore database
    var caloriesGoal     by remember { mutableStateOf(0) }
    var caloriesProgress by remember { mutableStateOf(0f) }
    var stepsGoal        by remember { mutableStateOf(0) }
    var stepsProgress    by remember { mutableStateOf(0) }
    var workoutsGoal     by remember { mutableStateOf(0) }
    var workoutsProgress by remember { mutableStateOf(0) }

    val uid = Firebase.auth.currentUser?.uid
    val db  = FirebaseFirestore.getInstance()
    // realtime snapshot listener for data changes
    DisposableEffect(uid) {
        if (uid == null) return@DisposableEffect onDispose {}
        val listener = db.collection("users").document(uid)
            .collection("goals").document("userGoals")
            .addSnapshotListener { doc, _ ->
                doc ?: return@addSnapshotListener
                caloriesGoal     = (doc.getLong("dailyCaloriesGoal")  ?: 0).toInt()
                caloriesProgress = (doc.getLong("caloriesProgress")   ?: 0).toFloat()
                stepsGoal        = (doc.getLong("dailyStepsGoal")     ?: 0).toInt()
                stepsProgress    = (doc.getLong("stepsProgress")      ?: 0).toInt()
                workoutsGoal     = (doc.getLong("WeeklyWorkOutGoal") ?: 0).toInt()
                workoutsProgress = (doc.getLong("workoutsProgress")   ?: 0).toInt()
            }
        // removes the listener when the composable is removed from the tree
        onDispose { listener.remove() }
    }
    // look up the workout plan for the currently selected category
    val currentPlan = WorkoutData.getPlan(selectedCategory)
    // precalculate the calories percentage for the circular indicator
    val caloriePercent  = if (caloriesGoal > 0) caloriesProgress / caloriesGoal else 0f
    val calorieText     = (caloriePercent * 100).toInt()
    // UI layout
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
    ) {

        Spacer(modifier = Modifier.size(8.dp))
        // workout category chip which is scrollable horizontally and reactively updates the banner below it
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workOutCategories) { category ->
                WorkoutChip(
                    text = category,
                    selected = category == selectedCategory,
                    onClick  = { selectedCategory = category }
                )
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        //featured work out banner, the tagline level badge, duration, and start button read from current category.
        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(id = R.color.light_purple))
                .fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp) .fillMaxWidth()
            ) {
                // Display selected workout tagline
                Text(
                    text = currentPlan?.tagline ?: selectedCategory,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.Black,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Start
                )

                Spacer(modifier = Modifier.size(8.dp))

                // Level colours
                val levelColor = when (currentPlan?.level) {
                    "Beginner"     -> Color(0xFF4CAF50)
                    "Intermediate" -> Color(0xFFFF9800)
                    "Advanced"     -> Color(0xFFF44336)
                    else           -> MaterialTheme.colorScheme.primary
                }

                // display selected workout difficulty level badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = levelColor.copy(alpha = 0.15f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            Icons.Default.Star, null,
                            tint = levelColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text(
                            text = currentPlan?.level?.let { "$it level" } ?: "Select level",
                            color = levelColor,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Start
                        )
                    }
                }



                Spacer(modifier = Modifier.size(8.dp))
                // Display the image associated with the category
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.weight(0.5f))
                        Icon(
                            imageVector = currentPlan?.icon ?: Icons.Default.FitnessCenter,
                            contentDescription = null,
                            modifier = Modifier.size(200.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = "Timer",
                        tint = Color.Black
                    )

                    Text(
                        text = currentPlan?.totalMinutes?.let { "$it minutes" } ?: "0 minutes",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))
                    // start button that takes the user to workout details
                    TextButton(onClick = { navController.navigate(Screen.Workout.createRoute(selectedCategory))}) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "Start",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Start",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        // calories burned progress card
        // Circular progress indicator showing today's calorie burn vs goal
        // Values come from the real-time Firestore snapshot listener above
        var caloriesGoal by remember { mutableStateOf(0) }
        var caloriesProgress by remember { mutableStateOf(0f) }

        val uid = Firebase.auth.currentUser?.uid
        val db = FirebaseFirestore.getInstance()

        LaunchedEffect(true) {

            uid?.let {
                db.collection("users")
                    .document(it)
                    .collection("goals")
                    .document("userGoals")
                    .get()
                    .addOnSuccessListener { doc ->

                        val goal = (doc.getLong("dailyCaloriesGoal") ?: 0).toFloat()
                        val progress = (doc.getLong("caloriesProgress") ?: 0).toFloat()

                        caloriesGoal = goal.toInt()
                        caloriesProgress = progress
                    }
            }
        }

        val progressPercent =
            if (caloriesGoal > 0) caloriesProgress / caloriesGoal else 0f

        val percentageText = (progressPercent * 100).toInt()

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(colorResource(id = R.color.orange))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {

                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(75.dp),
                        progress = progressPercent,
                        strokeWidth = 8.dp,
                        color = Color.Black
                    )
                    Text(
                        text = "$percentageText%",
                        fontWeight = FontWeight.Bold,
                        fontSize = 25.sp,
                        color = Color.Black
                    )
                }

                Column {
                    Text(
                        text = "Great!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "You have burned $caloriesProgress out of $caloriesGoal calories daily goal!",
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        // Goals summary card
        Text(
            text = "Today's Goals",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        Spacer(modifier = Modifier.size(8.dp))
        // Goals summary card, steps
        DashboardGoalCard(
            icon  = Icons.Default.DirectionsRun,
            title = "Daily Steps",
            progress  = if (stepsGoal > 0) stepsProgress.toFloat() / stepsGoal else 0f,
            valueText = "$stepsProgress / $stepsGoal steps",
            accentColor = MaterialTheme.colorScheme.tertiary
        )

        Spacer(modifier = Modifier.size(8.dp))
        // Goals Summary card, weekly sessions
        DashboardGoalCard(
            icon  = Icons.Default.FitnessCenter,
            title = "Weekly Workouts",
            progress  = if (workoutsGoal > 0) workoutsProgress.toFloat() / workoutsGoal else 0f,
            valueText = "$workoutsProgress / $workoutsGoal sessions",
            accentColor = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.size(24.dp))

    }
}

//Reusable goals card for dashboard
@Composable
fun DashboardGoalCard(
    icon: ImageVector,
    title: String,
    progress: Float,
    valueText: String,
    accentColor: androidx.compose.ui.graphics.Color
)

{
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // tinted circular icon container
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            // rounded linear progress bar
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title, style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = progress.coerceIn(0f, 1f),
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = accentColor.copy(alpha = 0.15f)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = valueText, style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.width(10.dp))

            Text(
                text = "${(progress * 100).toInt().coerceAtMost(100)}%",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = accentColor
            )
        }
    }


}

@Composable
fun GoalsSection() {

    var steps by remember { mutableStateOf(0) }
    var progress by remember { mutableStateOf(0) }

    val uid = Firebase.auth.currentUser?.uid
    val db = FirebaseFirestore.getInstance()

    LaunchedEffect(true) {

        uid?.let {

            db.collection("users")
                .document(it)
                .collection("goals")
                .document("userGoals")
                .get()
                .addOnSuccessListener { doc ->

                    steps = (doc.getLong("dailyStepsGoal") ?: 0).toInt()
                    progress = (doc.getLong("stepsProgress") ?: 0).toInt()

                }
        }
    }

    val percent = if (steps > 0) progress.toFloat() / steps else 0f

    Column(modifier = Modifier.padding(16.dp)) {

        Text("Steps Progress")

        LinearProgressIndicator(progress = percent)

        Text("You have completed $progress of $steps steps goal today")

    }
}