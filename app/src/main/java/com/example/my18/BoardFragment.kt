package com.example.my18

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my18.databinding.FragmentBoardBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

class BoardFragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    lateinit var binding : FragmentBoardBinding
    private var storageRef: StorageReference = FirebaseStorage.getInstance().reference
    private var firestoreDb: FirebaseFirestore = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoardBinding.inflate(inflater, container, false)

        myCheckPermission(requireActivity() as AppCompatActivity)

        binding.mainFab.setOnClickListener {
            if(MyApplication.checkAuth()){
                val intent = Intent(requireContext(), AddActivity::class.java)
                startActivity(intent)
            } else {
                Toast.makeText(requireContext(),"인증을 진행해 주세요",Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        if(MyApplication.checkAuth()){
            MyApplication.db.collection("news")
                .orderBy("date",Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener {
                        result->
                    val itemList = mutableListOf<ItemBoardModel>()

                    for(document in result){
                        val item = document.toObject(ItemBoardModel::class.java)
                        item.docId = document.id
                        itemList.add(item)
                    }

                    binding.boardRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.boardRecyclerView.adapter = MyBoardAdapter(requireContext(),itemList)

                }
                .addOnFailureListener{
                    Toast.makeText(requireContext(),"데이터 획득 실패",Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun uploadImage(file: Uri, filePath: String) {
        val imgRef = storageRef.child(filePath)
        val uploadTask = imgRef.putFile(file)

        uploadTask.addOnSuccessListener { taskSnapshot ->
            imgRef.downloadUrl.addOnSuccessListener { uri ->
                val data = hashMapOf(
                    "url" to uri.toString()
                )

                firestoreDb.collection("images").document().set(data)
                    .addOnSuccessListener {
                        Log.d("mobileApp", "Image URL saved in Firestore: ${uri.toString()}")
                    }
                    .addOnFailureListener { e ->
                        Log.w("mobileApp", "Error writing document", e)
                    }
            }
        }.addOnFailureListener { exception ->
            Log.e("mobileApp", "Failure: ${exception.message}")
        }
    }

    fun myCheckPermission(activity: AppCompatActivity) {
        val requestPermissionLauncher = activity.registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                Toast.makeText(activity, "권한 승인", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(activity, "권한 거부", Toast.LENGTH_SHORT).show()
            }
        }

        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (ContextCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                activity.startActivity(intent)
            }
        }
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            BoardFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}
