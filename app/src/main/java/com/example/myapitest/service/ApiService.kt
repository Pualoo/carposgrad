package com.example.myapitest.service

import com.example.myapitest.model.CarModel
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    @GET("car") suspend fun getCars(): List<CarModel>

    @POST("car") suspend fun addCar(@Body carModel: CarModel): CarModel
}