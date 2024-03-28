package com.sam.notes.ui

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

    fun login(callback: (Authentication, String?) -> Unit) {
        signIn(email, pass, callback)
    }

    fun signUp(callback: (Authentication, String?) -> Unit){
        when {
            passMismatch -> callback(Authentication.ConfirmPasswordMissMatch, null)
            !email.endsWith("@students.iitmandi.ac.in") -> callback(Authentication.InvalidEmail, null)
            pass.isEmpty() -> callback(Authentication.InvalidPass, null)
            else -> createUser(email, pass, callback)
        }
    }
}