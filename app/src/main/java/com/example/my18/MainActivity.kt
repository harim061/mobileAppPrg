package com.example.my18

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.RequiresApi
import com.example.my18.databinding.ActivityMainBinding
import java.security.MessageDigest

class MainActivity : AppCompatActivity() {
    lateinit var binding: ActivityMainBinding
    lateinit var volleyFragment: VolleyFragment
    lateinit var retrofitFragment: RetrofitFragment
    lateinit var boardFragment: BoardFragment
    var mode = "volley" //현재 내가 무엇을 보여주는지(상태를 알려주는) 변수
    var authMenuItem: MenuItem? = null
    private val TAG = "SOL_LOG"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        volleyFragment= VolleyFragment()
        retrofitFragment= RetrofitFragment()
        boardFragment = BoardFragment()

        supportFragmentManager.beginTransaction()
            .replace(R.id.activity_content, volleyFragment)
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.activity_content, retrofitFragment)
            .hide(retrofitFragment)
            .commit()
        supportFragmentManager.beginTransaction()
            .add(R.id.activity_content, boardFragment)
            .hide(boardFragment)
            .commit()
        supportActionBar?.title="DoDream"
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        authMenuItem = menu!!.findItem(R.id.menu_auth)
        if (MyApplication.checkAuth()){
            authMenuItem!!.title="${MyApplication.email?.substringBefore("@")}님"
        }
        else {
            authMenuItem!!.title = "인증"
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onStart() {
//        Intent에서 finish() 돌아올 때 실행
//        onCreate -> onStart -> onCreateOptionMenu
        if(authMenuItem != null){
            if (MyApplication.checkAuth()){
                authMenuItem!!.title="${MyApplication.email?.substringBefore("@")}님"
            }
            else {
                authMenuItem!!.title = "인증"
            }
        }
        super.onStart()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId === R.id.menu_volley && mode !== "volley"){
            supportFragmentManager.beginTransaction()
                .show(volleyFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(retrofitFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(boardFragment)
                .commit()
            mode="volley"
            supportActionBar?.title="봉사신청"
        }
        else if(item.itemId === R.id.menu_retrofit && mode !== "retrofit"){
            supportFragmentManager.beginTransaction()
                .show(retrofitFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(volleyFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(boardFragment)
                .commit()
            mode="retrofit"
            supportActionBar?.title="myPage"
        }
        else if(item.itemId === R.id.menu_board && mode !== "board"){
            supportFragmentManager.beginTransaction()
                .show(boardFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(volleyFragment)
                .commit()
            supportFragmentManager.beginTransaction()
                .hide(retrofitFragment)
                .commit()
            mode="board"
            supportActionBar?.title="상담소"
        }
        else if(item.itemId === R.id.menu_auth) {
            val intent = Intent(this, AuthActivity::class.java)
            if(authMenuItem!!.title!!.equals("인증")){
                intent.putExtra("data", "logout")
            }
            else {
                intent.putExtra("data", "login")
            }
            startActivity(intent)
        }
        return super.onOptionsItemSelected(item)
    }
}