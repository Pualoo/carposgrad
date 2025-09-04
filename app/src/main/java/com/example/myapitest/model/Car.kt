package com.example.myapitest.model

data class Place(
    val lat: Double,
    val long: Double
)

data class Car(
    val id: String? = null,
    val imageUrl: String,
    val name: String,
    val year: String,
    val license: String,
    val place: Place
)