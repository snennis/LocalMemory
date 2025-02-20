package com.example.map_test

import org.osmdroid.util.GeoPoint

data class SavedMarker(
    val geoPoint: GeoPoint,
    val activity: String,
    val description: String,
    val priority: String,
    val iconRes: Int  // Speichert die Icon-Resource-ID
)