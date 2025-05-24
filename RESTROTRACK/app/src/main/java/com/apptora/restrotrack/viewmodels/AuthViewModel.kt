package com.apptora.restrotrack.viewmodels

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth

import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase


class AuthViewModel : ViewModel() {
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    private val auth = Firebase.auth
    private val db = Firebase.firestore


    fun signUp(
        firstName: String,
        lastName: String,
        employeeId: String,
        email: String,
        password: String,
        role: String,
        customRole: String?,
        onSuccess: () -> Unit
    ) {
        if (firstName.isBlank() || lastName.isBlank() || employeeId.isBlank() || email.isBlank() || password.isBlank() || role.isBlank()) {
            errorMessage = "All fields are required"
            return
        }

        isLoading = true
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    val userId = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val userData = mapOf(
                        "firstName" to firstName,
                        "lastName" to lastName,
                        "employeeId" to employeeId,
                        "email" to email,
                        "role" to role,
                        "customRole" to (customRole ?: "")
                    )
                    db.collection("users").document(userId).set(userData)
                        .addOnSuccessListener { onSuccess() }
                        .addOnFailureListener { errorMessage = it.localizedMessage }
                } else {
                    errorMessage = task.exception?.localizedMessage
                }
            }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        if (email.isBlank() || password.isBlank()) {
            errorMessage = "Please enter both email and password"
            return
        }

        isLoading = true
        errorMessage = null

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                isLoading = false
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    errorMessage = task.exception?.localizedMessage ?: "Login failed"
                }
            }
    }
}