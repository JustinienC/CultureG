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
  - Réponses : "Paris", "Lyon", "Marseille", "Toulouse"
  - Bonne réponse : "Paris" (index 0)
  - Catégorie : "Géographie"
  - Difficulté : "Facile"
- Clique "Ajouter"
- ✅ La question devrait apparaître dans la liste

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
pip install websockets aiosqlite
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

## 🚀 Prochaines Étapes Après les Tests

Une fois que tout fonctionne :

1. ✅ **Tester le jeu complet** : Démarrer une partie depuis l'app
2. ✅ **Tester les scores** : Afficher les scores depuis le Raspberry Pi
3. ✅ **Tester les paramètres** : Modifier les paramètres du jeu
4. ✅ **Tester avec le matériel** : Si tu as un Raspberry Pi avec GPIO

---

**Bon test ! 🎉**

Si tu rencontres un problème, vérifie d'abord les logs (Android Studio Logcat + Terminal serveur) pour identifier l'erreur exacte.

