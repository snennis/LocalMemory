package com.example.map_test

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.VectorDrawable
import android.location.Location
import android.location.LocationListener
import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import org.osmdroid.views.overlay.Polyline

// Extension-Funktion, die ein Drawable in ein Bitmap umwandelt.
// Unterstützt BitmapDrawable und VectorDrawable.
private fun Drawable.toBitmapCompat(): Bitmap {
    return when (this) {
        is BitmapDrawable -> this.bitmap
        is VectorDrawable -> {
            val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
            Canvas(bitmap).apply {
                setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                this@toBitmapCompat.draw(this)
            }
            bitmap
        }
        else -> throw IllegalArgumentException("Unsupported drawable type")
    }
}

class MainActivity : AppCompatActivity() {

    private var mapView: MapView? = null
    private var myLocationOverlay: MyLocationNewOverlay? = null

    private var locationCentered = false

    private lateinit var selectedActivity: String
    private lateinit var description: String
    private lateinit var selectedPriority: String

    private val markersList = mutableListOf<Marker>()

    private lateinit var locationManagerHelper: LocationManagerHelper
    private lateinit var markerDialogManager: MarkerDialogManager
    private lateinit var preferencesHelper: PreferencesHelper
    private lateinit var geofencingHelper: GeofencingHelper

    // Variable zum Speichern des aktuell angezeigten Routen-Overlays
    private var currentRouteOverlay: Polyline? = null

    // Map zur Zuordnung von Aktivitätsnamen zu Icon-Resource-IDs
    private val activityIcons = mapOf(
        "Sport" to R.drawable.pixil_frame_0,
        "Einkaufen" to R.drawable.pixil_frame_0_2,
        "Haushalt" to R.drawable.pixil_frame_0_3,
        "Arbeit" to R.drawable.pixil_frame_0_4
    )
    private val defaultIcon = R.drawable.pixil_frame_0_3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Configuration.getInstance().userAgentValue = packageName

        // Initialisiere die Helper-Klassen
        preferencesHelper = PreferencesHelper(this)
        locationManagerHelper = LocationManagerHelper(this, locationListener)
        markerDialogManager = MarkerDialogManager(this)
        geofencingHelper = GeofencingHelper(this)

        // Initialisiere und konfiguriere die MapView
        mapView = findViewById<MapView>(R.id.map).apply {
            setTileSource(TileSourceFactory.MAPNIK)
            setMultiTouchControls(true)
        }

        // Standort-Overlay aktivieren
        myLocationOverlay = MyLocationNewOverlay(mapView).apply { enableMyLocation() }
        mapView?.overlays?.add(myLocationOverlay)

        // Lade gespeicherte Marker
        preferencesHelper.loadSavedMarkers().forEach { savedMarker ->
            addMarker(
                savedMarker.geoPoint,
                savedMarker.activity,
                savedMarker.description,
                savedMarker.priority,
                savedMarker.iconRes // Hier wird das gespeicherte Icon verwendet
            )
        }

        // Prüfe Standortberechtigungen
        locationManagerHelper.checkLocationPermission()

        // Zentriere die Karte auf den aktuellen Standort
        locationManagerHelper.getCurrentLocation { location ->
            location?.let {
                val geoPoint = GeoPoint(it.latitude, it.longitude)
                mapView?.controller?.apply {
                    setCenter(geoPoint)
                    setZoom(18.0)
                }
                locationCentered = true

                val nearestMarker = findNearestMarker(geoPoint)
                if (nearestMarker != null) {
                    Toast.makeText(
                        this,
                        "Nächster Punkt: ${nearestMarker.activity} (${nearestMarker.description})",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(this, "Kein gespeicherter Marker gefunden", Toast.LENGTH_SHORT).show()
                }
            } ?: Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }

        // Button "Meine Position"
        findViewById<Button>(R.id.button_my_location).setOnClickListener {
            myLocationOverlay?.myLocation?.let { location ->
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                mapView?.controller?.apply {
                    setCenter(geoPoint)
                    setZoom(18.0)
                }
            } ?: Toast.makeText(this, "Current location not available", Toast.LENGTH_SHORT).show()
        }

        // Button zum Hinzufügen eines neuen Markers
        findViewById<Button>(R.id.button_plus).setOnClickListener {
            markerDialogManager.showAttributeDialog { activity, desc, priority ->
                selectedActivity = activity
                description = desc
                selectedPriority = priority
                enableMapClick()
            }
        }
    }

    // Aktiviert den Klick-Modus auf der Karte zum Platzieren eines Markers
    private fun enableMapClick() {
        mapView?.setOnTouchListener { view, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                view.performClick() // Für Accessibility
                val geoPoint = mapView?.projection?.fromPixels(event.x.toInt(), event.y.toInt()) as? GeoPoint
                geoPoint?.let { addMarker(it) }
                mapView?.setOnTouchListener(null)
            }
            true
        }
    }

    // Fügt einen Marker hinzu. Falls iconRes nicht null, wird dieser Wert genutzt,
    // sonst wird anhand der Aktivität über die Map activityIcons der richtige Resource-Wert ermittelt.
    private fun addMarker(
        geoPoint: GeoPoint,
        activity: String = selectedActivity,
        description: String = this.description,
        priority: String = selectedPriority,
        iconRes: Int? = null
    ) {
        val marker = Marker(mapView).apply {
            position = geoPoint
            setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
            title = "$activity - $priority"
            snippet = description
        }

        val drawableResId = iconRes ?: (activityIcons[activity] ?: defaultIcon)
        val iconDrawable = ResourcesCompat.getDrawable(resources, drawableResId, theme)
        if (iconDrawable != null) {
            val scaledBitmap = iconDrawable.toBitmapCompat().let {
                Bitmap.createScaledBitmap(it, 64, 64, false)
            }
            marker.icon = BitmapDrawable(resources, scaledBitmap)
        } else {
            Log.e("MainActivity", "Failed to load icon for activity: $activity")
            return
        }

        // Marker-Click-Listener: Zeige Info-Dialog, starte Routing und entferne ggf. existierende Routen-Linie
        marker.setOnMarkerClickListener { _, _ ->
            markerDialogManager.showMarkerInfoDialog(marker) {
                // Marker entfernen
                mapView?.overlays?.remove(marker)
                markersList.remove(marker)
                mapView?.invalidate()
                saveMarkers()
                geofencingHelper.removeGeofences(marker.title)
                // Falls ein Routing-Overlay existiert, entferne es
                currentRouteOverlay?.let {
                    mapView?.overlays?.remove(it)
                    currentRouteOverlay = null
                    mapView?.invalidate()
                }
            }
            locationManagerHelper.getCurrentLocation { currentLocation ->
                currentLocation?.let {
                    val currentGeoPoint = GeoPoint(it.latitude, it.longitude)
                    // Entferne bestehendes Routing-Overlay
                    currentRouteOverlay?.let {
                        mapView?.overlays?.remove(it)
                        currentRouteOverlay = null
                    }
                    // Starte Routing und speichere das erzeugte Overlay
                    RoutingHelper.fetchAndDisplayRoute(currentGeoPoint, marker.position, mapView!!, this) { overlay ->
                        currentRouteOverlay = overlay
                    }
                } ?: Toast.makeText(this, "Aktueller Standort nicht verfügbar", Toast.LENGTH_SHORT).show()
            }
            true
        }

        mapView?.overlays?.add(marker)
        mapView?.invalidate()

        markersList.add(marker)
        saveMarkers()

        // Füge beispielhaft einen Geofence hinzu
        val radius = 50f
        geofencingHelper.addGeofence(marker.title, geoPoint, radius)
    }

    // Speichert alle Marker in den SharedPreferences
    private fun saveMarkers() {
        preferencesHelper.saveMarkers(markersList.map { marker ->
            // Extrahiere den Aktivitätsnamen aus dem Marker-Titel ("Activity - Priority")
            val activityName = marker.title?.substringBefore(" - ") ?: ""
            // Ermittle den Icon-Resource-Wert anhand der Map; falls nicht vorhanden, defaultIcon
            val iconRes = activityIcons[activityName] ?: defaultIcon

            SavedMarker(
                marker.position,
                activityName,
                marker.snippet ?: "",
                marker.title?.substringAfter(" - ") ?: "",
                iconRes
            )
        })
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        locationManagerHelper.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onResume() {
        super.onResume()
        mapView?.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView?.onPause()
        locationManagerHelper.removeLocationUpdates()
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            if (!locationCentered) {
                val geoPoint = GeoPoint(location.latitude, location.longitude)
                mapView?.controller?.apply {
                    setCenter(geoPoint)
                    setZoom(18.0)
                }
                locationCentered = true
                Log.d("MainActivity", "Location centered at: ${geoPoint.latitude}, ${geoPoint.longitude}")
            }
        }
        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
        override fun onProviderEnabled(provider: String) {}
        override fun onProviderDisabled(provider: String) {}
    }

    private fun findNearestMarker(currentGeoPoint: GeoPoint): SavedMarker? {
        val markers = preferencesHelper.loadSavedMarkers()
        return markers.minByOrNull { marker ->
            val distance = haversineDistance(
                currentGeoPoint.latitude, currentGeoPoint.longitude,
                marker.geoPoint.latitude, marker.geoPoint.longitude
            )
            distance
        }
    }

    private fun haversineDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371e3 // Erdradius in Metern
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = Math.sin(dLat / 2).pow(2.0) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                Math.sin(dLon / 2).pow(2.0)
        val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
        return R * c
    }
}

// Erweiterungsfunktion für Double.pow
private fun Double.pow(exp: Double): Double = Math.pow(this, exp)