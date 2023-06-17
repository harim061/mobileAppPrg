package com.example.my18

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.my18.databinding.ActivityBoardDetailBinding


class BoardDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBoardDetailBinding
    private lateinit var commentAdapter: CommentAdapter
    private val commentList = mutableListOf<Comment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBoardDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Get docId from Intent
        val docId = intent.getStringExtra("docId")

        // Get the details of the post
        MyApplication.db.collection("news").document(docId!!)
            .get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val item = document.toObject(ItemBoardModel::class.java)!!
                    binding.emailTextView.text = item.email
                    binding.dateTextView.text = item.date
                    binding.contentTextView.text = item.content
                } else {
                    Log.d("BoardDetail", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("BoardDetail", "get failed with ", exception)
            }

        // Setup RecyclerView for comments
        commentAdapter = CommentAdapter(commentList)
        binding.commentRecyclerView.adapter = commentAdapter
        binding.commentRecyclerView.layoutManager = LinearLayoutManager(this)

        // Get the comments for this post
        MyApplication.db.collection("news").document(docId)
            .collection("comments")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapshot, _ ->
                commentList.clear()
                for (document in querySnapshot!!.documents) {
                    val comment = document.toObject(Comment::class.java)!!
                    commentList.add(comment)
                }
                commentAdapter.notifyDataSetChanged()
            }

        // Post a new comment
        binding.submitCommentButton.setOnClickListener {
            val commentContent = binding.commentEditText.text.toString()
            if (commentContent.isNotBlank()) {
                val userEmail = if (MyApplication.checkAuth()) {
                    // 사용자가 인증된 상태라면 로그인된 사용자의 이메일 사용
                    MyApplication.email
                } else {
                    // 사용자가 인증되지 않은 상태라면 기본값인 "익명" 사용
                    "익명"
                }

                val commentData = hashMapOf(
                    "email" to userEmail,
                    "content" to commentContent,
                    "timestamp" to System.currentTimeMillis()
                )

                MyApplication.db.collection("news").document(docId)
                    .collection("comments")
                    .add(commentData)
                    .addOnSuccessListener {
                        Toast.makeText(this, "댓글이 성공적으로 작성되었습니다.", Toast.LENGTH_SHORT).show()
                        binding.commentEditText.text.clear()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "댓글 작성에 실패했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
        }
    }
}