package com.example.myapitest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import com.example.myapitest.view.login.CodeInputScreen
import com.example.myapitest.view.login.LoggedInUI
import com.example.myapitest.view.login.PhoneInputScreen
import com.example.myapitest.view.login.SelectionScreen
import com.example.yourapp.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(this, gso)

        setContent {
            MaterialTheme {
                SignInScreen(
                    viewModel = authViewModel,
                    googleSignInClient = googleSignInClient,
                    activity = this
                )
            }
        }
    }
    override fun onResume() {
        super.onResume()
        fetchCars()
    }

    private fun setupView() {

    }

    private fun requestLocationPermission() {

    }

    private fun fetchCars() {
        CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall { RetrofitClient.apiService.getCars() }
            println(result.toString());
        }
    }
}


@Composable
fun SignInScreen(
    viewModel: AuthViewModel = viewModel(),
    googleSignInClient: GoogleSignInClient,
    activity: ComponentActivity
) {
    val user by viewModel.user.collectAsState()
    val context = LocalContext.current

    var currentScreen by remember { mutableStateOf("Selection") }
    var phoneNumber by remember { mutableStateOf("") }
    var verificationCode by remember { mutableStateOf("") }
    var verificationId by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val googleAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult(),
        onResult = { result ->
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)!!
                viewModel.firebaseAuthWithGoogle(account) { success, message ->
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            } catch (e: ApiException) {
                Log.w("SignInScreen", "Login com Google falhou", e)
            }
        }
    )

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF0F4F8)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator()
            } else if (user == null) {
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
                            viewModel.sendVerificationCode(activity, "+$phoneNumber") { success, verId, error ->
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
                        onBack = { currentScreen = "Selection"}
                    )
                    "CodeInput" -> CodeInputScreen(
                        verificationCode = verificationCode,
                        onVerificationCodeChange = { verificationCode = it },
                        onVerifyClick = {
                            if (verificationId != null) {
                                isLoading = true
                                viewModel.verifyCode(verificationId!!, verificationCode) { success, error ->
                                    isLoading = false
                                    if (!success) {
                                        Toast.makeText(context, "Erro ao verificar: $error", Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        },
                        onBack = { currentScreen = "PhoneInput"}
                    )
                }
            } else {
                LoggedInUI(user) {
                    viewModel.signOut(googleSignInClient) {
                        Toast.makeText(context, "Você saiu.", Toast.LENGTH_SHORT).show()
                        currentScreen = "Selection"
                    }
                }
            }
        }
    }
}




