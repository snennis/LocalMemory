package com.example.map_test

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class PreferencesHelper(context: Context) {

    private val sharedPreferences: SharedPreferences =
        context.getSharedPreferences("MapTest", Context.MODE_PRIVATE)

    // Speichert eine Liste von Markern in SharedPreferences
    fun saveMarkers(markers: List<SavedMarker>) {
        try {
            val json = Gson().toJson(markers)
            sharedPreferences.edit().putString("markers_list", json).apply()
            Log.d(TAG, "Markers successfully saved")
        } catch (e: Exception) {
            Log.e(TAG, "Error saving markers", e)
        }
    }

    // Lädt die gespeicherte Marker-Liste aus SharedPreferences
    fun loadSavedMarkers(): List<SavedMarker> {
        val json = sharedPreferences.getString("markers_list", null)

        return if (!json.isNullOrEmpty()) {
            try {
                val type = object : TypeToken<List<SavedMarker>>() {}.type
                Gson().fromJson<List<SavedMarker>>(json, type) ?: emptyList()
            } catch (e: JsonSyntaxException) {
                Log.e(TAG, "Error parsing saved markers JSON", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // Entfernt alle gespeicherten Marker
    fun clearSavedMarkers() {
        sharedPreferences.edit().remove("markers_list").apply()
        Log.d(TAG, "All saved markers cleared")
    }

    companion object {
        private const val TAG = "PreferencesHelper"
    }
}