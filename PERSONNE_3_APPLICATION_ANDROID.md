# 📚 Documentation - Personne 3 : Application Android (UI + ViewModels + Navigation)

## 🎯 Votre Mission

Vous êtes responsable de l'**application Android** complète. Vous développez l'interface utilisateur (UI) avec Jetpack Compose, les ViewModels (logique métier), la navigation, la reconnaissance vocale Android, et la communication WebSocket avec la Raspberry Pi. Votre application est l'**interface utilisateur** du système : elle permet de se connecter au Raspberry Pi, de gérer les questions, et de répondre aux questions via le microphone du téléphone.

---

## 📁 Fichiers dont vous êtes responsable

### Structure du projet Android :

```
app/src/main/java/com/cultureg/
├── ui/                          # Interface utilisateur
│   ├── MainActivity.kt          # Activité principale et navigation
│   ├── screens/
│   │   ├── GameScreen.kt       # Écran de jeu avec bouton microphone
│   │   ├── QuestionsScreen.kt   # Gestion des questions (liste, ajout, modification)
│   │   ├── ConnectionScreen.kt  # Connexion au Raspberry Pi
│   │   └── QuestionFormDialog.kt # Dialogue d'ajout/modification de question
│   └── theme/                   # Thème Material Design 3
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── viewmodel/                   # ViewModels (MVVM)
│   ├── GameViewModel.kt         # Logique de l'écran de jeu
│   ├── QuestionsViewModel.kt    # Logique de gestion des questions
│   └── ConnectionViewModel.kt   # Logique de connexion WebSocket
├── data/
│   ├── api/
│   │   ├── WebSocketClient.kt   # Client WebSocket (communication avec Raspberry Pi)
│   │   └── RaspberryPiApi.kt    # API wrapper pour WebSocket
│   ├── speech/
│   │   └── SpeechRecognitionService.kt  # Reconnaissance vocale Android
│   ├── models/
│   │   └── Question.kt          # Modèle de données Question
│   └── repository/
│       └── QuestionsRepository.kt # Repository pour les questions locales
└── AndroidManifest.xml          # Permissions (RECORD_AUDIO)

```

---

## 🏗️ Architecture de votre partie

### 1. **Architecture MVVM (Model-View-ViewModel)**

Votre application suit le pattern **MVVM** :

- **Model** : `Question.kt`, `QuestionsRepository.kt` (données)
- **View** : `GameScreen.kt`, `QuestionsScreen.kt`, etc. (UI avec Compose)
- **ViewModel** : `GameViewModel.kt`, `QuestionsViewModel.kt` (logique métier)

**Principe** : Les ViewModels exposent des `StateFlow` que les Views observent. Les Views ne contiennent que la logique d'affichage, toute la logique métier est dans les ViewModels.

### 2. **Jetpack Compose (UI moderne)**

Vous utilisez **Jetpack Compose**, le framework UI moderne d'Android :

- **Composables** : Fonctions Kotlin qui retournent de l'UI
- **State** : `remember`, `mutableStateOf` pour gérer l'état local
- **Material Design 3** : Design system moderne

**Exemple :**
```kotlin
@Composable
fun GameScreen(viewModel: GameViewModel = viewModel()) {
    val isListening by viewModel.isListening.collectAsState()
    
    Button(onClick = { viewModel.startRecording() }) {
        Text(if (isListening) "Arrêter" else "Enregistrer")
    }
}
```

### 3. **Reconnaissance vocale Android (`SpeechRecognitionService`)**

Vous utilisez l'API native Android `SpeechRecognizer` pour la reconnaissance vocale :

**Fonctionnalités :**
- **Reconnaissance en temps réel** : Résultats partiels pendant que l'utilisateur parle
- **Langue française** : Configuré pour `fr-FR`
- **Pas de timeout** : L'enregistrement continue jusqu'à ce que l'utilisateur arrête
- **Gestion d'erreurs** : Messages d'erreur clairs (permission, réseau, etc.)

**Flux :**
1. Utilisateur appuie sur le bouton → `startListening()`
2. Android enregistre l'audio du microphone
3. Résultats partiels → `onPartialResults()` → Affichage en temps réel
4. Résultat final → `onResults()` → Envoi via WebSocket
5. Utilisateur appuie à nouveau → `stopListening()`

**Permissions :**
- `RECORD_AUDIO` : Demandée à l'exécution (runtime permission)

### 4. **Communication WebSocket (`WebSocketClient`)**

Vous utilisez **OkHttp WebSocket** pour communiquer avec la Raspberry Pi :

**États de connexion :**
- `Disconnected` : Non connecté
- `Connecting` : Connexion en cours
- `Connected` : Connecté et prêt
- `Error(message)` : Erreur avec message

**Messages envoyés :**
- `ANSWER_QUESTION` : Envoie la réponse reconnue
- `ADD_QUESTION` : Ajoute une nouvelle question
- `GET_QUESTIONS` : Demande la liste des questions
- `DELETE_QUESTION` : Supprime une question
- `PING` : Test de connexion

**Messages reçus :**
- `CONNECTED` : Confirmation de connexion
- `QUESTION_ADDED` : Confirmation d'ajout de question
- `QUESTIONS_LIST` : Liste des questions
- `PONG` : Réponse au ping

### 5. **Gestion des questions (`QuestionsViewModel`)**

Vous gérez un **cache local** des questions dans `QuestionsRepository` :

- **Ajout local** : Questions ajoutées sont d'abord sauvegardées localement
- **Synchronisation** : Si connecté au Raspberry Pi, envoie via WebSocket
- **Filtrage** : Recherche par texte et filtre par catégorie
- **CRUD complet** : Create, Read, Update, Delete

---

## 🔑 Concepts clés à maîtriser

### 1. **StateFlow et collectAsState()**

`StateFlow` est un flux de données observable :

```kotlin
// Dans ViewModel
private val _isListening = MutableStateFlow(false)
val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

// Dans Composable
val isListening by viewModel.isListening.collectAsState()
```

Quand `_isListening.value` change, tous les Composables qui observent `isListening` se mettent à jour automatiquement.

### 2. **Coroutines et ViewModelScope**

Les opérations asynchrones (WebSocket, reconnaissance vocale) utilisent des **coroutines** :

```kotlin
viewModelScope.launch {
    // Code asynchrone ici
    speechService.recognitionResult.collect { result ->
        // Traiter le résultat
    }
}
```

`viewModelScope` annule automatiquement les coroutines quand le ViewModel est détruit.

### 3. **Permissions runtime**

Android 6+ nécessite de demander les permissions sensibles à l'exécution :

```kotlin
val permissionLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.RequestPermission()
) { isGranted ->
    if (isGranted) {
        viewModel.startRecording()
    }
}
```

### 4. **Material Design 3**

Vous utilisez Material Design 3 pour un design moderne :
- **Thème** : Couleurs, typographie, formes
- **Composants** : `Card`, `Button`, `TextField`, `TopAppBar`, etc.
- **Navigation** : `NavigationBar` en bas de l'écran

### 5. **Singleton Pattern**

`WebSocketClient` et `SpeechRecognitionService` utilisent le pattern singleton pour une instance unique :

```kotlin
companion object {
    @Volatile
    private var instance: WebSocketClient? = null
    
    fun getInstance(): WebSocketClient {
        return instance ?: synchronized(this) {
            instance ?: WebSocketClient().also { instance = it }
        }
    }
}
```

---

## 🔄 Flux de données

### Scénario : Répondre à une question

1. **Raspberry Pi** pose une question via TTS (Personne 1)
2. **Utilisateur** appuie sur le bouton "Enregistrer" dans `GameScreen`
3. **GameViewModel** → `startRecording()` → `SpeechRecognitionService.startListening()`
4. **Android SpeechRecognizer** enregistre l'audio
5. **Résultats partiels** → `onPartialResults()` → Affichage en temps réel dans l'UI
6. **Utilisateur** appuie sur "Arrêter"
7. **Résultat final** → `onResults()` → `recognitionResult.value = "Paris"`
8. **GameViewModel** observe `recognitionResult` → `sendAnswer("Paris")`
9. **WebSocketClient** → Envoie `{"type": "ANSWER_QUESTION", "data": {"answer": "Paris"}}`
10. **Raspberry Pi** reçoit le message (Personne 5) → Valide avec Flask (Personne 4)

### Scénario : Ajouter une question

1. **Utilisateur** remplit le formulaire dans `QuestionsScreen`
2. **QuestionsViewModel** → `addQuestion(...)`
3. **QuestionsRepository** → Ajoute localement (cache)
4. **Si connecté** → `RaspberryPiApi.addQuestion()` → WebSocket `ADD_QUESTION`
5. **Raspberry Pi** (Personne 5) reçoit → Sauvegarde localement → Synchronise avec Flask (Personne 4)
6. **Confirmation** → `QUESTION_ADDED` → Affichage message de succès

### Scénario : Connexion au Raspberry Pi

1. **Utilisateur** entre l'IP et le port dans `ConnectionScreen`
2. **ConnectionViewModel** → `connect(ip, port)`
3. **WebSocketClient** → `connect("ws://192.168.1.100:8765")`
4. **OkHttp** établit la connexion WebSocket
5. **Raspberry Pi** (Personne 2) accepte la connexion
6. **État** → `ConnectionState.Connected` → UI se met à jour automatiquement

---

## 🛠️ Technologies utilisées

- **Kotlin** : Langage de programmation
- **Jetpack Compose** : Framework UI moderne
- **Material Design 3** : Design system
- **Coroutines & Flow** : Programmation asynchrone
- **StateFlow** : Gestion d'état réactive
- **OkHttp** : Client HTTP/WebSocket (`implementation 'com.squareup.okhttp3:okhttp:4.x'`)
- **SpeechRecognizer** : API Android native pour reconnaissance vocale
- **MVVM** : Architecture pattern
- **AndroidX Lifecycle** : Gestion du cycle de vie

---

## 📝 Questions fréquentes du professeur

### "Comment fonctionne votre reconnaissance vocale ?"

**Réponse :**
"J'utilise l'API native Android `SpeechRecognizer` qui fonctionne avec les services Google. L'utilisateur appuie sur un bouton pour démarrer l'enregistrement, et je reçois des résultats partiels en temps réel pendant qu'il parle. Quand il appuie à nouveau pour arrêter, je récupère le résultat final et l'envoie directement au Raspberry Pi via WebSocket. La reconnaissance est configurée pour le français (`fr-FR`) et n'a pas de timeout, l'utilisateur contrôle quand arrêter."

### "Pourquoi utilisez-vous StateFlow plutôt que LiveData ?"

**Réponse :**
"StateFlow est la solution moderne recommandée par Google pour Jetpack Compose. Il est plus léger que LiveData, fonctionne mieux avec les coroutines, et s'intègre naturellement avec Compose via `collectAsState()`. De plus, StateFlow est thread-safe et peut être utilisé dans des contextes Kotlin multiplateforme."

### "Comment gérez-vous les permissions microphone ?"

**Réponse :**
"Je demande la permission `RECORD_AUDIO` à l'exécution avec `rememberLauncherForActivityResult`. Si l'utilisateur refuse, j'affiche un message d'erreur explicite. La permission est nécessaire car Android 6+ exige une permission runtime pour accéder au microphone."

### "Quelle est la différence entre votre reconnaissance vocale et celle de la Raspberry Pi ?"

**Réponse :**
"La reconnaissance vocale sur Android utilise les services Google (nécessite Internet), tandis que la Raspberry Pi utilise VOSK (offline). Dans notre architecture actuelle, on utilise uniquement la reconnaissance Android car elle est plus précise et ne nécessite pas de matériel supplémentaire. Le téléphone enregistre, reconnaît, et envoie le texte au Raspberry Pi qui valide la réponse."

### "Comment synchronisez-vous les questions entre l'app et le Raspberry Pi ?"

**Réponse :**
"Quand une question est ajoutée, je la sauvegarde d'abord localement dans un cache (QuestionsRepository). Si l'application est connectée au Raspberry Pi, j'envoie aussi la question via WebSocket avec le message `ADD_QUESTION`. Le Raspberry Pi la sauvegarde localement puis la synchronise avec Flask. Si la connexion est perdue, la question reste dans le cache local et peut être synchronisée plus tard."

### "Comment gérez-vous les erreurs de connexion WebSocket ?"

**Réponse :**
"J'utilise un `ConnectionState` sealed class qui peut être `Connected`, `Connecting`, `Disconnected`, ou `Error(message)`. Quand une erreur survient (timeout, réseau inaccessible, etc.), je mets à jour l'état avec un message d'erreur explicite. L'UI observe cet état et affiche automatiquement le statut de connexion. L'utilisateur peut réessayer de se connecter depuis l'écran de connexion."

### "Pourquoi utilisez-vous Jetpack Compose plutôt que XML ?"

**Réponse :**
"Jetpack Compose est le framework UI moderne recommandé par Google. Il permet d'écrire l'UI de manière déclarative en Kotlin, ce qui est plus concis et type-safe que XML. Compose gère automatiquement la recomposition (mise à jour de l'UI) quand l'état change, ce qui simplifie la gestion d'état. De plus, Compose est plus performant et permet de créer des animations complexes plus facilement."

---

## 🚀 Points importants à retenir

1. **Vous êtes l'interface utilisateur** : Toute interaction passe par votre application
2. **Reconnaissance vocale Android** : Vous utilisez l'API native, pas VOSK
3. **Communication WebSocket** : Vous envoyez les réponses et questions au Raspberry Pi
4. **Cache local** : Les questions sont sauvegardées localement même sans connexion
5. **Architecture MVVM** : Séparation claire entre UI (Compose) et logique (ViewModel)
6. **Material Design 3** : Design moderne et cohérent
7. **Gestion d'état réactive** : StateFlow + Compose = UI qui se met à jour automatiquement

---

## 🔗 Interactions avec les autres parties

- **Personne 1 (TTS/VOSK)** : Vous n'interagissez pas directement, mais la Raspberry Pi utilise TTS pour poser les questions
- **Personne 2 (WebSocket/GPIO)** : Vous vous connectez à son serveur WebSocket
- **Personne 4 (Flask)** : Vous n'interagissez pas directement, mais vos questions sont synchronisées via Raspberry Pi → Flask
- **Personne 5 (Logique métier)** : Vous envoyez `ANSWER_QUESTION` et `ADD_QUESTION` via WebSocket, il traite et répond

---

## 🎨 Écrans principaux

### 1. **GameScreen** (Écran de jeu)
- Bouton microphone pour enregistrer les réponses
- Affichage du résultat partiel de reconnaissance
- Statut de connexion WebSocket
- Gestion des permissions microphone

### 2. **QuestionsScreen** (Gestion des questions)
- Liste des questions avec recherche et filtres
- Ajout/modification/suppression de questions
- Synchronisation avec Raspberry Pi
- Cache local

### 3. **ConnectionScreen** (Connexion)
- Formulaire IP/Port
- Statut de connexion en temps réel
- Test de connexion (Ping/Pong)
- Gestion des erreurs de connexion

### 4. **MainActivity** (Navigation)
- Navigation entre les écrans
- Barre de navigation en bas
- Thème Material Design 3

---

## 📊 Flux de jeu complet

```
┌─────────────────────────────────────────────────────────┐
│  GameScreen - Utilisateur appuie sur "Enregistrer"      │
│                                                          │
│  1. GameViewModel.startRecording()                       │
│     ↓                                                    │
│  2. SpeechRecognitionService.startListening()           │
│     ↓                                                    │
│  3. Android SpeechRecognizer enregistre                 │
│     ↓                                                    │
│  4. Résultats partiels → Affichage en temps réel        │
│     ↓                                                    │
│  5. Utilisateur appuie sur "Arrêter"                    │
│     ↓                                                    │
│  6. Résultat final → recognitionResult.value            │
│     ↓                                                    │
│  7. GameViewModel observe → sendAnswer()                 │
│     ↓                                                    │
│  8. WebSocketClient.sendAnswer() → WebSocket             │
│     ↓                                                    │
│  9. Raspberry Pi (Personne 5) reçoit ANSWER_QUESTION    │
│     ↓                                                    │
│  10. Validation Flask (Personne 4) → TTS feedback        │
└─────────────────────────────────────────────────────────┘
```

---

**Vous êtes l'interface utilisateur du système ! 🎯**
