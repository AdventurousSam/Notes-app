package com.sam.notes.ui

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.sam.notes.Authentication
import com.sam.notes.createUser
import com.sam.notes.signIn

class NotesViewModel: ViewModel() {
    var email by mutableStateOf("")
        private set
    var pass by mutableStateOf("")
        private set
    var viewPass by mutableStateOf(false)
    var confirmPass by mutableStateOf("")
        private set
    var passMismatch by mutableStateOf(false)

    fun updateEmail(input: String) {
        email = input
    }
    fun updatePass(input: String) {
        pass = input
        passMismatch = pass != confirmPass
    }
    fun updateConfirmPass(input: String) {
        confirmPass = input
        passMismatch = pass != confirmPass
    }

    fun login(callback: (Authentication) -> Unit) {
        signIn(email, pass) {
            callback(it)
        }
    }

    fun signUp(): Authentication {
        var authResult = when {
            passMismatch -> Authentication.ConfirmPasswordMissMatch
            !email.endsWith("@students.iitmandi.ac.in") -> Authentication.InvalidEmail
            pass.isEmpty() -> Authentication.InvalidPass
            else -> Authentication.Successful
        }
        if (authResult == Authentication.Successful) {
            createUser(email, pass).addOnCompleteListener { task ->
                authResult = if (task.isSuccessful) {
                    Log.d("Sign-Up", "Success")
                    Authentication.Successful
                } else {
                    Log.w("Sign-Up", task.exception)
                    Authentication.Failed
                }
            }
        }
        return authResult
    }


}