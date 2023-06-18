package com.example.my18



import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.my18.MyNotificationService.Companion.NOTIFICATION_CHANNEL_ID
import com.example.my18.databinding.ActivityDetail2Binding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import java.util.*


class DetailActivity2 : AppCompatActivity(), OnMapReadyCallback, TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityDetail2Binding
    private lateinit var itemDetails: ItemVolleyModel
    private lateinit var mGoogleMap: GoogleMap
    private lateinit var notificationBuilder: NotificationCompat.Builder
    private val notificationId = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetail2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        createNotificationBuilder()
        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
        mapFragment.getMapAsync(this)

        itemDetails = intent.getSerializableExtra("ITEM_DETAILS") as ItemVolleyModel

        binding.textName.text = itemDetails.name
        binding.textAddress.text = itemDetails.address
        binding.textCall.text = itemDetails.call
        binding.textAbout.text = itemDetails.about

        binding.buttonSave.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&
                !NotificationManagerCompat.from(this).areNotificationsEnabled()) {
                // 알림이 비활성화된 경우 알림 접근 설정 화면으로 이동
                val intent = Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                startActivity(intent)
            } else {
                // 알림 권한이 허용된 경우
                showTimePickerDialog()
            }
        }
    }

    private fun showTimePickerDialog() {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(this, { view, hourOfDay, minute ->
            val timeString = String.format("%02d:%02d", hourOfDay, minute)
            saveStore(timeString)
        }, hour, minute, true)

        timePickerDialog.show()
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        val timeString = String.format("%02d:%02d", hourOfDay, minute)
        saveStore(timeString)
    }

    private fun saveStore(time: String) {
        val currentUser = FirebaseAuth.getInstance().currentUser
        val userEmail = currentUser?.email ?: "Unknown"

        val data = mapOf(
            "textName" to itemDetails.name,
            "textAddress" to itemDetails.address,
            "textCall" to itemDetails.call,
            "textAbout" to itemDetails.about,
            "selectedTime" to time,
            "email" to userEmail
        )
        val intent = Intent(this, MyNotificationService::class.java)
        intent.putExtra("ITEM_DETAILS", itemDetails)

        MyApplication.db.collection("items")
            .document()
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "예약 완료", Toast.LENGTH_SHORT).show()
                startService(intent)// 알림 표시
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving item: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Error saving item", e)
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        Log.d("DetailActivity2", "onMapReady called")

        val lat = itemDetails.lat?.toDouble()
        val lon = itemDetails.lon?.toDouble()

        if (lat != null && lon != null) {
            val seoul = LatLng(lat, lon)

            val markerOptions = MarkerOptions()
            markerOptions.position(seoul)
            markerOptions.title(itemDetails.name)

            mGoogleMap.addMarker(markerOptions)
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 20f))
        } else {
            Log.d("mobileApp","lat, lon 값이 없습니다.")
        }
    }

    private fun createNotificationBuilder() {
        // Create the notification channel (if needed) for Android Oreo and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Notification Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        // Create the notification builder
        notificationBuilder = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle("<봉사 예약 정보>")
            .setSmallIcon(R.drawable.blue)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
    }

    private fun showNotification(itemDetails: ItemVolleyModel) {
        // Set the content text with item details
        val contentText = "Name: ${itemDetails.name}\nAddress: ${itemDetails.address}\nCall: ${itemDetails.call}\n봉사 예약 완료"
        notificationBuilder.setContentText(contentText)

        // Show the notification
        val notificationManager = getSystemService(NotificationManager::class.java)
        notificationManager?.notify(notificationId, notificationBuilder.build())
    }
}