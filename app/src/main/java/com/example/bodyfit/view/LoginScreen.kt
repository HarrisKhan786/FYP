package com.example.bodyfit.view

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.Transparent
import androidx.compose.ui.graphics.Color.Companion.Unspecified
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.bodyfit.R
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.Firebase
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.res.stringResource

// handles all the user authentication entry points
// leads to successful login, or signup
@Composable
fun LoginScreen(navController: NavController, paddingValues: PaddingValues){

    // login form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    //inline validation error messages
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Load the running animation using lottie animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.running))
    val progress by animateLottieCompositionAsState(
        isPlaying = true,
        composition = composition,
        iterations = LottieConstants.IterateForever, // loop forever
        speed = 0.7f // slightly slower than the normal speed
    )

    // Launcher to handle results from Google Sign-In screen
    // contains the results code where 10 or 0 shows SHA-1 mismatch
    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        Log.d("GOOGLE_AUTH", "Result code: ${result.resultCode}")
        Log.d("GOOGLE_AUTH", "Data: ${result.data}")
        Log.d("GOOGLE_AUTH", "Data extras: ${result.data?.extras}")
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val credential = GoogleAuthProvider.getCredential(account.idToken, null)
                // hand the firebase credentials to the sign in handler
                handleFirebaseSignIn(credential, context, navController)
            } catch (e: ApiException) {
                Log.e("GOOGLE_AUTH", "Status Code: ${e.statusCode}")
                Toast.makeText(context, "Error code: ${e.statusCode}", Toast.LENGTH_LONG).show()
            }

        } else {
            // try to extract the exception so that we can handle the exact error code for debug
            try {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                task.getResult(ApiException::class.java)
            } catch (e: ApiException) {
                Log.e("GOOGLE_AUTH", "Actual error code: ${e.statusCode}, message: ${e.message}")
                Toast.makeText(context, "Error ${e.statusCode}: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    // The Login UI starts here with a column

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // loading of the lottie animation shown at the top of the screen
        LottieAnimation(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            composition = composition,
            progress = {progress}
        )

        // header text - salutation

        Text( text = "Welcome Back, we missed you!")

        Spacer(modifier = Modifier.height(5.dp))
        // Title that tells the user we are in login page
        Text(text = "Login", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(10.dp))
        // email field that doubles as inline error fiel
        TextField(value = email, onValueChange = {
            email =it
        }, label = {
            Text( emailError.ifEmpty{"Email"}, color = if (emailError.isNotEmpty()) Red else Unspecified)},
            leadingIcon = {
                Icon( Icons.Rounded.AccountCircle,
                    contentDescription = "")
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 20.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Transparent,
                unfocusedIndicatorColor = Transparent
            )
            )

        Spacer(modifier = Modifier.height(8.dp))
        // password field with visibility toggle icon - eye
        TextField(value = password, onValueChange = {
            password = it
        }, label = {
            Text( passwordError.ifEmpty { "Password" }, color = if(passwordError.isNotEmpty()) Red else Unspecified)
        },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = "")
            },
            //handling click to show or hide password
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            // eye icon for password visibility toggle
            trailingIcon = {
                val image = if (passwordVisible)
                    painterResource(id= R.drawable.visibility_24)
                else painterResource(id= R.drawable.visibility_off_24)

                Icon(
                    painter = image,
                    contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    modifier = Modifier
                        .clickable{passwordVisible = !passwordVisible}
                )
            },
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp, horizontal = 20.dp),
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Transparent,
                unfocusedIndicatorColor = Transparent
            )
            )

        Spacer(modifier = Modifier.height(10.dp))
        // Login button that validates the user inputs and then calls SignInUserWithEmailAndPassword
        Button(onClick = {
            // validating the email and password fields before making a network call
            emailError = if (email.isBlank()) "Email is required!" else ""
            passwordError = if (password.isBlank()) "Password is required!" else ""
            if (emailError.isEmpty() && passwordError.isEmpty()) {
             // Login logic for firebase authentication
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Sign in Successful, Welcome Back", Toast.LENGTH_SHORT).show()
                            // navigate to dashboard and clear the backstack so that navigating back does not take you to login but exits the app
                            navController.navigate("dashboard"){
                                popUpTo("login") { inclusive = true}
                            }
                        }
                        else {

                            Toast.makeText(context, "Sign In Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                        }
                    }
            }
        },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 90.dp)
        ){
            Text(text = "Login")
        }

        Spacer(modifier = Modifier.height(10.dp))
        // forgot password field that requires the email to be filled in so that the firebase knows which email address to send reset code to.
        Text(text = "forgot password?",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable{
               //Forgot password logic is handled here
                if (email.isBlank()) {
                    emailError = "Enter Your Email to Reset Password"
                    Toast.makeText(context, "Please enter your email address in the above Field", Toast.LENGTH_SHORT).show()
                } else {
                    Firebase.auth.sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful){
                                Toast.makeText(context, "Reset Link Sent to $email", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(context, "Failed : ${task.exception?.message}",
                                    Toast.LENGTH_LONG).show()
                            }
                        }
                }
            })

        Spacer(modifier = Modifier.height(10.dp))
        // Navigation link to take unregistered users to register screen for registration
        Row {
            Text(text = "Not a Member?")
            Text(text = "Sign Up",
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                .clickable{
            // Registration logic -  go to registration screen upon click
                navController.navigate("register")
             })
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "or")
        val context = LocalContext.current
        val scope = rememberCoroutineScope()

        // google sign in button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .padding(horizontal = 16.dp)
                    .clickable {
                        // Trigger Google Sign-In Logic here -  build GoogleSignInOptions requesting server ID token and email
                        val gso = GoogleSignInOptions
                            .Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()

                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        // Sign out first so the account picker always appears, Without this, returning users are re-authenticated silently with their last account and never see the picker again.
                        googleSignInClient.signOut().addOnCompleteListener{
                            googleSignInLauncher.launch(googleSignInClient.signInIntent)

                        }
                    },
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                color = MaterialTheme.colorScheme.surface
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.google),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Sign in with Google",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }


        }

    }

}
// Complete a firebase sign in after credentials have been validated
private fun handleFirebaseSignIn(credential: AuthCredential, context: Context, navController: NavController) {
    val auth = Firebase.auth
    val db = FirebaseFirestore.getInstance()

    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                // use the user returned from the task result
                task.result?.user?.let { user ->
                    val isNewUser = task.result?.additionalUserInfo?.isNewUser ?: false

                    if (isNewUser) {
                        // push user details to Firestore for google sign for new users
                        // The documentID is the user UID helping for future look ups
                        val userData = hashMapOf(
                            "uid" to user.uid,
                            "name" to (user.displayName ?: "User"),
                            "email" to user.email,
                            "photoUrl" to user.photoUrl.toString(),
                            "createdAt" to FieldValue.serverTimestamp()
                        )

                        db.collection("users")
                            .document(user.uid)
                            .set(userData)
                            .addOnSuccessListener {
                                // Navigate Dashboard
                                Toast.makeText(
                                    context,
                                    "Welcome, ${user.displayName}!",
                                    Toast.LENGTH_SHORT
                                ).show()
                                navController.navigate("dashboard") {
                                    popUpTo("login") { inclusive = true }
                                }

                            }
                            .addOnFailureListener { e ->
                                Log.e("FIRESTORE", "Failed to save user: ${e.message}")
                                Toast.makeText(
                                    context,
                                    "Failed to save profile: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                    } else {
                        // Existing user - Navigate to Dashboard, no firestore database write needed
                        Toast.makeText(
                            context,
                            "Welcome back, ${user.displayName}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        navController.navigate("dashboard") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                } ?: Toast.makeText(
                    context,
                    "Authentication failed: No user found",
                    Toast.LENGTH_SHORT
                ).show()
            }
            else {
                // Firebase rejected the credential — log and show the error
                Log.e("FIREBASE_AUTH", "signInWithCredential failed: ${task.exception?.message}")
                Toast.makeText(context, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
}