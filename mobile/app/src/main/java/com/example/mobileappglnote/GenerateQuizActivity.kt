package com.example.mobileappglnote

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileappglnote.model.QuizQuestion
import com.example.mobileappglnote.network.ApiClient
import com.example.mobileappglnote.network.QuizApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.net.ConnectException
import java.net.SocketTimeoutException
import javax.net.ssl.SSLHandshakeException

class GenerateQuizActivity : AppCompatActivity() {

    private lateinit var themeEditText: EditText
    private lateinit var levelSpinner: Spinner
    private lateinit var numQuestionsEditText: EditText
    private lateinit var generateQuizButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_generate_quiz)

        // Initialize UI elements with proper null checks
        themeEditText = findViewById<EditText>(R.id.theme).apply {
            if (this == null) throw IllegalStateException("Theme EditText not found")
        }
        levelSpinner = findViewById<Spinner>(R.id.level).apply {
            if (this == null) throw IllegalStateException("Level Spinner not found")
        }
        numQuestionsEditText = findViewById<EditText>(R.id.num_questions).apply {
            if (this == null) throw IllegalStateException("NumQuestions EditText not found")
        }
        generateQuizButton = findViewById<Button>(R.id.generate_quiz_button).apply {
            if (this == null) throw IllegalStateException("Generate Button not found")
        }

        generateQuizButton.setOnClickListener {
            try {
                val theme = themeEditText.text.toString().trim()
                val level = levelSpinner.selectedItem?.toString()?.trim() ?: ""
                val numQuestionsStr = numQuestionsEditText.text.toString().trim()

                when {
                    theme.isEmpty() -> showError("Please enter a theme")
                    level.isEmpty() -> showError("Please select a difficulty level")
                    numQuestionsStr.isEmpty() -> showError("Please enter number of questions")
                    else -> {
                        try {
                            val numQuestions = numQuestionsStr.toInt()
                            when {
                                numQuestions <= 0 -> showError("Number must be positive")
                                numQuestions > 20 -> showError("Maximum 20 questions allowed")
                                else -> generateQuiz(theme, level, numQuestions)
                            }
                        } catch (e: NumberFormatException) {
                            showError("Invalid number format")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("GenerateQuiz", "Button click failed", e)
                showError("Unexpected error occurred")
            }
        }
    }

    private fun generateQuiz(theme: String, level: String, numQuestions: Int) {
        if (isFinishing || isDestroyed) return

        try {
            val generatedQuestions = generateQuestions(theme, level, numQuestions).takeIf { it.isNotEmpty() }
                ?: run {
                    showError("Failed to generate questions")
                    return
                }

            val quizData = QuizApi.QuizData(
                questions = generatedQuestions.map { question ->
                    QuizApi.QuestionData(
                        question = question.questionText,
                        answers = question.options,
                        correctAnswers = question.correctAnswerIndex.toString(),
                        type = level
                    )
                }
            )

            ApiClient.instance.createQuiz(quizData).enqueue(object : Callback<QuizApi.CreateQuizResponse> {
                override fun onResponse(
                    call: Call<QuizApi.CreateQuizResponse>,
                    response: Response<QuizApi.CreateQuizResponse>
                ) {
                    if (isFinishing || isDestroyed) return

                    try {
                        when {
                            response.isSuccessful -> {
                                response.body()?.let { quizResponse ->

                                } ?: showError("Empty server response")
                            }
                            else -> {
                                val errorBody = response.errorBody()?.string() ?: "No details"
                                showError("Server error ${response.code()}: $errorBody")
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("API_ERROR", "Response processing failed", e)
                        showError("Error processing response")
                    }
                }

                override fun onFailure(call: Call<QuizApi.CreateQuizResponse>, t: Throwable) {
                    if (isFinishing || isDestroyed) return

                    val errorMessage = when (t) {
                        is SocketTimeoutException -> "Request timeout"
                        is ConnectException -> "No internet connection"
                        is SSLHandshakeException -> "Security error"
                        else -> "Network error: ${t.localizedMessage ?: "Unknown error"}"
                    }
                    showError(errorMessage)
                }
            })
        } catch (e: Exception) {
            Log.e("GenerateQuiz", "Quiz generation failed", e)
            showError("Failed to generate quiz")
        }
    }

    private fun generateQuestions(theme: String, level: String, count: Int): List<QuizQuestion> {
        return try {
            List(count) { i ->
                QuizQuestion(
                    id = i + 1,
                    questionText = "Question about $theme ($level) #${i + 1}",
                    options = listOf("Correct", "Wrong 1", "Wrong 2", "Wrong 3"),
                    correctAnswerIndex = 0
                )
            }
        } catch (e: Exception) {
            Log.e("QuestionGen", "Failed to generate questions", e)
            emptyList()
        }
    }

    private fun showError(message: String) {
        if (!isFinishing && !isDestroyed) {
            runOnUiThread {
                Toast.makeText(
                    this@GenerateQuizActivity,
                    message,
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    companion object {
        private const val MAX_QUESTIONS = 20
    }
}