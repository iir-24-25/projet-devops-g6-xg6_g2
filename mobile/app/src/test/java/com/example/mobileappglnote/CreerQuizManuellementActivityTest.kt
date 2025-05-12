package com.example.mobileappglnote

import android.widget.CheckBox
import android.widget.EditText

import com.example.mobileappglnote.network.QuizApi
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith

 // Required for Android-specific components
class CreerQuizManuellementActivityTest {

    @Test
    fun testPrepareQuizData() {
        // 1. Initialize activity in a test environment
        val activity = CreerQuizManuellementActivity()

        // 2. Simulate UI inputs
        activity.quizTitleEditText = EditText(activity.baseContext)  // Mock EditText
        activity.quizTitleEditText.setText("Test Quiz")

        activity.questionsContainer = android.widget.LinearLayout(activity.baseContext)
        activity.questionViews = mutableListOf()

        // 3. Add test question/options
        activity.addQuestionView()
        val questionView = activity.questionViews[0]

        val questionEditText = questionView.findViewById<EditText>(R.id.question_edittext)
        questionEditText.setText("Test question")

        // Add first option (correct)
        activity.addOptionView(questionView)
        val option1EditText = questionView.findViewById<EditText>(R.id.option_edittext)
        val option1Checkbox = questionView.findViewById<CheckBox>(R.id.correct_checkbox)
        option1EditText.setText("Option 1")
        option1Checkbox.isChecked = true

        // Add second option
        activity.addOptionView(questionView)
        val option2EditText = questionView.findViewById<EditText>(R.id.option_edittext)
        option2EditText.setText("Option 2")

        // 4. Verify output
        val quizData = activity.prepareQuizData()
        assertEquals("Test Quiz", quizData?.title)
        assertEquals(1, quizData?.questions?.size)
        assertEquals(2, quizData?.questions?.get(0)?.options?.size)
        assertEquals(0, quizData?.questions?.get(0)?.correctAnswerIndex)  // First option is correct
    }
}