package com.example.myapitest.view.addcar

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.myapitest.model.CarModel
import com.example.myapitest.model.Place
import com.example.myapitest.viewmodel.CarUIState
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.location.LocationServices
import java.util.UUID

enum class PermissionState { UNKNOWN, GRANTED, DENIED }

@Composable
fun AddCarScreen(navController: NavController, carViewModel: CarViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licence by remember { mutableStateOf("") }
    val uiState by carViewModel.uiState.collectAsState()
    var lat by remember { mutableStateOf<Double?>(null) }
    var long by remember { mutableStateOf<Double?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationPermissionState by remember { mutableStateOf(PermissionState.UNKNOWN) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            locationPermissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
            if (granted) {
                isLocationLoading = true
                fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                    if (location != null) {
                        lat = location.latitude
                        long = location.longitude
                        locationError = null
                    } else {
                        locationError = "Localização não disponível"
                    }
                    isLocationLoading = false
                }.addOnFailureListener {
                    locationError = "Erro ao obter localização"
                    isLocationLoading = false
                }
            }
        }
    )
    LaunchedEffect(Unit) {
        val permissionCheck = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            locationPermissionState = PermissionState.GRANTED
            isLocationLoading = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    long = location.longitude
                    locationError = null
                } else {
                    locationError = "Localização não disponível"
                }
                isLocationLoading = false
            }.addOnFailureListener {
                locationError = "Erro ao obter localização"
                isLocationLoading = false
            }
        } else {
            locationPermissionState = PermissionState.UNKNOWN
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Car Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = year,
            onValueChange = { year = it },
            label = { Text("Year") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = licence,
            onValueChange = { licence = it },
            label = { Text("License Plate") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        val isFormValid = name.isNotBlank() && year.isNotBlank() && licence.isNotBlank() && lat != null && long != null
        Button(
            onClick = {
                val newCarModel = CarModel(
                    id = UUID.randomUUID().toString(),
                    imageUrl = "https://portalsolar-images.s3.us-east-2.amazonaws.com/institucional-and-info-production/images/102e6ad1-371b-495e-bc0a-8b1c12b8bb76/byd-lanca-carro-eletrico-mais-barato-do-brasil-POST.jpg",
                    year = year,
                    name = name,
                    licence = licence,
                    place = Place(lat = lat ?: 0.0, long = long ?: 0.0)
                )
                carViewModel.addCar(newCarModel) {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = isFormValid
        ) {
            when (uiState) {
                is CarUIState.Loading -> CircularProgressIndicator()
                else -> Text("Add Car")
            }
        }
        when {
            isLocationLoading -> {
                Text("Obtendo localização...", color = Color.Gray, modifier = Modifier.padding(top = 8.dp))
            }
            locationPermissionState == PermissionState.DENIED -> {
                Text("Permissão de localização negada", color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            locationError != null -> {
                Text(locationError!!, color = Color.Red, modifier = Modifier.padding(top = 8.dp))
            }
            lat != null && long != null -> {
                Text("Sua localização atual é: $lat, $long", color = Color.DarkGray, modifier = Modifier.padding(top = 8.dp))
            }
        }
        (uiState as? CarUIState.Error)?.let {
            Text(
                text = it.message,
                color = Color.Red,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
    }
}