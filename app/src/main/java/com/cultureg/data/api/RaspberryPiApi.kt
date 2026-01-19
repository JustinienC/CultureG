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
    
    fun connect(ipAddress: String, port: Int = 8765) {
        val url = "ws://$ipAddress:$port"
        Log.d(TAG, "Connexion à $url")
        try {
            webSocketClient.connect(url)
        } catch (e: Exception) {
            Log.e(TAG, "Erreur lors de la connexion: ${e.message}", e)
        }
    }
    
    fun disconnect() {
        webSocketClient.disconnect()
    }
    
    fun getConnectionState(): StateFlow<ConnectionState> {
        return webSocketClient.connectionState
    }
    
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
        val success = webSocketClient.sendMessage("ADD_QUESTION", questionData)
        if (!success) {
            Log.e(TAG, "Échec envoi question au serveur")
        }
        return success
    }
    
    fun getQuestions(): Boolean {
        return webSocketClient.sendMessage("GET_QUESTIONS")
    }
    
    fun deleteQuestion(questionId: String): Boolean {
        return webSocketClient.sendMessage("DELETE_QUESTION", mapOf("questionId" to questionId))
    }
    
    fun updateSettings(settings: Map<String, Any>): Boolean {
        return webSocketClient.sendMessage("UPDATE_SETTINGS", mapOf("data" to settings))
    }
    
    fun getSettings(): Boolean {
        return webSocketClient.sendMessage("GET_SETTINGS")
    }
    
    fun startGame(numberOfQuestions: Int): Boolean {
        return webSocketClient.sendMessage("START_GAME", mapOf("numberOfQuestions" to numberOfQuestions))
    }
    
    fun answerQuestion(answerIndex: Int): Boolean {
        return webSocketClient.sendMessage("ANSWER_QUESTION", mapOf("answerIndex" to answerIndex))
    }
    
    fun getScores(limit: Int = 10): Boolean {
        return webSocketClient.sendMessage("GET_SCORES", mapOf("limit" to limit))
    }
    
    fun ping(): Boolean {
        return webSocketClient.ping()
    }
    
    fun observeMessages(): Flow<WebSocketMessage> {
        return webSocketClient.lastMessage
            .filter { it != null }
            .map { it!! }
    }
    
    fun observeMessageType(type: String): Flow<WebSocketMessage> {
        return observeMessages()
            .filter { message -> message.type == type }
    }
    
    fun parseQuestions(message: WebSocketMessage): List<Question> {
        return try {
            val data = message.data ?: return emptyList()
            val jsonData = JSONObject(data)
            val questionsArray = jsonData.optJSONArray("data") ?: JSONArray(data)
            
            val questions = mutableListOf<Question>()
            for (i in 0 until questionsArray.length()) {
                val qJson = questionsArray.getJSONObject(i)
                
                val correctAnswer = if (qJson.has("correctAnswer")) {
                    qJson.getString("correctAnswer")
                } else if (qJson.has("answers") && qJson.has("correctAnswerIndex")) {
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

