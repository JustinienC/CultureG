package com.cultureg.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.cultureg.data.api.ConnectionState
import com.cultureg.viewmodel.ConnectionViewModel
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter

/**
 * Écran de connexion au Raspberry Pi
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = viewModel()
) {
    val connectionState by viewModel.connectionState.collectAsState()
    val isConnected by viewModel.isConnected.collectAsState()
    val ipAddress by viewModel.ipAddress.collectAsState()
    val port by viewModel.port.collectAsState()
    
    var ipInput by remember { mutableStateOf("") }
    var portInput by remember { mutableStateOf("8765") }
    var showTestDialog by remember { mutableStateOf(false) }
    var pingResult by remember { mutableStateOf<String?>(null) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Connexion Raspberry Pi") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Icône de connexion
            Icon(
                imageVector = when (connectionState) {
                    is ConnectionState.Connected -> Icons.Filled.CheckCircle
                    is ConnectionState.Connecting -> Icons.Filled.Sync
                    is ConnectionState.Error -> Icons.Filled.Error
                    else -> Icons.Filled.LinkOff
                },
                contentDescription = "Statut",
                modifier = Modifier.size(80.dp),
                tint = when (connectionState) {
                    is ConnectionState.Connected -> MaterialTheme.colorScheme.primary
                    is ConnectionState.Connecting -> MaterialTheme.colorScheme.secondary
                    is ConnectionState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                }
            )
            
            // Statut de connexion
            Text(
                text = when (connectionState) {
                    is ConnectionState.Connected -> "Connecté ✓"
                    is ConnectionState.Connecting -> "Connexion en cours..."
                    is ConnectionState.Error -> "Erreur: ${(connectionState as ConnectionState.Error).message}"
                    is ConnectionState.Disconnecting -> "Déconnexion..."
                    else -> "Déconnecté"
                },
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = when (connectionState) {
                    is ConnectionState.Connected -> MaterialTheme.colorScheme.primary
                    is ConnectionState.Error -> MaterialTheme.colorScheme.error
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
            
            if (isConnected) {
                // Informations de connexion
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Adresse IP:", fontWeight = FontWeight.Bold)
                            Text(ipAddress)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Port:", fontWeight = FontWeight.Bold)
                            Text(port.toString())
                        }
                    }
                }
                
                // Bouton déconnexion
                Button(
                    onClick = { viewModel.disconnect() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Filled.LinkOff, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Déconnecter")
                }
                
                // Observer les messages PONG
                LaunchedEffect(Unit) {
                    viewModel.messages
                        .filter { message -> message.type == "PONG" }
                        .collect { message ->
                            pingResult = "✅ PONG reçu ! Connexion fonctionnelle"
                        }
                }
                
                // Bouton test ping
                
                OutlinedButton(
                    onClick = { 
                        pingResult = null
                        viewModel.ping()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.NetworkCheck, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tester la connexion (Ping)")
                }
                
                pingResult?.let { result ->
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = result,
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            } else {
                // Afficher l'erreur si présente
                if (connectionState is ConnectionState.Error) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.Error,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = (connectionState as ConnectionState.Error).message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer
                            )
                        }
                    }
                }
                
                // Formulaire de connexion
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = "Connexion au Raspberry Pi",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        
                        // Champ IP
                        OutlinedTextField(
                            value = ipInput,
                            onValueChange = { ipInput = it },
                            label = { Text("Adresse IP") },
                            placeholder = { Text("192.168.1.100") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Computer, contentDescription = null) },
                            singleLine = true
                        )
                        
                        // Champ Port
                        OutlinedTextField(
                            value = portInput,
                            onValueChange = { portInput = it },
                            label = { Text("Port") },
                            placeholder = { Text("8765") },
                            modifier = Modifier.fillMaxWidth(),
                            leadingIcon = { Icon(Icons.Filled.Numbers, contentDescription = null) },
                            singleLine = true
                        )
                        
                        // Info
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    Icons.Filled.Info,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Assure-toi que le Raspberry Pi est démarré et sur le même réseau WiFi",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        
                        // Bouton connexion
                        Button(
                            onClick = {
                                val port = portInput.toIntOrNull() ?: 8765
                                if (ipInput.isNotBlank()) {
                                    Log.d("ConnectionScreen", "Clic sur connecter: IP=$ipInput, Port=$port")
                                    viewModel.connect(ipInput.trim(), port)
                                } else {
                                    Log.w("ConnectionScreen", "IP vide, connexion impossible")
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            enabled = ipInput.isNotBlank() && connectionState !is ConnectionState.Connecting
                        ) {
                            if (connectionState is ConnectionState.Connecting) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            } else {
                                Icon(Icons.Filled.Link, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text("Se connecter")
                        }
                    }
                }
                
                // Adresses IP courantes
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp)
                    ) {
                        Text(
                            text = "Adresses IP courantes:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        listOf("192.168.1.100", "192.168.0.100", "10.0.0.100").forEach { ip ->
                            TextButton(
                                onClick = { ipInput = ip },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(ip)
                            }
                        }
                    }
                }
            }
        }
    }
    
    // Dialogue de test ping
    if (showTestDialog) {
        AlertDialog(
            onDismissRequest = { showTestDialog = false },
            title = { Text("Test de connexion") },
            text = { 
                Text("Un ping a été envoyé. Attends quelques secondes pour voir si un PONG arrive.") 
            },
            confirmButton = {
                TextButton(onClick = { showTestDialog = false }) {
                    Text("OK")
                }
            }
        )
    }
}

