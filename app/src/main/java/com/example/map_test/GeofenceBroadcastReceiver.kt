package com.example.map_test

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent
import com.google.android.gms.location.GeofenceStatusCodes

class GeofenceBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "onReceive triggered with action: ${intent.action}")

        if (intent.action != ACTION_GEOFENCE_EVENT) {
            Log.e(TAG, "Unsupported action received: ${intent.action}")
            return
        }

        val geofencingEvent = GeofencingEvent.fromIntent(intent) ?: run {
            Log.e(TAG, "Geofencing event is null")
            return
        }

        if (geofencingEvent.hasError()) {
            val errorMessage = getGeofenceErrorMessage(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        val geofenceTransition = geofencingEvent.geofenceTransition
        Log.d(TAG, "Geofence transition detected: $geofenceTransition")

        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
            geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            val triggeringGeofences = geofencingEvent.triggeringGeofences
            val triggeringGeofencesIds = getTriggeringGeofencesIds(triggeringGeofences)

            Log.d(TAG, "Processing geofence event: $geofenceTransition for $triggeringGeofencesIds")

            // Verarbeite das Geofence-Ereignis direkt hier
            processGeofenceEvent(context, geofenceTransition, triggeringGeofencesIds)

            if (shouldSendNotification(context)) {
                sendNotification(context, triggeringGeofencesIds)
            }

        } else {
            Log.e(TAG, "Invalid geofence transition: $geofenceTransition")
        }
    }

    /**
     * Verarbeitung des Geofence-Ereignisses.
     * Falls nötig, können hier weitere Hintergrundaufgaben wie Datenbank- oder API-Updates hinzugefügt werden.
     */
    private fun processGeofenceEvent(context: Context, geofenceTransition: Int, triggeringGeofencesIds: String) {
        Log.d(TAG, "Handling geofence event: Transition=$geofenceTransition, Geofences=$triggeringGeofencesIds")

        // Hier möglich um API oder DB Schnittstelle hinzufügen können
    }

    // Wandelt einen Geofence-Fehlercode in eine menschenlesbare Fehlermeldung um
    private fun getGeofenceErrorMessage(errorCode: Int): String = when (errorCode) {
        GeofenceStatusCodes.GEOFENCE_NOT_AVAILABLE -> "Geofence-Dienst ist derzeit nicht verfügbar."
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_GEOFENCES -> "Ihre App hat zu viele Geofences registriert."
        GeofenceStatusCodes.GEOFENCE_TOO_MANY_PENDING_INTENTS -> "Zu viele PendingIntents für den Geofence-Dienst bereitgestellt."
        else -> "Unbekannter Geofence-Fehler ($errorCode)"
    }

    // Erstellt eine Liste der IDs der ausgelösten Geofences
    private fun getTriggeringGeofencesIds(triggeringGeofences: List<Geofence>?): String {
        return triggeringGeofences?.joinToString(", ") { it.requestId } ?: "Unbekannt"
    }

    // Prüft, ob die App die Berechtigung zum Senden von Benachrichtigungen hat
    private fun shouldSendNotification(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true
        }
    }

    // Sendet eine Benachrichtigung an den Nutzer, wenn ein Geofence-Übergang erkannt wurde
    private fun sendNotification(context: Context, notificationDetails: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "geofence_channel"

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Geofence Alerts", NotificationManager.IMPORTANCE_DEFAULT)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationIntent = Intent(context, MainActivity::class.java)
        val notificationPendingIntent = PendingIntent.getActivity(
            context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle("Geofence Event")
            .setContentText("In deiner Nähe: $notificationDetails")
            .setContentIntent(notificationPendingIntent)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(0, notification)
        Log.d(TAG, "Notification sent: $notificationDetails")
    }

    companion object {
        private const val TAG = "GeofenceReceiver"
        private const val ACTION_GEOFENCE_EVENT = "com.example.map_test.ACTION_GEOFENCE_EVENT"

        // Sendet eine Broadcast-Nachricht für Geofencing-Ereignisse
        fun sendGeofenceEventBroadcast(context: Context) {
            val intent = Intent(ACTION_GEOFENCE_EVENT)
            context.sendBroadcast(intent)
        }
    }
}