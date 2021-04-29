package com.example.revisedweatherapp.model

import java.io.Serializable

data class CommonSystemModel(
    val type: Int,
    val message: Double,
    val country: String,
    val sunrise: Long,
    val sunset: Long
) : Serializable