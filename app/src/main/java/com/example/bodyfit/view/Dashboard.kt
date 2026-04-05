package com.example.bodyfit.view
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
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
import com.example.bodyfit.R
import com.example.bodyfit.view.Screen.Dashboard.route
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier
) {
    DashboardContent(
        modifier = modifier.fillMaxSize()

    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardTopBar(
    onNotificationClick: () -> Unit = {}
) {
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

                Box(
                    modifier = Modifier
                        .size(65.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.profile),
                        contentDescription = "Profile picture",
                        contentScale = ContentScale.Crop
                    )
                }

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
                label = "Profile & Settings",
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
                icon = Icons.Outlined.TrendingUp,
                label = "Progress",
                selected = currentRoute == Screen.Progress.route
            ) {
                navController.navigate(Screen.Progress.route)
            }
        }
    }
}

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

@Composable
fun WorkoutChip(text: String) {
    var selected by remember { mutableStateOf(false) }

    AssistChip(
        onClick = { selected = !selected },
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

@Composable
fun DashboardContent(
    modifier: Modifier = Modifier
) {
    val workOutCategories = listOf(
        "Full body", "Cardio", "Cross Fit", "Cyclist", "Glutes", "Power"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
    ) {

        Spacer(modifier = Modifier.size(8.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(workOutCategories) { workOut ->
                WorkoutChip(workOut)
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

        Box(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(colorResource(id = R.color.light_purple))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 22.dp)
            ) {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Loose\nbelly fat",
                        fontWeight = FontWeight.Bold,
                        fontSize = 30.sp,
                        color = Color.Black
                    )

                    Button(
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(6.dp),
                        onClick = { /* TODO */ },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colorResource(id = R.color.purple_200)
                        )
                    ) {
                        Text("Intermediate level", color = Color.White, fontSize = 18.sp)
                    }
                }

                Spacer(modifier = Modifier.size(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(30.dp))
                        .background(Color.White)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Spacer(modifier = Modifier.weight(0.5f))
                        Image(
                            painter = painterResource(id = R.drawable.dumbell),
                            contentDescription = "Dumbbell",
                            modifier = Modifier.size(200.dp)
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
                        text = "40 minutes",
                        fontSize = 18.sp,
                        color = Color.Black,
                        modifier = Modifier.padding(start = 5.dp)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    TextButton(onClick = { /* TODO */ }) {
                        Row(horizontalArrangement = Arrangement.spacedBy(5.dp)) {
                            Text(
                                text = "Start",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                            Icon(
                                imageVector = Icons.Filled.ArrowForward,
                                contentDescription = "Start",
                                tint = Color.Black
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.size(8.dp))

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

        Text("$progress / $steps steps")

    }
}