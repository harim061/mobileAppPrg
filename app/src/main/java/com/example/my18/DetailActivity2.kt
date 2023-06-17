package com.example.my18



import android.app.TimePickerDialog
import android.os.Bundle
import android.util.Log
import android.widget.TimePicker
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.my18.databinding.ActivityDetail2Binding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import java.util.*

class DetailActivity2 : AppCompatActivity(), OnMapReadyCallback, TimePickerDialog.OnTimeSetListener {
    private lateinit var binding: ActivityDetail2Binding
    private lateinit var itemDetails: ItemVolleyModel
    private lateinit var mGoogleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetail2Binding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        val mapFragment = SupportMapFragment.newInstance()
        supportFragmentManager.beginTransaction().replace(R.id.map, mapFragment).commit()
        mapFragment.getMapAsync(this)

        itemDetails = intent.getSerializableExtra("ITEM_DETAILS") as ItemVolleyModel

        binding.textName.text = itemDetails.name
        binding.textAddress.text = itemDetails.address
        binding.textCall.text = itemDetails.call
        binding.textAbout.text = itemDetails.about

        binding.buttonSave.setOnClickListener {
            showTimePickerDialog()
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
        val data = mapOf(
            "textName" to itemDetails.name,
            "textAddress" to itemDetails.address,
            "textCall" to itemDetails.call,
            "textAbout" to itemDetails.about,
            "selectedTime" to time
        )

        MyApplication.db.collection("items")
            .document()
            .set(data)
            .addOnSuccessListener {
                Toast.makeText(this, "Item saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving item: ${e.message}", Toast.LENGTH_SHORT).show()
                Log.e("Firebase", "Error saving item", e)
            }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mGoogleMap = googleMap
        Log.d("DetailActivity2", "onMapReady called")
        val seoul = LatLng(37.452856614289075, 126.62819490355945)

        val markerOptions = MarkerOptions()
        markerOptions.position(seoul)
        markerOptions.title("중구장애인복지관")
        markerOptions.snippet("중구장애인복지관")

        mGoogleMap.addMarker(markerOptions)

        mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 20f))
    }
}