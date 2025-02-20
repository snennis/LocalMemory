package com.example.map_test

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.widget.Toast
import androidx.core.content.ContextCompat

class LocationHelper(
    private val context: Context,
    private val locationListener: LocationListener
) {
    private val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    companion object {
        private const val REQUEST_LOCATION_PERMISSION = 1
    }

    // Prüft, ob die App Standortberechtigungen hat
    fun checkLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    // Aktiviert Standortupdates, falls die Berechtigung vorhanden ist
    fun requestLocationUpdates() {
        if (checkLocationPermission()) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(context, "GPS ist deaktiviert. Bitte aktivieren!", Toast.LENGTH_SHORT).show()
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0L, 0f, locationListener)
        }
    }

    // Stoppt Standortupdates
    fun removeLocationUpdates() {
        locationManager.removeUpdates(locationListener)
    }

    // Reagiert auf das Ergebnis der Berechtigungsanfrage
    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED })) {
                requestLocationUpdates()
            } else {
                Toast.makeText(context, "Standortberechtigungen nicht gewährt", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Holt die aktuelle bekannte Position und gibt sie an den Callback weiter
    fun getCurrentLocation(callback: (Location?) -> Unit) {
        if (checkLocationPermission()) {
            if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                Toast.makeText(context, "GPS ist deaktiviert", Toast.LENGTH_SHORT).show()
                callback(null)
                return
            }
            val location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            callback(location)
        } else {
            callback(null)
        }
    }
}