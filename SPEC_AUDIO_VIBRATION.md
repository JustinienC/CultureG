# 🔊📳 SPEC - Système Audio et Vibration

**Date** : Janvier 2026  
**Version** : 2.1  
**Statut** : 📝 En spécification

---

## 🎯 Objectif

Décrire l'implémentation du système audio (TTS + sons + reconnaissance vocale) et de vibration pour le feedback utilisateur.

---

## 🔊 Système Audio

### 1. Text-To-Speech (TTS)

#### Objectif
Lire automatiquement les questions à travers un casque branché sur la Raspberry Pi.

#### Implémentation

**Option 1 : pyttsx3 (Recommandé)**
```python
import pyttsx3

engine = pyttsx3.init()
engine.setProperty('rate', 150)  # Vitesse de lecture
engine.setProperty('volume', 0.8)  # Volume (0.0 à 1.0)
engine.say("Quelle est la capitale de la France ?")
engine.runAndWait()
```

**Avantages** :
- ✅ Fonctionne hors ligne
- ✅ Pas de dépendance internet
- ✅ Léger et rapide
- ✅ Support français natif

**Option 2 : gTTS (Google Text-To-Speech)**
```python
from gtts import gTTS
import pygame

tts = gTTS(text="Quelle est la capitale de la France ?", lang='fr')
tts.save("question.mp3")
pygame.mixer.init()
pygame.mixer.music.load("question.mp3")
pygame.mixer.music.play()
```

**Avantages** :
- ✅ Qualité vocale supérieure
- ✅ Plus naturel

**Inconvénients** :
- ❌ Nécessite une connexion internet
- ❌ Plus lent (génération + téléchargement)

**Recommandation** : Utiliser `pyttsx3` pour la simplicité et l'autonomie.

#### Configuration

**Paramètres configurables** :
- Vitesse de lecture (mots/minute)
- Volume (0.0 à 1.0)
- Voix (masculine/féminine, si disponible)
- Langue (français par défaut)

**Fichier de configuration** :
```json
{
  "tts": {
    "engine": "pyttsx3",
    "rate": 150,
    "volume": 0.8,
    "voice": "default"
  }
}
```

#### Intégration dans le Serveur

**Fonction TTS** :
```python
def speak_question(question_text: str):
    """Lit une question via TTS"""
    try:
        engine = pyttsx3.init()
        engine.setProperty('rate', settings.get('tts_rate', 150))
        engine.setProperty('volume', settings.get('tts_volume', 0.8))
        engine.say(question_text)
        engine.runAndWait()
    except Exception as e:
        logger.error(f"Erreur TTS: {e}")
        # Fallback : affichage texte uniquement
```

**Appel dans le flux de jeu** :
```python
async def send_next_question(websocket):
    question = get_next_question()
    
    # Envoyer la question à l'app
    await send_message(websocket, {
        "type": "CURRENT_QUESTION",
        "data": {"question": question}
    })
    
    # Lire la question via TTS
    speak_question(question['question'])
```

---

### 2. Son de Succès

#### Objectif
Jouer un son de confirmation quand la réponse est correcte.

#### Fichiers Audio

**Format recommandé** : WAV ou MP3

**Exemples de sons** :
- `success.wav` : Son court et agréable (~1 seconde)
- `ding.wav` : Son de cloche
- `correct.mp3` : Son personnalisé

**Emplacement** : `raspberry-pi/audio/success.wav`

#### Implémentation

**Option 1 : pygame (Recommandé)**
```python
import pygame

pygame.mixer.init()
sound = pygame.mixer.Sound("audio/success.wav")
sound.set_volume(0.7)
sound.play()
```

**Option 2 : playsound**
```python
from playsound import playsound

playsound("audio/success.wav")
```

**Option 3 : aplay (Linux natif)**
```python
import subprocess

subprocess.run(["aplay", "audio/success.wav"])
```

**Recommandation** : Utiliser `pygame` pour la flexibilité et le contrôle du volume.

#### Intégration

**Fonction de son de succès** :
```python
def play_success_sound():
    """Joue le son de succès"""
    try:
        pygame.mixer.init()
        sound = pygame.mixer.Sound("audio/success.wav")
        sound.set_volume(settings.get('audio_volume', 0.7))
        sound.play()
    except Exception as e:
        logger.error(f"Erreur lecture son: {e}")
```

**Appel dans la validation** :
```python
if is_answer_correct(user_answer, correct_answer):
    play_success_sound()
    activate_led(LED_PIN)
    increment_score()
else:
    activate_vibrator()
```

---

## 📳 Système de Vibration

### 1. Matériel

#### Composant
- **Vibreur** : Module vibreur 5V (ex: Vibration Motor Module)
- **Connexion** : GPIO de la Raspberry Pi
- **Pin recommandé** : GPIO 23 (déjà configuré dans `server.py`)

#### Schéma de Connexion
```
Raspberry Pi GPIO 23 ────[Résistance 220Ω]───[Vibreur]─── GND
```

### 2. Implémentation (Phase Ultérieure)

#### Contrôle GPIO

**Fonction d'activation** :
```python
def activate_vibrator(duration: float = 0.5, intensity: float = 0.5):
    """Active le vibreur
    
    Args:
        duration: Durée en secondes (défaut: 0.5s)
        intensity: Intensité 0.0-1.0 (défaut: 0.5)
    """
    if not GPIO_AVAILABLE:
        logger.warning("GPIO non disponible - Simulation vibration")
        return
    
    try:
        # PWM pour contrôler l'intensité
        pwm = GPIO.PWM(VIBRATOR_PIN, 100)  # Fréquence 100Hz
        pwm.start(int(intensity * 100))  # Duty cycle selon intensité
        
        time.sleep(duration)
        
        pwm.stop()
        GPIO.output(VIBRATOR_PIN, GPIO.LOW)
    except Exception as e:
        logger.error(f"Erreur activation vibreur: {e}")
```

#### Patterns de Vibration

**Réponse incorrecte** :
- 2 vibrations courtes (0.2s ON, 0.1s OFF, 0.2s ON)

**Timeout** :
- 1 vibration longue (0.5s)

**Erreur système** :
- 3 vibrations courtes (0.1s ON, 0.1s OFF, répété 3 fois)

#### Configuration

**Paramètres configurables** :
- Intensité (0.0 à 1.0)
- Durée par défaut
- Patterns personnalisés

**Fichier de configuration** :
```json
{
  "vibration": {
    "enabled": true,
    "intensity": 0.5,
    "duration_wrong": 0.5,
    "duration_timeout": 0.5,
    "pattern_wrong": [0.2, 0.1, 0.2],
    "pattern_timeout": [0.5]
  }
}
```

---

## 🔧 Intégration Complète

### Flux Audio + Vibration

```python
async def handle_answer(websocket, answer_data):
    """Gère la réponse d'un utilisateur"""
    question = get_current_question()
    user_answer = answer_data['answer']
    
    # Comparaison
    is_correct = is_answer_correct(user_answer, question['correctAnswer'])
    
    if is_correct:
        # ✅ Réponse correcte
        play_success_sound()  # Son dans casque
        activate_led(LED_PIN, duration=1.0)  # LED verte
        increment_score()
    else:
        # ❌ Réponse incorrecte
        activate_vibrator(duration=0.5, intensity=0.5)  # Vibration
        # Pas de son (ou son d'erreur optionnel)
    
    # Envoyer résultat à l'app
    await send_message(websocket, {
        "type": "ANSWER_RESULT",
        "data": {
            "isCorrect": is_correct,
            "correctAnswer": question['correctAnswer']
        }
    })
```

---

## 📋 Dépendances Python

### Installation

```bash
# TTS
pip install pyttsx3

# Audio (son de succès)
pip install pygame

# Alternative TTS (si choix gTTS)
pip install gtts
```

### requirements.txt

```txt
# Audio et TTS
pyttsx3>=2.90
pygame>=2.5.0

# GPIO (déjà présent)
# RPi.GPIO>=0.7.0
```

---

## ✅ Checklist d'Implémentation

### Phase 1 : Audio (Priorité Haute)
- [ ] Installer `pyttsx3`
- [ ] Implémenter fonction `speak_question()`
- [ ] Tester lecture de questions
- [ ] Configurer vitesse et volume
- [ ] Intégrer dans le flux de jeu

### Phase 2 : Son de Succès (Priorité Haute)
- [ ] Créer/récupérer fichier audio `success.wav`
- [ ] Installer `pygame`
- [ ] Implémenter fonction `play_success_sound()`
- [ ] Tester lecture du son
- [ ] Intégrer dans la validation de réponse

### Phase 3 : Vibration (Priorité Moyenne - Phase Ultérieure)
- [ ] Connecter le vibreur au GPIO 23
- [ ] Implémenter fonction `activate_vibrator()`
- [ ] Tester différents patterns
- [ ] Configurer intensité et durée
- [ ] Intégrer dans la validation de réponse
- [ ] Gérer le timeout (vibration automatique)

---

## 🐛 Gestion des Erreurs

### Erreurs TTS
- **Si TTS échoue** : Logger l'erreur, continuer sans audio (affichage texte uniquement)
- **Si voix non disponible** : Utiliser voix par défaut du système

### Erreurs Audio
- **Si fichier son introuvable** : Logger warning, continuer sans son
- **Si pygame échoue** : Fallback sur `aplay` (Linux) ou désactiver

### Erreurs Vibration
- **Si GPIO non disponible** : Mode simulation (log uniquement)
- **Si vibreur non connecté** : Logger warning, continuer sans vibration

### Erreurs Reconnaissance Vocale
- **Si micro non détecté** : Logger erreur, demander connexion du micro
- **Si reconnaissance échoue** : Nouvelle tentative ou timeout
- **Si API échoue (Google)** : Fallback sur PocketSphinx (hors ligne) ou message d'erreur

---

## 🎤 Système de Reconnaissance Vocale

### 1. Matériel

#### Microphone
- **Type** : Micro USB ou micro branché sur l'entrée audio de la Raspberry Pi
- **Configuration** : Entrée audio par défaut du système

#### Bouton d'Enregistrement
- **GPIO** : GPIO 16 (ou autre pin disponible)
- **Type** : Bouton poussoir avec pull-up interne
- **Fonction** : Démarrage/arrêt de l'enregistrement

### 2. Implémentation

#### Bibliothèque Python

**Option 1 : speech_recognition (Recommandé)**
```python
import speech_recognition as sr

r = sr.Recognizer()
with sr.Microphone() as source:
    r.adjust_for_ambient_noise(source)  # Calibration
    audio = r.listen(source, timeout=5, phrase_time_limit=10)
    
try:
    text = r.recognize_google(audio, language='fr-FR')
    print(f"Réponse reconnue: {text}")
except sr.UnknownValueError:
    print("Impossible de comprendre l'audio")
except sr.RequestError as e:
    print(f"Erreur API: {e}")
```

**Avantages** :
- ✅ Simple à utiliser
- ✅ Support français
- ✅ Plusieurs moteurs (Google, Sphinx, etc.)

**Inconvénients** :
- ❌ Nécessite internet pour Google Speech API
- ❌ Alternative hors ligne : PocketSphinx (moins précis)

**Recommandation** : Utiliser `speech_recognition` avec Google Speech API pour la simplicité et la précision.

### 3. Flux d'Enregistrement

**Fonction d'enregistrement** :
```python
import speech_recognition as sr

def record_answer(timeout=30, phrase_time_limit=10):
    """Enregistre la réponse de l'utilisateur
    
    Args:
        timeout: Temps max d'attente avant démarrage (secondes)
        phrase_time_limit: Temps max d'enregistrement (secondes)
    
    Returns:
        str: Texte reconnu ou None si échec
    """
    r = sr.Recognizer()
    
    try:
        with sr.Microphone() as source:
            # Calibration du bruit ambiant
            r.adjust_for_ambient_noise(source, duration=0.5)
            
            # Enregistrement (démarre après premier appui bouton)
            audio = r.listen(source, timeout=timeout, phrase_time_limit=phrase_time_limit)
            
            # Reconnaissance vocale
            text = r.recognize_google(audio, language='fr-FR')
            return text
            
    except sr.WaitTimeoutError:
        logger.warning("Timeout: Aucun son détecté")
        return None
    except sr.UnknownValueError:
        logger.warning("Impossible de comprendre l'audio")
        return None
    except sr.RequestError as e:
        logger.error(f"Erreur API reconnaissance vocale: {e}")
        return None
```

### 4. Gestion du Bouton d'Enregistrement

**Configuration GPIO** :
```python
RECORD_BUTTON_PIN = 16  # GPIO 16 pour bouton d'enregistrement

def setup_record_button():
    """Configure le bouton d'enregistrement"""
    GPIO.setup(RECORD_BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)
    GPIO.add_event_detect(RECORD_BUTTON_PIN, GPIO.FALLING, 
                         callback=record_button_callback, 
                         bouncetime=300)

def record_button_callback(channel):
    """Callback appelé quand le bouton est pressé"""
    global is_recording, recording_started
    
    if not recording_started:
        # Démarrage de l'enregistrement
        recording_started = True
        is_recording = True
        # LED clignote (enregistrement en cours)
        start_recording_led()
    else:
        # Arrêt de l'enregistrement
        is_recording = False
        recording_started = False
        # LED s'arrête
        stop_recording_led()
        # Traitement de l'audio
        asyncio.create_task(process_recorded_audio())
```

### 5. Dépendances Python

**Installation** :
```bash
# Reconnaissance vocale
pip install SpeechRecognition

# Pour utiliser Google Speech API (nécessite internet)
pip install pyaudio
```

### 6. Configuration

**Paramètres configurables** :
- Langue de reconnaissance (fr-FR par défaut)
- Timeout d'attente (secondes)
- Durée max d'enregistrement (secondes)
- Sensibilité du micro
- Moteur de reconnaissance (Google, Sphinx)

**Fichier de configuration** :
```json
{
  "speech_recognition": {
    "engine": "google",
    "language": "fr-FR",
    "timeout": 30,
    "phrase_time_limit": 10,
    "ambient_noise_duration": 0.5
  }
}
```

---

**Prochaine étape** : Voir `SPEC_IMPLEMENTATION_PLAN.md` pour le plan d'implémentation.

