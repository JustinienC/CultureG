# 🧪 Comment Tester CultureG - Guide Rapide

## 🎯 Test Rapide (10 minutes)

### Étape 1 : Tester l'App Android SEULE (sans Raspberry Pi)

#### 1.1 Lancer l'app dans Android Studio

1. **Ouvre Android Studio**
2. **Ouvre le projet** : `File > Open` → Sélectionne le dossier `Culture G`
3. **Attends la synchronisation Gradle** (2-5 min la première fois)
4. **Lance l'app** : Clique sur ▶️ (Run 'app') ou `Shift + F10`

#### 1.2 Tester les écrans

✅ **Navigation** : Tu devrais voir 5 onglets en bas :
- 🏠 Accueil
- ❓ Questions  
- ⚙️ Paramètres
- 📊 Statistiques
- 🔌 Connexion

✅ **Écran Questions** :
- Clique sur l'onglet "Questions"
- Clique sur le bouton ➕ (FAB en bas à droite)
- Ajoute une question de test :
  - Question : "Quelle est la capitale de la France ?"
  - Réponse correcte : "Paris"
  - Catégorie : "Géographie"
  - Difficulté : "Facile"
  - Temps limite (optionnel) : "30" secondes
- Clique "Ajouter"
- ✅ La question devrait apparaître dans la liste avec la réponse correcte affichée

✅ **Écran Connexion** :
- Clique sur l'onglet "Connexion"
- Tu devrais voir le formulaire de connexion
- Pour l'instant, ne te connecte pas (on va tester le serveur d'abord)

---

### Étape 2 : Tester le Serveur Raspberry Pi SEUL

#### 2.1 Sur Windows (ton PC)

1. **Ouvre un terminal PowerShell** dans le dossier `raspberry-pi`

2. **Installe les dépendances** (si pas déjà fait) :
```powershell
cd raspberry-pi
pip install -r requirements.txt
```

**Ou manuellement** :
```powershell
pip install websockets aiosqlite pyttsx3 pygame SpeechRecognition pyaudio
```

3. **Lance le serveur** :
```powershell
python server.py
```

**Résultat attendu** :
```
==================================================
Serveur CultureG - Raspberry Pi
==================================================
Mode: SIMULATION (GPIO non disponible)
Écoute sur ws://0.0.0.0:8765
Base de données: cultureg.db
Appuyez sur Ctrl+C pour arrêter
==================================================
```

✅ **Si tu vois ça, le serveur fonctionne !**

#### 2.2 Tester le serveur avec le script de test

**Dans un NOUVEAU terminal** (laisse le serveur tourner) :

```powershell
cd raspberry-pi
python test_server.py
```

**Résultat attendu** : Tous les tests passent ✅

---

### Étape 3 : Tester la CONNEXION App ↔ Raspberry Pi

#### 3.1 Trouver l'IP de ton PC

**Sur Windows (PowerShell)** :
```powershell
ipconfig
```

Cherche **"Adresse IPv4"** de ta carte WiFi ou Ethernet, par exemple :
- `192.168.1.50`
- `192.168.0.100`
- `10.0.0.5`

⚠️ **IMPORTANT** : Note cette IP, tu en auras besoin !

#### 3.2 Lancer le serveur (si pas déjà lancé)

```powershell
cd raspberry-pi
python server.py
```

Laisse-le tourner dans ce terminal.

#### 3.3 Se connecter depuis l'app Android

1. **Dans l'app Android**, va sur l'onglet **"Connexion"**

2. **Entre l'IP de ton PC** (ex: `192.168.1.50`)

3. **Le port est déjà `8765`** (par défaut)

4. **Clique "Se connecter"**

**Résultat attendu** :
- ✅ Statut passe à "Connexion en cours..."
- ✅ Puis "Connecté ✓" avec icône verte
- ✅ Affiche l'IP et le port connectés

**Côté serveur**, tu devrais voir :
```
INFO - Nouveau client connecté: ('192.168.1.XX', XXXXX)
```

#### 3.4 Tester le Ping/Pong

1. **Dans l'app**, clique sur **"Tester la connexion (Ping)"**

2. **Attends 2-3 secondes**

3. **Résultat attendu** :
   - ✅ Un message apparaît : "✅ PONG reçu ! Connexion fonctionnelle"

**Côté serveur**, tu devrais voir :
```
INFO - Message reçu: PING
```

---

### Étape 4 : Tester la Synchronisation des Questions

#### 4.1 Ajouter une question depuis l'app

1. **Dans l'app**, va sur l'onglet **"Questions"**

2. **Ajoute une nouvelle question** (bouton ➕)

3. **Remplis le formulaire** et clique "Ajouter"

**Côté serveur**, tu devrais voir :
```
INFO - Message reçu: ADD_QUESTION
INFO - Question ajoutée: [uuid-de-la-question]
```

#### 4.2 Vérifier dans la base de données

**Dans un nouveau terminal** :
```powershell
cd raspberry-pi
python -c "import sqlite3; conn = sqlite3.connect('cultureg.db'); cursor = conn.cursor(); cursor.execute('SELECT question FROM questions'); print('\n'.join([row[0] for row in cursor.fetchall()]))"
```

Tu devrais voir ta question dans la liste !

---

## 🐛 Problèmes Courants et Solutions

### ❌ Problème 1 : "Impossible de se connecter"

**Symptômes** :
- L'app reste sur "Connexion en cours..."
- Puis erreur "Connection refused" ou "Timeout"

**Solutions** :
1. ✅ Vérifie que le serveur est bien lancé (`python server.py`)
2. ✅ Vérifie l'IP (doit être celle de ton PC, pas `localhost`)
3. ✅ Vérifie que ton téléphone et ton PC sont sur le **même WiFi**
4. ✅ Désactive temporairement le **Firewall Windows** :
   - Paramètres Windows > Pare-feu Windows Defender
   - Désactiver temporairement pour tester

### ❌ Problème 2 : "PONG non reçu"

**Symptômes** :
- Connecté mais le ping ne répond pas

**Solutions** :
1. ✅ Vérifie les logs du serveur (doit afficher "Message reçu: PING")
2. ✅ Vérifie les logs de l'app dans Android Studio (Logcat)
3. ✅ Redémarre la connexion (déconnecte puis reconnecte)

### ❌ Problème 3 : "Questions non synchronisées"

**Symptômes** :
- Question ajoutée dans l'app mais pas dans la base de données

**Solutions** :
1. ✅ Vérifie que tu es bien connecté (statut "Connecté ✓")
2. ✅ Vérifie les logs du serveur (doit afficher "ADD_QUESTION")
3. ✅ Vérifie les logs de l'app (Logcat, filtre "QuestionsViewModel")

---

## 📊 Checklist de Test Complète

### ✅ Tests de Base
- [ ] App Android se lance sans erreur
- [ ] Navigation entre les écrans fonctionne
- [ ] Ajout de question fonctionne (localement)
- [ ] Serveur Raspberry Pi démarre sans erreur
- [ ] Script de test passe tous les tests

### ✅ Tests de Connexion
- [ ] App se connecte au serveur
- [ ] Statut affiche "Connecté ✓"
- [ ] Ping/Pong fonctionne
- [ ] Messages apparaissent dans les logs serveur

### ✅ Tests de Synchronisation
- [ ] Question ajoutée depuis l'app → Apparaît dans la base de données
- [ ] Question supprimée depuis l'app → Disparaît de la base de données
- [ ] Questions chargées depuis le serveur au démarrage

---

## 🎯 Test Ultra-Rapide (2 minutes)

Si tu veux juste vérifier que tout compile :

1. **Android Studio** : `Build > Rebuild Project` → ✅ Pas d'erreurs
2. **Serveur** : `python raspberry-pi/server.py` → ✅ Démarre sans erreur
3. **Test serveur** : `python raspberry-pi/test_server.py` → ✅ Tous les tests passent

**Si ces 3 étapes fonctionnent, tu es prêt ! 🎉**

---

## 📱 Tester sur un Vrai Téléphone Android

### Option 1 : USB Debugging (Recommandé)

1. **Active le Mode Développeur** sur ton téléphone :
   - Paramètres > À propos du téléphone
   - Tape 7 fois sur "Numéro de build"

2. **Active le Débogage USB** :
   - Paramètres > Options pour les développeurs
   - Active "Débogage USB"

3. **Connecte ton téléphone en USB** à ton PC

4. **Dans Android Studio** :
   - Clique sur le menu déroulant des devices
   - Sélectionne ton téléphone
   - Lance l'app (▶️)

### Option 2 : WiFi Debugging

1. **Connecte ton téléphone et ton PC au même WiFi**

2. **Dans Android Studio** :
   - Tools > Device Manager
   - Clique sur "..." à côté de ton téléphone
   - "Pair using WiFi"
   - Suis les instructions

---

## 🔍 Voir les Logs

### Logs Android (Android Studio)

1. **Ouvre Logcat** (en bas de l'écran)
2. **Filtre par tag** :
   - `WebSocketClient` : Messages WebSocket
   - `RaspberryPiApi` : Appels API
   - `QuestionsViewModel` : Gestion des questions
   - `ConnectionViewModel` : Connexion

### Logs Serveur (Terminal)

Les logs s'affichent directement dans le terminal où tu as lancé `server.py` :
- `INFO` : Messages informatifs
- `ERROR` : Erreurs (en rouge)

---

---

## 🎮 Tester le Jeu Complet (Nouvelles Fonctionnalités)

### Étape 5 : Tester le Jeu avec Matériel Raspberry Pi

#### 5.1 Prérequis Matériel

- ✅ Raspberry Pi avec GPIO configuré
- ✅ Casque branché (pour TTS et son de succès)
- ✅ Micro branché (USB ou jack)
- ✅ Bouton GPIO 25 (démarrage jeu)
- ✅ Bouton GPIO 16 (enregistrement vocal)
- ✅ Vibreur GPIO 23 (mauvaise réponse)
- ✅ LED GPIO 18 (bonne réponse)

#### 5.2 Préparer le Serveur

1. **Sur la Raspberry Pi**, lance le serveur :
```bash
cd raspberry-pi
python3 server.py
```

**Résultat attendu** :
```
Mode: RÉEL (GPIO disponible)
TTS initialisé avec succès
Reconnaissance vocale initialisée avec succès
Écoute sur ws://0.0.0.0:8765
```

#### 5.3 Ajouter des Questions depuis l'App

1. **Connecte-toi** depuis l'app Android (écran Connexion)
2. **Ajoute au moins 3 questions** avec des réponses simples :
   - "Quelle est la capitale de la France ?" → "Paris"
   - "Combien font 2 + 2 ?" → "4"
   - "Qui a peint la Joconde ?" → "Léonard de Vinci"

#### 5.4 Tester le Démarrage du Jeu

1. **Appuie sur le bouton GPIO 25** (sur la Raspberry Pi)
2. **Résultat attendu** :
   - ✅ Le serveur affiche "Démarrage du jeu..."
   - ✅ La première question est lue via TTS dans le casque
   - ✅ Le chronomètre démarre (si temps limite défini)

#### 5.5 Tester la Réponse Vocale

1. **Appuie sur le bouton GPIO 16** pour démarrer l'enregistrement
2. **Dis ta réponse** dans le micro (ex: "Paris")
3. **Appuie à nouveau sur GPIO 16** pour arrêter l'enregistrement
4. **Résultat attendu** :
   - ✅ Si bonne réponse :
     - Son de succès joué dans le casque
     - LED GPIO 18 s'allume
     - Score augmenté
   - ✅ Si mauvaise réponse :
     - Vibreur GPIO 23 activé
     - Score non augmenté

#### 5.6 Tester le Chronomètre

1. **Ajoute une question avec temps limite** (ex: 10 secondes)
2. **Démarre le jeu** (GPIO 25)
3. **N'attends pas** et laisse le temps s'écouler
4. **Résultat attendu** :
   - ✅ Après 10 secondes, timeout automatique
   - ✅ Réponse marquée comme incorrecte
   - ✅ Vibreur activé

#### 5.7 Tester les Statistiques en Fin de Partie

1. **Termine une partie** (réponds à toutes les questions)
2. **Résultat attendu** :
   - ✅ Message `GAME_ENDED` envoyé automatiquement à l'app
   - ✅ Statistiques affichées dans l'app :
     - Nombre de bonnes réponses
     - Nombre de mauvaises réponses
     - Score total
     - Questions posées

---

## 🧪 Tests en Mode Simulation (Sans Matériel)

Si tu n'as pas le matériel, le serveur fonctionne en **mode simulation** :

### Test TTS (Simulation)
- ✅ Les questions sont "lues" (log dans la console)
- ✅ Pas de son réel, mais le code s'exécute

### Test Reconnaissance Vocale (Simulation)
- ✅ Retourne une réponse simulée
- ✅ Permet de tester le flux du jeu

### Test GPIO (Simulation)
- ✅ Les actions GPIO sont loggées
- ✅ Pas d'action réelle sur les composants

**Pour tester en simulation** :
1. Lance `python3 server.py` (sans matériel GPIO)
2. Le serveur détecte automatiquement le mode simulation
3. Tous les tests fonctionnent mais sans effets réels

---

## 🚀 Prochaines Étapes Après les Tests

Une fois que tout fonctionne :

1. ✅ **Tester le jeu complet** : Démarrer une partie depuis l'app
2. ✅ **Tester les scores** : Afficher les scores depuis le Raspberry Pi
3. ✅ **Tester les paramètres** : Modifier les paramètres du jeu
4. ✅ **Tester avec le matériel** : Si tu as un Raspberry Pi avec GPIO

---

## 📋 Checklist de Test Complète (Nouveau Format)

### ✅ Tests Application Android
- [ ] Ajouter une question avec réponse unique
- [ ] Modifier une question existante
- [ ] Supprimer une question
- [ ] Définir un temps limite pour une question
- [ ] Se connecter à la Raspberry Pi
- [ ] Recevoir les statistiques en fin de partie

### ✅ Tests Raspberry Pi (Sans Matériel)
- [ ] Serveur démarre en mode simulation
- [ ] TTS fonctionne (simulation)
- [ ] Reconnaissance vocale fonctionne (simulation)
- [ ] Chronomètre fonctionne
- [ ] Statistiques envoyées en fin de partie

### ✅ Tests Raspberry Pi (Avec Matériel)
- [ ] GPIO 25 : Démarrage du jeu
- [ ] TTS : Questions lues dans le casque
- [ ] GPIO 16 : Enregistrement vocal (démarrage/arrêt)
- [ ] Micro : Reconnaissance de la réponse
- [ ] Son de succès : Joué pour bonne réponse
- [ ] LED GPIO 18 : S'allume pour bonne réponse
- [ ] Vibreur GPIO 23 : Active pour mauvaise réponse
- [ ] Chronomètre : Timeout fonctionne
- [ ] Statistiques : Envoyées automatiquement à l'app

---

**Bon test ! 🎉**

Si tu rencontres un problème, vérifie d'abord les logs (Android Studio Logcat + Terminal serveur) pour identifier l'erreur exacte.

