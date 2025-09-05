package com.example.myapitest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapitest.model.CarModel
import com.example.myapitest.view.addcar.AddCarScreen
import com.example.myapitest.view.dashboard.Dashboard
import com.example.myapitest.view.login.LoginFlow
import com.example.myapitest.view.map.MapScreen
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.gson.Gson
import java.net.URLDecoder
import java.net.URLEncoder

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
                                val carJson = Gson().toJson(car)
                                val encodedUrl = URLEncoder.encode(carJson, "UTF-8")
                                navController.navigate("map_screen/$encodedUrl")
                            }
                        )
                    }
                    composable("add_car") {
                        AddCarScreen(navController = navController, carViewModel = carViewModel)
                    }
                    composable(
                        "map_screen/{car}",
                        arguments = listOf(navArgument("car") { type = NavType.StringType })
                    ) { backStackEntry ->
                        backStackEntry.arguments?.getString("car")?.let { carJson ->
                            val decodedUrl = URLDecoder.decode(carJson, "UTF-8")
                            val car = Gson().fromJson(decodedUrl, CarModel::class.java)
                            MapScreen(car = car, onBackClick = { navController.popBackStack() })
                        }
                    }
                }
            }
        }
    }
}