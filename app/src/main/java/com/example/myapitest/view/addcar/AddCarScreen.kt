package com.example.myapitest.view.addcar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.myapitest.model.Car
import com.example.myapitest.model.Place
import com.example.myapitest.viewmodel.CarUIState
import com.example.myapitest.viewmodel.CarViewModel

@Composable
fun AddCarScreen(navController: NavController, carViewModel: CarViewModel) {
    var name by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var licence by remember { mutableStateOf("") }
    val uiState by carViewModel.uiState.collectAsState()

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
        Button(
            onClick = {
                val newCar = Car(
                    imageUrl = "https://portalsolar-images.s3.us-east-2.amazonaws.com/institucional-and-info-production/images/102e6ad1-371b-495e-bc0a-8b1c12b8bb76/byd-lanca-carro-eletrico-mais-barato-do-brasil-POST.jpg",
                    year = year,
                    name = name,
                    license = licence,
                    place = Place(lat = 0.0, long = 0.0)
                )
                carViewModel.addCar(newCar) {
                    navController.popBackStack()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            when (uiState) {
                is CarUIState.Loading -> CircularProgressIndicator()
                else -> Text("Add Car")
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