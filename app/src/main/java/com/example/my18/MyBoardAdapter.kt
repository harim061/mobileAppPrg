package com.example.my18

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.my18.databinding.ItemBoardBinding


class MyBoardViewHolder(val binding: ItemBoardBinding) : RecyclerView.ViewHolder(binding.root)

class MyBoardAdapter(val context: Context, val itemList: MutableList<ItemBoardModel>): RecyclerView.Adapter<MyBoardViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyBoardViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return MyBoardViewHolder(ItemBoardBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return itemList.size
    }

    override fun onBindViewHolder(holder: MyBoardViewHolder, position: Int) {
        val data = itemList.get(position)

        holder.binding.run {
            itemEmailView.text=data.email
            itemDateView.text=data.date
            itemContentView.text=data.content

            root.setOnClickListener {
                val intent = Intent(context, BoardDetailActivity::class.java).apply {
                    putExtra("docId", data.docId)
                }
                context.startActivity(intent)
            }
        }

        //스토리지 이미지 다운로드........................

    }
}
