package com.example.myapitest.view.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.myapitest.R

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
