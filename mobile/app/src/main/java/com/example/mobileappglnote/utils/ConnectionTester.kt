// Create a new file: app/src/main/java/com/example/mobileappglnote/utils/ConnectionTest.kt
package com.example.mobileappglnote.utils

import com.example.mobileappglnote.network.ApiClient
import com.example.mobileappglnote.network.QuizApi
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.util.Log

object ConnectionTester {
    fun testBackendConnection() {
        ApiClient.instance.testConnection().enqueue(object : Callback<QuizApi.TestResponse> {
            override fun onResponse(
                call: Call<QuizApi.TestResponse>,
                response: Response<QuizApi.TestResponse>
            ) {
                Log.d("CONNECTION_TEST", "Success! ${response.body()?.message}")
            }

            override fun onFailure(call: Call<QuizApi.TestResponse>, t: Throwable) {
                Log.e("CONNECTION_TEST", "Failed: ${t.message}")
            }
        })
    }
}