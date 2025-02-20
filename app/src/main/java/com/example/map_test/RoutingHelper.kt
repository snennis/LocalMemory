package com.example.map_test

import android.graphics.Color
import android.graphics.DashPathEffect
import android.util.Log
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.overlay.Polyline
import java.io.IOException

object RoutingHelper {
    private const val ORS_API_KEY = "5b3ce3597851110001cf624805103b917d6c45229160cba06be372d8"
    private const val ORS_URL = "https://api.openrouteservice.org/v2/directions/driving-car/geojson"

    fun fetchAndDisplayRoute(
        origin: GeoPoint,
        destination: GeoPoint,
        mapView: org.osmdroid.views.MapView,
        activity: MainActivity,
        onRouteReady: (Polyline) -> Unit
    ) {
        val jsonBody = JSONObject().apply {
            val coordinates = JSONArray().apply {
                put(JSONArray().apply {
                    put(origin.longitude)
                    put(origin.latitude)
                })
                put(JSONArray().apply {
                    put(destination.longitude)
                    put(destination.latitude)
                })
            }
            put("coordinates", coordinates)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = jsonBody.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(ORS_URL)
            .addHeader("Authorization", ORS_API_KEY)
            .post(requestBody)
            .build()

        val client = OkHttpClient()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                activity.runOnUiThread {
                    Toast.makeText(activity, "Routing fehlgeschlagen: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                Log.e("RoutingHelper", "onFailure: ${e.message}")
            }

            override fun onResponse(call: Call, response: Response) {
                val responseString = response.body?.string()
                Log.d("RoutingHelper", "Response: $responseString")
                if (responseString == null) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Leere Antwort erhalten", Toast.LENGTH_SHORT).show()
                    }
                    return
                }
                try {
                    val json = JSONObject(responseString)
                    if (!json.has("features")) {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Keine Route gefunden: $responseString", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    val features = json.getJSONArray("features")
                    if (features.length() > 0) {
                        val feature = features.getJSONObject(0)
                        val geometry = feature.getJSONObject("geometry")
                        val coordinates = geometry.getJSONArray("coordinates")

                        val polylinePoints = mutableListOf<GeoPoint>()
                        for (i in 0 until coordinates.length()) {
                            val coordPair = coordinates.getJSONArray(i)
                            val lon = coordPair.getDouble(0)
                            val lat = coordPair.getDouble(1)
                            polylinePoints.add(GeoPoint(lat, lon))
                        }

                        activity.runOnUiThread {
                            val routeOverlay = Polyline().apply {
                                setPoints(polylinePoints)
                                color = Color.BLUE
                                width = 8f
                                // Gestricheltes Muster: 20px Linie, 10px Lücke
                                paint.pathEffect = DashPathEffect(floatArrayOf(20f, 10f), 0f)
                            }
                            // Füge den Overlay hinzu
                            mapView.overlays.add(routeOverlay)
                            mapView.invalidate()
                            // Übergib den erstellten Overlay über den Callback
                            onRouteReady(routeOverlay)
                        }
                    } else {
                        activity.runOnUiThread {
                            Toast.makeText(activity, "Keine Route gefunden", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    activity.runOnUiThread {
                        Toast.makeText(activity, "Fehler beim Parsen der Route", Toast.LENGTH_SHORT).show()
                    }
                    Log.e("RoutingHelper", "Parsing error: ${e.message}")
                }
            }
        })
    }
}