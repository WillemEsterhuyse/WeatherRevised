package com.example.revisedweatherapp.model

import java.io.Serializable

data class CommonModel(
    val temp: Double,
    val pressure: Double,
    val humidity: Int,
    val temp_min: Double,
    val temp_max: Double
) : Serializable