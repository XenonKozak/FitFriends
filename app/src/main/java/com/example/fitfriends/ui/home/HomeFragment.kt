package com.example.fitfriends.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.fitfriends.R
import com.example.fitfriends.ui.tracking.ActivityTrackingViewModel
import com.google.android.material.button.MaterialButton
import com.google.android.gms.location.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.room.Room

class HomeFragment : Fragment() {
    private lateinit var tvDuration: TextView
    private lateinit var tvDistance: TextView
    private lateinit var tvCalories: TextView
    private lateinit var startButton: MaterialButton
    private lateinit var stopButton: MaterialButton

    private val trackingViewModel: ActivityTrackingViewModel by activityViewModels()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        tvDuration = view.findViewById(R.id.tv_duration)
        tvDistance = view.findViewById(R.id.tv_distance)
        tvCalories = view.findViewById(R.id.tv_calories)
        startButton = view.findViewById(R.id.btn_start_activity)
        stopButton = view.findViewById(R.id.btn_stop_activity)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        startButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1001)
            } else {
                trackingViewModel.startTracking()
                startLocationUpdates()
                startButton.isEnabled = false
                stopButton.isEnabled = true
                Toast.makeText(requireContext(), "Aktywność rozpoczęta!", Toast.LENGTH_SHORT).show()
            }
        }

        stopButton.setOnClickListener {
            trackingViewModel.stopTracking()
            stopLocationUpdates()
            startButton.isEnabled = true
            stopButton.isEnabled = false
            Toast.makeText(requireContext(), "Aktywność zakończona!", Toast.LENGTH_SHORT).show()
        }

        stopButton.isEnabled = false

        trackingViewModel.stats.observe(viewLifecycleOwner) { stats ->
            // Czas
            val seconds = stats.duration / 1000
            val h = seconds / 3600
            val m = (seconds % 3600) / 60
            val s = seconds % 60
            tvDuration.text = String.format("%d:%02d:%02d", h, m, s)
            // Dystans
            val km = stats.distance / 1000.0
            tvDistance.text = String.format("%.2f km", km)
            // Kalorie
            tvCalories.text = String.format("%d kcal", stats.calories)
        }

        return view
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000 // 2 sekundy
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                trackingViewModel.onNewLocation(location)
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1001 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            trackingViewModel.startTracking()
            startLocationUpdates()
            startButton.isEnabled = false
            stopButton.isEnabled = true
            Toast.makeText(requireContext(), "Aktywność rozpoczęta!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(requireContext(), "Brak uprawnień do lokalizacji!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopLocationUpdates()
    }
} 