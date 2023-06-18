package com.example.my18

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.my18.databinding.FragmentVolleyBinding
import org.json.JSONArray
import org.json.JSONObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [VolleyFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class VolleyFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

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
    ): View? {
        // Inflate the layout for this fragment
        val binding = FragmentVolleyBinding.inflate(inflater, container, false)
        var mutableList: MutableList<ItemVolleyModel>



            val url ="https://smart.incheon.go.kr/server/rest/services/Hosted/장애인_복지시설_정보/FeatureServer/187/query?where=1%3D1&outFields=*&outSR=4326&f=json"
            Log.d("mobileApp",url)
            val queue = Volley.newRequestQueue(activity)
            val jsonRequest:JsonObjectRequest = object:JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                Response.Listener<JSONObject>{
                     response ->
                    mutableList = mutableListOf<ItemVolleyModel>()
                    val jsonArray:JSONArray = response.getJSONArray("features")
                    for(i in 0 until jsonArray.length()){
                        val feature = jsonArray.getJSONObject(i)
                        val attributes = feature.getJSONObject("attributes")
                        ItemVolleyModel().run{
                            address = attributes.getString("주소")
                            call = attributes.getString("전화번호")
                            about = attributes.getString("비고")
                            name = attributes.getString("시설명")
                            lat = attributes.getString("위도")
                            lon = attributes.getString("경도")
                            mutableList.add(this)
                        }
                    }

                    binding.volleyRecyclerView.layoutManager = LinearLayoutManager(activity)

                    binding.volleyRecyclerView.adapter = MyVolleyAdapter(activity as Context, mutableList).apply {
                        onItemClick = { item ->
                            // TODO: Implement your action to open a new activity or fragment here.
                            // For example, if you have a DetailActivity2:
                            val intent = Intent(context, DetailActivity2::class.java).apply {
                                putExtra("ITEM_DETAILS", item)
                            }
                            Log.d("mobileApp","완료")
                            startActivity(intent)
                        }
                    }
                },
                Response.ErrorListener{
                    Log.d("mobileApp","error .. $it")
                }
            ){
                override fun getHeaders(): MutableMap<String, String> {
                    val map =  mutableMapOf<String,String>(
                        "User-agent" to "Mozilla/5.0"
                    )
                    return map
                }
            }
            queue.add(jsonRequest)


        mutableList = mutableListOf<ItemVolleyModel>()
        binding.volleyRecyclerView.layoutManager = LinearLayoutManager(activity)
        binding.volleyRecyclerView.adapter = MyVolleyAdapter(activity as Context, mutableList)

        return binding.root
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment VolleyFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            VolleyFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}