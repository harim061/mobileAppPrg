package com.example.my18

import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my18.MyApplication.Companion.auth
import com.example.my18.databinding.FragmentRetrofitBinding
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RetrofitFragment.newInstance] factory method to
 * create an instance of this fragment.
 */


class RetrofitFragment : Fragment() {
    private lateinit var binding: FragmentRetrofitBinding
    private lateinit var adapter: MyRetrofitAdapter
    private lateinit var sharedPreferences: SharedPreferences
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
    private lateinit var authStateListener: FirebaseAuth.AuthStateListener
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRetrofitBinding.inflate(inflater, container, false)

        // Set up SharedPreferences
        sharedPreferences = requireContext().getSharedPreferences("MyPrefs", MODE_PRIVATE)

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val email = currentUser.email

            val loadedImage = loadImageFromSharedPreferences(email)
            loadedImage?.let { binding.userImg.setImageBitmap(it) }

            val loadedText = loadTextFromSharedPreferences(email)
            binding.introEditText.setText(loadedText)

            val username = email?.substringBefore("@")
            binding.email.text = username
        }

        // Set up RecyclerView and Adapter
        adapter = MyRetrofitAdapter()
        binding.retrofitRecyclerView.adapter = adapter
        binding.retrofitRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch data from Firestore and update the adapter
        fetchFirebaseData()

        binding.saveButton.setOnClickListener {
            auth.currentUser?.email?.let { email ->
                saveTextToSharedPreferences(binding.introEditText.text.toString(), email)
            }
        }

        binding.btnGallery.setOnClickListener{
            var intent = Intent(Intent.ACTION_VIEW, Uri.parse("content://media/internal/images/media"))
            startActivity(intent)
        }

        val requestGalleryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            try{
                val calRatio = calculateInSampleSize(it.data!!.data!!,
                    resources.getDimensionPixelSize(R.dimen.imgSize),
                    resources.getDimensionPixelSize(R.dimen.imgSize) )
                val option = BitmapFactory.Options()
                option.inSampleSize = calRatio
                val inputStream = requireContext().contentResolver.openInputStream(it.data!!.data!!)
                val bitmap = BitmapFactory.decodeStream(inputStream, null, option)
                inputStream?.close()
                auth.currentUser?.email?.let { email ->
                    binding.userImg.setImageBitmap(bitmap)
                    if (bitmap != null) {
                        saveImageToSharedPreferences(bitmap, email)
                    }
                }
            }catch (e:Exception) {
                e.printStackTrace()
            }
        }

        binding.btnGallery.setOnClickListener{
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.type = "image/*"
            requestGalleryLauncher.launch(intent)
        }

        authStateListener = FirebaseAuth.AuthStateListener {
            val user = it.currentUser
            if (user != null) {
                // 사용자가 로그인한 경우
                val email = user.email
                val username = email?.substringBefore("@")
                binding.email.text = username

                val loadedImage = loadImageFromSharedPreferences(email)
                loadedImage?.let { binding.userImg.setImageBitmap(it) }

                val loadedText = loadTextFromSharedPreferences(email)
                binding.introEditText.setText(loadedText)

                fetchFirebaseData() // 데이터 다시 불러오기
            } else {
                // 사용자가 로그아웃한 경우
                binding.email.text = "로그인"
                binding.userImg.setImageBitmap(null)
                binding.introEditText.setText("")
                adapter.setData(emptyList()) // 목록 비우기
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        auth.addAuthStateListener(authStateListener)
    }

    override fun onStop() {
        super.onStop()
        if (authStateListener != null) {
            auth.removeAuthStateListener(authStateListener)
        }
    }

    private fun calculateInSampleSize(fileUri: Uri, reqWidth: Int, reqHeight: Int): Int {
        val options = BitmapFactory.Options()
        options.inJustDecodeBounds = true
        try {
            val inputStream = requireContext().contentResolver.openInputStream(fileUri)
            BitmapFactory.decodeStream(inputStream, null, options)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun fetchFirebaseData() {
        val currentUser = auth.currentUser
        val addressCounts = mutableMapOf<String, Int>()

        currentUser?.let { user ->
            val email = user.email
            firestore.collection("items")
                .whereEqualTo("email", email)
                .addSnapshotListener { snapshot, exception ->
                    if (exception != null) {
                        Log.e("Firebase", "Error getting documents: ${exception.message}")
                        return@addSnapshotListener
                    }

                    val customItems = mutableListOf<CustomItem>()
                    snapshot?.let { snapshot ->
                        for (document in snapshot.documents) {
                            val address = document.getString("textAddress") ?: ""

                            // 주소값에 따라 카운트 증가
                            if (address.contains("중구")) {
                                incrementAddressCount(addressCounts, "중구")
                            }
                            if (address.contains("연수구")) {
                                incrementAddressCount(addressCounts, "연수구")
                            }
                            if (address.contains("남동구")) {
                                incrementAddressCount(addressCounts, "남동구")
                            }
                            if (address.contains("미추홀구")) {
                                incrementAddressCount(addressCounts, "미추홀구")
                            }
                            else{
                                incrementAddressCount(addressCounts,"그 외")
                            }
                            // ... 나머지 주소에 대한 처리 ...

                            val name = document.getString("textName") ?: ""
                            val call = document.getString("textCall") ?: ""
                            val about = document.getString("textAbout") ?: ""
                            val selectedTime = document.getString("selectedTime") ?: ""
                            val customItem = CustomItem(name, address, call, about, selectedTime)
                            customItems.add(customItem)
                        }


                        for (document in snapshot.documents) {
                            val name = document.getString("textName") ?: ""
                            val address = document.getString("textAddress") ?: ""
                            val call = document.getString("textCall") ?: ""
                            val about = document.getString("textAbout") ?: ""
                            val selectedTime = document.getString("selectedTime") ?: ""
                            val customItem = CustomItem(name, address, call, about, selectedTime)
                            customItems.add(customItem)
                        }
                        updateChart(addressCounts)
                        adapter.setData(customItems)
                        adapter.setData(customItems)
                    }
                }
        }

    }
    private fun incrementAddressCount(addressCounts: MutableMap<String, Int>, address: String) {
        val count = addressCounts[address] ?: 0
        addressCounts[address] = count + 1
    }

    private fun updateChart(addressCounts: Map<String, Int>) {
        val chart = binding.chart
        val c = Color.parseColor("#4169E1")
        val entries = mutableListOf<BarEntry>()
        val labels = mutableListOf<String>()
        val otherLabel = "그 외"

        // 주소별 카운트를 차트 데이터로 변환
        addressCounts.forEach { (address, count) ->
            if (address in listOf("중구", "연수구", "남동구", "미추홀구")) {
                entries.add(BarEntry(entries.size.toFloat(), count.toFloat()))
                labels.add(address)
            }
        }

        // "그 외" 라벨과 카운트 추가
        val otherCount = addressCounts.values.sum() - entries.sumBy { it.y.toInt() }
        if (otherCount > 0) {
            entries.add(BarEntry(entries.size.toFloat(), otherCount.toFloat()))
            labels.add(otherLabel)
        }

        val dataSet = BarDataSet(entries, "")
        dataSet.color = c
        dataSet.valueTextColor = Color.BLACK

        val data = BarData(dataSet)

        chart.data = data
        data.barWidth = 0.6f
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.setCenterAxisLabels(true)
        chart.xAxis.granularity = 1f
        chart.xAxis.axisMinimum = -0.5f
        chart.xAxis.axisMaximum = entries.size - 0.5f
        chart.invalidate()
        chart.animateY(500)
    }




    private fun saveTextToSharedPreferences(text: String, email: String) {
        val editor = sharedPreferences.edit()
        editor.putString(email + "text", text)
        editor.apply()
    }

    private fun loadTextFromSharedPreferences(email: String?): String? {
        return sharedPreferences.getString(email + "text", null)
    }

    private fun saveImageToSharedPreferences(bitmap: Bitmap, email: String) {
        val editor = sharedPreferences.edit()

        val byteArrayOutputStream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()

        val encodedImage = Base64.encodeToString(byteArray, Base64.DEFAULT)
        editor.putString(email + "image", encodedImage)
        editor.apply()
    }

    private fun loadImageFromSharedPreferences(email: String?): Bitmap? {
        val encodedImage = sharedPreferences.getString(email + "image", null)

        if (encodedImage != null) {
            val byteArray = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        }
        return null
    }

    companion object {
        fun newInstance(param1: String, param2: String) = RetrofitFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_PARAM1, param1)
                putString(ARG_PARAM2, param2)
            }
        }
    }
}