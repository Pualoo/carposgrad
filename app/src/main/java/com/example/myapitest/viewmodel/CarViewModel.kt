package com.example.myapitest.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapitest.model.Car
import com.example.myapitest.service.RetrofitClient
import com.example.myapitest.service.safeApiCall
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.myapitest.service.Result

class CarViewModel : ViewModel() {

    private val _cars = MutableStateFlow<List<Car>>(emptyList())
    val cars: StateFlow<List<Car>> = _cars

    fun fetchCars() {
        viewModelScope.launch {
            when (val result = safeApiCall { RetrofitClient.apiService.getCars() }) {
                is Result.Success -> {
                    _cars.value = result.data
                }
                is Result.Error -> {
                    // Handle error
                }
            }
        }
    }
}