package com.example.mobileappglnote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import com.example.mobileappglnote.network.ApiClient
import com.example.mobileappglnote.network.QuizApi
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import retrofit2.Call
import retrofit2.Callback
import java.io.IOException

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val menuButton: ImageButton = findViewById(R.id.menuButton)
        menuButton.setOnClickListener {
            val popupMenu = PopupMenu(this, menuButton)
            menuInflater.inflate(R.menu.nav_menu, popupMenu.menu)
            popupMenu.show()

            popupMenu.setOnMenuItemClickListener { item: MenuItem ->
                when (item.itemId) {
                    R.id.menu_home -> true
                    R.id.menu_create -> {
                        startActivity(Intent(this, CreerQuizManuellementActivity::class.java))
                        true
                    }
                    R.id.menu_multi -> {
                        startActivity(Intent(this, ResultsActivity::class.java))
                        true
                    }
                    R.id.menu_auto -> {
                        startActivity(Intent(this, GenerateQuizActivity::class.java))
                        true
                    }
                    else -> false
                }
            }
        }

        findViewById<Button>(R.id.btn_generate_quiz).setOnClickListener {
            startActivity(Intent(this, GenerateQuizActivity::class.java))
        }

        findViewById<Button>(R.id.btn_create_manual)?.setOnClickListener {
            startActivity(Intent(this, CreerQuizManuellementActivity::class.java))
        }


        findViewById<Button>(R.id.btn_group_play).setOnClickListener {
            val intent = Intent(this, GroupPlayActivity::class.java)
            startActivity(intent)
        }
    }



}