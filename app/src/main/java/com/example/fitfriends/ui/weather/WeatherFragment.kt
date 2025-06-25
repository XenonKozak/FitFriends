package com.example.fitfriends.ui.weather

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.fitfriends.R
import com.example.fitfriends.api.WeatherApiService
import com.example.fitfriends.data.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class WeatherFragment : Fragment() {
    private lateinit var tvTemperature: TextView
    private lateinit var tvHumidity: TextView
    private lateinit var tvWindSpeed: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnRefresh: MaterialButton
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var weatherApi: WeatherApiService
    private val apiKey by lazy { getString(R.string.openweathermap_api_key) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weather, container, false)
        tvTemperature = view.findViewById(R.id.tv_temperature)
        tvHumidity = view.findViewById(R.id.tv_humidity)
        tvWindSpeed = view.findViewById(R.id.tv_wind_speed)
        tvDescription = view.findViewById(R.id.tv_description)
        btnRefresh = view.findViewById(R.id.btn_refresh_weather)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        weatherApi = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(WeatherApiService::class.java)

        btnRefresh.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 2001)
            } else {
                fetchWeatherForCurrentLocation()
            }
        }
        return view
    }

    private fun fetchWeatherForCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                Log.d("WeatherFragment", "Pobieram pogodę dla: ${location.latitude}, ${location.longitude}")
                fetchWeather(location)
            } else {
                Log.e("WeatherFragment", "Nie udało się pobrać lokalizacji (location == null)")
                Toast.makeText(requireContext(), "Nie udało się pobrać lokalizacji", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun fetchWeather(location: Location) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = weatherApi.getCurrentWeather(
                    lat = location.latitude,
                    lon = location.longitude,
                    apiKey = apiKey
                )
                withContext(Dispatchers.Main) {
                    showWeather(response)
                }
            } catch (e: Exception) {
                Log.e("WeatherFragment", "Błąd pobierania pogody", e)
                if (e is HttpException) {
                    Log.e("WeatherFragment", "Kod odpowiedzi: ${e.code()} Body: ${e.response()?.errorBody()?.string()}")
                }
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Błąd pobierania pogody", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showWeather(weather: WeatherResponse) {
        tvTemperature.text = String.format("%.1f°C", weather.main.temperature)
        tvHumidity.text = "${weather.main.humidity}%"
        tvWindSpeed.text = String.format("%.1f km/h", weather.wind.speed * 3.6)
        tvDescription.text = weather.weather.firstOrNull()?.description ?: "--"
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 2001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            fetchWeatherForCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Brak uprawnień do lokalizacji!", Toast.LENGTH_SHORT).show()
        }
    }
} 