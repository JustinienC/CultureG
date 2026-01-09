# 🔧 Dépannage Réseau - CultureG

## 🐛 Problème Actuel

**Erreur** : `EHOSTUNREACH (No route to host)`

**Cause** : Les appareils sont sur des sous-réseaux différents :
- PC : `172.22.221.103` (sous-réseau 221)
- Téléphone : `172.22.220.114` (sous-réseau 220)

C'est un réseau universitaire qui isole les appareils.

---

## ✅ Solutions

### Solution 1 : Hotspot WiFi du PC (Recommandé)

Créer un hotspot WiFi depuis ton PC Windows :

1. **Ouvre les Paramètres Windows**
2. **Réseau et Internet > Point d'accès mobile**
3. **Active "Point d'accès mobile"**
4. **Note le nom du réseau et le mot de passe**
5. **Connecte ton téléphone à ce hotspot**
6. **Trouve la nouvelle IP du PC** :
   ```powershell
   ipconfig
   ```
   Cherche "Carte réseau sans fil Wi-Fi" ou "Carte réseau Connexion au réseau local*"
7. **Utilise cette nouvelle IP dans l'app**

### Solution 2 : Hotspot WiFi du Téléphone

1. **Active le hotspot WiFi sur ton téléphone**
2. **Connecte ton PC à ce hotspot**
3. **Trouve l'IP du PC** :
   ```powershell
   ipconfig
   ```
4. **Utilise cette IP dans l'app**

### Solution 3 : Câble USB + ADB (Pour émulateur)

Si tu utilises un émulateur Android :

1. **Connecte le téléphone en USB**
2. **Active le débogage USB**
3. **Dans l'app, utilise** : `10.0.2.2` (alias localhost pour émulateur)

### Solution 4 : Vérifier le Firewall

Même si les appareils sont sur le même réseau, le firewall peut bloquer :

```powershell
# Autoriser le port 8765
New-NetFirewallRule -DisplayName "CultureG Server" -Direction Inbound -LocalPort 8765 -Protocol TCP -Action Allow
```

---

## 🔍 Vérifications

### 1. Le serveur est-il lancé ?

Dans PowerShell :
```powershell
cd "C:\Users\sami\Desktop\Etudes\N7\3A\Culture G\raspberry-pi"
python server.py
```

Tu devrais voir :
```
Écoute sur ws://0.0.0.0:8765
```

### 2. Le port est-il ouvert ?

```powershell
Test-NetConnection -ComputerName localhost -Port 8765
```

### 3. Les appareils sont-ils sur le même réseau ?

**PC** :
```powershell
ipconfig | findstr "IPv4"
```

**Téléphone** : 
- Paramètres > À propos du téléphone > Statut > Adresse IP

Ils doivent être sur le même sous-réseau (ex: 192.168.1.x)

---

## 🎯 Solution Rapide (Hotspot PC)

### Étape 1 : Créer le hotspot

1. Windows + I (Paramètres)
2. Réseau et Internet > Point d'accès mobile
3. Active "Point d'accès mobile"
4. Note le nom et mot de passe

### Étape 2 : Connecter le téléphone

1. Sur le téléphone, va dans WiFi
2. Connecte-toi au hotspot créé
3. Entre le mot de passe

### Étape 3 : Trouver la nouvelle IP

Sur le PC :
```powershell
ipconfig
```

Cherche "Carte réseau Connexion au réseau local*" ou "Carte réseau sans fil Wi-Fi" :
```
Adresse IPv4. . . . . . . . . . . . . . . . . . . . . : 192.168.137.1
```

### Étape 4 : Se connecter depuis l'app

1. Ouvre l'app Android
2. Va dans "Connexion"
3. Entre l'IP (ex: `192.168.137.1`)
4. Port : `8765`
5. Clique "Se connecter"

---

## 📱 Alternative : Tester sur Émulateur

Si tu utilises un émulateur Android Studio :

1. **Lance le serveur sur ton PC** : `python server.py`
2. **Dans l'app (émulateur), utilise** : `10.0.2.2:8765`
   - `10.0.2.2` est l'alias de `localhost` pour l'émulateur

---

## 🔥 Firewall Windows

Si le hotspot ne fonctionne pas, vérifie le firewall :

```powershell
# Vérifier les règles
Get-NetFirewallRule | Where-Object {$_.DisplayName -like "*CultureG*"}

# Créer une règle si nécessaire
New-NetFirewallRule -DisplayName "CultureG Server" -Direction Inbound -LocalPort 8765 -Protocol TCP -Action Allow
```

---

## ✅ Checklist

- [ ] Serveur lancé (`python server.py`)
- [ ] Hotspot WiFi créé (PC ou téléphone)
- [ ] Téléphone connecté au hotspot
- [ ] IP trouvée avec `ipconfig`
- [ ] Firewall autorise le port 8765
- [ ] IP correcte entrée dans l'app
- [ ] Port 8765 dans l'app

---

**La solution la plus simple : Créer un hotspot WiFi depuis ton PC ! 🚀**



