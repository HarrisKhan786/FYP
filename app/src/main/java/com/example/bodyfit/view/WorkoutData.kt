package com.example.bodyfit.view

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsBike
import androidx.compose.material.icons.automirrored.filled.DirectionsRun
import androidx.compose.material.icons.filled.AccessibilityNew
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsBike
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Timer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.twotone.FitnessCenter
import androidx.compose.material.icons.twotone.DirectionsRun
import androidx.compose.material.icons.twotone.Speed
import androidx.compose.material.icons.twotone.SportsGymnastics

data class Exercise(
    val name: String,
    val sets: String,
    val reps: String,
    val durationMinutes: Int,
    val description: String,
    val muscleGroup: String
)

data class WorkoutPlan(
    val category: String,
    val tagline: String,
    val totalMinutes: Int,
    val level: String,
    val icon: ImageVector,
    val exercises: List<Exercise>
)

//Contains details for all the data that is displayed when a certain category is selected
object WorkoutData {

    val plans: Map<String, WorkoutPlan> = mapOf(

        "Full body" to WorkoutPlan(
            category      = "Full body",
            tagline       = "Train every muscle in one session",
            totalMinutes  = 45,
            level         = "Beginner",
            icon         = Icons.TwoTone.SportsGymnastics,
            exercises     = listOf(
                Exercise("Squat",          "3", "12", 5,  "Stand feet shoulder-width, lower until thighs parallel, drive up.",               "Legs & Glutes"),
                Exercise("Push-Up",        "3", "15", 4,  "Hands shoulder-width, lower chest to floor, press back up.",                      "Chest & Triceps"),
                Exercise("Bent-Over Row",  "3", "12", 5,  "Hinge at hips, pull dumbbells to ribcage, squeeze shoulder blades.",              "Back & Biceps"),
                Exercise("Plank",          "3", "60s hold", 4, "Elbows under shoulders, body straight, brace core.",                         "Core"),
                Exercise("Glute Bridge",   "3", "15", 4,  "Feet flat, drive hips up, squeeze glutes at top.",                               "Glutes & Hamstrings"),
                Exercise("Dumbbell Press", "3", "12", 5,  "Lie back, press dumbbells from chest to full extension.",                         "Shoulders & Chest"),
                Exercise("Mountain Climber","3","20", 4,  "High plank, alternate driving knees to chest at speed.",                          "Core & Cardio"),
                Exercise("Cool-Down Stretch","1","5 min",5,"Full body static stretches — hold each 20–30 seconds.",                          "Full Body")
            )
        ),

        "Cardio" to WorkoutPlan(
            category      = "Cardio",
            tagline       = "Boost your heart rate & burn fat",
            totalMinutes  = 40,
            level         = "Intermediate",
            icon          = Icons.Default.DirectionsRun,
            exercises     = listOf(
                Exercise("Jumping Jacks",   "3", "60s", 4,  "Full extension arms and legs on jump, feet together on land.",                  "Full Body"),
                Exercise("High Knees",      "3", "45s", 3,  "Run in place, drive knees to hip height, pump arms.",                           "Legs & Core"),
                Exercise("Burpee",          "3", "10",  5,  "Jump, drop to plank, push-up, jump feet in, jump up with arms overhead.",       "Full Body"),
                Exercise("Jump Rope",       "3", "90s", 5,  "Keep elbows at sides, wrists rotate rope, stay on balls of feet.",              "Calves & Cardio"),
                Exercise("Box Jump",        "3", "10",  5,  "Soft knees on landing, step down rather than jumping down.",                    "Legs & Power"),
                Exercise("Lateral Shuffle", "3", "45s", 4,  "Stay low, shuffle side-to-side, touch floor at each end.",                     "Legs & Agility"),
                Exercise("Sprint Intervals","5", "30s", 8,  "Maximum effort sprint, 30 s rest between sets.",                               "Full Body"),
                Exercise("Cool-Down Walk",  "1", "5 min",5, "Slow walk followed by calf and hip flexor stretches.",                         "Full Body")
            )
        ),

        "Cross Fit" to WorkoutPlan(
            category      = "Cross Fit",
            tagline       = "High intensity functional movement",
            totalMinutes  = 50,
            level         = "Advanced",
            icon         = Icons.Default.AccessibilityNew,
            exercises     = listOf(
                Exercise("Deadlift",        "5", "5",   7,  "Barbell over mid-foot, neutral spine, drive floor away, lock out hips.",        "Posterior Chain"),
                Exercise("Pull-Up",         "4", "8",   5,  "Full hang, pull chin over bar, controlled descent.",                            "Back & Biceps"),
                Exercise("Thruster",        "4", "10",  6,  "Front squat into overhead press in one fluid movement.",                        "Full Body"),
                Exercise("Box Jump",        "4", "12",  5,  "Explosive jump, full hip extension at top, step down.",                         "Legs & Power"),
                Exercise("Kettlebell Swing","4", "15",  5,  "Hinge hips, drive hips forward to swing bell to shoulder height.",              "Posterior Chain"),
                Exercise("Double-Under",    "3", "50",  5,  "Rope passes twice per jump, stay relaxed in shoulders.",                        "Cardio & Coordination"),
                Exercise("Wall Ball",       "4", "15",  6,  "Squat to parallel, explode up throwing ball to target.",                        "Full Body"),
                Exercise("AMRAP Finisher",  "1", "7 min",7, "Max rounds: 5 pull-ups, 10 push-ups, 15 squats.",                              "Full Body")
            )
        ),

        "Cyclist" to WorkoutPlan(
            category      = "Cyclist",
            tagline       = "Stronger legs & better endurance",
            totalMinutes  = 55,
            level         = "Intermediate",
            icon          = Icons.Default.DirectionsBike,
            exercises     = listOf(
                Exercise("Warm-Up Spin",    "1", "10 min",10,"Easy cadence 70–80 rpm, gradually increase heart rate.",                       "Legs & Cardio"),
                Exercise("Leg Press",       "4", "12",   6, "Full range of motion, don't lock knees at top.",                               "Quads & Glutes"),
                Exercise("Single-Leg Squat","3", "10",   5, "Hinge on one leg, keep knee tracking over toe.",                               "Balance & Quads"),
                Exercise("Calf Raise",      "4", "20",   4, "Full extension at top, slow eccentric on the way down.",                       "Calves"),
                Exercise("Hip Flexor Stretch","2","60s hold",4,"Lunge position, press hips forward, hold.",                                 "Hip Flexors"),
                Exercise("Interval Ride",   "6", "3 min",18,"Hard effort at 90% max HR, 2 min easy between sets.",                          "Cardio & Legs"),
                Exercise("Standing Climb",  "3", "5 min",15,"High resistance, stand on pedals, drive through heel.",                        "Glutes & Power"),
                Exercise("Cool-Down Roll",  "1", "5 min",5, "Easy spin + foam roll quads, ITB, and calves.",                               "Recovery")
            )
        ),

        "Glutes" to WorkoutPlan(
            category      = "Glutes",
            tagline       = "Build & sculpt your posterior",
            totalMinutes  = 40,
            level         = "Beginner",
            icon         = Icons.Default.SelfImprovement,
            exercises     = listOf(
                Exercise("Hip Thrust",       "4", "12",  6, "Upper back on bench, barbell across hips, drive up and squeeze.",               "Glutes"),
                Exercise("Romanian Deadlift","4", "10",  5, "Soft knee bend, hinge hips back, feel hamstring stretch, drive hips forward.",  "Glutes & Hamstrings"),
                Exercise("Sumo Squat",       "3", "15",  5, "Wide stance toes out, sit between heels, squeeze at top.",                     "Inner Thighs & Glutes"),
                Exercise("Cable Kickback",   "3", "15",  4, "Keep hips square, extend leg straight back, squeeze glute.",                   "Glutes"),
                Exercise("Lateral Band Walk","3", "20",  4, "Band above knees, stay low, step wide keeping tension.",                       "Glute Med & Hips"),
                Exercise("Glute Bridge",     "3", "20",  4, "Feet hip-width, press hips to ceiling, hold 2 s at top.",                     "Glutes"),
                Exercise("Donkey Kick",      "3", "15",  4, "On all fours, kick heel to ceiling, keep core braced.",                       "Glutes"),
                Exercise("Stretch",          "1", "5 min",5,"Pigeon pose and figure-four stretch each side.",                              "Hips & Glutes")
            )
        ),

        "Power" to WorkoutPlan(
            category      = "Power",
            tagline       = "Explosive strength & athletic performance",
            totalMinutes  = 50,
            level         = "Advanced",
            icon         = Icons.Default.FitnessCenter,
            exercises     = listOf(
                Exercise("Power Clean",      "5", "3",   8, "Bar from floor to front rack in one explosive pull — focus on timing.",         "Full Body"),
                Exercise("Hang Snatch",      "4", "3",   7, "Explosive hip drive, pull bar overhead, catch in squat.",                      "Full Body & Shoulders"),
                Exercise("Plyometric Push-Up","4","8",   5, "Explosive push off floor, clap at top, controlled landing.",                   "Chest & Power"),
                Exercise("Broad Jump",       "4", "6",   5, "Max horizontal jump, soft landing, reset and repeat.",                        "Legs & Power"),
                Exercise("Medicine Ball Slam","4","10",  5, "Raise ball overhead, slam to floor with full body force.",                     "Core & Power"),
                Exercise("Trap Bar Deadlift", "5","5",   7, "Neutral grip, explosive drive from floor, tall finish.",                       "Posterior Chain"),
                Exercise("Sprint 40m",        "6","40m", 6, "Block start, max acceleration, walk back full recovery.",                      "Speed & Power"),
                Exercise("Core Finisher",     "3","15",  5, "Cable woodchop — rotate explosively, brace on return.",                       "Core & Power")
            )
        )
    )

    fun getPlan(category: String): WorkoutPlan? = plans[category]
}
