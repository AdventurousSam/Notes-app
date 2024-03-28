package com.sam.notes

import android.widget.Toast
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.SpringSpec
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.sam.notes.ui.NotesViewModel

@Composable
fun LoginScreen(auth: FirebaseAuth, onLogin: () -> Unit, onLogout: () -> Unit) {
    when {
        auth.currentUser?.isEmailVerified == true -> onLogin()
        auth.currentUser != null -> LoggedIn(auth.currentUser!!, onLogout)
        else -> StartLoginScreen(onLogin)
    }
}

@Composable
fun LoggedIn(user: FirebaseUser, onLogout: () -> Unit) {
    var sent by remember { mutableStateOf(false) }
    val email = user.email?.substringBefore("@")
    var message by remember {mutableStateOf("You are logged in as $email. Verify your email to continue!")}
    Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
    ) {
        Text(text = message, modifier = Modifier.animateContentSize(SpringSpec(Spring.DampingRatioNoBouncy, Spring.StiffnessLow)))
        Button(onClick = {
                    user.sendEmailVerification()
                    message = "Verification Email sent! Verify and re-login to continue."
                    sent = true
                }, enabled = !sent) {
            Text(text = "Send Verification Email")
        }
        Button(onClick = {
            signOut()
            onLogout()
        }) {
            Text("Logout")
        }
    }
}

@Composable
fun StartLoginScreen(onLogin: () -> Unit) {
    val viewModel = NotesViewModel()
    var action by rememberSaveable { mutableStateOf(0) }
    Column (
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,

            ) {
        when (action) {
            0 -> {
                Text(
                        text = "Welcome to Sam's Notes App",
                        fontSize = 40.sp,
                        textAlign = TextAlign.Center,
                        lineHeight = 45.sp
                )
                Button(
                        onClick = {action = 1},
                        modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                            text = "Login",
                            fontSize = 20.sp
                    )
                }
                Button(
                        onClick = {action = 2},
                        modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                            text = "Signup",
                            fontSize = 20.sp
                    )
                }
            }
            1 -> {
                EmailAndPasswordInput(0, viewModel, onLogin)
                OutlinedButton(onClick = {action = 0}, modifier = Modifier.padding(12.dp)) { Text("Back") }
            }
            2 -> {
                EmailAndPasswordInput(1, viewModel, onLogin)
                OutlinedButton(onClick = {action = 0}, modifier = Modifier.padding(12.dp)) { Text("Back") }
            }
            else -> {
                action = 0
            }
        }
    }
}

@Composable
fun EmailAndPasswordInput(type: Int, viewModel: NotesViewModel, onLogin: () -> Unit) {
    val text = if (type == 0) { "Login" } else "Signup"
    Text(
            text = text,
            fontSize = 40.sp,
            textAlign = TextAlign.Center,
            lineHeight = 45.sp,
            modifier = Modifier.padding(16.dp)
    )
    OutlinedTextField(
            label = { Text(text = "Email") },
            singleLine = true,
            value = viewModel.email,
            onValueChange = { viewModel.updateEmail(it) },
            modifier = Modifier.padding(12.dp)
    )
    OutlinedTextField(
            label = { Text(text = "Password") },
            singleLine = true,
            visualTransformation = if (viewModel.viewPass) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { PasswordIcon(viewModel) },
            value = viewModel.pass,
            onValueChange = { viewModel.updatePass(it) },
            modifier = Modifier.padding(8.dp)
    )
    if (type == 1) {
        OutlinedTextField(
                label = { Text(text = "ConfirmPassword") },
                singleLine = true,
                value = viewModel.confirmPass,
                visualTransformation = if (viewModel.viewPass) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = { PasswordIcon(viewModel) },
                onValueChange = { viewModel.updateConfirmPass(it) },
                isError = viewModel.passMismatch,
                modifier = Modifier.padding(8.dp)

        )
        if (viewModel.passMismatch) {
            Text(
                    text = "Confirm Password does not match!",
                    fontSize = 12.sp,
                    textAlign = TextAlign.Left

            )
        }
    }
    SignUpButton(viewModel, onLogin, text)
}

@Composable
fun PasswordIcon(viewModel: NotesViewModel) {
    val image = if (viewModel.viewPass) Icons.Filled.Visibility
    else Icons.Filled.VisibilityOff
    val description = if (viewModel.viewPass) "Hide password" else "Show password"

    IconButton(onClick = {viewModel.viewPass = !viewModel.viewPass}){
        Icon(imageVector  = image, description)
    }
}

@Composable
fun SignUpButton(viewModel: NotesViewModel, onLogin: () -> Unit, signOrLog: String) {
    val context = LocalContext.current
    Button (
            onClick = {
                if (signOrLog == "Signup") {
                    viewModel.signUp { authResult, exp ->
                    val toastText = when (authResult) {
                        Authentication.ConfirmPasswordMissMatch -> "Password and Confirm password must match!"
                        Authentication.InvalidEmail -> "Enter a valid student email"
                        Authentication.InvalidPass -> "Password can't be blank"
                        Authentication.Failed -> "Create Account Failed! $exp"
                        Authentication.Successful -> "Account Created!"
                    }
                    Toast.makeText(context, toastText, Toast.LENGTH_LONG).show()
                    if (authResult == Authentication.Successful) onLogin()
                    }

                } else {
                    viewModel.login { authResult, exp ->
                        if (authResult == Authentication.Successful) onLogin()
                        else {
                            Toast.makeText(
                                    context,
                                    exp,
                                    Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            },
            enabled = !viewModel.passMismatch || signOrLog=="Login",
            modifier = Modifier.padding(12.dp)
    ) {
        Text(text = signOrLog)
    }
}