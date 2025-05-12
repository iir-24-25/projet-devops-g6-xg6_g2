package com.example.mobileappglnote.network

import com.example.mobileappglnote.model.QuizQuestion
import retrofit2.Call
import retrofit2.http.*

interface QuizApi {

    @POST("create_manual")
    fun createQuiz(@Body quizData: QuizData): Call<CreateQuizResponse>

    @GET("quiz/{room_id}/questions")
    fun getQuizQuestions(@Path("room_id") roomId: String): Call<List<QuizQuestion>>

    @POST("quiz/{room_id}/submit")
    fun submitAnswers(
        @Path("room_id") roomId: String,
        @Body answers: List<Answer>
    ): Call<QuizResult>

    @POST("/generate_quiz")
    fun generateQuiz(@Body request: GenerateQuizRequest): Call<QuizResponse>

    @GET("/quiz/{room_id}/status")
    fun getQuizStatus(@Path("room_id") roomId: String): Call<QuizStatusResponse>

    @PATCH("/quiz/{room_id}/status")
    fun updateQuizStatus(@Path("room_id") roomId: String): Call<ApiResponse>

    @GET("test_connection")
    fun testConnection(): Call<TestResponse>

    data class ApiResponse(
        val message: String
    )

    // Add this data class
    data class TestResponse(
        val status: String,
        val message: String
    )

    data class QuizData(
        val questions: List<QuestionData>  // Removed title field
    )

    data class QuestionData(
        val question: String,
        val answers: List<String>,
        val correctAnswers: String,
        val type: String // Ajout du type de la question
    )


    data class Answer(
        val questionId: Int,
        val selectedOptionIndex: Int
    )

    data class QuizResult(
        val score: Int,
        val totalQuestions: Int,
        val correctAnswers: List<Int>
    )


    data class GenerateQuizRequest(
        val theme: String,
        val niveau: String,
        val nb_questions: Int
    )

    data class QuizResponse(
        val quiz: List<QuizQuestionData>
    )

    data class QuizQuestionData(
        val question: String,
        val choix: Map<String, String>,
        val reponse: String
    )

    data class QuizStatusResponse(
        val status: String
    )



    data class CreateQuizResponse(
        val room_id: String
    )


}