# 🧪 Guide de Test - Système VOSK + Flask

Ce guide explique comment tester le nouveau système avec reconnaissance vocale VOSK et intégration Flask.

## 📋 Prérequis

### Sur Raspberry Pi

1. **Modèle VOSK téléchargé :**
```bash
cd raspberry-pi
ls vosk-model-small-fr-0.22/
# Doit contenir: am/, graph/, ivector/, conf/
```

2. **Dépendances installées :**
```bash
pip3 install -r raspberry-pi/requirements.txt
```

3. **Base de données Flask avec des questions :**
```bash
cd serveur
python3 init_db.py
# Ou ajoutez des questions manuellement via l'API
```

## 🚀 Démarrage

### Méthode 1 : Script automatique

```bash
bash start_game_system.sh
```

### Méthode 2 : Manuel

**Terminal 1 - Flask :**
```bash
cd serveur
python3 webServer.py
```

**Terminal 2 - WebSocket :**
```bash
cd raspberry-pi
python3 server.py
```

## ✅ Tests à effectuer

### Test 1 : Démarrage du système

**Attendu :**
- Flask démarre sur port 5000
- WebSocket démarre sur port 8765
- Message : `🎮 Démarrage du mode jeu en boucle infinie`
- VOSK initialisé avec micro détecté

**Logs à vérifier :**
```
VOSK initialisé - Micro: [nom du micro], rate: [sample rate]
Mode: RÉEL (GPIO disponible)
🎮 Mode jeu en boucle infinie activé
```

### Test 2 : Connexion de l'app Android

1. Lance l'app Android
2. Va dans l'écran "Connexion"
3. Entre l'IP de la Raspberry Pi
4. Clique "Se connecter"

**Attendu :**
- Statut : "Connecté ✓"
- Message de bienvenue reçu
- Logs serveur : `Nouveau client connecté`

### Test 3 : Première question automatique

**Sans interaction, le système doit :**
1. Récupérer une question depuis Flask
2. Lire la question via TTS dans le casque
3. Afficher : `🎤 En attente de la réponse (appuyez sur GPIO 16)...`

**Logs attendus :**
```
Question récupérée depuis Flask: [texte de la question]
📝 Question #1: [texte]
🎤 En attente de la réponse (appuyez sur GPIO 16)...
```

### Test 4 : Réponse vocale (avec matériel)

1. Appuie sur le bouton GPIO 16
2. Dis la réponse dans le micro
3. Appuie à nouveau sur GPIO 16

**Attendu si bonne réponse :**
- Son de succès dans le casque
- LED GPIO 18 s'allume
- Log : `✅ Bonne réponse!`
- Score augmente de 10

**Attendu si mauvaise réponse :**
- Vibreur GPIO 23 activé
- Log : `❌ Mauvaise réponse. Bonne réponse: [texte]`

**Logs VOSK :**
```
Enregistrement démarré (VOSK)...
VOSK: Réponse reconnue: [texte]
Validation Flask: ✅ Correct (ou ❌ Incorrect)
```

### Test 5 : Mode simulation (sans matériel)

Si tu n'as pas de Raspberry Pi avec GPIO :

1. Lance le serveur sur PC
2. Le système fonctionne en mode simulation
3. Pas de TTS réel, pas de GPIO réel
4. Logs : `SIMULATION: Bonne réponse (LED + Son)`

**Réponse simulée :**
- Après 2 secondes, une "réponse simulée" est générée
- Le flux continue comme si une vraie réponse était donnée

### Test 6 : Statistiques périodiques

**Après 10 questions :**
- Message `GAME_STATS` envoyé à l'app Android
- Contient :
  - `total_questions`
  - `correct_answers`
  - `wrong_answers`
  - `current_score`
  - `session_duration`

**Log attendu :**
```
📊 Envoi des statistiques (session de 10 questions)
```

### Test 7 : Arrêt propre

1. Appuie sur `Ctrl+C` dans le terminal
2. Le système doit :
   - Annuler la tâche de jeu
   - Envoyer les stats finales (`GAME_STATS_FINAL`)
   - Nettoyer GPIO
   - Arrêter Flask (si script utilisé)

**Logs attendus :**
```
🛑 Arrêt du serveur...
GPIO nettoyé
Serveur arrêté
```

## 🐛 Dépannage

### Problème : "aiohttp non disponible"

```bash
pip3 install aiohttp
```

### Problème : "VOSK non disponible"

```bash
pip3 install vosk sounddevice scipy numpy
```

### Problème : "Erreur connexion Flask (serveur non démarré?)"

1. Vérifie que Flask tourne : `curl http://localhost:5000/questions/random`
2. Si erreur 404, ajoute des questions dans la BDD Flask
3. Lance Flask avant le serveur WebSocket

### Problème : "Timeout: Pas d'enregistrement"

- Le bouton GPIO 16 n'est pas pressé assez vite
- Augmente le timeout dans `wait_for_answer(timeout=60)`
- En mode simulation, une réponse est générée après 2s

### Problème : "input overflow" (VOSK)

- Le micro enregistre trop vite pour le traitement
- Normal au démarrage, ne devrait pas bloquer la reconnaissance
- Augmente le buffer si trop fréquent

### Problème : Questions non trouvées (Flask 404)

```bash
# Ajouter une question de test
curl -X POST http://localhost:5000/questions \
  -H "Content-Type: application/json" \
  -d '{
    "question": "Test question ?",
    "correct_answer": "test",
    "category": "Test"
  }'
```

## 📊 Vérification des bases de données

### Base Flask (qa_database.db)

```bash
cd serveur
sqlite3 qa_database.db "SELECT * FROM questions;"
```

### Base WebSocket (cultureg.db)

```bash
cd raspberry-pi
sqlite3 cultureg.db "SELECT * FROM questions;"
```

**Note :** Ces deux bases sont séparées. Flask contient les questions pour le jeu, cultureg.db contient les questions ajoutées via l'app Android.

## ✅ Checklist complète

- [ ] Flask démarre sans erreur
- [ ] WebSocket démarre sans erreur
- [ ] VOSK initialisé avec micro
- [ ] Questions récupérées depuis Flask
- [ ] TTS lit les questions (ou mode simulation)
- [ ] Bouton GPIO 16 déclenche l'enregistrement
- [ ] VOSK reconnaît les réponses
- [ ] Validation Flask fonctionne
- [ ] Feedback GPIO (LED/Vibreur) correct
- [ ] Stats envoyées toutes les 10 questions
- [ ] App Android reçoit les messages
- [ ] Arrêt propre avec Ctrl+C

---

**Bon test ! 🎉**
