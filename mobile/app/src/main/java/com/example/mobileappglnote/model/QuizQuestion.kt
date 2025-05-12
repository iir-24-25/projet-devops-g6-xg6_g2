package com.example.mobileappglnote.model

data class QuizQuestion(
    val id: Int,
    val questionText: String,
    val options: List<String>,
    val correctAnswerIndex: Int
)

