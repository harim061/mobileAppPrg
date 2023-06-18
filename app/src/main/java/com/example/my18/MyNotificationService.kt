package com.example.my18

import android.Manifest
import android.app.IntentService
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

class MyNotificationService : IntentService("MyNotificationService") {
    companion object {
        const val NOTIFICATION_CHANNEL_ID = "10001"
        const val NOTIFICATION_ID = 100
    }

    override fun onHandleIntent(intent: Intent?) {
        createNotificationChannel()

        val itemDetails = intent?.getSerializableExtra("ITEM_DETAILS") as? ItemVolleyModel
        if (itemDetails != null) {
            showNotification(itemDetails)
        }
    }
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Notification Channel"
            val descriptionText = "Notification Description"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(NOTIFICATION_CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager?.createNotificationChannel(channel)
        }
    }

    private fun showNotification(itemDetails: ItemVolleyModel) {
        val contentTitle = "My notification"

        val builder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(contentTitle)
            .setSmallIcon(R.drawable.blue) // 작은 아이콘 지정
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationContent = "Name: ${itemDetails.name}\nAddress: ${itemDetails.address}\nCall: ${itemDetails.call}"
        builder.setContentText(notificationContent)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MyNotificationService,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notify(NOTIFICATION_ID, builder.build())
        }
    }
}