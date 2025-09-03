package com.example.yourapp

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class AuthViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val _user = MutableStateFlow(auth.currentUser)
    val user: StateFlow<FirebaseUser?> = _user

    init {
        auth.addAuthStateListener {
            _user.value = it.currentUser
        }
    }

    fun firebaseAuthWithGoogle(account: GoogleSignInAccount, callback: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                val credential = GoogleAuthProvider.getCredential(account.idToken!!, null)
                auth.signInWithCredential(credential).await()
                callback(true, "Login efetuado com sucesso!")
            } catch (e: Exception) {
                callback(false, "Falha na autenticação: ${e.message}")
            }
        }
    }

    fun sendVerificationCode(
        activity: Activity,
        phoneNumber: String,
        callback: (Boolean, String?, String?) -> Unit
    ) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential) { success, error ->
                        if (!success) {
                            callback(false, null, error)
                        }
                    }
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    callback(false, null, e.message)
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    callback(true, verificationId, null)
                }
            })
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    fun verifyCode(
        verificationId: String,
        code: String,
        callback: (Boolean, String?) -> Unit
    ) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        signInWithPhoneAuthCredential(credential, callback)
    }

    private fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
        callback: (Boolean, String?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                auth.signInWithCredential(credential).await()
                callback(true, null)
            } catch (e: Exception) {
                callback(false, e.message)
            }
        }
    }

    fun signOut(googleSignInClient: GoogleSignInClient, callback: () -> Unit) {
        viewModelScope.launch {
            auth.signOut()
            try {
                googleSignInClient.signOut().await()
            } catch (e: Exception) {
                // Ignore errors if the user was not signed in with Google
            }
            callback()
        }
    }
}
