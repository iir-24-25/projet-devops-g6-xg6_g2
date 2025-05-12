package com.example.mobileappglnote

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class GroupPlayActivity : AppCompatActivity() {

    private lateinit var pseudoEditText: EditText
    private lateinit var roomIdEditText: EditText
    private lateinit var joinRoomButton: Button
    private lateinit var waitingMessage: TextView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_play)

        pseudoEditText = findViewById(R.id.et_pseudo)
        roomIdEditText = findViewById(R.id.et_room_id)
        joinRoomButton = findViewById(R.id.btn_join_room)
        waitingMessage = findViewById(R.id.tv_waiting_message)

        joinRoomButton.setOnClickListener {
            val pseudo = pseudoEditText.text.toString().trim()
            val roomId = roomIdEditText.text.toString().trim()

            if (pseudo.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer un pseudonyme.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (roomId.isEmpty()) {
                Toast.makeText(this, "Veuillez entrer l'ID de la room.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            waitingMessage.visibility = TextView.VISIBLE

            // Appeler l'API Flask pour obtenir le statut du quiz
            getQuizStatus(roomId)
        }
    }

    private fun getQuizStatus(roomId: String) {
        val request = Request.Builder()
            .url("http://192.168.1.44:5000/quiz/$roomId/status")  // Remplacez l'URL par celle de votre serveur Flask
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Erreur de connexion.", Toast.LENGTH_SHORT).show()
                    waitingMessage.visibility = TextView.GONE
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string()
                    val jsonObject = JSONObject(jsonResponse)

                    val status = jsonObject.optString("status")

                    if (status == "running") {
                        runOnUiThread {
                            waitingMessage.visibility = TextView.GONE
                            val intent = Intent(this@GroupPlayActivity, QuizActivity::class.java)
                            intent.putExtra("roomId", roomIdEditText.text.toString())
                            startActivity(intent)
                            //finish()
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Le quiz n'est pas encore commencé.", Toast.LENGTH_SHORT).show()
                            waitingMessage.visibility = TextView.GONE
                        }
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Erreur lors de la récupération du statut.", Toast.LENGTH_SHORT).show()
                        waitingMessage.visibility = TextView.GONE
                    }
                }
            }
        })
    }
}
