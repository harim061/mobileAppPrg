package com.example.my18

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

import com.example.my18.databinding.ItemCommentBinding


class Comment(
    val email: String = "",
    val content: String = "",

)
class CommentViewHolder(val binding: ItemCommentBinding) : RecyclerView.ViewHolder(binding.root)

class CommentAdapter(val commentList: MutableList<Comment>) : RecyclerView.Adapter<CommentViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return CommentViewHolder(ItemCommentBinding.inflate(layoutInflater))
    }

    override fun getItemCount(): Int {
        return commentList.size
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = commentList[position]
        holder.binding.emailTextView.text = comment.email
        holder.binding.contentTextView.text = comment.content
        // format and display timestamp
    }
}