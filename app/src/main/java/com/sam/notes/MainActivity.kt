package com.sam.notes

import android.content.Context
import android.os.Bundle
import android.view.WindowManager.LayoutParams.FLAG_SECURE
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.compose.AppTheme
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase

private lateinit var auth: FirebaseAuth
private lateinit var database: DatabaseReference

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = Firebase.auth
        database = Firebase.database.reference
        window.setFlags(FLAG_SECURE, FLAG_SECURE)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}


@Composable
fun AppScreen(
    navController: NavHostController = rememberNavController()
) {
    val currentUser = auth.currentUser
    val startDes = if (currentUser != null && currentUser.isEmailVerified) NotesScreen.Main.name
                          else NotesScreen.Login.name
    NavHost(
            navController = navController,
            startDestination = startDes
    ) {
        composable(NotesScreen.Login.name) {
            LoginScreen(auth, onLogin = {
                if (auth.currentUser?.isEmailVerified == true) {
                    navController.navigate(NotesScreen.Main.name)
                } else {
                    navController.navigate(NotesScreen.Login.name)
                }
            }) {
                navController.navigate(NotesScreen.Login.name)
            }
        }
        composable(NotesScreen.Main.name) {
            HomeScreen(
                    auth,
                    onCS304 = { navController.navigate(NotesScreen.CS304.name) },
                    onSignOut = { signOut(); navController.navigate(NotesScreen.Login.name) },
                    onDenied = { auth.signOut(); navController.navigate(NotesScreen.Login.name) }
            )
        }
        composable(NotesScreen.CS304.name) {
            CS304notes (auth) {
                navController.navigate("${NotesScreen.PdfView.name}/$it")
            }
        }
        composable(NotesScreen.PdfView.name+"/{fileName}") {
            val fileName = it.arguments?.getString("fileName")
            if (fileName == null) navController.navigate(NotesScreen.Login.name)
            else PDFViewerScreen({ navController.navigateUp() }, context = LocalContext.current, fileName)
        }
    }
}

fun signIn(email: String, pass: String, callback: (Authentication) -> Unit) {
    auth.signInWithEmailAndPassword(email, pass)
        .addOnCompleteListener {task ->
            val authResult = if (task.isSuccessful) {
                Authentication.Successful
            } else Authentication.Failed
            callback(authResult)
        }
}

fun createUser(email: String, pass: String): Task<AuthResult> {
    return auth.createUserWithEmailAndPassword(email, pass)
}

fun signOut() {
    val userRef = database.child(auth.currentUser?.email?.substringBefore("@")?:"sam")
    userRef.removeValue()
    auth.signOut()
}

fun checkUserLogin(email: String, context: Context, callback: (Boolean) -> Unit) {
    val deviceID = getDeviceId(context)
    val user = email.substringBefore("@")
    val userRef = database.child(user)
    userRef.get().addOnSuccessListener {
        if (it.value == null) {
            userRef.setValue(deviceID)
            callback(true)
        }
        else callback(it.value == deviceID)
    }.addOnFailureListener {callback(false)}
}