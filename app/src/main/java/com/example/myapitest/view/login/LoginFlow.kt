package com.example.myapitest.view.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.viewmodel.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.common.api.ApiException

@Composable
fun LoginFlow(
    authViewModel: AuthViewModel = viewModel(),
    googleSignInClient: GoogleSignInClient,
    activity: Activity,
    onLoginSuccess: () -> Unit
) {
    val user by authViewModel.user.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(user) {
        if (user != null) {
            onLoginSuccess()
        }
    }

    var currentScreen by remember { mutableStateOf("Selection") }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try {
            val account = task.getResult(ApiException::class.java)!!
            authViewModel.firebaseAuthWithGoogle(account) { success, message ->
                if (success) {
                    onLoginSuccess()
                } else {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        } catch (e: ApiException) {
            // Tratar falha no login com Google
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F4F8)
    ) {
        if (user == null) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else {
                    when (currentScreen) {
                        "Selection" -> SelectionScreen(
                            onGoogleClick = { googleAuthLauncher.launch(googleSignInClient.signInIntent) },
                            onPhoneClick = { currentScreen = "PhoneInput" }
                        )
                        "PhoneInput" -> PhoneInputScreen(
                            phoneNumber = phoneNumber,
                            onPhoneNumberChange = { phoneNumber = it },
                            onSendCodeClick = {
                                isLoading = true
                                authViewModel.sendVerificationCode(activity, "+$phoneNumber") { success, verId, error ->
                                    isLoading = false
                                    if (success && verId != null) {
                                        verificationId = verId
                                        currentScreen = "CodeInput"
                                        Toast.makeText(context, "Código enviado!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Erro: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            },
                            onBack = { currentScreen = "Selection" }
                        )
                        "CodeInput" -> CodeInputScreen(
                            verificationCode = verificationCode,
                            onVerificationCodeChange = { verificationCode = it },
                            onVerifyClick = {
                                if (verificationId != null) {
                                    isLoading = true
                                    authViewModel.verifyCode(verificationId!!, verificationCode) { success, error ->
                                        isLoading = false
                                        if (success) {
                                            onLoginSuccess()
                                        } else {
                                            Toast.makeText(context, "Erro ao verificar: $error", Toast.LENGTH_LONG).show()
                                        }
                                    }
                                }
                            },
                            onBack = { currentScreen = "PhoneInput" }
                        )
                    }
                }
            }
        }
    }
}