package com.cultureg.data.api

import android.util.Log
import com.cultureg.data.models.Question
import com.cultureg.data.models.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

/**
 * API pour communiquer avec le Raspberry Pi
 * Wrapper autour du WebSocketClient
 */
class RaspberryPiApi(private val webSocketClient: WebSocketClient) {
    
    companion object {
        private const val TAG = "RaspberryPiApi"
        
        @Volatile
        private var instance: RaspberryPiApi? = null
        
        fun getInstance(): RaspberryPiApi {
            val wsClient = WebSocketClient.getInstance()
            return instance ?: synchronized(this) {
                instance ?: RaspberryPiApi(wsClient).also { instance = it }
            }
        }
    }
    
    /**
     * Se connecter au Raspberry Pi
     */
    fun connect(ipAddress: String, port: Int = 8765) {
        val url = "ws://$ipAddress:$port"
        Log.d(TAG, "Connexion à $url")
        try {
            webSocketClient.connect(url)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la connexion: ${e.message}", e)
        }
    }
    
    /**
     * Se déconnecter
     */
    fun disconnect() {
        webSocketClient.disconnect()
    }
    
    /**
     * Obtenir l'état de la connexion
     */
    fun getConnectionState(): StateFlow<ConnectionState> {
        return webSocketClient.connectionState
    }
    
    /**
     * Ajouter une question (format Question-Réponse)
     */
    fun addQuestion(question: Question): Boolean {
        val questionData = mapOf(
            "id" to question.id,
            "question" to question.question,
            "correctAnswer" to question.correctAnswer,
            "category" to question.category,
            "difficulty" to question.difficulty.name,
            "timeLimit" to (question.timeLimit ?: JSONObject.NULL)
        )
        Log.d(TAG, "Envoi question au serveur: ${question.id}")
        // Le serveur attend {"type": "ADD_QUESTION", "data": {...}}
        val success = webSocketClient.sendMessage("ADD_QUESTION", questionData)
        if (!success) {
            Log.e(TAG, "Échec envoi question au serveur")
        }
        return success
    }
    
    /**
     * Récupérer toutes les questions
     */
    fun getQuestions(): Boolean {
        return webSocketClient.sendMessage("GET_QUESTIONS")
    }
    
    /**
     * Supprimer une question
     */
    fun deleteQuestion(questionId: String): Boolean {
        return webSocketClient.sendMessage("DELETE_QUESTION", mapOf("questionId" to questionId))
    }
    
    /**
     * Mettre à jour les paramètres du jeu
     */
    fun updateSettings(settings: Map<String, Any>): Boolean {
        return webSocketClient.sendMessage("UPDATE_SETTINGS", mapOf("data" to settings))
    }
    
    /**
     * Récupérer les paramètres
     */
    fun getSettings(): Boolean {
        return webSocketClient.sendMessage("GET_SETTINGS")
    }
    
    /**
     * Démarrer un jeu
     */
    fun startGame(numberOfQuestions: Int): Boolean {
        return webSocketClient.sendMessage("START_GAME", mapOf("numberOfQuestions" to numberOfQuestions))
    }
    
    /**
     * Répondre à une question
     */
    fun answerQuestion(answerIndex: Int): Boolean {
        return webSocketClient.sendMessage("ANSWER_QUESTION", mapOf("answerIndex" to answerIndex))
    }
    
    /**
     * Récupérer les scores
     */
    fun getScores(limit: Int = 10): Boolean {
        return webSocketClient.sendMessage("GET_SCORES", mapOf("limit" to limit))
    }
    
    /**
     * Envoyer un ping
     */
    fun ping(): Boolean {
        return webSocketClient.ping()
    }
    
    /**
     * Observer les messages reçus
     */
    fun observeMessages(): Flow<WebSocketMessage> {
        return webSocketClient.lastMessage
            .filter { it != null }
            .map { it!! }
    }
    
    /**
     * Observer un type de message spécifique
     */
    fun observeMessageType(type: String): Flow<WebSocketMessage> {
        return observeMessages()
            .filter { message -> message.type == type }
    }
    
    /**
     * Parser une liste de questions depuis un message (format Question-Réponse)
     */
    fun parseQuestions(message: WebSocketMessage): List<Question> {
        return try {
            val data = message.data ?: return emptyList()
            val jsonData = JSONObject(data)
            val questionsArray = jsonData.optJSONArray("data") ?: JSONArray(data)
            
            val questions = mutableListOf<Question>()
            for (i in 0 until questionsArray.length()) {
                val qJson = questionsArray.getJSONObject(i)
                
                // Gérer l'ancien format (migration) ou nouveau format
                val correctAnswer = if (qJson.has("correctAnswer")) {
                    qJson.getString("correctAnswer")
                } else if (qJson.has("answers") && qJson.has("correctAnswerIndex")) {
                    // Ancien format : extraire la réponse correcte
                    val answers = parseStringList(qJson.getJSONArray("answers"))
                    val correctIndex = qJson.getInt("correctAnswerIndex")
                    answers.getOrNull(correctIndex) ?: ""
                } else {
                    ""
                }
                
                questions.add(
                    Question(
                        id = qJson.getString("id"),
                        question = qJson.getString("question"),
                        correctAnswer = correctAnswer,
                        category = qJson.optString("category", "Général"),
                        difficulty = Difficulty.valueOf(qJson.optString("difficulty", "MEDIUM")),
                        timeLimit = if (qJson.has("timeLimit") && !qJson.isNull("timeLimit")) {
                            qJson.optInt("timeLimit")
                        } else null
                    )
                )
            }
            questions
        } catch (e: Exception) {
            Log.e(TAG, "Erreur parsing questions: ${e.message}", e)
            emptyList()
        }
    }
    
    private fun parseStringList(jsonArray: JSONArray): List<String> {
        val list = mutableListOf<String>()
        for (i in 0 until jsonArray.length()) {
            list.add(jsonArray.getString(i))
        }
        return list
    }
}

