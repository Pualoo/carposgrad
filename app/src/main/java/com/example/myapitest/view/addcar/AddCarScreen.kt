package com.example.myapitest.view.addcar

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.myapitest.model.CarModel
import com.example.myapitest.model.Place
import com.example.myapitest.viewmodel.CarUIState
import com.example.myapitest.viewmodel.CarViewModel
import com.google.android.gms.location.LocationServices
import com.google.firebase.storage.FirebaseStorage
import java.util.UUID

enum class PermissionState { UNKNOWN, GRANTED, DENIED }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCarScreen(navController: NavController, carViewModel: CarViewModel) {
    val context = LocalContext.current
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licence by remember { mutableStateOf("") }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageUrl by remember { mutableStateOf<String?>(null) }
    var isImageUploading by remember { mutableStateOf(false) }
    var imageUploadSuccess by remember { mutableStateOf(false) }
    var imageUploadError by remember { mutableStateOf<String?>(null) }
    val uiState by carViewModel.uiState.collectAsState()
    var lat by remember { mutableStateOf<Double?>(null) }
    var long by remember { mutableStateOf<Double?>(null) }
    var isLocationLoading by remember { mutableStateOf(false) }
    var locationPermissionState by remember { mutableStateOf(PermissionState.UNKNOWN) }
    var locationError by remember { mutableStateOf<String?>(null) }
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    LaunchedEffect(imageUri) {
        imageUri?.let { uri ->
            isImageUploading = true
            imageUploadSuccess = false
            imageUploadError = null
            val storageRef = FirebaseStorage.getInstance().reference
            val imageRef = storageRef.child("images/${UUID.randomUUID()}")
            val uploadTask = imageRef.putFile(uri)

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    task.exception?.let { throw it }
                }
                imageRef.downloadUrl
            }.addOnCompleteListener { task ->
                isImageUploading = false
                if (task.isSuccessful) {
                    imageUrl = task.result.toString()
                    imageUploadSuccess = true
                } else {
                    imageUploadError = task.exception?.message ?: "Unknown error"
                }
            }
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            locationPermissionState = if (granted) PermissionState.GRANTED else PermissionState.DENIED
            if (granted) {
                // Permission granted, get location
            }
        }
    )

    LaunchedEffect(locationPermissionState) {
        if (locationPermissionState == PermissionState.GRANTED) {
            isLocationLoading = true
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    lat = location.latitude
                    long = location.longitude
                } else {
                    locationError = "Location not available"
                }
                isLocationLoading = false
            }.addOnFailureListener {
                locationError = "Error getting location"
                isLocationLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        when (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)) {
            PackageManager.PERMISSION_GRANTED -> locationPermissionState = PermissionState.GRANTED
            else -> permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add New Car") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (imageUri != null) {
                        Image(
                            painter = rememberAsyncImagePainter(imageUri),
                            contentDescription = "Selected Image",
                            modifier = Modifier
                                .height(200.dp)
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                        Text(if (imageUri == null) "Select Image" else "Change Image")
                    }

                    when {
                        isImageUploading -> {
                            CircularProgressIndicator(modifier = Modifier.padding(top = 8.dp))
                            Text("Uploading image...", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        }
                        imageUploadSuccess -> {
                            Text("Image uploaded successfully!", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF006400))
                        }
                        imageUploadError != null -> {
                            Text(imageUploadError!!, style = MaterialTheme.typography.bodyMedium, color = Color.Red)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val isFormValid = name.isNotBlank() && year.isNotBlank() && licence.isNotBlank() && lat != null && long != null && imageUrl != null
            Button(
                onClick = {
                    imageUrl?.let { url ->
                        val newCarModel = CarModel(
                            id = UUID.randomUUID().toString(),
                            imageUrl = url,
                            year = year,
                            name = name,
                            licence = licence,
                            place = Place(lat = lat ?: 0.0, long = long ?: 0.0)
                        )
                        carViewModel.addCar(newCarModel) {
                            navController.popBackStack()
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                enabled = isFormValid
            ) {
                when (uiState) {
                    is CarUIState.Loading -> CircularProgressIndicator(color = Color.White)
                    else -> Text("Add Car")
                }
            }

            (uiState as? CarUIState.Error)?.let {
                Text(
                    text = it.message,
                    color = Color.Red,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            Spacer(modifier = Modifier.height(100.dp))
        }
    }
}
