package com.example.map_test

import android.app.Application
import org.osmdroid.config.Configuration
import java.io.File
import androidx.preference.PreferenceManager

class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Configuration.getInstance().load(this, PreferenceManager.getDefaultSharedPreferences(this))
        val osmdroidTileCache = File(cacheDir, "osmdroid")
        Configuration.getInstance().osmdroidBasePath = osmdroidTileCache
        Configuration.getInstance().osmdroidTileCache = osmdroidTileCache
    }
}