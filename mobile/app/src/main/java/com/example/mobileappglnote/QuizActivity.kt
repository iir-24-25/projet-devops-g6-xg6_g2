package com.example.mobileappglnote

import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import okhttp3.*
import java.io.IOException

class QuizActivity : AppCompatActivity() {

    private lateinit var tvQuestion: TextView
    private lateinit var tvTimer: TextView
    private lateinit var answersContainer: LinearLayout
    private lateinit var submitButton: Button

    private val client = OkHttpClient()
    private val questions = mutableListOf<QuizQuestionData>()
    private val userResponses = mutableListOf<UserResponse>()
    private var currentIndex = 0
    private var countdown: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        tvQuestion = findViewById(R.id.tv_question)
        tvTimer = findViewById(R.id.tv_timer)
        answersContainer = findViewById(R.id.answers_container)
        submitButton = findViewById(R.id.btn_submit)

        val roomId = intent.getStringExtra("roomId") ?: ""
        fetchQuestions(roomId)

        submitButton.setOnClickListener {
            countdown?.cancel()
            handleAnswer()
        }
    }

    private fun fetchQuestions(roomId: String) {
        val url = "http://192.168.1.44:5000/quiz/$roomId/questions"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@QuizActivity, "Erreur réseau", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body?.string()?.let { json ->
                    val type = object : TypeToken<List<QuizQuestionData>>() {}.type
                    val data: List<QuizQuestionData> = Gson().fromJson(json, type)
                    questions.addAll(data)
                    runOnUiThread { showQuestion() }
                }
            }
        })
    }

    private fun showQuestion() {
        if (currentIndex >= questions.size) {
            showResults()
            return
        }

        val question = questions[currentIndex]
        tvQuestion.text = "Q${currentIndex + 1}. ${question.question}"
        answersContainer.removeAllViews()

        val isMultiple = question.correctAnswerIds.size > 1

        question.answers.forEach { answer ->
            val view = if (isMultiple) CheckBox(this) else RadioButton(this)
            view.text = answer.text
            view.tag = answer.id
            answersContainer.addView(view)
        }

        startTimer()
    }

    private fun startTimer() {
        tvTimer.visibility = View.VISIBLE
        var secondsLeft = 20
        tvTimer.text = "Temps : $secondsLeft s"

        countdown = object : CountDownTimer(20000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                secondsLeft--
                tvTimer.text = "Temps : $secondsLeft s"
            }

            override fun onFinish() {
                handleAnswer()
            }
        }.start()
    }

    private fun handleAnswer() {
        val selectedIds = mutableListOf<Int>()

        for (i in 0 until answersContainer.childCount) {
            val view = answersContainer.getChildAt(i)
            if ((view is CheckBox && view.isChecked) || (view is RadioButton && view.isChecked)) {
                selectedIds.add(view.tag as Int)
            }
        }

        val correctIds = questions[currentIndex].correctAnswerIds
        val isCorrect = selectedIds.sorted() == correctIds.sorted()

        userResponses.add(
            UserResponse(
                question = questions[currentIndex].question,
                selected = selectedIds,
                correct = correctIds,
                isCorrect = isCorrect
            )
        )

        currentIndex++
        showQuestion()
    }

    private fun showResults() {
        tvQuestion.text = "Quiz terminé !"
        answersContainer.removeAllViews()
        submitButton.visibility = View.GONE
        tvTimer.visibility = View.GONE

        val score = userResponses.count { it.isCorrect }
        val scoreText = TextView(this).apply {
            text = "Score : $score / ${userResponses.size}"
            textSize = 20f
        }
        answersContainer.addView(scoreText)

        userResponses.forEachIndexed { index, response ->
            val correction = TextView(this).apply {
                text = """
                    Q${index + 1}. ${response.question}
                    ✅ Correct : ${response.correct.joinToString()}
                    🧠 Votre réponse : ${response.selected.joinToString()}
                    ${if (response.isCorrect) "Bonne réponse ✅" else "Mauvaise réponse ❌"}
                """.trimIndent()
                setPadding(0, 16, 0, 16)
            }
            answersContainer.addView(correction)
        }
    }

    override fun onDestroy() {
        countdown?.cancel()
        super.onDestroy()
    }

    data class QuizQuestionData(
        val id: Int,
        val question: String,
        val answers: List<Answer>,
        val correctAnswerIds: List<Int>
    )

    data class Answer(
        val id: Int,
        val text: String
    )

    data class UserResponse(
        val question: String,
        val selected: List<Int>,
        val correct: List<Int>,
        val isCorrect: Boolean
    )
}


