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


@Composable
fun SignUpScreen(navController: NavController, paddingValues: PaddingValues) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }

    val context = LocalContext.current


    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.gymtrainer))
    val progress by animateLottieCompositionAsState(
        isPlaying = true,
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.7f
    )


    Column (
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        horizontalAlignment = Alignment.CenterHorizontally
    ){
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

        Spacer(modifier = Modifier.height(5.dp))
        Button(onClick = {
            nameError = if (name.isBlank()) "Name is required!!" else ""
            emailError =  if (email.isBlank()) "Email is required!!"
            else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) "Enter a valid email address!!" else ""
            passwordError =  if (password.isBlank()) "Password is required!!" else ""
            confirmPasswordError = if (confirmPassword.isBlank()) "Confirm password is required!!"
            else (if (password != confirmPassword) "Passowrd and confirm password do not match!!" else "")
                if (nameError.isEmpty() && emailError.isEmpty() && passwordError.isEmpty() && confirmPasswordError.isEmpty()) {
                    // sign up logic after clicking register button
                    val auth = Firebase.auth
                    val usersDB = FirebaseFirestore.getInstance()

                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
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
                                            navController.navigate("login") {
                                                popUpTo("signup") { inclusive = true}
                                            }
                                        }
                                        .addOnFailureListener {
                                            Toast.makeText(context, "User Created but failed to save details", Toast.LENGTH_SHORT).show()
                                        }
                                }

                            }
                            else {

                                Toast.makeText(context, "Sign Up Failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()

                            }
                        }

                }
            
        }) {
            Text("Register")
        }
        Spacer(modifier = Modifier.height(16.dp))

        TextButton(onClick = {
            navController.navigate("login")
        }) {
            Text("Already have an account? Login")
        }
    }

}