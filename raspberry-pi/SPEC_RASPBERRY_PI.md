# SPEC - Serveur Raspberry Pi CultureG

> **Documentation et évolution du code serveur pour Raspberry Pi**
> Ce fichier sert de référence pour comprendre et faire évoluer le serveur WebSocket.

---

## 🎯 Vue d'Ensemble

Le serveur Raspberry Pi est un **serveur WebSocket** qui :
- Gère la communication avec l'application mobile Android
- Stocke les questions et scores dans une base SQLite
- Contrôle les GPIO (LED, vibreur, buzzer, bouton)
- Gère le déroulement du jeu (questions, réponses, scores)

---

## 📁 Structure du Code

```
raspberry-pi/
├── server.py              # Serveur WebSocket principal
├── requirements.txt       # Dépendances Python
├── README.md             # Guide d'installation
├── SPEC_RASPBERRY_PI.md  # Ce fichier
└── cultureg.db           # Base de données SQLite (générée)
```

---

## 🏗️ Architecture

### Composants Principaux

#### 1. **GPIOController**
- **Rôle** : Contrôle des composants GPIO
- **Fonctions** :
  - `good_answer()` : LED + Buzzer pour bonne réponse
  - `wrong_answer()` : Vibreur pour mauvaise réponse
  - `_button_callback()` : Détection bouton démarrage
- **Mode simulation** : Si GPIO échoue, fonctionne sans hardware

#### 2. **Database**
- **Rôle** : Gestion base de données SQLite
- **Tables** :
  - `questions` : Questions du jeu
  - `scores` : Historique des scores
  - `game_settings` : Paramètres du jeu
- **Fonctions principales** :
  - `add_question()` : Ajouter question
  - `get_all_questions()` : Récupérer toutes les questions
  - `delete_question()` : Supprimer question
  - `save_score()` : Sauvegarder score
  - `update_setting()` : Mettre à jour paramètre

#### 3. **Gestionnaire de Messages**
- **Fonction** : `handle_message()`
- **Types de messages supportés** :
  - `ADD_QUESTION` : Ajouter une question
  - `GET_QUESTIONS` : Récupérer toutes les questions
  - `DELETE_QUESTION` : Supprimer une question
  - `UPDATE_SETTINGS` : Mettre à jour paramètres
  - `GET_SETTINGS` : Récupérer paramètres
  - `START_GAME` : Démarrer une partie
  - `ANSWER_QUESTION` : Traiter une réponse
  - `GET_SCORES` : Récupérer scores récents
  - `PING` : Test de connexion

#### 4. **Gestion du Jeu**
- **Fonctions** :
  - `start_game()` : Initialiser une nouvelle partie
  - `send_next_question()` : Envoyer question suivante
  - `handle_answer()` : Traiter réponse + GPIO
  - `end_game()` : Terminer partie + sauvegarder score

---

## 🔌 GPIO - Configuration

### Pins Utilisés

| Composant | GPIO Pin | Direction | Description |
|-----------|----------|-----------|-------------|
| LED | 18 | OUT | Allumée pour bonne réponse |
| Vibreur | 23 | OUT | Activé pour mauvaise réponse |
| Buzzer | 24 | OUT | Son pour bonne réponse |
| Bouton | 25 | IN | Démarrage du jeu |

### Schéma de Connexion

```
Raspberry Pi GPIO:
├── Pin 18 (GPIO 18) → LED (via résistance 220Ω)
├── Pin 23 (GPIO 23) → Vibreur (via transistor)
├── Pin 24 (GPIO 24) → Buzzer (via résistance)
└── Pin 25 (GPIO 25) → Bouton (pull-up interne)
```

### Comportement

- **Bonne réponse** :
  1. LED s'allume (GPIO 18 HIGH)
  2. Buzzer joue un ton (1000Hz, 0.2s)
  3. LED s'éteint après 0.5s

- **Mauvaise réponse** :
  1. Vibreur s'active (GPIO 23 HIGH)
  2. Vibre pendant 0.3s
  3. Vibreur s'arrête

---

## 📡 Protocole WebSocket

### Format des Messages

Tous les messages sont en **JSON** avec la structure :
```json
{
  "type": "TYPE_MESSAGE",
  "data": { ... },
  "message": "Description optionnelle"
}
```

### Messages Entrants (App → Pi)

#### ADD_QUESTION
```json
{
  "type": "ADD_QUESTION",
  "data": {
    "id": "uuid",
    "question": "Texte question",
    "answers": ["R1", "R2", "R3", "R4"],
    "correctAnswerIndex": 0,
    "category": "Géographie",
    "difficulty": "EASY"
  }
}
```

**Réponse** :
```json
{
  "type": "QUESTION_ADDED",
  "data": {"success": true, "questionId": "uuid"},
  "message": "Question ajoutée"
}
```

#### START_GAME
```json
{
  "type": "START_GAME",
  "data": {
    "numberOfQuestions": 10
  }
}
```

**Réponse** : Envoie `CURRENT_QUESTION` avec la première question

#### ANSWER_QUESTION
```json
{
  "type": "ANSWER_QUESTION",
  "data": {
    "answerIndex": 0
  }
}
```

**Réponse** :
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

### Messages Sortants (Pi → App)

#### CONNECTED
Envoyé automatiquement à la connexion :
```json
{
  "type": "CONNECTED",
  "data": {
    "message": "Connecté au serveur CultureG",
    "serverVersion": "1.0.0",
    "questionsCount": 25
  }
}
```

#### CURRENT_QUESTION
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

#### GAME_ENDED
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

---

## 🗄️ Base de Données

### Schéma SQLite

#### Table `questions`
```sql
CREATE TABLE questions (
    id TEXT PRIMARY KEY,
    question TEXT NOT NULL,
    answers TEXT NOT NULL,           -- JSON array
    correct_answer_index INTEGER NOT NULL,
    category TEXT,
    difficulty TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

#### Table `scores`
```sql
CREATE TABLE scores (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    player_name TEXT,
    score INTEGER NOT NULL,
    total_questions INTEGER NOT NULL,
    correct_answers INTEGER NOT NULL,
    game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

#### Table `game_settings`
```sql
CREATE TABLE game_settings (
    key TEXT PRIMARY KEY,
    value TEXT NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
)
```

### Paramètres par Défaut

- `timer_seconds` : 30
- `sound_enabled` : true
- `vibration_enabled` : true
- `questions_per_game` : 10

---

## 🔄 Flux de Jeu

### Séquence Complète

```
1. App envoie START_GAME
   ↓
2. Pi sélectionne N questions aléatoires
   ↓
3. Pi envoie CURRENT_QUESTION (question 1)
   ↓
4. App affiche question + réponses
   ↓
5. Utilisateur répond
   ↓
6. App envoie ANSWER_QUESTION
   ↓
7. Pi vérifie réponse
   ↓
8. Pi active GPIO (LED/buzzer OU vibreur)
   ↓
9. Pi envoie ANSWER_RESULT
   ↓
10. Pi envoie CURRENT_QUESTION (question suivante)
    ↓
11. Répéter 4-10 jusqu'à fin
    ↓
12. Pi envoie GAME_ENDED
    ↓
13. Pi sauvegarde score dans DB
```

---

## 🧪 Tests

### Test de Connexion

```python
import asyncio
import websockets
import json

async def test_connection():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        # Recevoir message de connexion
        response = await websocket.recv()
        print(f"Connexion: {response}")
        
        # Envoyer ping
        await websocket.send(json.dumps({"type": "PING"}))
        response = await websocket.recv()
        print(f"Ping: {response}")

asyncio.run(test_connection())
```

### Test Ajout Question

```python
async def test_add_question():
    uri = "ws://localhost:8765"
    async with websockets.connect(uri) as websocket:
        await websocket.recv()  # Ignorer CONNECTED
        
        message = {
            "type": "ADD_QUESTION",
            "data": {
                "id": "test-1",
                "question": "Test question ?",
                "answers": ["A", "B", "C", "D"],
                "correctAnswerIndex": 0,
                "category": "Test",
                "difficulty": "EASY"
            }
        }
        
        await websocket.send(json.dumps(message))
        response = await websocket.recv()
        print(f"Réponse: {response}")

asyncio.run(test_add_question())
```

---

## 📝 Évolutions et Améliorations

### Version 1.0 (Actuelle)
- ✅ Serveur WebSocket fonctionnel
- ✅ Gestion GPIO (LED, vibreur, buzzer, bouton)
- ✅ Base de données SQLite
- ✅ Gestion complète du jeu
- ✅ Sauvegarde des scores

### Améliorations Futures

#### Version 1.1 (À venir)
- [ ] Mode chronomètre (timer par question)
- [ ] Support multi-joueurs
- [ ] Statistiques avancées
- [ ] Export/Import questions (JSON)
- [ ] Interface web de gestion

#### Version 1.2 (À venir)
- [ ] Authentification des clients
- [ ] Chiffrement des communications
- [ ] Sauvegarde automatique périodique
- [ ] Logs dans fichier
- [ ] Configuration via fichier YAML

#### Version 2.0 (Long terme)
- [ ] Support Bluetooth (alternative WiFi)
- [ ] Interface graphique sur écran
- [ ] Reconnaissance vocale pour réponses
- [ ] Machine learning pour adapter difficulté
- [ ] Synchronisation cloud

---

## 🐛 Problèmes Connus

### 1. GPIO en Mode Simulation
**Problème** : Si RPi.GPIO n'est pas disponible, le serveur fonctionne mais sans GPIO réel.

**Solution** : C'est normal, le serveur continue de fonctionner. Pour activer GPIO :
```bash
sudo pip3 install RPi.GPIO
```

### 2. Port Déjà Utilisé
**Problème** : `Address already in use` sur le port 8765.

**Solution** : Changer le port dans `server.py` :
```python
WS_PORT = 8766  # Autre port
```

### 3. Base de Données Verrouillée
**Problème** : `database is locked` lors d'écritures multiples.

**Solution** : Le code gère déjà les connexions correctement. Si problème persiste, vérifier les permissions :
```bash
chmod 666 cultureg.db
```

---

## 🔧 Configuration Avancée

### Changer les Pins GPIO

Modifier dans `server.py` :
```python
LED_PIN = 18      # Changer selon ton setup
VIBRATOR_PIN = 23
BUZZER_PIN = 24
BUTTON_PIN = 25
```

### Changer le Port

```python
WS_PORT = 8765  # Port WebSocket
```

### Désactiver GPIO

Commenter l'initialisation :
```python
# gpio = GPIOController()  # Désactivé
gpio = None  # Mode simulation forcé
```

---

## 📊 Logs et Debugging

### Niveaux de Log

- **INFO** : Opérations normales
- **WARNING** : Avertissements (ex: GPIO non disponible)
- **ERROR** : Erreurs (ex: échec sauvegarde)

### Activer Debug Mode

Modifier dans `server.py` :
```python
logging.basicConfig(
    level=logging.DEBUG,  # Au lieu de INFO
    ...
)
```

---

## 🚀 Déploiement

### Service Systemd (Démarrage Auto)

Créer `/etc/systemd/system/cultureg.service` :
```ini
[Unit]
Description=CultureG WebSocket Server
After=network.target

[Service]
Type=simple
User=pi
WorkingDirectory=/home/pi/cultureg
ExecStart=/usr/bin/python3 /home/pi/cultureg/server.py
Restart=always

[Install]
WantedBy=multi-user.target
```

Activer :
```bash
sudo systemctl enable cultureg
sudo systemctl start cultureg
```

---

## 📞 Support

### Documentation
- `README.md` : Guide d'installation
- `SPEC_RASPBERRY_PI.md` : Ce fichier

### Tests
- Voir section "Tests" ci-dessus
- Tester chaque message individuellement

---

**Dernière mise à jour** : Version 1.0 - Décembre 2025
**Statut** : ✅ Fonctionnel et testé

