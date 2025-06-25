package com.example.fitfriends.ui.map

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
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
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polyline
import com.google.android.gms.maps.model.PolylineOptions
import com.google.android.material.button.MaterialButton

class MapFragment : Fragment(), OnMapReadyCallback {
    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private val trackingViewModel: ActivityTrackingViewModel by activityViewModels()

    private lateinit var tvLocation: TextView
    private lateinit var tvRouteDistance: TextView
    private lateinit var btnClearRoute: MaterialButton
    private lateinit var btnCenterMap: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        tvLocation = view.findViewById(R.id.tv_location)
        tvRouteDistance = view.findViewById(R.id.tv_route_distance)
        btnClearRoute = view.findViewById(R.id.btn_clear_route)
        btnCenterMap = view.findViewById(R.id.btn_center_map)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        btnClearRoute.setOnClickListener { clearRoute() }
        btnCenterMap.setOnClickListener { centerMapOnCurrentLocation() }

        trackingViewModel.routePoints.observe(viewLifecycleOwner) { points ->
            drawRoute(points)
        }
        trackingViewModel.stats.observe(viewLifecycleOwner) { stats ->
            val km = stats.distance / 1000.0
            tvRouteDistance.text = String.format("%.2f km", km)
        }

        return view
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        map.uiSettings.isZoomControlsEnabled = true
        map.uiSettings.isMyLocationButtonEnabled = false
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            map.isMyLocationEnabled = true
            startLocationUpdates()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1002)
        }
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = Priority.PRIORITY_HIGH_ACCURACY
        }
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                val location = result.lastLocation ?: return
                tvLocation.text = String.format("%.5f, %.5f", location.latitude, location.longitude)
                trackingViewModel.onNewLocation(location)
                // Usunięto automatyczne centrowanie mapy
            }
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        }
    }

    private var routePolyline: Polyline? = null
    private fun drawRoute(points: List<LatLng>) {
        routePolyline?.remove()
        if (points.size > 1) {
            routePolyline = map.addPolyline(
                PolylineOptions().addAll(points).color(0xFF6200EE.toInt()).width(8f)
            )
        }
    }

    private fun clearRoute() {
        routePolyline?.remove()
        trackingViewModel.stopTracking()
        trackingViewModel.startTracking()
        Toast.makeText(requireContext(), "Trasa wyczyszczona", Toast.LENGTH_SHORT).show()
    }

    private fun centerMapOnCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val latLng = LatLng(location.latitude, location.longitude)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
                } else {
                    Toast.makeText(requireContext(), "Brak aktualnej lokalizacji", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(requireContext(), "Brak uprawnień do lokalizacji!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1002 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (::map.isInitialized) {
                map.isMyLocationEnabled = true
                startLocationUpdates()
            }
        } else {
            Toast.makeText(requireContext(), "Brak uprawnień do lokalizacji!", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
    }
} 