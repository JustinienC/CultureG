# 🪟 Installation sur Windows

## ⚠️ Note Importante

Le serveur CultureG peut fonctionner sur **Windows** pour tester, mais sans les fonctionnalités GPIO réelles (LED, vibreur, buzzer).

## ✅ Installation Simple

### 1. Installer Python 3.7+

Télécharger depuis : https://www.python.org/downloads/

### 2. Installer les dépendances

```powershell
cd raspberry-pi
pip install websockets
```

**Note** : `RPi.GPIO` n'est **PAS nécessaire** sur Windows. Le serveur fonctionnera en mode simulation.

### 3. Lancer le serveur

```powershell
python server.py
```

Tu verras :
```
⚠️  RPi.GPIO non disponible - Mode simulation activé
==================================================
Serveur CultureG - Raspberry Pi
==================================================
Écoute sur ws://0.0.0.0:8765
```

## 🎯 Mode Simulation

Sur Windows, le serveur fonctionne en **mode simulation** :
- ✅ WebSocket fonctionne normalement
- ✅ Base de données SQLite fonctionne
- ✅ Tous les messages sont gérés
- ⚠️ GPIO simulé (pas de LED/vibreur/buzzer réel)

Les logs afficheront :
```
SIMULATION: Bonne réponse (LED + Son)
SIMULATION: Mauvaise réponse (Vibreur)
```

## 🧪 Tester sur Windows

### 1. Lancer le serveur

```powershell
python server.py
```

### 2. Tester avec le script de test

Dans un autre terminal :
```powershell
python test_server.py
```

Tous les tests devraient passer ✅

### 3. Connecter l'app Android

- Utilise l'IP de ton PC Windows (ex: `192.168.1.50`)
- Port : `8765`
- L'app devrait se connecter normalement

## 🔍 Trouver l'IP de ton PC Windows

```powershell
ipconfig
```

Cherche "IPv4 Address" sous "Wi-Fi" ou "Ethernet".

Exemple : `192.168.1.50`

## ✅ Avantages de Tester sur Windows

1. **Pas besoin de Raspberry Pi** pour développer
2. **Débogage plus facile** avec ton IDE habituel
3. **Test rapide** de la communication WebSocket
4. **Même code** fonctionne sur Raspberry Pi ensuite

## 🚀 Déployer sur Raspberry Pi Plus Tard

Quand tu auras un Raspberry Pi :

1. Transférer les fichiers sur le Raspberry Pi
2. Installer les dépendances :
   ```bash
   pip3 install -r requirements.txt
   ```
3. Le GPIO fonctionnera automatiquement !

---

**Le serveur fonctionne parfaitement sur Windows pour tester ! 🎉**

