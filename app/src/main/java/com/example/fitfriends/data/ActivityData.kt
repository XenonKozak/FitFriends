package com.example.fitfriends.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity(tableName = "activities")
data class ActivityData(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val userId: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distance: Double = 0.0,
    val calories: Int = 0,
    val routePoints: List<LatLng> = emptyList()
)

data class ActivityStats(
    val duration: Long,
    val distance: Double,
    val calories: Int
)

class LatLngListConverter {
    @TypeConverter
    fun fromLatLngList(list: List<LatLng>?): String {
        return Gson().toJson(list)
    }
    @TypeConverter
    fun toLatLngList(data: String?): List<LatLng> {
        if (data.isNullOrEmpty()) return emptyList()
        val type = object : TypeToken<List<LatLng>>() {}.type
        return Gson().fromJson(data, type)
    }
} 