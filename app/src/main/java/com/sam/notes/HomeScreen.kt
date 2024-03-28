package com.sam.notes

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun HomeScreen(auth: FirebaseAuth, onCS304: () -> Unit, onSignOut: () -> Unit, onDenied: () -> Unit) {
    if (auth.currentUser == null) onSignOut()
    val context = LocalContext.current
    checkUserLogin(auth.currentUser?.email?:"", LocalContext.current) {
        if (!it) {
            Toast.makeText(
                    context,
                    "Already Logged in on another device!",
                    Toast.LENGTH_SHORT
            ).show()
            onDenied()
        }
    }
    val name = auth.currentUser?.email?.substringBefore("@") ?: ""
    Column(
            modifier = Modifier,
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceAround
    ){
        Text(
                "Hello $name!",
                Modifier.padding(16.dp),
                fontSize = 24.sp
        )
        Button(onClick = onCS304) {
            Text(text = "CS304", fontSize = 20.sp)
        }
        Button(onClick = onSignOut) {
            Text("Log out", fontSize = 20.sp)
        }
    }
}