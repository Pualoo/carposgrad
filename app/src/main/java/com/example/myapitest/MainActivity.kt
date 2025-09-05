package com.example.myapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myapitest.view.addcar.AddCarScreen
import com.example.myapitest.view.dashboard.Dashboard
import com.example.myapitest.view.login.LoginFlow
import com.example.myapitest.view.map.MapScreen
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions

class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()
    private val carViewModel: CarViewModel by viewModels()

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
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "login_flow") {
                    composable("login_flow") {
                        LoginFlow(
                            authViewModel = authViewModel,
                            googleSignInClient = googleSignInClient,
                            activity = this@MainActivity,
                            onLoginSuccess = {
                                navController.navigate("dashboard") {
                                    popUpTo("login_flow") { inclusive = true }
                                }
                            }
                        )
                    }
                    composable("dashboard") {
                        Dashboard(
                            userInfo = authViewModel.getCurrentUser()?.displayName,
                            onAddClick = { navController.navigate("add_car") },
                            onLogoutClick = {
                                authViewModel.logout()
                                googleSignInClient.signOut().addOnCompleteListener {
                                    navController.navigate("login_flow") {
                                        popUpTo("dashboard") { inclusive = true }
                                    }
                                }
                            },
                            carViewModel = carViewModel,
                            onCarClick = { car ->
                                carViewModel.selectCar(car)
                                navController.navigate("map_screen")
                            }
                        )
                    }
                    composable("add_car") {
                        AddCarScreen(navController = navController, carViewModel = carViewModel)
                    }
                    composable(
                        "map_screen"
                    ) {
                        MapScreen(carViewModel = carViewModel, onBackClick = { navController.popBackStack() })
                    }
                }
            }
        }
    }
}