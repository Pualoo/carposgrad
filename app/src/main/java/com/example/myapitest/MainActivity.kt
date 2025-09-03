package com.example.myapitest

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.yourapp.AuthViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseUser

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
}

private class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        // text.text contains only digits from onValueChange
        val digits = text.text

        val out = buildAnnotatedString {
            if (digits.isEmpty()) return@buildAnnotatedString

            append("+") // +
            append(digits.take(2)) // 55
            if (digits.length > 2) {
                append(" (") // +55 (
                append(digits.substring(2).take(2)) // 11
            }
            if (digits.length > 4) {
                append(") ") // +55 (11)
                append(digits.substring(4).take(5)) // 91234
            }
            if (digits.length > 9) {
                append("-") // +55 (11) 91234-
                append(digits.substring(9).take(4)) // 5678
            }
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                // offset in the original digit string
                return when {
                    offset <= 2 -> offset + 1
                    offset <= 4 -> offset + 4
                    offset <= 9 -> offset + 6
                    else -> offset + 7
                }.coerceAtMost(out.length)
            }

            override fun transformedToOriginal(offset: Int): Int {
                // offset in the formatted string
                return when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= 7 -> 2
                    offset <= 9 -> offset - 4
                    offset <= 12 -> 4
                    offset <= 17 -> offset - 6
                    offset <= 18 -> 9
                    else -> offset - 7
                }.coerceIn(0, digits.length) // Ensure the cursor position is always valid
            }
        }
        return TransformedText(out, offsetMapping)
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

@Composable
fun SelectionScreen(onGoogleClick: () -> Unit, onPhoneClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Escolha um método de login", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E2A3B))
        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onGoogleClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Image(painter = painterResource(id = R.drawable.ic_google_logo), contentDescription = null, modifier = Modifier.size(24.dp))
            Text("Login com Google", modifier = Modifier.padding(start = 12.dp), color = Color.DarkGray)
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onPhoneClick,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Login com Telefone", color = Color.White)
        }
    }
}

@Composable
fun PhoneInputScreen(
    phoneNumber: String,
    onPhoneNumberChange: (String) -> Unit,
    onSendCodeClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Login por Telefone", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E2A3B))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = phoneNumber,
            onValueChange = {
                if (it.length <= 13) {
                    onPhoneNumberChange(it.filter { char -> char.isDigit() })
                }
            },
            label = { Text("Telefone (Ex: 5511912345678)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PhoneVisualTransformation()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onSendCodeClick, modifier = Modifier.fillMaxWidth()) {
            Text("Enviar Código")
        }
        TextButton(onClick = onBack) {
            Text("Voltar")
        }
    }
}

@Composable
fun CodeInputScreen(
    verificationCode: String,
    onVerificationCodeChange: (String) -> Unit,
    onVerifyClick: () -> Unit,
    onBack: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Verificar Código", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E2A3B))
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = verificationCode,
            onValueChange = onVerificationCodeChange,
            label = { Text("Código de 6 dígitos") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onVerifyClick, modifier = Modifier.fillMaxWidth()) {
            Text("Verificar e Entrar")
        }
        TextButton(onClick = onBack) {
            Text("Voltar")
        }
    }
}


@Composable
fun LoggedInUI(user: FirebaseUser?, onSignOutClick: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text("Logado como:", fontSize = 20.sp, color = Color(0xFF3A4A5B))
        Spacer(modifier = Modifier.height(16.dp))

        if (user?.photoUrl != null) {
            AsyncImage(
                model = user.photoUrl,
                contentDescription = "Foto de Perfil",
                modifier = Modifier.size(100.dp).clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(modifier = Modifier.size(100.dp).clip(CircleShape).padding(8.dp)) {
                Text("📱", fontSize = 60.sp, textAlign = TextAlign.Center)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Text(user?.displayName ?: user?.phoneNumber ?: "Usuário", fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text(user?.email ?: "", fontSize = 16.sp, color = Color.Gray)
        Text("UID: ${user?.uid}", fontSize = 12.sp, color = Color.Gray)
        Spacer(modifier = Modifier.height(32.dp))
        Button(onClick = onSignOutClick, colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE53935))) {
            Text("Sair (Sign Out)", color = Color.White)
        }
    }
}


