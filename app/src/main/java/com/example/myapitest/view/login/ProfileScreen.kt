package com.example.myapitest.view.login

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.firebase.auth.FirebaseUser

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
