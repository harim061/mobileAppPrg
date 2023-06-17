package com.example.my18

import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my18.MyApplication.Companion.auth
import com.example.my18.databinding.FragmentRetrofitBinding
import com.google.firebase.firestore.FirebaseFirestore


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
            val username = email?.substringBefore("@")
            binding.email.text = username
        }

        // Set up RecyclerView and Adapter
        adapter = MyRetrofitAdapter()
        binding.retrofitRecyclerView.adapter = adapter
        binding.retrofitRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        // Fetch data from Firestore and update the adapter
        fetchFirebaseData()

        // Set up the one-liner introduction
        binding.introEditText.setText(getOneLinerIntroduction())
        binding.saveButton.setOnClickListener {
            saveOneLinerIntroduction()
        }

        return binding.root
    }

    private fun fetchFirebaseData() {
        firestore.collection("items")
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    Log.e("Firebase", "Error getting documents: ${exception.message}")
                    return@addSnapshotListener
                }

                val customItems = mutableListOf<CustomItem>()
                snapshot?.let { snapshot ->
                    for (document in snapshot.documents) {
                        val name = document.getString("textName") ?: ""
                        val address = document.getString("textAddress") ?: ""
                        val call = document.getString("textCall") ?: ""
                        val about = document.getString("textAbout") ?: ""
                        val selectedTime = document.getString("selectedTime") ?:""
                        val customItem = CustomItem(name, address, call, about,selectedTime)
                        customItems.add(customItem)
                    }
                    adapter.setData(customItems)
                }
            }
    }

    private fun getOneLinerIntroduction(): String {
        return sharedPreferences.getString("oneLinerIntroduction", "") ?: ""
    }

    private fun saveOneLinerIntroduction() {
        val oneLiner = binding.introEditText.text.toString()
        sharedPreferences.edit().putString("oneLinerIntroduction", oneLiner).apply()
    }

    companion object {
        fun newInstance(): RetrofitFragment {
            return RetrofitFragment()
        }
    }
}