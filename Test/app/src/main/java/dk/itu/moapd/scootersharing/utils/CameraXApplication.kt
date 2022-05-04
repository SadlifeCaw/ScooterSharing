package dk.itu.moapd.scootersharing.utils

import android.app.Application
import androidx.camera.camera2.Camera2Config
import androidx.camera.core.CameraXConfig

class CameraXApplication : Application(), CameraXConfig.Provider{
    override fun getCameraXConfig(): CameraXConfig {
        return CameraXConfig.Builder.fromConfig(Camera2Config.defaultConfig()).build()
    }
}