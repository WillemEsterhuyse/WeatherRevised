package com.example.revisedweatherapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.revisedweatherapp.api.WeatherService
import com.example.revisedweatherapp.constants.Constants
import com.example.revisedweatherapp.model.WeatherResponse
import com.google.android.gms.location.*
import com.google.gson.Gson
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var mClient: FusedLocationProviderClient

    private var mDialog : Dialog? =null

    private lateinit var  sharedPreferences : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mClient = LocationServices.getFusedLocationProviderClient(this)

        sharedPreferences = getSharedPreferences(Constants.PREFERENCE_NAME, Context.MODE_PRIVATE)

        setupUI()

        if(!isLocationEnabled())
        {
            Toast.makeText(this, "Enable Location Service", Toast.LENGTH_SHORT).show()

            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)

            startActivity(intent)
        }
        else
        {
            Dexter.withActivity(this).withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION).withListener(object : MultiplePermissionsListener
                {
                    override fun onPermissionsChecked(report: MultiplePermissionsReport?)
                    {
                        if(report!!.areAllPermissionsGranted())
                        {
                            requestLocationData()
                        }
                        if(report.isAnyPermissionPermanentlyDenied)
                        {
                            Toast.makeText(this@MainActivity, "Please allow permissions", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onPermissionRationaleShouldBeShown(permissions: MutableList<PermissionRequest>?, token: PermissionToken?)
                    {
                        showRationalDialogForPermissions()
                    }

                }).onSameThread()
                .check()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestLocationData()
    {
        val  locationRequest = LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 100
        }

        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        @Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
        mClient.requestLocationUpdates(locationRequest,locationCallback, Looper.myLooper())
    }

    private var locationCallback = object : LocationCallback()
    {
        override fun onLocationResult(locationResult: LocationResult?)
        {
            val location : Location = locationResult!!.lastLocation
            val latitude = location.latitude
            val longitude = location.longitude

            getLocationWeatherDetails(latitude,longitude)
        }
    }

    private fun getLocationWeatherDetails(latitude:Double,longitude:Double)
    {
        if(Constants.isNetworkAvailable(this))
        {
            val retrofit : Retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            val service : WeatherService = retrofit.create(WeatherService::class.java)

            val serviceCall : Call<WeatherResponse> = service.getWeather(latitude,longitude,Constants.METRIC_UNIT, Constants.APP_ID)

            showProgressDialog()

            serviceCall.enqueue(object : Callback<WeatherResponse>
            {
                override fun onFailure(call: Call<WeatherResponse>, t: Throwable?) = Toast.makeText(this@MainActivity,
                    "Unable to perform action", Toast.LENGTH_SHORT).show()

                override fun onResponse(call: Call<WeatherResponse>, response: Response<WeatherResponse>)
                {
                    if(response.isSuccessful)
                    {
                        hideProgressDialog()

                        val weatherList: WeatherResponse? = response.body()

                        if (weatherList != null) {
                            val weatherResponseJsonString = Gson().toJson(weatherList)

                            val editor = sharedPreferences.edit()
                            editor.putString(Constants.WEATHER_RESPONSE_DATA,weatherResponseJsonString)
                            editor.apply()

                            setupUI()
                        }
                    }
                    else
                    {
                        // Common error codes
                        when(response.code())
                        {
                            400->
                            {
                                Log.e("Error 400","Not Connected")
                            }
                            404->{
                                Log.e("Error 404","Not found")
                            }
                            else->
                            {
                                Log.e("Error","Error has occurred")
                            }
                        }
                    }
                }
            })
        }
        else
        {
            Toast.makeText(this@MainActivity,
                "Unable to connect"
                , Toast.LENGTH_SHORT).show()
        }
    }

    private fun isLocationEnabled():Boolean
    {
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        return  locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
    private  fun showRationalDialogForPermissions()
    {
        AlertDialog.Builder(this)
            .setMessage("Please turn on permissions in settings")
            .setPositiveButton("Settings"){_,_->
                try
                {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)

                    val uri = Uri.fromParts("package",packageName,null)
                    intent.data =uri
                    startActivity(intent)
                }
                catch (e: ActivityNotFoundException)
                {
                    e.printStackTrace()
                }

            }
            .setNegativeButton("Cancel"){dialog, _ ->
                dialog.dismiss()

            }
            .show()
    }

    private fun showProgressDialog()
    {
        mDialog = Dialog(this)

        mDialog!!.setContentView(R.layout.dialog_progress)

        mDialog!!.show()
    }

    private fun hideProgressDialog()
    {
        if(mDialog != null){
            mDialog!!.dismiss()
        }
    }


    // Resources in progress...


    @SuppressLint("SetTextI18n")
    private fun setupUI()
    {
        val weatherResponseJsonString = sharedPreferences.getString(Constants.WEATHER_RESPONSE_DATA,"No Data")

        if(!weatherResponseJsonString.isNullOrEmpty())
        {
            val list = Gson().fromJson(weatherResponseJsonString,WeatherResponse::class.java)
           //Setup textviews in json response
            for(index in list.weather.indices)
            {
                tv_common.text = list.weather[index].main
                tv_common_description.text = list.weather[index].description
                tv_temperature.text = list.main.temp.toString() +"Degrees(Celsius)"
                tv_sunrise.text = unixTime(list.sys.sunrise)
                tv_sunset.text = unixTime(list.sys.sunset)
                tv_humidity.text =list.main.humidity.toString()+" % "
                tv_minimum_temp.text =list.main.temp_min.toString()+" minimum"
                tv_maximum_temp.text =list.main.temp_max.toString()+" maximum"
                tv_wind_speed.text = list.wind.speed.toString()
                tv_name.text = list.name
                tv_country_name.text = list.sys.country

                when(list.weather[index].icon){

                    //Image resources to be included

                }
            }
        }
    }

    private fun unixTime(timeX:Long):String
    {
        val date = Date(timeX *1000L)
        val simpleDateFormat = SimpleDateFormat("hh:mm", Locale.ENGLISH)
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(date)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean
    {
        menuInflater.inflate(R.menu.menu_main,menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean
    {
        return when(item.itemId)
        {
            R.id.action_refresh->{
                requestLocationData()
                true
            }
            else->return super.onOptionsItemSelected(item)
        }
    }
}