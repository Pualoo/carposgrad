package com.example.myapitest.model

import com.google.gson.annotations.SerializedName

data class Place(
    val lat: Double,
    val long: Double
)

data class Car(
    val id: String? = null,
    val imageUrl: String,
    val name: String,
    val year: String,
    val licence: String,
    val place: Place
)