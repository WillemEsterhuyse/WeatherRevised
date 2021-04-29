package com.example.revisedweatherapp.model

import java.io.Serializable

data class WeatherResponse(
    val coord: CoordsModel,
    val weather: List<WeatherModel>,
    val base: String,
    val main: CommonModel,
    val visibility: Int,
    val wind: WindModel,
    val clouds: CloudModel,
    val dt: Int,
    val commonSystemModel: CommonSystemModel,
    val id: Int,
    val name: String,
    val cod: Int
) : Serializable
