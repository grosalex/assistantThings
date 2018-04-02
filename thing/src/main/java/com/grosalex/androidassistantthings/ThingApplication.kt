package com.grosalex.androidassistantthings

import android.app.Application
import com.philips.lighting.hue.sdk.wrapper.HueLog
import com.philips.lighting.hue.sdk.wrapper.Persistence



/**
 * Created by grosalex on 02/04/2018.
 */
class ThingApplication : Application() {


    override fun onCreate() {
        super.onCreate()

        // Configure the storage location and log level for the Hue SDK
        Persistence.setStorageLocation(filesDir.absolutePath, "HueQuickStart")
        HueLog.setConsoleLogLevel(HueLog.LogLevel.INFO)
    }
    companion object {
        init {
            System.loadLibrary("huesdk")
        }
    }
}