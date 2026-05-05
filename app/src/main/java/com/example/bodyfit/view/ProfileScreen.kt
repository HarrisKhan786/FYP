package com.example.bodyfit.view

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.storage.storage
// The user profile and settings hub with header, settings, dialog for editing, and navigation wiring.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController, // used to navigate to notifications
    onLogout: () -> Unit = {} // clears backstack on logout
) {
    // controls visibility of the edit profile dialog
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profile & Settings") },
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
                .padding(8.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // contains profile picture, name, email, and quick edit button
            ProfileHeader(onEditClick = { showEditDialog = true })
            // template setting rows which are clickable
            SettingsSection( onNotificationsClick = { navController.navigate(Screen.Notifications.route)
            }, onEditProfileClick = { showEditDialog = true })

            LogoutButton(onLogout = onLogout)
        }
        // shows the edit dialogue as overlay when requested
        if (showEditDialog) {
            EditProfileDialog(onDismiss = { showEditDialog = false })
        }
    }
}

// Dialogue for editing the profile, edit display name and optionally password
@Composable
fun EditProfileDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current

    val auth = Firebase.auth
    val db = FirebaseFirestore. getInstance()
    val uid = auth.currentUser?.uid ?: return
    // check if the account uses email or google signin
    val isEmailProvider = auth.currentUser?.providerData
        ?.any { it.providerId == EmailAuthProvider.PROVIDER_ID } == true
    // initial form state
    var name            by remember { mutableStateOf("") }
    var newPassword     by remember { mutableStateOf("") }
    var currentPassword by remember { mutableStateOf("") }
    var nameError       by remember { mutableStateOf("") }
    var pwError         by remember { mutableStateOf("") }
    var isSaving        by remember { mutableStateOf(false) } // button is disabled while saving
    // name is prefilled from firebase to ensure use know stheir current value
    LaunchedEffect(Unit) {
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc -> name = doc.getString("name") ?: "" }
    }

    AlertDialog(
        onDismissRequest = {if(isSaving) onDismiss()}, // block dismisses while saving details
        title = {Text("Edit Profile", fontWeight = FontWeight.Bold)},
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // name field is always visible
                OutlinedTextField(
                    value = name,
                    onValueChange = {name = it; nameError = ""},
                    label = {Text("Display Name")},
                    leadingIcon = { Icon(Icons.Default.Person, contentDescription = null) },
                    isError = nameError.isNotEmpty(),
                    supportingText = if (nameError.isNotEmpty()) {{ Text(nameError) }} else null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                // password section only visible for email auth users
                if (isEmailProvider) {
                    HorizontalDivider()
                    Text(
                        text = "Change Password (Optional)",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    // current passowrd need for reauthentication before update
                    OutlinedTextField(
                        value         = currentPassword,
                        onValueChange = { currentPassword = it; pwError = "" },
                        label         = { Text("Current Password") },
                        leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError       = pwError.isNotEmpty(),
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value         = newPassword,
                        onValueChange = { newPassword = it; pwError = "" },
                        label         = { Text("New Password") },
                        leadingIcon   = { Icon(Icons.Default.Lock, contentDescription = null) },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        isError       = pwError.isNotEmpty(),
                        supportingText = if (pwError.isNotEmpty()) {{ Text(pwError) }} else null,
                        singleLine    = true,
                        modifier      = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    nameError = if (name.isBlank()) "Name cannot be blank" else ""
                    if (isEmailProvider && newPassword.isNotBlank()){
                        if (currentPassword.isBlank()){
                            pwError = "Enter your current password"
                            return@TextButton
                        }
                        if (newPassword.length < 6) {
                            pwError = "Password must be at least 6 characters"
                            return@TextButton
                        }
                    }
                    // validate name
                    if (nameError.isNotEmpty()) return@TextButton

                    isSaving = true

                    //save data to the database, updated display name
                    db.collection("users").document(uid)
                        .update("name", name.trim())
                        .addOnSuccessListener {
                            // change password if the user provided one during registration
                            if (isEmailProvider && newPassword.isNotBlank() && currentPassword.isNotBlank()) {
                                val email = auth.currentUser?.email ?: ""
                                val credential = EmailAuthProvider.getCredential(email, currentPassword)
                                // reauthenticate the user to ensure that they know their current password
                                auth.currentUser?.reauthenticate(credential)
                                    ?.addOnSuccessListener {
                                        auth.currentUser?.updatePassword(newPassword)
                                            ?.addOnSuccessListener {
                                                isSaving = false
                                                Toast.makeText(context, "Profile & password updated!", Toast.LENGTH_SHORT).show()
                                                onDismiss()
                                            }
                                            ?.addOnFailureListener { e ->
                                                isSaving = false
                                                pwError = e.message ?: "Failed to update password"
                                            }
                                    }
                                    ?.addOnFailureListener {
                                        isSaving = false
                                        pwError = "Current password is incorrect"
                                    }
                                // name only update
                            } else {
                                isSaving = false
                                Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                                onDismiss()
                            }
                        }

                        .addOnFailureListener { e ->
                            isSaving = false
                            Toast.makeText(context, "Profile updated!", Toast.LENGTH_SHORT).show()
                        }

                },
                enabled = !isSaving
            ) {
                // show a spinner inside button while saving
                if (isSaving) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                } else {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!isSaving) onDismiss() }) { Text("Cancel") }
        }
    )
}
// full width button that handles firebase.auth.signout()
@Composable
fun LogoutButton(onLogout: () -> Unit) {
    Button(
        onClick = onLogout,
        modifier = Modifier.fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.error

        )

    ) {
        Text(
            text = "Log Out",
            color = MaterialTheme.colorScheme.onError
        )
    }
}

// card containing the three settings items which are wired to their real actions
@Composable
fun SettingsSection(onEditProfileClick: () -> Unit = {}, onNotificationsClick: () -> Unit = {}) {
    val context = LocalContext.current

    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column {

            SettingsItem(
                icon = Icons.Default.Notifications,
                title = "Notifications",
                subtitle = "Manage workout reminders",
                onClick = onNotificationsClick
            )
            // dark mode which displays toast since the app follows system theme
            Divider()
            SettingsItem(
                icon = Icons.Default.DarkMode,
                title = "Dark Mode",
                subtitle = "Follow system theme",
                onClick = {
                    Toast.makeText(context, "The app follows the system defaults", Toast.LENGTH_LONG).show()
                }
            )

            Divider()

            SettingsItem(
                icon = Icons.Default.Edit,
                title = "Edit Profile",
                subtitle = "Update name and password",
                onClick = onEditProfileClick
            )
        }
    }
}

// settings items, single row in settings section card
// Displays an icon, title, optional subtitle and a chevron on the right
@Composable
fun SettingsItem(icon: ImageVector, title: String, subtitle: String? = null,  onClick: () -> Unit = {}) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick()}
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge)
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.weight(1f))
        // chevron icon indicates that the item is clickable
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}


//A reusable profile header or toolbar, user avatar, email, name, and edit profile
@Composable
fun ProfileHeader(onEditClick: () -> Unit = {}) {
    val context = LocalContext.current

    var userName  by remember { mutableStateOf("") }
    var userEmail by remember { mutableStateOf("") }
    var photoUrl  by remember { mutableStateOf<String?>(null) }
    var uploading by remember { mutableStateOf(false) }
    // load user data from firestore on first composition
    LaunchedEffect(Unit) {
        val uid = Firebase.auth.currentUser?.uid ?: return@LaunchedEffect
        userEmail = Firebase.auth.currentUser?.email ?: ""
        FirebaseFirestore.getInstance()
            .collection("users").document(uid)
            .get()
            .addOnSuccessListener { doc ->
                userName = doc.getString("name") ?: "User"
                photoUrl = doc.getString("photoUrl")
            }
    }
    // image picker for user to select image from the gallery
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri ?: return@rememberLauncherForActivityResult
        val uid = Firebase.auth.currentUser?.uid ?: return@rememberLauncherForActivityResult
        uploading = true
        uploadProfileImage(
            uri = uri,
            uid = uid,
            onSuccess = { downloadUrl ->
                photoUrl  = downloadUrl // updates the UI immediately without having to load from firestore
                uploading = false
                Toast.makeText(context, "Profile photo updated!", Toast.LENGTH_SHORT).show()
            },
            onFailure = { e ->
                uploading = false
                Toast.makeText(context, "Upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // avatar box which when tapped launches the image picker
        Box(
            modifier = Modifier
                .size(90.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable{launcher.launch("image/*")},
            contentAlignment = Alignment.Center
        ) {
            if (photoUrl != null) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(photoUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Profile Picture",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            } else {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Profile Picture",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(48.dp)
                )
            }
            // spinner shown on the avatar is the image uploading is in progress
            if (uploading) {
                CircularProgressIndicator(modifier = Modifier.size(30.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = userName.ifEmpty { "Loading..." },
            style = MaterialTheme.typography.titleMedium
        )

        Text(
            text = userEmail.ifEmpty { "Loading..." },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))
        // Quick access edit button for profile editting
        FilledTonalButton(onClick = onEditClick) {
            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Edit Profile")
        }

    }
}

// Uploads a profile image to Firebase Storage and updates the user's Firestore document
private fun uploadProfileImage(
    uri: Uri,
    uid: String,
    onSuccess: (String) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storageRef = Firebase.storage.reference
        .child("profile_images/$uid.jpg")

    storageRef.putFile(uri)
    // If the upload itself failed, re-throw so addOnFailureListener catches it
        .continueWithTask { task ->
            if (!task.isSuccessful) task.exception?.let { throw it }
            storageRef.downloadUrl
        }
        .addOnSuccessListener { downloadUri ->
            // Persist the download URL to Firestore so ProfileHeader and DashboardTopBar, can load it via Coil on subsequent app launches
            val url = downloadUri.toString()
            FirebaseFirestore.getInstance()
                .collection("users").document(uid)
                .update("photoUrl", url)
                .addOnSuccessListener { onSuccess(url) }
                .addOnFailureListener { onFailure(it) }
        }
        .addOnFailureListener { onFailure(it) }
}


