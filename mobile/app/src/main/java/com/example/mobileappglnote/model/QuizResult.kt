package com.example.mobileappglnote.model

data class QuizResult(
    val score: Int,
    val totalQuestions: Int,
    val correctAnswers: List<Int>
)
