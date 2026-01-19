package com.cultureg.data.api

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import okhttp3.*
import okio.ByteString
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class WebSocketClient {
    
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()
    
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState.asStateFlow()
    
    private val _messages = MutableStateFlow<List<WebSocketMessage>>(emptyList())
    val messages: StateFlow<List<WebSocketMessage>> = _messages.asStateFlow()
    
    private val _lastMessage = MutableStateFlow<WebSocketMessage?>(null)
    val lastMessage: StateFlow<WebSocketMessage?> = _lastMessage.asStateFlow()
    
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    fun connect(url: String) {
        if (_connectionState.value == ConnectionState.Connected) {
            Log.w(TAG, "Déjà connecté")
            return
        }
        
        Log.d(TAG, "Tentative de connexion à: $url")
        _connectionState.value = ConnectionState.Connecting
        
        webSocket?.close(1000, "Nouvelle connexion")
        
        val request = Request.Builder()
            .url(url)
            .build()
        
        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                Log.d(TAG, "WebSocket ouvert")
                scope.launch {
                    _connectionState.value = ConnectionState.Connected
                }
            }
            
            override fun onMessage(webSocket: WebSocket, text: String) {
                Log.d(TAG, "Message reçu: $text")
                scope.launch {
                    try {
                        val json = JSONObject(text)
                        val message = WebSocketMessage(
                            type = json.optString("type", "UNKNOWN"),
                            data = json.optJSONObject("data")?.toString(),
                            rawMessage = text
                        )
                        
                        _lastMessage.value = message
                        _messages.value = _messages.value + message
                    } catch (e: Exception) {
                        Log.e(TAG, "Erreur parsing message: ${e.message}")
                    }
                }
            }
            
            override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
                Log.d(TAG, "Message binaire reçu")
                onMessage(webSocket, bytes.utf8())
            }
            
            override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Fermeture: $code - $reason")
                scope.launch {
                    _connectionState.value = ConnectionState.Disconnecting
                }
            }
            
            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                Log.d(TAG, "Fermé: $code - $reason")
                scope.launch {
                    _connectionState.value = ConnectionState.Disconnected
                }
            }
            
            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.e(TAG, "Erreur WebSocket: ${t.message}", t)
                Log.e(TAG, "Type d'erreur: ${t.javaClass.simpleName}")
                if (response != null) {
                    Log.e(TAG, "Code réponse: ${response.code}")
                    Log.e(TAG, "Message: ${response.message}")
                }
                scope.launch {
                    val errorMessage = when {
                        t.message?.contains("Failed to connect") == true -> "Impossible de se connecter. Vérifie l'IP et que le serveur est démarré."
                        t.message?.contains("timeout") == true -> "Timeout de connexion. Vérifie le réseau."
                        t.message?.contains("Network is unreachable") == true -> "Réseau inaccessible. Vérifie la connexion WiFi."
                        else -> t.message ?: "Erreur de connexion inconnue"
                    }
                    _connectionState.value = ConnectionState.Error(errorMessage)
                }
            }
        })
    }
    
    fun sendMessage(type: String, data: Map<String, Any>? = null): Boolean {
        val webSocket = this.webSocket ?: run {
            Log.e(TAG, "WebSocket non connecté")
            return false
        }
        
        return try {
            val json = JSONObject().apply {
                put("type", type)
                if (data != null) {
                    val dataObj = JSONObject()
                    data.forEach { (key, value) ->
                        dataObj.put(key, value)
                    }
                    put("data", dataObj)
                }
            }
            
            val message = json.toString()
            Log.d(TAG, "Envoi message: $message")
            webSocket.send(message)
            true
        } catch (e: Exception) {
            Log.e(TAG, "Erreur envoi message: ${e.message}", e)
            false
        }
    }
    
    fun ping(): Boolean {
        return sendMessage("PING")
    }
    
    fun sendAnswer(answer: String): Boolean {
        return sendMessage("ANSWER_QUESTION", mapOf("answer" to answer))
    }
    
    fun disconnect() {
        webSocket?.close(1000, "Déconnexion normale")
        webSocket = null
        scope.launch {
            _connectionState.value = ConnectionState.Disconnected
        }
    }
    
    fun cleanup() {
        disconnect()
        scope.cancel()
    }
    
    companion object {
        private const val TAG = "WebSocketClient"
        
        @Volatile
        private var instance: WebSocketClient? = null
        
        fun getInstance(): WebSocketClient {
            return instance ?: synchronized(this) {
                instance ?: WebSocketClient().also { instance = it }
            }
        }
    }
}

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connecting : ConnectionState()
    object Connected : ConnectionState()
    object Disconnecting : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}

data class WebSocketMessage(
    val type: String,
    val data: String?,
    val rawMessage: String
)

