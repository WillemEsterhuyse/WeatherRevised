package com.example.revisedweatherapp.model

import java.io.Serializable

data class WindModel(
    val speed: Double,
    val deg: Int
) : Serializable