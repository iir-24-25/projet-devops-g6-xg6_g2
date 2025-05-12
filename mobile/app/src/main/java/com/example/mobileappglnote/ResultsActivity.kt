package com.example.mobileappglnote

import android.os.Bundle
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)

        val score = intent.getIntExtra("SCORE", 0)
        findViewById<TextView>(R.id.scoreTitle).text = "Your score: $score"
    }
}
