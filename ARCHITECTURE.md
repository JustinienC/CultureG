# Architecture CultureG Mobile App

## 📐 Vue d'Ensemble

CultureG suit une architecture **MVVM simplifiée** (Model-View-ViewModel) avec **Jetpack Compose** pour l'UI.

```
┌─────────────────────────────────────────────────┐
│                   UI Layer                      │
│  (Jetpack Compose - Screens & Components)      │
│                                                 │
│  HomeScreen | QuestionsScreen | SettingsScreen │
│             | StatsScreen                       │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│              ViewModel Layer                    │
│  (State Management & Business Logic)            │
│                                                 │
│  QuestionsVM | SettingsVM | StatsVM |          │
│  ConnectionVM                                   │
└────────────────────┬────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────┐
│              Repository Layer                   │
│  (Data Access Abstraction)                      │
│                                                 │
│  QuestionsRepo | SettingsRepo | StatsRepo      │
└────────────────────┬────────────────────────────┘
                     │
         ┌───────────┴───────────┐
         ▼                       ▼
┌──────────────────┐    ┌─────────────────┐
│   Local Storage  │    │  Raspberry Pi   │
│   (DataStore)    │    │  API (WebSocket)│
└──────────────────┘    └─────────────────┘
```

## 🏗️ Couches de l'Architecture

### 1. UI Layer (Jetpack Compose)

#### Responsabilités
- Affichage de l'interface utilisateur
- Gestion des interactions utilisateur
- Observation de l'état depuis les ViewModels

#### Composants
- **Screens** : Écrans complets (HomeScreen, QuestionsScreen, etc.)
- **Components** : Composants réutilisables (Cards, Buttons, etc.)
- **Theme** : Configuration du thème Material3
- **Navigation** : Navigation entre écrans

#### Exemple
```kotlin
@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val connectionState by viewModel.connectionState.collectAsState()
    
    // UI basée sur l'état
    when (connectionState) {
        is ConnectionState.Connected -> ConnectedView()
        is ConnectionState.Disconnected -> DisconnectedView()
        is ConnectionState.Connecting -> LoadingView()
    }
}
```

### 2. ViewModel Layer

#### Responsabilités
- Gestion de l'état de l'UI
- Logique de présentation
- Communication avec les Repositories
- Gestion du cycle de vie

#### Technologies
- `ViewModel` (Android Architecture Components)
- `StateFlow` / `MutableStateFlow` pour l'état
- `Coroutines` pour les opérations asynchrones

#### Exemple
```kotlin
class QuestionsViewModel : ViewModel() {
    private val repository = QuestionsRepository()
    
    private val _questions = MutableStateFlow<List<Question>>(emptyList())
    val questions: StateFlow<List<Question>> = _questions.asStateFlow()
    
    fun loadQuestions() {
        viewModelScope.launch {
            _questions.value = repository.getAllQuestions()
        }
    }
}
```

### 3. Repository Layer

#### Responsabilités
- Abstraction de la source de données
- Décision entre données locales ou distantes
- Cache des données
- Gestion des erreurs

#### Pattern
- Pattern Repository (source unique de vérité)
- Interface pour faciliter les tests

#### Exemple
```kotlin
class QuestionsRepository {
    private val localDataStore = LocalDataStore()
    private val raspberryPiApi = RaspberryPiApi()
    
    suspend fun getAllQuestions(): List<Question> {
        // D'abord essayer depuis le Raspberry Pi
        return try {
            raspberryPiApi.fetchQuestions()
        } catch (e: Exception) {
            // Fallback vers données locales
            localDataStore.getQuestions()
        }
    }
}
```

### 4. Data Layer

#### Sources de Données

##### Local Storage (DataStore)
- Préférences utilisateur
- Configuration du jeu
- Cache des questions

```kotlin
class LocalDataStore(private val context: Context) {
    private val dataStore = context.dataStore
    
    suspend fun saveSettings(settings: GameSettings) {
        dataStore.edit { preferences ->
            preferences[TIMER_KEY] = settings.timerSeconds
            preferences[SOUND_ENABLED_KEY] = settings.soundEnabled
        }
    }
}
```

##### Remote API (WebSocket)
- Communication bidirectionnelle avec Raspberry Pi
- Envoi de questions/configurations
- Réception de scores/événements

```kotlin
class RaspberryPiApi {
    private val webSocket = WebSocketClient()
    
    suspend fun sendQuestion(question: Question) {
        val json = Json.encodeToString(question)
        webSocket.send(json)
    }
    
    fun observeScores(): Flow<Score> = webSocket.messages
        .map { Json.decodeFromString<Score>(it) }
}
```

## 🔄 Flux de Données

### Exemple : Ajout d'une Question

```
1. User clique "Ajouter Question" dans QuestionsScreen
        ↓
2. QuestionsScreen appelle viewModel.addQuestion()
        ↓
3. QuestionsViewModel traite la logique
        ↓
4. ViewModel appelle repository.addQuestion()
        ↓
5. Repository sauvegarde localement ET envoie au Raspberry Pi
        ↓
6. Repository retourne succès/erreur
        ↓
7. ViewModel met à jour le StateFlow
        ↓
8. QuestionsScreen observe le StateFlow et se recompose
        ↓
9. UI affiche la nouvelle question
```

## 📦 Organisation des Packages

```
com.cultureg/
│
├── ui/                                # UI Layer
│   ├── screens/                       # Écrans Compose
│   │   ├── home/
│   │   │   └── HomeScreen.kt
│   │   ├── questions/
│   │   │   ├── QuestionsScreen.kt
│   │   │   └── QuestionFormScreen.kt
│   │   ├── settings/
│   │   │   └── SettingsScreen.kt
│   │   └── stats/
│   │       └── StatsScreen.kt
│   │
│   ├── components/                    # Composants réutilisables
│   │   ├── QuestionCard.kt
│   │   ├── ConnectionStatus.kt
│   │   └── StatCard.kt
│   │
│   ├── navigation/                    # Navigation
│   │   └── NavGraph.kt
│   │
│   ├── theme/                         # Thème Material3
│   │   ├── Color.kt
│   │   ├── Type.kt
│   │   └── Theme.kt
│   │
│   └── MainActivity.kt                # Activité principale
│
├── viewmodel/                         # ViewModel Layer
│   ├── QuestionsViewModel.kt
│   ├── SettingsViewModel.kt
│   ├── StatsViewModel.kt
│   └── ConnectionViewModel.kt
│
├── data/                              # Data Layer
│   ├── models/                        # Modèles de données
│   │   ├── Question.kt
│   │   ├── GameSettings.kt
│   │   ├── Score.kt
│   │   └── ConnectionStatus.kt
│   │
│   ├── repository/                    # Repositories
│   │   ├── QuestionsRepository.kt
│   │   ├── SettingsRepository.kt
│   │   └── StatsRepository.kt
│   │
│   ├── local/                         # Stockage local
│   │   └── LocalDataStore.kt
│   │
│   └── remote/                        # API distante
│       ├── RaspberryPiApi.kt
│       ├── WebSocketClient.kt
│       └── ApiModels.kt
│
├── utils/                             # Utilitaires
│   ├── Constants.kt
│   ├── NetworkUtils.kt
│   └── Extensions.kt
│
└── CultureGApplication.kt             # Application
```

## 🌊 Gestion de l'État

### StateFlow pour l'État UI

```kotlin
// ViewModel
private val _uiState = MutableStateFlow(QuestionsUiState())
val uiState: StateFlow<QuestionsUiState> = _uiState.asStateFlow()

// Screen
val uiState by viewModel.uiState.collectAsState()
```

### États Possibles

```kotlin
sealed class UiState<out T> {
    object Idle : UiState<Nothing>()
    object Loading : UiState<Nothing>()
    data class Success<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
```

## 🔌 Communication Raspberry Pi

### WebSocket (Bidirectionnel)

```kotlin
class WebSocketClient {
    private var webSocket: WebSocket? = null
    private val _messages = MutableSharedFlow<String>()
    val messages: SharedFlow<String> = _messages.asSharedFlow()
    
    fun connect(url: String) {
        val request = Request.Builder()
            .url(url)
            .build()
        
        webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
            override fun onMessage(webSocket: WebSocket, text: String) {
                viewModelScope.launch {
                    _messages.emit(text)
                }
            }
        })
    }
    
    fun send(message: String) {
        webSocket?.send(message)
    }
}
```

### Messages JSON

```json
{
  "type": "ADD_QUESTION",
  "timestamp": 1234567890,
  "data": {
    "id": "uuid",
    "question": "Quelle est la capitale de la France ?",
    "answers": ["Paris", "Lyon", "Marseille", "Toulouse"],
    "correctAnswer": 0,
    "difficulty": "EASY"
  }
}
```

## 🧪 Tests

### Organisation
```
test/                                  # Tests unitaires
├── viewmodel/
│   └── QuestionsViewModelTest.kt
├── repository/
│   └── QuestionsRepositoryTest.kt
└── data/
    └── WebSocketClientTest.kt

androidTest/                           # Tests d'intégration
└── ui/
    └── QuestionsScreenTest.kt
```

## 🎯 Principes Appliqués

### 1. Séparation des Responsabilités
- Chaque couche a un rôle bien défini
- Pas de logique métier dans l'UI
- Pas d'accès direct aux données depuis l'UI

### 2. Single Source of Truth
- Repository = source unique de vérité
- ViewModel expose l'état via StateFlow
- UI observe et réagit

### 3. Unidirectional Data Flow
- Données descendent (ViewModel → UI)
- Événements remontent (UI → ViewModel)

### 4. Dependency Injection (Simple)
- Pas de framework DI complexe pour ce projet
- Construction manuelle des dépendances
- Facilite les tests

## 🚀 Performance

### Optimisations
- **Recomposition intelligente** : Compose recompose uniquement ce qui change
- **LazyColumn** : Virtualisation des listes longues
- **remember** : Mise en cache des calculs coûteux
- **Coroutines** : Opérations asynchrones non-bloquantes

### Gestion Mémoire
- `viewModelScope` pour gérer le lifecycle
- Annulation automatique des coroutines
- Pas de leaks mémoire

---

**Cette architecture permet un code maintenable, testable et évolutif pour le projet CultureG !**



