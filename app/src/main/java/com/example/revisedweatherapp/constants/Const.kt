package com.example.revisedweatherapp.constants

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

object Constants{
    const val APP_ID : String ="325c47bf6c23748a8265dd7961d27673"
    const val BASE_URL :String ="http://api.openweathermap.org/data/"
    const val METRIC_UNIT :String ="metric"
    const val  PREFERENCE_NAME ="SharedWeatherDataPreference"
    const val  WEATHER_RESPONSE_DATA = "weather_response_data"

    fun isNetworkAvailable(context:Context):Boolean
    {
        val connectivityManager =context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M)
        {
            val network = connectivityManager.activeNetwork?:return false

            val activeNetwork =connectivityManager.getNetworkCapabilities(network)?:return false

            return when
            {
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)->true
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)->true
                else->false
            }
        }
        else
        {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnectedOrConnecting
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun checkNetworkAvailable(context: Context) =
        (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
            getNetworkCapabilities(activeNetwork)?.run {
                hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
            } ?: false
        }
}