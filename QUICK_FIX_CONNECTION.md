# 🔧 Fix Rapide - Connexion Timeout

## 🐛 Problème Actuel

**Erreur** : `SocketTimeoutException` - Le téléphone essaie de se connecter mais le serveur ne répond pas.

**IP utilisée** : `172.22.221.103` (ancienne IP du réseau universitaire)
**IP téléphone** : `172.20.10.2` (probablement sur hotspot)

---

## ✅ Solution Étape par Étape

### Étape 1 : Vérifier que le serveur est lancé

Dans PowerShell :
```powershell
cd "C:\Users\sami\Desktop\Etudes\N7\3A\Culture G\raspberry-pi"
python server.py
```

**Tu DOIS voir** :
```
==================================================
Serveur CultureG - Raspberry Pi
==================================================
Écoute sur ws://0.0.0.0:8765
Base de données: cultureg.db
Appuyez sur Ctrl+C pour arrêter
==================================================
```

**Si tu ne vois pas ça, le serveur n'est pas lancé !**

### Étape 2 : Trouver la VRAIE IP du PC

Maintenant que tu es sur un hotspot, l'IP a changé !

Dans PowerShell :
```powershell
ipconfig
```

**Cherche** :
- Si hotspot PC → "Carte réseau Connexion au réseau local*" → IP généralement `192.168.137.1`
- Si hotspot téléphone → "Carte réseau sans fil Wi-Fi" → IP généralement `192.168.43.x` ou `192.168.xxx.x`

**Exemple** :
```
Carte réseau Connexion au réseau local* X :
   Adresse IPv4. . . . . . . . . . . . . . . . . . . . . : 192.168.137.1
```

### Étape 3 : Autoriser le Firewall

Le firewall Windows bloque probablement le port. Autorise-le :

```powershell
New-NetFirewallRule -DisplayName "CultureG Server" -Direction Inbound -LocalPort 8765 -Protocol TCP -Action Allow
```

### Étape 4 : Utiliser la BONNE IP dans l'app

1. **Ouvre l'app Android**
2. **Va dans "Connexion"**
3. **EFFACE l'ancienne IP** (`172.22.221.103`)
4. **Entre la NOUVELLE IP** (ex: `192.168.137.1` si hotspot PC)
5. **Port** : `8765`
6. **Clique "Se connecter"**

---

## 🔍 Vérifications Rapides

### Vérifier que le serveur écoute

Dans un nouveau PowerShell (pendant que le serveur tourne) :
```powershell
netstat -an | findstr "8765"
```

Tu devrais voir :
```
TCP    0.0.0.0:8765           0.0.0.0:0              LISTENING
```

### Tester la connexion depuis le PC

```powershell
Test-NetConnection -ComputerName localhost -Port 8765
```

Résultat attendu :
```
TcpTestSucceeded : True
```

---

## 🎯 Checklist Complète

- [ ] **Serveur lancé** (`python server.py` dans le bon dossier)
- [ ] **Serveur affiche "Écoute sur ws://0.0.0.0:8765"**
- [ ] **Hotspot actif** (PC ou téléphone)
- [ ] **Téléphone connecté au hotspot**
- [ ] **Nouvelle IP trouvée** avec `ipconfig`
- [ ] **Firewall autorisé** (commande New-NetFirewallRule)
- [ ] **Bonne IP dans l'app** (pas l'ancienne `172.22.221.103`)
- [ ] **Port 8765 dans l'app**

---

## 🚨 Erreurs Courantes

### "Le serveur n'est pas lancé"
→ Lance `python server.py` dans le dossier `raspberry-pi`

### "J'utilise toujours l'ancienne IP"
→ Efface et entre la nouvelle IP trouvée avec `ipconfig`

### "Firewall bloque"
→ Exécute la commande New-NetFirewallRule ci-dessus

### "Timeout toujours"
→ Vérifie que le serveur tourne ET que tu utilises la bonne IP

---

## 💡 Astuce

**Test rapide** : Avant de tester dans l'app, teste avec le script Python :

1. **Lance le serveur** : `python server.py`
2. **Dans un autre terminal** : `python test_server.py`
3. **Si les tests passent**, le serveur fonctionne
4. **Alors teste dans l'app** avec la bonne IP

---

**La clé : Utilise la NOUVELLE IP du hotspot, pas l'ancienne IP du réseau universitaire ! 🎯**



