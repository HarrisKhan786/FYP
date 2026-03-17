package com.example.bodyfit.view

import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
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
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

@Composable
fun LoginScreen(navController: NavController, paddingValues: PaddingValues){

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    val context = LocalContext.current

    val composition by rememberLottieComposition(LottieCompositionSpec.RawRes(R.raw.running))
    val progress by animateLottieCompositionAsState(
        isPlaying = true,
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 0.7f
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(paddingValues),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {

        LottieAnimation(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.CenterHorizontally),
            composition = composition,
            progress = {progress}
        )

        Text( text = "Welcome Back, we missed you!")

        Spacer(modifier = Modifier.height(5.dp))

        Text(text = "Login", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(modifier = Modifier.height(10.dp))

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
                Modifier.size(32.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        Button(onClick = {
            emailError = if (email.isBlank()) "Email is required!" else ""
            passwordError = if (password.isBlank()) "Password is required!" else ""
            if (emailError.isEmpty() && passwordError.isEmpty()) {
//                Handle Login logic here
                Firebase.auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Toast.makeText(context, "Sign in Successful, Welcome Back", Toast.LENGTH_SHORT).show()
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

        Text(text = "forgot password?",
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.clickable{
//                Forgot password logic is handled here
            })

        Spacer(modifier = Modifier.height(10.dp))

        Row {
            Text(text = "Not a Member?")
            Text(text = "sign up", modifier = Modifier.clickable{
            // Registration logic
                navController.navigate("register")
             })
        }
        Spacer(modifier = Modifier.height(10.dp))

        Text(text = "or continue with:")

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Image(painter = painterResource(R.drawable.google), "Sign up with Google", modifier = Modifier
                .size(60.dp)
                .clickable {
//                handle google sign in logic here
                })
            Image(painter = painterResource(R.drawable.facebook), "Sign up with Facebook", modifier = Modifier
                .size(60.dp)
                .clickable {
//                Handle facebook sign in logic here
                } )
            Image(painter = painterResource(R.drawable.x_logo), "Sign up with X", modifier = Modifier
                .size(60.dp)
                .clickable {
//                Handle X sign in logic hare
                })

        }

    }

}