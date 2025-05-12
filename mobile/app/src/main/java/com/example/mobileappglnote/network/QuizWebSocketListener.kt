package com.example.mobileappglnote.network

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString

class QuizWebSocketListener(private val onStatusUpdated: (String) -> Unit) : WebSocketListener() {

    override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
        super.onOpen(webSocket, response)
        // Vous pouvez envoyer des données au serveur si nécessaire, par exemple pour s'abonner à un quiz spécifique
    }

    override fun onMessage(webSocket: WebSocket, text: String) {
        super.onMessage(webSocket, text)
        // Vérifiez si le statut du quiz est "running"
        if (text == "running") {
            onStatusUpdated("running")
        }
    }

    override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
        super.onFailure(webSocket, t, response)
        // Gérer les erreurs
    }

    override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
        super.onClosed(webSocket, code, reason)
    }
}
