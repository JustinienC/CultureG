package com.cultureg.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.cultureg.data.models.Difficulty
import com.cultureg.data.models.Question
import com.cultureg.data.models.QuestionCategories

/**
 * Dialogue pour ajouter ou modifier une question
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuestionFormDialog(
    question: Question? = null, // null = mode ajout, non-null = mode édition
    onDismiss: () -> Unit,
    onSave: (question: String, answers: List<String>, correctAnswerIndex: Int, category: String, difficulty: Difficulty) -> Unit
) {
    // État du formulaire
    var questionText by remember { mutableStateOf(question?.question ?: "") }
    var answer1 by remember { mutableStateOf(question?.answers?.getOrNull(0) ?: "") }
    var answer2 by remember { mutableStateOf(question?.answers?.getOrNull(1) ?: "") }
    var answer3 by remember { mutableStateOf(question?.answers?.getOrNull(2) ?: "") }
    var answer4 by remember { mutableStateOf(question?.answers?.getOrNull(3) ?: "") }
    var correctAnswerIndex by remember { mutableIntStateOf(question?.correctAnswerIndex ?: 0) }
    var selectedCategory by remember { mutableStateOf(question?.category ?: "Général") }
    var selectedDifficulty by remember { mutableStateOf(question?.difficulty ?: Difficulty.MEDIUM) }
    var showCategoryMenu by remember { mutableStateOf(false) }
    var showDifficultyMenu by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f),
            shape = MaterialTheme.shapes.large
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
            ) {
                // En-tête
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (question == null) "Nouvelle Question" else "Modifier Question",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.Filled.Close,
                                contentDescription = "Fermer",
                                tint = MaterialTheme.colorScheme.onPrimary
                            )
                        }
                    }
                }
                
                // Formulaire scrollable
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Message d'erreur
                    errorMessage?.let { error ->
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer
                            )
                        ) {
                            Text(
                                text = error,
                                modifier = Modifier.padding(12.dp),
                                color = MaterialTheme.colorScheme.onErrorContainer,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                    
                    // Question
                    OutlinedTextField(
                        value = questionText,
                        onValueChange = { questionText = it },
                        label = { Text("Question") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2,
                        maxLines = 4,
                        supportingText = { Text("${questionText.length}/200") }
                    )
                    
                    // Catégorie
                    ExposedDropdownMenuBox(
                        expanded = showCategoryMenu,
                        onExpandedChange = { showCategoryMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Catégorie") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showCategoryMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showCategoryMenu,
                            onDismissRequest = { showCategoryMenu = false }
                        ) {
                            QuestionCategories.ALL.forEach { category ->
                                DropdownMenuItem(
                                    text = { Text(category) },
                                    onClick = {
                                        selectedCategory = category
                                        showCategoryMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    // Difficulté
                    ExposedDropdownMenuBox(
                        expanded = showDifficultyMenu,
                        onExpandedChange = { showDifficultyMenu = it }
                    ) {
                        OutlinedTextField(
                            value = selectedDifficulty.displayName,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Difficulté") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = showDifficultyMenu)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor()
                        )
                        ExposedDropdownMenu(
                            expanded = showDifficultyMenu,
                            onDismissRequest = { showDifficultyMenu = false }
                        ) {
                            Difficulty.values().forEach { difficulty ->
                                DropdownMenuItem(
                                    text = { Text(difficulty.displayName) },
                                    onClick = {
                                        selectedDifficulty = difficulty
                                        showDifficultyMenu = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Divider()
                    
                    // Réponses
                    Text(
                        text = "Réponses",
                        style = MaterialTheme.typography.titleMedium
                    )
                    
                    // Réponse 1
                    AnswerField(
                        value = answer1,
                        onValueChange = { answer1 = it },
                        label = "Réponse 1",
                        isCorrect = correctAnswerIndex == 0,
                        onSetCorrect = { correctAnswerIndex = 0 }
                    )
                    
                    // Réponse 2
                    AnswerField(
                        value = answer2,
                        onValueChange = { answer2 = it },
                        label = "Réponse 2",
                        isCorrect = correctAnswerIndex == 1,
                        onSetCorrect = { correctAnswerIndex = 1 }
                    )
                    
                    // Réponse 3
                    AnswerField(
                        value = answer3,
                        onValueChange = { answer3 = it },
                        label = "Réponse 3 (optionnelle)",
                        isCorrect = correctAnswerIndex == 2,
                        onSetCorrect = { correctAnswerIndex = 2 }
                    )
                    
                    // Réponse 4
                    AnswerField(
                        value = answer4,
                        onValueChange = { answer4 = it },
                        label = "Réponse 4 (optionnelle)",
                        isCorrect = correctAnswerIndex == 3,
                        onSetCorrect = { correctAnswerIndex = 3 }
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
                                text = "Cliquez sur ✓ pour marquer la réponse correcte",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
                
                // Boutons d'action
                Divider()
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Annuler")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            // Validation
                            val answers = listOfNotNull(
                                answer1.takeIf { it.isNotBlank() },
                                answer2.takeIf { it.isNotBlank() },
                                answer3.takeIf { it.isNotBlank() },
                                answer4.takeIf { it.isNotBlank() }
                            )
                            
                            when {
                                questionText.isBlank() -> {
                                    errorMessage = "La question ne peut pas être vide"
                                }
                                answers.size < 2 -> {
                                    errorMessage = "Il faut au moins 2 réponses"
                                }
                                correctAnswerIndex >= answers.size -> {
                                    errorMessage = "Sélectionnez une réponse correcte parmi les réponses remplies"
                                }
                                else -> {
                                    onSave(
                                        questionText,
                                        answers,
                                        correctAnswerIndex,
                                        selectedCategory,
                                        selectedDifficulty
                                    )
                                }
                            }
                        }
                    ) {
                        Icon(Icons.Filled.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (question == null) "Ajouter" else "Modifier")
                    }
                }
            }
        }
    }
}

/**
 * Champ de réponse avec bouton de sélection
 */
@Composable
fun AnswerField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    isCorrect: Boolean,
    onSetCorrect: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier.weight(1f),
            singleLine = true
        )
        Spacer(modifier = Modifier.width(8.dp))
        IconButton(
            onClick = onSetCorrect,
            enabled = value.isNotBlank()
        ) {
            Icon(
                imageVector = if (isCorrect) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = "Réponse correcte",
                tint = if (isCorrect) 
                    MaterialTheme.colorScheme.primary 
                else 
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
            )
        }
    }
}



