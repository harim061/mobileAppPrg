package com.example.my18

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my18.databinding.ItemRetrofitBinding
data class CustomItem(val name: String, val address: String, val call: String, val about: String, val selectedTime:String) {
    // 다른 코드와 기능 구현
}

class MyRetrofitAdapter : RecyclerView.Adapter<MyRetrofitAdapter.MyViewHolder>() {
    private val itemList: MutableList<CustomItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ItemRetrofitBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = itemList[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    fun setData(data: List<CustomItem>) {
        itemList.clear()
        itemList.addAll(data)
        notifyDataSetChanged()
    }

    inner class MyViewHolder(private val binding: ItemRetrofitBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CustomItem) {
            binding.itemTitle.text = item.name
            // Set other views accordingly
            binding.itemAbout.text = item.about
            binding.itemCall.text =item.call
            binding.itemAdd.text= item.address
            binding.itemTime.text= "예약 시간 : "+ item.selectedTime
            // Add click listener if needed
            binding.root.setOnClickListener {
                // Handle item click event
            }
        }
    }
}