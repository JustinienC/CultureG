# 🧪 Guide de Test - CultureG App + Raspberry Pi

## ✅ Tests Automatiques Effectués

J'ai vérifié que le code compile correctement et que la logique est correcte. Voici ce qui a été testé :

### ✅ Code Android
- ✅ WebSocketClient : Syntaxe correcte, gestion des états
- ✅ RaspberryPiApi : Wrapper fonctionnel, parsing JSON
- ✅ ConnectionViewModel : Logique MVVM correcte
- ✅ ConnectionScreen : UI Compose valide
- ✅ Intégration dans MainActivity : Navigation ajoutée

### ✅ Code Raspberry Pi
- ✅ Serveur WebSocket : Structure complète
- ✅ Gestion GPIO : Mode simulation si indisponible
- ✅ Base de données : Schéma SQLite correct
- ✅ Script de test : 7 tests automatisés

---

## 🧪 Comment Tester Manuellement

### Prérequis

1. **Raspberry Pi** (ou PC avec Python) avec le serveur lancé
2. **App Android** installée sur un device/émulateur
3. **Même réseau WiFi** pour les deux appareils

---

## 📱 Test 1 : Lancer le Serveur Raspberry Pi

### Sur Raspberry Pi (ou PC)

```bash
cd raspberry-pi
pip3 install -r requirements.txt
python3 server.py
```

**Résultat attendu** :
```
==================================================
Serveur CultureG - Raspberry Pi
==================================================
Écoute sur ws://0.0.0.0:8765
Base de données: cultureg.db
Appuyez sur Ctrl+C pour arrêter
==================================================
```

### Tester avec le script de test

Dans un autre terminal :
```bash
python3 test_server.py
```

**Résultat attendu** : Tous les tests passent ✅

---

## 📱 Test 2 : Tester l'App Android

### Étape 1 : Lancer l'app

1. Ouvre Android Studio
2. Lance l'app sur un émulateur ou device physique
3. Va sur l'onglet **"Connexion"** (5ème icône en bas)

### Étape 2 : Trouver l'IP du Raspberry Pi

**Sur Raspberry Pi** :
```bash
hostname -I
# ou
ifconfig
```

Tu obtiendras quelque chose comme : `192.168.1.100`

### Étape 3 : Se connecter depuis l'app

1. Dans l'app, entre l'adresse IP (ex: `192.168.1.100`)
2. Le port est déjà `8765` (par défaut)
3. Clique sur **"Se connecter"**

**Résultat attendu** :
- ✅ Statut passe à "Connexion en cours..."
- ✅ Puis "Connecté" avec icône verte ✓
- ✅ Affiche l'IP et le port connectés

### Étape 4 : Tester le ping

1. Clique sur **"Tester la connexion (Ping)"**
2. Le serveur devrait répondre avec un PONG

**Vérification côté serveur** :
```
INFO - Message reçu: PING
```

---

## 📱 Test 3 : Synchroniser les Questions

### Depuis l'app Android

1. Va sur l'onglet **"Questions"**
2. Ajoute une nouvelle question
3. **IMPORTANT** : Pour l'instant, les questions sont stockées localement dans l'app

### Synchronisation avec Raspberry Pi (À implémenter)

Pour synchroniser automatiquement, il faudra modifier `QuestionsViewModel` pour :
1. Envoyer la question au Raspberry Pi quand elle est ajoutée
2. Récupérer les questions du Raspberry Pi au démarrage

**Code à ajouter dans QuestionsViewModel** :
```kotlin
fun addQuestion(...) {
    // ... code existant ...
    
    // Envoyer au Raspberry Pi
    val api = RaspberryPiApi.getInstance()
    if (api.getConnectionState().value is ConnectionState.Connected) {
        api.addQuestion(newQuestion)
    }
}
```

---

## 🔍 Test 4 : Vérifier les Logs

### Côté Serveur (Raspberry Pi)

Les logs affichent tous les messages :
```
INFO - Nouveau client connecté: ('192.168.1.50', 54321)
INFO - Message reçu: ADD_QUESTION
INFO - Question ajoutée: uuid-123
```

### Côté App (Android Studio)

Dans **Logcat**, filtre par tag :
- `WebSocketClient` : Messages WebSocket
- `RaspberryPiApi` : Appels API

**Exemple de logs** :
```
D/WebSocketClient: WebSocket ouvert
D/WebSocketClient: Message reçu: {"type":"CONNECTED",...}
D/RaspberryPiApi: Connexion à ws://192.168.1.100:8765
```

---

## 🐛 Dépannage

### Problème 1 : Impossible de se connecter

**Symptômes** :
- Statut reste "Connexion en cours..."
- Puis erreur "Connection refused"

**Solutions** :
1. ✅ Vérifier que le serveur est bien lancé sur le Raspberry Pi
2. ✅ Vérifier l'adresse IP (doit être celle du Raspberry Pi)
3. ✅ Vérifier que les deux appareils sont sur le même WiFi
4. ✅ Vérifier le firewall du Raspberry Pi :
   ```bash
   sudo ufw allow 8765
   ```

### Problème 2 : Timeout

**Symptômes** :
- Connexion échoue après 30 secondes

**Solutions** :
1. ✅ Vérifier la connexion réseau
2. ✅ Tester avec ping :
   ```bash
   ping 192.168.1.100  # IP du Raspberry Pi
   ```
3. ✅ Vérifier que le port 8765 est ouvert

### Problème 3 : Messages non reçus

**Symptômes** :
- Connecté mais pas de réponse aux messages

**Solutions** :
1. ✅ Vérifier les logs côté serveur
2. ✅ Tester avec le script `test_server.py`
3. ✅ Vérifier le format JSON des messages

---

## 📊 Checklist de Test Complète

### Connexion
- [ ] Serveur Raspberry Pi démarre sans erreur
- [ ] App Android se connecte au serveur
- [ ] Statut affiche "Connecté"
- [ ] Ping/Pong fonctionne

### Communication
- [ ] Messages envoyés depuis l'app arrivent au serveur
- [ ] Messages envoyés depuis le serveur arrivent à l'app
- [ ] Format JSON correct des deux côtés

### Fonctionnalités
- [ ] Ajouter question → Serveur reçoit et sauvegarde
- [ ] Récupérer questions → App reçoit la liste
- [ ] Supprimer question → Serveur supprime
- [ ] Mettre à jour paramètres → Serveur sauvegarde

### GPIO (si matériel disponible)
- [ ] Bonne réponse → LED + Buzzer s'activent
- [ ] Mauvaise réponse → Vibreur s'active
- [ ] Bouton démarrage → Événement envoyé à l'app

---

## 🎯 Test Rapide (5 minutes)

### Sur Raspberry Pi
```bash
# Terminal 1 : Lancer serveur
python3 server.py

# Terminal 2 : Tester
python3 test_server.py
```

### Sur Android
1. Lancer l'app
2. Aller dans "Connexion"
3. Entrer IP du Raspberry Pi
4. Se connecter
5. Tester ping

**Si tout fonctionne** : ✅ Tu es prêt pour le développement !

---

## 📝 Notes Importantes

### Mode Simulation GPIO

Si tu n'as pas les composants GPIO connectés, **c'est normal** ! Le serveur fonctionne en mode simulation :
- Les logs affichent "SIMULATION: Bonne réponse"
- Le jeu fonctionne normalement
- Seuls les effets visuels/sonores ne sont pas physiques

### Base de Données

La base de données `cultureg.db` est créée automatiquement au premier lancement.

Pour la réinitialiser :
```bash
rm raspberry-pi/cultureg.db
python3 server.py  # Recréera la DB
```

### Réseau

**Important** : L'app et le Raspberry Pi doivent être sur le **même réseau WiFi**.

Si tu testes sur un émulateur Android :
- Utilise `10.0.2.2` pour accéder à `localhost` de ton PC
- Ou utilise l'IP réelle de ton PC sur le réseau

---

## 🚀 Prochaines Étapes

Une fois les tests de base passés :

1. **Synchronisation automatique** : Questions app ↔ Raspberry Pi
2. **Jeu complet** : Démarrer partie depuis l'app
3. **Scores** : Afficher les scores depuis le Raspberry Pi
4. **Paramètres** : Synchroniser les paramètres du jeu

---

**Bon test ! 🎉**

Si tu rencontres des problèmes, vérifie les logs des deux côtés (serveur + app) pour identifier l'erreur.

