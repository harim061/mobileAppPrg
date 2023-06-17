package com.example.my18

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my18.databinding.ItemVolleyBinding

class MyVolleyViewHolder(val binding: ItemVolleyBinding): RecyclerView.ViewHolder(binding.root)

class MyVolleyAdapter(val context: Context, val datas: MutableList<ItemVolleyModel>?): RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    var onItemClick: ((ItemVolleyModel) -> Unit)? = null
    override fun getItemCount(): Int{
        return datas?.size ?: 0
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder
            = MyVolleyViewHolder(ItemVolleyBinding.inflate(LayoutInflater.from(parent.context), parent, false))

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val binding=(holder as MyVolleyViewHolder).binding

        //add......................................
        val model = datas!![position]

        binding.itemTitle.text =model.name.toString()
        binding.itemAdd.text = "주소 : "+model.address.toString()
        binding.itemCall.text="전화번호 : "+model.call.toString()
        binding.itemAbout.text="봉사 내용 : "+model.about.toString()
        //binding.itemDesc.text = model.description
        binding.root.setOnClickListener{
            onItemClick?.invoke(model)
        }
        //Glide.with(context).load(model.urlToImage).into(binding.itemImage)
    }
}