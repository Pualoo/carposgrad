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

sealed class CarUIState {
    object Loading : CarUIState()
    data class Success(val cars: List<Car>) : CarUIState()
    object Empty : CarUIState()
    data class Error(val message: String) : CarUIState()
}

class CarViewModel : ViewModel() {

    private val _uiState = MutableStateFlow<CarUIState>(CarUIState.Loading)
    val uiState: StateFlow<CarUIState> = _uiState

    fun fetchCars() {
        viewModelScope.launch {
            _uiState.value = CarUIState.Loading
            when (val result = safeApiCall { RetrofitClient.apiService.getCars() }) {
                is Result.Success -> {
                    if (result.data.isEmpty()) {
                        _uiState.value = CarUIState.Empty
                    } else {
                        _uiState.value = CarUIState.Success(result.data)
                    }
                }
                is Result.Error -> {
                    _uiState.value = CarUIState.Error(result.message)
                }
            }
        }
    }
}