# LocalMemory

LocalMemory ist eine mobile Android-App, die mit Kotlin entwickelt wurde. Die App bietet Funktionen zur Erstellung von Erinnerungen durch Marker und deren Anzeige auf einer Karte. Sie nutzt Geofencing und speichert benutzerdefinierte Marker.

## Funktionen

- Speichern und Anzeigen von benutzerdefinierten Markern
- Geofencing-Unterstützung
- Verwaltung von Markerinformationen
- Integration mit Android-Location-Services

## Installation

### Voraussetzungen

- Android Studio (empfohlen)
- Kotlin 1.5 oder höher
- Gradle
- Android 14.0

### Schritte zur Installation

1. **Repository klonen**:
   ```bash
   git clone https://github.com/snennis/LocalMemory.git
   cd LocalMemory
   ```
2. **Projekt in Android Studio öffnen**
3. **Gradle-Synchronisation durchführen**
4. **App auf einem Emulator oder einem physischen Gerät ausführen**

## Projektstruktur

```
LocalMemory/
├── app/src/main/java/com/example/map_test/  # Hauptcode
│   ├── MainActivity.kt                     # Hauptaktivität der App
│   ├── LocationManagerHelper.kt            # Verwaltung von Standortdaten
│   ├── GeofenceManager.kt                  # Verwaltung von Geofencing
│   ├── MarkerDialogManager.kt              # Dialog für Markerinformationen
│   ├── RoutingHelper.kt                     # Unterstützung für Navigation
│   ├── PreferencesHelper.kt                 # Speicherung von Benutzereinstellungen
├── app/src/main/res/layout/                 # UI-Layouts
│   ├── activity_main.xml
│   ├── marker_info_dialog.xml
│   ├── dialog_layout.xml
├── app/src/main/res/values/                  # Werte und Themes
│   ├── colors.xml
│   ├── themes.xml
│   ├── strings.xml
├── gradle/                                  # Gradle-Konfiguration
│   ├── wrapper/
│   ├── libs.versions.toml
│   ├── gradle-wrapper.properties
├── build.gradle.kts                         # Build-Konfigurationsdatei
└── settings.gradle.kts                      # Gradle-Projekteinstellungen
```

## Lizenz

Dieses Projekt steht unter der MIT-Lizenz. Siehe die Datei `LICENSE` für weitere Details.

