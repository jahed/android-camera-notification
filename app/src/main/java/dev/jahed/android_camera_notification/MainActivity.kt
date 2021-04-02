package dev.jahed.android_camera_notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraManager.AvailabilityCallback
import android.hardware.camera2.CameraMetadata
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        println("onCreate")
        setContentView(R.layout.activity_main)

        val context = this

        val notificationChannel = NotificationChannel(
            "dev.jahed.android_camera_notification",
            "Camera Notification",
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            enableLights(true)
            lightColor = Color.BLUE
        }

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        cameraManager.registerAvailabilityCallback(object : AvailabilityCallback() {
            private val idMap = HashMap<String, Int>()
            private val facingMap = HashMap<Int, String>().apply {
                put(CameraMetadata.LENS_FACING_FRONT, "Front Camera")
                put(CameraMetadata.LENS_FACING_BACK, "Back Camera")
                put(CameraMetadata.LENS_FACING_EXTERNAL, "External Camera")
            }

            override fun onCameraAvailable(cameraId: String) {
                super.onCameraAvailable(cameraId)
                println("Camera[$cameraId] available")
                val notificationId = idMap[cameraId]
                if (notificationId != null) {
                    notificationManager.cancel(notificationId)
                }
            }

            override fun onCameraUnavailable(cameraId: String) {
                super.onCameraUnavailable(cameraId)
                println("Camera[$cameraId] unavailable")
                val camera = cameraManager.getCameraCharacteristics(cameraId)
                val facing = camera.get(CameraCharacteristics.LENS_FACING)
                val facingText = facingMap.getOrDefault(facing, "Camera")
                val notification = Notification.Builder(context, notificationChannel.id)
                        .setSmallIcon(R.drawable.ic_app_icon)
                        .setContentText("$facingText is active.")
                        .setOngoing(true)
                        .build()
                val notificationId = Random().nextInt()
                idMap[cameraId] = notificationId
                notificationManager.notify(notificationId, notification)
            }
        }, null)
    }
}
