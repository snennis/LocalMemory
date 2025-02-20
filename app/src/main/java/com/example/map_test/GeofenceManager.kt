package com.example.map_test

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import org.osmdroid.util.GeoPoint

class GeofencingHelper(private val context: Context) {

    private val geofencingClient: GeofencingClient = LocationServices.getGeofencingClient(context)

    // Fügt ein Geofence für eine bestimmte Position hinzu
    fun addGeofence(geofenceId: String, geoPoint: GeoPoint, radius: Float) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            Toast.makeText(context, "Standortberechtigung fehlt!", Toast.LENGTH_SHORT).show()
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(geofenceId)
            .setCircularRegion(geoPoint.latitude, geoPoint.longitude, radius)
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_EXIT)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.addGeofences(geofencingRequest, getGeofencePendingIntent()).apply {
            addOnSuccessListener {
                Log.d(TAG, "Geofence hinzugefügt: $geofenceId")
            }
            addOnFailureListener { e ->
                Log.e(TAG, "Fehler beim Hinzufügen des Geofence: $geofenceId", e)
            }
        }
    }

    // Entfernt ein einzelnes Geofence oder alle, wenn keine requestId übergeben wird
    fun removeGeofences(requestId: String? = null) {
        if (!hasLocationPermission()) {
            Log.e(TAG, "Location permission not granted")
            Toast.makeText(context, "Standortberechtigung fehlt!", Toast.LENGTH_SHORT).show()
            return
        }

        if (requestId != null) {
            // Entfernt ein spezifisches Geofence
            geofencingClient.removeGeofences(listOf(requestId))
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Geofence entfernt: $requestId")
                    } else {
                        Log.e(TAG, "Fehler beim Entfernen des Geofence: $requestId")
                    }
                }
        } else {
            // Entfernt alle Geofences
            geofencingClient.removeGeofences(getGeofencePendingIntent())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Alle Geofences erfolgreich entfernt")
                    } else {
                        Log.e(TAG, "Fehler beim Entfernen aller Geofences")
                    }
                }
        }
    }

    // Erstellt den PendingIntent für Geofence-Ereignisse
    private fun getGeofencePendingIntent(): PendingIntent {
        val intent = Intent(context, GeofenceBroadcastReceiver::class.java).apply {
            action = ACTION_GEOFENCE_EVENT
        }
        return PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE)
    }

    // Prüft, ob die App die Berechtigung hat, den Standort zu verwenden
    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "GeofencingHelper"
        private const val ACTION_GEOFENCE_EVENT = "com.example.map_test.ACTION_GEOFENCE_EVENT"
    }
}