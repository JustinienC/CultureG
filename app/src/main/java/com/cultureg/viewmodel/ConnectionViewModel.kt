package com.cultureg.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cultureg.data.api.ConnectionState
import com.cultureg.data.api.RaspberryPiApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ConnectionViewModel : ViewModel() {
    
    private val api = RaspberryPiApi.getInstance()
    
    val connectionState: StateFlow<ConnectionState> = api.getConnectionState()
    
    private val _ipAddress = MutableStateFlow<String>("")
    val ipAddress: StateFlow<String> = _ipAddress.asStateFlow()
    
    private val _port = MutableStateFlow<Int>(8765)
    val port: StateFlow<Int> = _port.asStateFlow()
    
    val messages = api.observeMessages()
    
    val isConnected: StateFlow<Boolean> = connectionState.map { 
        it is ConnectionState.Connected 
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )
    
    fun connect(ip: String, port: Int = 8765) {
        Log.d("ConnectionViewModel", "connect() appelé: IP=$ip, Port=$port")
        _ipAddress.value = ip
        _port.value = port
        api.connect(ip, port)
    }
    
    fun disconnect() {
        api.disconnect()
    }
    
    fun ping() {
        viewModelScope.launch {
            api.ping()
        }
    }
    
    fun observeConnectionMessages(): Flow<String> {
        return messages
            .filter { it.type == "CONNECTED" }
            .map { 
                val data = it.data
                if (data != null) {
                    "Connecté: ${it.data}"
                } else {
                    "Connecté au serveur"
                }
            }
    }
}

