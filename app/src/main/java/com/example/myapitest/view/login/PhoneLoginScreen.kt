package com.example.myapitest.view.login

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

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
            Text("Enviar Telefone")
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