package com.example.myapitest.view.map

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.myapitest.model.CarModel
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState

@Composable
fun MapScreen(car: CarModel) {
    Column {
        Text(text = "Nome: ${car.name}")
        Text(text = "Ano: ${car.year}")
        Text(text = "Placa: ${car.licence}")

        GoogleMap(
            cameraPositionState = com.google.maps.android.compose.rememberCameraPositionState {
                position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(LatLng(car.place.lat, car.place.long), 15f)
            }
        ) {
            Marker(
                state = MarkerState(position = LatLng(car.place.lat, car.place.long)),
                title = car.name
            )
        }
    }
}