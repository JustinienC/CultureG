# 🎮 Serveur CultureG - Raspberry Pi

Serveur WebSocket pour le jeu de culture générale sur Raspberry Pi.

## 📋 Prérequis

- Raspberry Pi (modèle 3 ou supérieur recommandé)
- Python 3.7 ou supérieur
- Composants GPIO :
  - LED (GPIO 18)
  - Vibreur (GPIO 23)
  - Buzzer/Haut-parleur (GPIO 24)
  - Bouton de démarrage (GPIO 25)

## 🚀 Installation

### 1. Installer les dépendances

```bash
pip3 install -r requirements.txt
```

### 2. Configuration GPIO (optionnel)

Si tu n'as pas les composants GPIO connectés, le serveur fonctionnera en mode simulation.

**Connexions GPIO :**
- LED → GPIO 18 (bonne réponse)
- Vibreur → GPIO 23 (mauvaise réponse)
- Buzzer → GPIO 24 (son bonne réponse)
- Bouton → GPIO 25 (démarrage jeu)

### 3. Lancer le serveur

```bash
python3 server.py
```

Le serveur écoute sur `ws://0.0.0.0:8765`

## 📡 Communication

### Messages App → Raspberry Pi

#### Ajouter une question
```json
{
  "type": "ADD_QUESTION",
  "data": {
    "id": "uuid-123",
    "question": "Quelle est la capitale de la France ?",
    "answers": ["Paris", "Lyon", "Marseille", "Toulouse"],
    "correctAnswerIndex": 0,
    "category": "Géographie",
    "difficulty": "EASY"
  }
}
```

#### Démarrer un jeu
```json
{
  "type": "START_GAME",
  "data": {
    "numberOfQuestions": 10
  }
}
```

#### Répondre à une question
```json
{
  "type": "ANSWER_QUESTION",
  "data": {
    "answerIndex": 0
  }
}
```

#### Mettre à jour les paramètres
```json
{
  "type": "UPDATE_SETTINGS",
  "data": {
    "timer_seconds": 30,
    "sound_enabled": true,
    "vibration_enabled": true
  }
}
```

### Messages Raspberry Pi → App

#### Question actuelle
```json
{
  "type": "CURRENT_QUESTION",
  "data": {
    "question": {...},
    "questionNumber": 1,
    "totalQuestions": 10
  }
}
```

#### Résultat de la réponse
```json
{
  "type": "ANSWER_RESULT",
  "data": {
    "isCorrect": true,
    "correctAnswerIndex": 0,
    "currentScore": 5,
    "totalQuestions": 10
  }
}
```

#### Fin du jeu
```json
{
  "type": "GAME_ENDED",
  "data": {
    "finalScore": 8,
    "totalQuestions": 10,
    "correctAnswers": 8,
    "wrongAnswers": 2
  }
}
```

## 🗄️ Base de données

Le serveur utilise SQLite (`cultureg.db`) avec 3 tables :

- **questions** : Stocke toutes les questions
- **scores** : Historique des scores
- **game_settings** : Paramètres du jeu

## 🔧 Configuration

Modifier les constantes dans `server.py` :

```python
LED_PIN = 18          # GPIO pour LED
VIBRATOR_PIN = 23     # GPIO pour vibreur
BUZZER_PIN = 24       # GPIO pour buzzer
BUTTON_PIN = 25       # GPIO pour bouton
WS_PORT = 8765        # Port WebSocket
```

## 🧪 Test

### Test simple avec Python

```python
import asyncio
import websockets
import json

async def test():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        # Envoyer un ping
        await websocket.send(json.dumps({"type": "PING"}))
        response = await websocket.recv()
        print(f"Réponse: {response}")

asyncio.run(test())
```

## 📝 Logs

Les logs sont affichés dans la console avec le format :
```
2025-12-XX XX:XX:XX - INFO - Message reçu: ADD_QUESTION
```

## 🛠️ Dépannage

### Le serveur ne démarre pas
- Vérifier que le port 8765 n'est pas déjà utilisé
- Vérifier les permissions Python

### GPIO ne fonctionne pas
- Vérifier les connexions physiques
- Vérifier que RPi.GPIO est installé
- Le serveur fonctionne en mode simulation si GPIO échoue

### Base de données corrompue
- Supprimer `cultureg.db` et relancer (sera recréée automatiquement)

## 📞 Support

Voir `SPEC_RASPBERRY_PI.md` pour la documentation complète.

