package com.example.bodyfit.view

import android.content.Context
import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.example.bodyfit.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

/**
 * New-user registration screen.
 * Collects:
 *  - Display name
 *  - Email address
 *  - Password
 *  - Confirm password
* On successful registration:
*  1. Firebase Auth creates the account via CreateUserWithEmailAndPassword
*  2. A Firestore document is written at `users/{uid}` with the user's name,
*     email, and UID
*  3. The user is navigated back to Login with a toast instructing them to log in
* Error handling:
*  - All four fields show inline red error labels when blank or invalid
*  - Firebase errors for example email already in use, surface as Toast messages
**/
@Composable
fun SignUpScreen(navController: NavController, paddingValues: PaddingValues) {
    // Form state at the beginning
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    // controls the visibility of password characters
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    // inline validation error messages
    var nameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val context = LocalContext.current

    // Lottie animation to load the gym trainer animation
    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gymtrainer))
    val progress by animateLottieCompositionAsState(
        isPlaying = true,
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.7f
    )

    // Registration UI definition
    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
        // Animated gym trainer display
        LottieAnimation(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            composition = composition,
            progress = {progress}
        )

        Text(text = "Create Account",
            modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 15.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp
        )
        Spacer(modifier = Modifier.height(5.dp))
        Text(text = "Please enter your details",
            fontSize = 24.sp,
            modifier = Modifier.fillMaxWidth().padding(start = 25.dp, top = 15.dp),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Light)

        Spacer(modifier = Modifier.height(5.dp))
        // Name input field that becomes error field if empty
        TextField(value = name, onValueChange = {
            name =it
        }, label = {
            Text( nameError.ifEmpty{"Name"}, color = if (nameError.isNotEmpty()) Red else Unspecified)},
            leadingIcon = {
                Icon( Icons.Rounded.Person,
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

        Spacer(modifier = Modifier.height(5.dp))
        // Email field that becomes error field if empty
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
        Spacer(modifier = Modifier.height(5.dp))
        // Password field that has visibility toggle
        TextField(value = password, onValueChange = {
            password = it
        }, label = {
            Text( passwordError.ifEmpty { "Password" }, color = if(passwordError.isNotEmpty()) Red else Unspecified)
        },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = "")
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible)
                    painterResource(id= R.drawable.visibility_24)
                else painterResource(id= R.drawable.visibility_off_24)

                Icon(
                    painter = image,
                    contentDescription = "",
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
        Spacer(modifier = Modifier.height(5.dp))
        // Confirm password field. Validates that the characters in password and confirm password fileds match
        TextField(value = confirmPassword, onValueChange = {
            confirmPassword = it
        }, label = {
            Text( confirmPasswordError.ifEmpty { "Confirm Password" }, color = if(confirmPasswordError.isNotEmpty()) Red else Unspecified)
        },
            leadingIcon = {
                Icon(Icons.Rounded.Lock, contentDescription = "")
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (confirmPasswordVisible)
                    painterResource(id= R.drawable.visibility_24)
                else painterResource(id= R.drawable.visibility_off_24)

                Icon(
                    painter = image,
                    contentDescription = "",
                    modifier = Modifier
                        .clickable{confirmPasswordVisible = !confirmPasswordVisible}
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
        // Register button that validates all the input fields and then calls the firebase auth for user creation
        Spacer(modifier = Modifier.height(5.dp))
        Button(onClick = {
            // validation of nameError, EmailError, PasswordError, and ConfirmPasswordError
            nameError = if (name.isBlank()) "Name is required!!" else ""
            emailError =  if (email.isBlank()) "Email is required!!"
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Enter a valid email address!!" else ""
            passwordError =  if (password.isBlank()) "Password is required!!" else ""
            confirmPasswordError = if (confirmPassword.isBlank()) "Confirm password is required!!"
            else (if (password != confirmPassword) "Passowrd and confirm password do not match!!" else "")
                // only proceed with registration if all fields pass registration hence use of &, and symbol
                if (nameError.isEmpty() && emailError.isEmpty() && passwordError.isEmpty() && confirmPasswordError.isEmpty()) {
                    // sign up logic after clicking register button is executed as follows
                    val auth = Firebase.auth
                    val usersDB = FirebaseFirestore.getInstance()
                    // create the firebase auth account first
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // after creation, persist use info into in firestore with document ID as user id for easy retrieval
                                val userID = auth.currentUser?.uid
                                val userData = hashMapOf(
                                    "uid" to userID,
                                    "name" to name,
                                    "email" to email
                                )
                                userID?.let {
                                    usersDB.collection("users")
                                        .document(it)
                                        .set(userData)
                                        .addOnSuccessListener {
                                            Toast.makeText(context, "Sign Up is Successful, Please Login", Toast.LENGTH_SHORT).show()
                                            // Return to login screen and remove register screen from backstack
                                            navController.navigate("login") {
                                                popUpTo("signup") { inclusive = true}
                                            }
                                        }
                                        // auth succeeded but firestore write failed, use can still login but the data will be incomplete
                                        .addOnFailureListener {
                                            Toast.makeText(context, "User Created but failed to save details", Toast.LENGTH_SHORT).show()
                                        }
                                }

                            }
                            // show the failure message if firebase as rejected the registration
                            else {

                                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                            }
                        }

                }
            
        }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))
        // Link back to users who have the an account registered
        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text("Already have an account? Login")
        }
    }

}