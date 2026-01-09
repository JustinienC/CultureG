# 🐛 Guide de Débogage - Connexion Android ↔ Raspberry Pi

## ✅ Corrections Apportées

J'ai amélioré le code pour :
1. ✅ Ajouter des logs détaillés
2. ✅ Améliorer la gestion des erreurs
3. ✅ Afficher les messages d'erreur dans l'UI
4. ✅ Nettoyer les anciennes connexions

---

## 🔍 Comment Déboguer

### Étape 1 : Vérifier les Logs Android

Dans **Android Studio**, ouvre **Logcat** et filtre par :

```
tag:WebSocketClient OR tag:RaspberryPiApi OR tag:ConnectionViewModel OR tag:ConnectionScreen
```

**Logs attendus quand tu cliques sur "Se connecter"** :
```
D/ConnectionScreen: Clic sur connecter: IP=192.168.1.100, Port=8765
D/ConnectionViewModel: connect() appelé: IP=192.168.1.100, Port=8765
D/RaspberryPiApi: Connexion à ws://192.168.1.100:8765
D/WebSocketClient: Tentative de connexion à: ws://192.168.1.100:8765
```

**Si connexion réussie** :
```
D/WebSocketClient: WebSocket ouvert
```

**Si erreur** :
```
E/WebSocketClient: Erreur WebSocket: [message d'erreur]
E/WebSocketClient: Type d'erreur: [type]
```

### Étape 2 : Vérifier le Serveur

Sur le **Raspberry Pi** (ou PC Windows), vérifie que le serveur est lancé :

```bash
python server.py
```

Tu devrais voir :
```
==================================================
Serveur CultureG - Raspberry Pi
==================================================
Écoute sur ws://0.0.0.0:8765
```

**Quand l'app se connecte**, tu verras :
```
INFO - Nouveau client connecté: ('192.168.1.50', 54321)
```

---

## 🐛 Problèmes Courants et Solutions

### Problème 1 : Le bouton ne fait rien

**Symptômes** :
- Aucun log dans Logcat
- Le bouton reste cliquable mais rien ne se passe

**Solutions** :
1. ✅ Vérifie que l'IP n'est pas vide
2. ✅ Vérifie les logs : `tag:ConnectionScreen`
3. ✅ Rebuild l'app : `Build > Rebuild Project`

### Problème 2 : "Impossible de se connecter"

**Symptômes** :
- Logs montrent : `Failed to connect`
- Message d'erreur dans l'app

**Solutions** :
1. ✅ **Vérifie que le serveur est lancé** sur le Raspberry Pi
2. ✅ **Vérifie l'IP** : Doit être l'IP du Raspberry Pi, pas `localhost`
3. ✅ **Vérifie le réseau** : App et Raspberry Pi sur le même WiFi
4. ✅ **Teste avec ping** :
   ```bash
   ping 192.168.1.100  # IP du Raspberry Pi
   ```

### Problème 3 : "Timeout de connexion"

**Symptômes** :
- Connexion démarre mais échoue après 30 secondes

**Solutions** :
1. ✅ Vérifie le firewall du Raspberry Pi :
   ```bash
   sudo ufw allow 8765
   ```
2. ✅ Vérifie que le port 8765 est bien utilisé :
   ```bash
   netstat -tuln | grep 8765
   ```

### Problème 4 : "Réseau inaccessible"

**Symptômes** :
- Erreur : `Network is unreachable`

**Solutions** :
1. ✅ Vérifie la connexion WiFi de l'appareil Android
2. ✅ Vérifie que l'app et le Raspberry Pi sont sur le même réseau
3. ✅ Si émulateur Android : Utilise `10.0.2.2` pour accéder à `localhost` du PC

### Problème 5 : Connexion réussie mais pas de messages

**Symptômes** :
- Statut "Connecté" mais pas de communication

**Solutions** :
1. ✅ Teste avec ping depuis l'app
2. ✅ Vérifie les logs côté serveur (doit recevoir les messages)
3. ✅ Vérifie le format JSON des messages

---

## 🧪 Test Rapide

### Test 1 : Vérifier que le bouton fonctionne

1. Ouvre l'app
2. Va dans "Connexion"
3. Entre une IP (ex: `192.168.1.100`)
4. Clique "Se connecter"
5. **Vérifie Logcat** : Tu devrais voir des logs

### Test 2 : Vérifier la connexion réseau

**Sur Android** (via ADB) :
```bash
adb shell ping -c 3 192.168.1.100
```

**Sur PC Windows** :
```powershell
ping 192.168.1.100
```

### Test 3 : Tester le serveur directement

```bash
python test_server.py
```

Tous les tests doivent passer.

---

## 📱 Sur Émulateur Android

Si tu testes sur un **émulateur Android** :

### Option 1 : Utiliser l'IP du PC Windows

1. Trouve l'IP de ton PC :
   ```powershell
   ipconfig
   ```
2. Utilise cette IP dans l'app (ex: `192.168.1.50`)

### Option 2 : Utiliser 10.0.2.2

Si le serveur tourne sur `localhost` de ton PC :
- Utilise `10.0.2.2` dans l'app (c'est l'alias de localhost pour l'émulateur)

---

## 🔧 Commandes Utiles

### Vérifier l'IP du Raspberry Pi
```bash
hostname -I
# ou
ifconfig
```

### Vérifier que le serveur écoute
```bash
netstat -tuln | grep 8765
# ou
ss -tuln | grep 8765
```

### Tester la connexion WebSocket manuellement
```python
python test_server.py
```

### Voir les logs en temps réel (Raspberry Pi)
```bash
python server.py
# Les logs s'affichent directement
```

---

## 📊 Checklist de Débogage

- [ ] Serveur Raspberry Pi lancé et visible dans les logs
- [ ] IP correcte dans l'app (pas `localhost` sur device physique)
- [ ] Même réseau WiFi pour app et Raspberry Pi
- [ ] Port 8765 ouvert (pas de firewall qui bloque)
- [ ] Logs Android montrent la tentative de connexion
- [ ] Logs serveur montrent la connexion entrante
- [ ] Pas d'erreur dans Logcat

---

## 💡 Astuce

**Active le mode Debug dans Logcat** :
1. Dans Android Studio, ouvre Logcat
2. Filtre : `package:com.cultureg`
3. Niveau : `Debug` ou `Verbose`
4. Tu verras tous les logs détaillés

---

**Si le problème persiste, partage les logs (Logcat + serveur) pour que je puisse t'aider ! 🚀**

