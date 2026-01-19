# 📚 Documentation - Personne 5 : Logique métier du jeu (Raspberry Pi)

## 🎯 Votre Mission

Vous êtes responsable de la **logique métier du jeu** sur la Raspberry Pi. Vous orchestrez le déroulement du jeu : récupération des questions depuis Flask, gestion des réponses, calcul des scores, synchronisation des données. Votre code fait le lien entre le serveur Flask (Personne 4), le WebSocket (Personne 2), la reconnaissance vocale (Personne 1), et l'application Android (Personne 3).

---

## 📁 Fichiers dont vous êtes responsable

Dans `raspberry-pi/server.py` :
- Classe `Database` (lignes ~507-750) - Base de données locale Raspberry Pi
- Fonction `infinite_game_loop()` (lignes ~1351-1496) - Boucle principale du jeu
- Fonction `send_next_question()` (lignes ~1158-1208) - Envoi d'une question
- Fonction `handle_answer()` (lignes ~1211-1287) - Traitement d'une réponse
- Fonction `fetch_random_question_from_flask()` (lignes ~782-804) - Récupération depuis Flask
- Fonction `validate_answer_with_flask()` (lignes ~807-827) - Validation via Flask
- Fonction `add_question_to_flask()` (lignes ~830-853) - Synchronisation avec Flask
- Fonction `handle_message()` (lignes ~876-1020) - Gestion des messages WebSocket (partie métier)
- Fonction `is_answer_correct()` (lignes ~750-780) - Comparaison locale des réponses

Fichiers associés :
- `raspberry-pi/cultureg.db` - Base de données locale (fichier généré)

---

## 🏗️ Architecture de votre partie

### 1. **Boucle de jeu infinie (`infinite_game_loop()`)**

C'est le **cœur du système**. Cette fonction tourne en continu et :

1. **Récupère une question** depuis Flask (via `fetch_random_question_from_flask()`)
2. **Évite les répétitions** : Garde en mémoire les 5 dernières questions posées
3. **Lit la question** via TTS (Text-to-Speech) - géré par Personne 1
4. **Attend la réponse** via WebSocket depuis l'application Android
5. **Valide la réponse** via Flask (via `validate_answer_with_flask()`)
6. **Met à jour les statistiques** (score, bonnes/mauvaises réponses)
7. **Annonce le résultat** via TTS ("Correct !" ou "Incorrect. La bonne réponse était X")
8. **Répète** indéfiniment

**Variables globales importantes :**
- `game_stats` : Dictionnaire avec `total_questions`, `correct_answers`, `wrong_answers`, `current_score`
- `recent_question_ids` : Liste des IDs des 5 dernières questions (pour éviter répétitions)

### 2. **Base de données locale (`Database` classe)**

La Raspberry Pi a sa **propre base de données SQLite** (`cultureg.db`) qui sert de **cache local** et stocke :

**Table `questions`** :
- Cache des questions récupérées depuis Flask
- Structure : `id`, `question`, `correct_answer`, `category`, `difficulty`, `time_limit`

**Table `scores`** :
- Historique des parties jouées
- Structure : `id`, `player_name`, `score`, `total_questions`, `correct_answers`, `game_date`

**Table `game_settings`** :
- Paramètres du jeu (timer, sons activés, etc.)
- Structure : `key`, `value`, `updated_at`

**Migration automatique** : Votre code détecte si la structure de la table `questions` est ancienne et la migre automatiquement.

### 3. **Gestion des messages WebSocket (`handle_message()`)**

Vous gérez les messages reçus depuis l'application Android :

**`ADD_QUESTION`** :
- Reçoit une question depuis Android
- Sauvegarde dans la base locale Raspberry Pi
- **Synchronise avec Flask** (envoie à l'API Flask pour que la question soit disponible pour le jeu)

**`ANSWER_QUESTION`** :
- Reçoit la réponse reconnue par l'application Android
- Appelle `handle_answer()` pour traiter la réponse

**`GET_QUESTIONS`**, `DELETE_QUESTION`, `UPDATE_SETTINGS`, etc. :
- Gestion des autres opérations

### 4. **Synchronisation Flask ↔ Raspberry Pi**

Vous êtes le **pont** entre Flask et la Raspberry Pi :

**Flask → Raspberry Pi** :
- `fetch_random_question_from_flask()` : Récupère une question aléatoire via `GET /questions/random`
- Utilise `aiohttp` (client HTTP asynchrone) pour communiquer avec Flask

**Raspberry Pi → Flask** :
- `validate_answer_with_flask()` : Valide une réponse via `POST /answers`
- `add_question_to_flask()` : Ajoute une question via `POST /questions`

**Pourquoi deux bases de données ?**
- **Flask (Personne 4)** : Source de vérité, toutes les questions
- **Raspberry Pi (Vous)** : Cache local pour performance + historique des scores locaux

---

## 🔑 Concepts clés à maîtriser

### 1. **Programmation asynchrone (asyncio)**

Votre code utilise `async/await` car :
- Le serveur WebSocket est asynchrone
- Les appels HTTP à Flask sont asynchrones
- Plusieurs clients peuvent se connecter simultanément

**Exemple :**
```python
async def fetch_random_question_from_flask():
    async with aiohttp.ClientSession() as session:
        async with session.get(f"{FLASK_SERVER_URL}/questions/random") as resp:
            data = await resp.json()
            return data
```

### 2. **Gestion des états globaux**

Vous utilisez des **variables globales** pour partager l'état entre fonctions :
- `game_stats` : Statistiques du jeu en cours
- `current_game` : Jeu actuel (si mode jeu fini)
- `connected_clients` : Clients WebSocket connectés

### 3. **Évitement des répétitions**

Pour éviter de poser la même question plusieurs fois de suite :
```python
recent_question_ids = []  # Liste des IDs récents
MAX_RECENT_QUESTIONS = 5  # Maximum à garder

# Avant de poser une question
if question_id not in recent_question_ids:
    # Question OK, on l'ajoute à la liste
    recent_question_ids.append(question_id)
    if len(recent_question_ids) > MAX_RECENT_QUESTIONS:
        recent_question_ids.pop(0)  # Enlever la plus ancienne
```

### 4. **Calcul des scores**

Le score dépend de la difficulté :
- **EASY** : +1 point
- **MEDIUM** : +2 points
- **HARD** : +3 points

### 5. **Broadcast de messages**

Quand un événement important se produit (bonne réponse, stats, etc.), vous envoyez un message à **tous les clients connectés** via `broadcast_message()`.

---

## 🔄 Flux de données détaillé

### Scénario : Une question est posée et répondue

1. **`infinite_game_loop()`** démarre
2. **Appel Flask** : `fetch_random_question_from_flask()` → Récupère question aléatoire
3. **Vérification répétition** : Vérifie si la question n'est pas dans `recent_question_ids`
4. **TTS** : Personne 1 lit la question via `tts_controller.speak()`
5. **Attente réponse** : La boucle attend un message WebSocket `ANSWER_QUESTION` depuis Android
6. **Réception réponse** : `handle_message()` reçoit `ANSWER_QUESTION` → Appelle `handle_answer()`
7. **Validation Flask** : `validate_answer_with_flask()` → Envoie à Flask pour validation
8. **Mise à jour stats** : Incrémente `correct_answers` ou `wrong_answers`, met à jour le score
9. **Feedback TTS** : Personne 1 annonce "Correct !" ou "Incorrect. La bonne réponse était X"
10. **Feedback GPIO** : Personne 2 active LED (bonne réponse) ou vibreur (mauvaise réponse)
11. **Broadcast** : Envoie `ANSWER_RESULT` à tous les clients WebSocket
12. **Pause** : Attend 2 secondes
13. **Boucle** : Retour à l'étape 2

### Scénario : Ajout d'une question depuis Android

1. **Android** envoie `ADD_QUESTION` via WebSocket
2. **`handle_message()`** reçoit le message
3. **Validation** : Vérifie que les champs requis (`id`, `question`, `correctAnswer`) sont présents
4. **Sauvegarde locale** : `db.add_question()` → Sauvegarde dans `cultureg.db`
5. **Synchronisation Flask** : `add_question_to_flask()` → Envoie à l'API Flask
6. **Confirmation** : Envoie `QUESTION_ADDED` à Android avec le statut de synchronisation

---

## 🛠️ Technologies utilisées

- **asyncio** : Programmation asynchrone Python (intégré)
- **aiohttp** : Client HTTP asynchrone (`pip install aiohttp`)
- **sqlite3** : Base de données locale (intégré)
- **json** : Format d'échange (intégré)
- **websockets** : Serveur WebSocket (utilisé par Personne 2, mais vous gérez les messages)

---

## 📝 Questions fréquentes du professeur

### "Comment évitez-vous de répéter les mêmes questions ?"

**Réponse :**
"J'utilise une liste `recent_question_ids` qui garde en mémoire les IDs des 5 dernières questions posées. Avant de poser une nouvelle question, je vérifie si son ID n'est pas dans cette liste. Si c'est le cas, je réessaie avec une autre question. Cela garantit une variété dans les questions posées."

### "Pourquoi avez-vous deux bases de données (Flask et Raspberry Pi) ?"

**Réponse :**
"La base Flask est la **source de vérité** : elle contient toutes les questions disponibles. La base Raspberry Pi sert de **cache local** pour améliorer les performances et stocker l'historique des scores locaux. Quand une question est ajoutée depuis Android, elle est d'abord sauvegardée localement, puis synchronisée avec Flask pour qu'elle soit disponible pour tous."

### "Comment gérez-vous la synchronisation entre Flask et la Raspberry Pi ?"

**Réponse :**
"J'utilise `aiohttp` pour faire des requêtes HTTP asynchrones vers Flask. Quand je dois récupérer une question, j'appelle `GET /questions/random`. Quand je dois valider une réponse, j'appelle `POST /answers` avec le `question_id` et la réponse de l'utilisateur. La validation se fait toujours côté Flask pour garantir la cohérence."

### "Que se passe-t-il si Flask n'est pas disponible ?"

**Réponse :**
"Mon code gère les erreurs de connexion avec des try/except. Si Flask n'est pas disponible, je log une erreur et je réessaie après une pause. Le jeu peut continuer avec les questions déjà en cache local, mais la validation des réponses nécessite Flask. J'utilise aussi `AIOHTTP_AVAILABLE` pour vérifier si `aiohttp` est installé."

### "Comment calculez-vous les scores ?"

**Réponse :**
"Le score dépend de la difficulté de la question : EASY = +1 point, MEDIUM = +2 points, HARD = +3 points. Je maintiens aussi des statistiques globales : nombre total de questions, bonnes réponses, mauvaises réponses. Tous les 10 questions, j'envoie un message de statistiques à tous les clients connectés."

### "Quelle est la différence entre votre `handle_answer()` et la validation Flask ?"

**Réponse :**
"`handle_answer()` est ma fonction qui orchestre le traitement d'une réponse : elle appelle `validate_answer_with_flask()` pour valider, met à jour les stats, déclenche le TTS et le GPIO, et envoie le résultat aux clients. La validation Flask (Personne 4) fait la comparaison textuelle normalisée. Je pourrais aussi faire une validation locale avec `is_answer_correct()`, mais j'utilise toujours Flask pour garantir la cohérence."

### "Comment gérez-vous plusieurs clients WebSocket connectés simultanément ?"

**Réponse :**
"J'utilise un set `connected_clients` qui contient tous les clients connectés. Quand un événement important se produit (bonne réponse, stats, etc.), j'appelle `broadcast_message()` qui envoie le message à tous les clients. Cela permet à plusieurs téléphones de suivre le jeu en temps réel."

---

## 🚀 Points importants à retenir

1. **Vous orchestrez tout** : Vous coordonnez Flask, WebSocket, TTS, GPIO, et Android
2. **Boucle infinie** : Le jeu tourne en continu, posant des questions sans fin
3. **Cache intelligent** : Vous gardez un cache local mais synchronisez avec Flask
4. **Gestion d'erreurs** : Votre code doit être robuste si Flask ou un client se déconnecte
5. **Statistiques en temps réel** : Vous maintenez et diffusez les stats toutes les 10 questions

---

## 🔗 Interactions avec les autres parties

- **Personne 1 (TTS/VOSK)** : Vous appelez `tts_controller.speak()` pour lire les questions et annoncer les résultats
- **Personne 2 (WebSocket/GPIO)** : Vous recevez les messages WebSocket et déclenchez `gpio.good_answer()` / `gpio.wrong_answer()`
- **Personne 3 (Android)** : Vous recevez `ADD_QUESTION` et `ANSWER_QUESTION` via WebSocket
- **Personne 4 (Flask)** : Vous appelez son API pour récupérer questions et valider réponses

---

## 🎮 Flux de jeu complet

```
┌─────────────────────────────────────────────────────────┐
│  infinite_game_loop() - Boucle principale               │
│                                                          │
│  1. fetch_random_question_from_flask()                  │
│     ↓                                                    │
│  2. Vérifier répétition (recent_question_ids)           │
│     ↓                                                    │
│  3. tts_controller.speak(question)  [Personne 1]        │
│     ↓                                                    │
│  4. Attendre message WebSocket ANSWER_QUESTION           │
│     ↓                                                    │
│  5. handle_message() → handle_answer()                   │
│     ↓                                                    │
│  6. validate_answer_with_flask()  [Personne 4]          │
│     ↓                                                    │
│  7. Mettre à jour game_stats (score, correct/wrong)      │
│     ↓                                                    │
│  8. tts_controller.speak("Correct!" ou "Incorrect...")  │
│     ↓                                                    │
│  9. gpio.good_answer() ou gpio.wrong_answer() [Personne 2]│
│     ↓                                                    │
│  10. broadcast_message(ANSWER_RESULT)                    │
│     ↓                                                    │
│  11. Attendre 2 secondes                                 │
│     ↓                                                    │
│  12. Retour à l'étape 1 (boucle infinie)                │
└─────────────────────────────────────────────────────────┘
```

---

**Vous êtes le chef d'orchestre du jeu ! 🎯**
