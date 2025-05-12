package com.example.mobileappglnote

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileappglnote.model.QuizQuestion
import com.example.mobileappglnote.network.QuizApi
import com.example.mobileappglnote.network.ApiClient
import android.content.Intent
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayQuizActivity : AppCompatActivity() {
    private lateinit var questionsContainer: LinearLayout
    private val userAnswers = mutableMapOf<Int, Int>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_quiz)

        val roomId = intent.getStringExtra("ROOM_ID")!!
        fetchQuizQuestions(roomId)

        findViewById<Button>(R.id.submit_button).setOnClickListener {
            submitAnswers(roomId)
        }
    }

    private fun fetchQuizQuestions(roomId: String) {
        ApiClient.instance.getQuizQuestions(roomId).enqueue(object : Callback<List<QuizQuestion>> {
            override fun onResponse(call: Call<List<QuizQuestion>>, response: Response<List<QuizQuestion>>) {
                if (response.isSuccessful) {
                    renderQuestions(response.body()!!)
                }
            }
            override fun onFailure(call: Call<List<QuizQuestion>>, t: Throwable) {
                Toast.makeText(this@PlayQuizActivity, "Failed to load questions", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun renderQuestions(questions: List<QuizQuestion>) {
        questions.forEach { q ->
            val radioGroup = RadioGroup(this).apply {
                orientation = RadioGroup.VERTICAL
                q.options.forEachIndexed { idx, option ->
                    addView(RadioButton(this@PlayQuizActivity).apply {
                        text = option
                        setOnClickListener { userAnswers[q.id] = idx }
                    })
                }
            }
            questionsContainer.addView(TextView(this).apply { text = q.questionText })
            questionsContainer.addView(radioGroup)
        }
    }

    private fun submitAnswers(roomId: String) {
        val answers = userAnswers.map { QuizApi.Answer(it.key, it.value) }
        ApiClient.instance.submitAnswers(roomId, answers).enqueue(object : Callback<QuizApi.QuizResult> {
            override fun onResponse(call: Call<QuizApi.QuizResult>, response: Response<QuizApi.QuizResult>) {
                startActivity(Intent(this@PlayQuizActivity, ResultsActivity::class.java).apply {
                    putExtra("SCORE", response.body()?.score)
                })
            }
            override fun onFailure(call: Call<QuizApi.QuizResult>, t: Throwable) {
                Toast.makeText(this@PlayQuizActivity, "Submission failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}