package com.example.fitfriends.ui.tracking

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.fitfriends.data.ActivityStats
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ActivityTrackingViewModel(application: Application) : AndroidViewModel(application) {
    private val _stats = MutableLiveData(ActivityStats(0L, 0.0, 0))
    val stats: LiveData<ActivityStats> = _stats

    private val _routePoints = MutableLiveData<List<LatLng>>(emptyList())
    val routePoints: LiveData<List<LatLng>> = _routePoints

    private var startTime: Long = 0L
    private var elapsedTime: Long = 0L
    private var distance: Double = 0.0
    private var calories: Int = 0
    private var lastLocation: Location? = null
    private val weightKg = 70.0
    private var tracking = false
    private var timerJob: Job? = null

    fun startTracking() {
        if (tracking) return
        tracking = true
        startTime = System.currentTimeMillis()
        elapsedTime = 0L
        distance = 0.0
        calories = 0
        lastLocation = null
        _routePoints.value = emptyList()
        updateStats()
        startTimer()
    }

    fun stopTracking() {
        tracking = false
        timerJob?.cancel()
    }

    fun onNewLocation(location: Location) {
        if (!tracking) return
        val latLng = LatLng(location.latitude, location.longitude)
        val points = _routePoints.value?.toMutableList() ?: mutableListOf()
        if (lastLocation != null) {
            val delta = lastLocation!!.distanceTo(location)
            if (delta > 1) {
                distance += delta
                points.add(latLng)
                _routePoints.value = points
                updateStats()
            }
        } else {
            points.add(latLng)
            _routePoints.value = points
        }
        lastLocation = location
    }

    private fun startTimer() {
        timerJob = CoroutineScope(Dispatchers.Main).launch {
            while (tracking) {
                elapsedTime = System.currentTimeMillis() - startTime
                updateStats()
                kotlinx.coroutines.delay(1000)
            }
        }
    }

    private fun updateStats() {
        val km = distance / 1000.0
        calories = (km * 52.5).toInt()
        _stats.value = ActivityStats(elapsedTime, distance, calories)
    }
} 