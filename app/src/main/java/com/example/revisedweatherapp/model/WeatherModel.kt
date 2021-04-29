package com.example.revisedweatherapp.model

import java.io.Serializable

data class WeatherModel(
    val id: Int,
    val main: String,
    val description: String,
    val icon: String
) : Serializable