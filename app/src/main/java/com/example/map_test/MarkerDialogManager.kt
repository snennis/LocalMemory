package com.example.map_test

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.osmdroid.views.overlay.Marker

class DialogHelper(private val context: Context) {

    // Zeigt einen Dialog zum Erstellen eines neuen Markers an
    fun showAttributeDialog(onSave: (String, String, String) -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_layout, null) ?: return

        val activitySpinner = dialogView.findViewById<Spinner>(R.id.spinner_activity)
        val descriptionEditText = dialogView.findViewById<EditText>(R.id.editText_description)
        val prioritySpinner = dialogView.findViewById<Spinner>(R.id.spinner_priority)
        val saveButton = dialogView.findViewById<Button>(R.id.button_save)

        if (activitySpinner == null || descriptionEditText == null || prioritySpinner == null || saveButton == null) {
            return // Falls UI-Elemente fehlen, Dialog nicht anzeigen
        }

        styleButton(saveButton)

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true) // Nutzer kann Dialog durch Tippen außerhalb schließen
            .create()

        saveButton.setOnClickListener {
            val selectedActivity = activitySpinner.selectedItem?.toString() ?: ""
            val description = descriptionEditText.text.toString().trim()
            val selectedPriority = prioritySpinner.selectedItem?.toString() ?: ""

            if (selectedActivity.isNotEmpty() && selectedPriority.isNotEmpty()) {
                dialog.dismiss()
                onSave(selectedActivity, description, selectedPriority)
            } else {
                descriptionEditText.error = "Beschreibung darf nicht leer sein"
            }
        }

        dialog.show()
    }

    // Zeigt einen Dialog mit den Details eines Markers an und ermöglicht das Löschen
    fun showMarkerInfoDialog(marker: Marker, onDelete: () -> Unit) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.marker_info_dialog, null) ?: return

        val infoTextView = dialogView.findViewById<TextView>(R.id.marker_info_text)
        val deleteButton = dialogView.findViewById<Button>(R.id.button_delete)

        if (infoTextView == null || deleteButton == null) {
            return // Falls UI-Elemente fehlen, Dialog nicht anzeigen
        }

        styleButton(deleteButton)

        infoTextView.text = buildString {
            append("Aktivität: ${marker.title ?: "Unbekannt"}")
            marker.snippet?.takeIf { it.isNotBlank() }?.let {
                append("\nBeschreibung: $it")
            }
        }

        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true) // Nutzer kann Dialog durch Tippen außerhalb schließen
            .create()

        deleteButton.setOnClickListener {
            onDelete()
            dialog.dismiss()
        }

        dialog.show()
    }

    // Setzt die Standard-Gestaltung für Buttons
    private fun styleButton(button: Button) {
        button.apply {
            setBackgroundColor(ContextCompat.getColor(context, androidx.cardview.R.color.cardview_dark_background))
            setTextColor(ContextCompat.getColor(context, android.R.color.white))
        }
    }
}