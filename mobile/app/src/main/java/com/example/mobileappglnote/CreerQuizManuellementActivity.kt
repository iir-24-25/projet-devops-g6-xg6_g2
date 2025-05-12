package com.example.mobileappglnote

import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.mobileappglnote.network.ApiClient
import com.example.mobileappglnote.network.QuizApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CreerQuizManuellementActivity : AppCompatActivity() {

    private lateinit var questionsContainer: LinearLayout
    private lateinit var roomIdText: TextView
    private lateinit var saveQuizButton: Button
    private val questionViews = mutableListOf<View>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_creer_quiz)

        questionsContainer = findViewById(R.id.questions_container)
        roomIdText = findViewById(R.id.room_id)
        saveQuizButton = findViewById(R.id.save_quiz_btn)

        findViewById<Button>(R.id.add_question_btn).setOnClickListener {
            addQuestionView()
        }

        saveQuizButton.setOnClickListener {
            validateAndSaveQuiz()
        }
    }

    private fun addQuestionView() {
        val questionView = layoutInflater.inflate(R.layout.item_question, null)
        questionViews.add(questionView)
        questionsContainer.addView(questionView)

        // Initialiser le Spinner avec les types de questions
        val questionTypeSpinner: Spinner = questionView.findViewById(R.id.question_type_spinner)
        val adapter = ArrayAdapter.createFromResource(
            this,
            R.array.question_types, // Liste des types dans strings.xml
            android.R.layout.simple_spinner_item
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        questionTypeSpinner.adapter = adapter

        val addOptionButton = questionView.findViewById<Button>(R.id.add_option_btn)
        addOptionButton.setOnClickListener {
            addOptionView(questionView)
        }

        // Ajouter 2 options par défaut
        addOptionView(questionView)
        addOptionView(questionView)
    }

    private fun addOptionView(questionView: View) {
        val optionsContainer = questionView.findViewById<LinearLayout>(R.id.options_container)
        val optionView = layoutInflater.inflate(R.layout.item_option, null)
        optionsContainer.addView(optionView)
    }

    private fun validateAndSaveQuiz() {
        val quizData = prepareQuizData() ?: return

        saveQuizButton.isEnabled = false

        ApiClient.instance.createQuiz(quizData).enqueue(object : Callback<QuizApi.CreateQuizResponse> {
            override fun onResponse(
                call: Call<QuizApi.CreateQuizResponse>,
                response: Response<QuizApi.CreateQuizResponse>
            ) {
                saveQuizButton.isEnabled = true

                if (response.isSuccessful) {
                    handleSuccess(response.body()?.room_id.toString())
                } else {
                    // Log de la réponse complète pour obtenir plus d'informations
                    val errorMsg = response.errorBody()?.string() ?: "Erreur inconnue"
                    Log.e("APIError", "Code d'erreur : ${response.code()}, Message : $errorMsg")
                    showError("Erreur serveur : ${response.code()} - $errorMsg")
                }
            }

            override fun onFailure(call: Call<QuizApi.CreateQuizResponse>, t: Throwable) {
                saveQuizButton.isEnabled = true
                showError("Connection failed: ${t.message}")
            }
        })
    }

    private fun prepareQuizData(): QuizApi.QuizData? {
        if (questionViews.isEmpty()) {
            showError("Veuillez ajouter au moins une question.")
            return null
        }

        val questions = mutableListOf<QuizApi.QuestionData>()

        for (questionView in questionViews) {
            val questionText = questionView.findViewById<EditText>(R.id.question_edittext).text.toString().trim()
            if (questionText.isEmpty()) {
                showError("Veuillez saisir l'intitulé de la question.")
                return null
            }

            val optionsContainer = questionView.findViewById<LinearLayout>(R.id.options_container)
            if (optionsContainer.childCount < 2) {
                showError("Chaque question doit avoir au moins deux options.")
                return null
            }

            val options = mutableListOf<String>()
            var correctAnswerIndex: Int? = null

            for (i in 0 until optionsContainer.childCount) {
                val optionView = optionsContainer.getChildAt(i)
                val optionText = optionView.findViewById<EditText>(R.id.option_edittext).text.toString().trim()
                if (optionText.isEmpty()) {
                    showError("Veuillez remplir le texte de chaque option.")
                    return null
                }

                options.add(optionText)

                if (optionView.findViewById<CheckBox>(R.id.correct_checkbox).isChecked) {
                    if (correctAnswerIndex != null) {
                        showError("Une seule réponse correcte par question est autorisée.")
                        return null
                    }
                    correctAnswerIndex = i
                }
            }

            if (correctAnswerIndex == null) {
                showError("Veuillez sélectionner la bonne réponse.")
                return null
            }

            // Récupérer le type de question sélectionné
            val questionType = questionView.findViewById<Spinner>(R.id.question_type_spinner).selectedItem.toString()

            // Ajout de la question avec les options et la réponse correcte
            questions.add(QuizApi.QuestionData(questionText, options, correctAnswerIndex.toString(), questionType))
        }

        return QuizApi.QuizData(questions)
    }

    private fun handleSuccess(roomId: String?) {
        roomId?.let { id ->
            // Affiche les infos de la room
            findViewById<LinearLayout>(R.id.room_info).visibility = View.VISIBLE
            findViewById<TextView>(R.id.room_id).text = "Room ID: $id"
            showSuccessToast("Quiz créé avec succès !")

            // Bouton "Démarrer le Quiz"
            findViewById<Button>(R.id.start_quiz_btn).apply {
                isEnabled = true
                setOnClickListener {
                    // Appel à l'API pour changer le statut de la room
                    ApiClient.instance.updateQuizStatus(id).enqueue(object : Callback<QuizApi.ApiResponse> {
                        override fun onResponse(call: Call<QuizApi.ApiResponse>, response: Response<QuizApi.ApiResponse>) {
                            if (response.isSuccessful) {
                                Toast.makeText(this@CreerQuizManuellementActivity, "Quiz démarré !", Toast.LENGTH_SHORT).show()
                            } else {
                                showError("Erreur lors du démarrage du quiz. Code : ${response.code()}")
                            }
                        }

                        override fun onFailure(call: Call<QuizApi.ApiResponse>, t: Throwable) {
                            showError("Erreur de connexion : ${t.message}")
                        }
                    })
                }
            }

        } ?: showError("ID de la room manquant.")
    }



    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_LONG).apply {
                setGravity(Gravity.CENTER, 0, 0)
                show()
            }
        }
    }

    private fun showSuccessToast(message: String) {
        runOnUiThread {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }
    }
}
