#!/usr/bin/env python3
"""
Serveur WebSocket pour le jeu CultureG sur Raspberry Pi
Gère la communication avec l'application mobile Android
"""

import asyncio
import websockets
import json
import sqlite3
import logging
from datetime import datetime
from typing import Set, Dict, Optional
try:
    import RPi.GPIO as GPIO
    GPIO_AVAILABLE = True
except ImportError:
    GPIO_AVAILABLE = False
    # Mode simulation si GPIO non disponible (ex: Windows, PC sans Raspberry Pi)
    print("⚠️  RPi.GPIO non disponible - Mode simulation activé")
import time

# Configuration du logging (doit être avant les imports qui utilisent logger)
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# TTS (Text-To-Speech)
try:
    import pyttsx3
    TTS_AVAILABLE = True
except ImportError:
    TTS_AVAILABLE = False
    logger.warning("pyttsx3 non disponible - TTS désactivé")

# Audio (pour son de succès)
try:
    import pygame
    AUDIO_AVAILABLE = True
except ImportError:
    AUDIO_AVAILABLE = False
    logger.warning("pygame non disponible - Audio désactivé")

# VOSK (Reconnaissance vocale offline)
try:
    import sounddevice as sd
    import numpy as np
    from scipy.signal import resample
    from vosk import Model, KaldiRecognizer
    import queue
    VOSK_AVAILABLE = True
except ImportError:
    VOSK_AVAILABLE = False
    logger.warning("VOSK non disponible - Reconnaissance vocale désactivée")

# HTTP Client pour Flask
try:
    import aiohttp
    AIOHTTP_AVAILABLE = True
except ImportError:
    AIOHTTP_AVAILABLE = False
    logger.warning("aiohttp non disponible - Communication avec Flask désactivée")

# Configuration GPIO
LED_PIN = 18          # GPIO 18 pour LED (bonne réponse)
VIBRATOR_PIN = 23     # GPIO 23 pour vibreur (mauvaise réponse)
BUZZER_PIN = 24       # GPIO 24 pour buzzer/haut-parleur
RECORD_BUTTON_PIN = 16  # GPIO 16 pour bouton d'enregistrement vocal (démarrage/arrêt)

# Configuration WebSocket
WS_HOST = "0.0.0.0"   # Écouter sur toutes les interfaces
WS_PORT = 8765        # Port WebSocket

# Configuration Flask Server
FLASK_SERVER_URL = "http://localhost:5000"

# Base de données
DB_FILE = "cultureg.db"

# État global
connected_clients: Set[websockets.WebSocketServerProtocol] = set()
current_game: Optional[Dict] = None
game_stats = {
    "total_questions": 0,
    "correct_answers": 0,
    "wrong_answers": 0,
    "current_score": 0
}
# Timer pour le chronomètre
question_timer_task: Optional[asyncio.Task] = None
question_start_time: Optional[float] = None


class TTSController:
    """Contrôleur TTS pour lire les questions"""
    
    def __init__(self):
        self.engine = None
        self.initialized = False
        
        if not TTS_AVAILABLE:
            logger.warning("TTS non disponible - Mode simulation activé")
            return
        
        try:
            self.engine = pyttsx3.init()
            # Configuration par défaut
            self.engine.setProperty('rate', 150)  # Vitesse de lecture (mots/minute)
            self.engine.setProperty('volume', 0.8)  # Volume (0.0 à 1.0)
            
            # Essayer de trouver une voix française
            voices = self.engine.getProperty('voices')
            french_voice = None
            for voice in voices:
                if 'french' in voice.name.lower() or 'fr' in voice.id.lower():
                    french_voice = voice
                    break
            
            if french_voice:
                self.engine.setProperty('voice', french_voice.id)
                logger.info(f"Voix française sélectionnée: {french_voice.name}")
            else:
                logger.info("Voix française non trouvée, utilisation de la voix par défaut")
            
            self.initialized = True
            logger.info("TTS initialisé avec succès")
        except Exception as e:
            logger.error(f"Erreur initialisation TTS: {e}")
            logger.warning("Mode simulation activé (pas de TTS réel)")
    
    def speak(self, text: str, rate: int = 150, volume: float = 0.8):
        """Lit un texte via TTS
        
        Args:
            text: Texte à lire
            rate: Vitesse de lecture (mots/minute, défaut: 150)
            volume: Volume (0.0 à 1.0, défaut: 0.8)
        """
        if not self.initialized or not self.engine:
            logger.warning(f"TTS non disponible - Simulation: '{text}'")
            return
        
        try:
            self.engine.setProperty('rate', rate)
            self.engine.setProperty('volume', volume)
            self.engine.say(text)
            self.engine.runAndWait()
            logger.info(f"Question lue via TTS: {text[:50]}...")
        except Exception as e:
            logger.error(f"Erreur lecture TTS: {e}")

# Instance globale TTS
tts_controller = TTSController()


class AudioController:
    """Contrôleur audio pour jouer des sons"""
    
    def __init__(self):
        self.initialized = False
        
        if not AUDIO_AVAILABLE:
            logger.warning("Audio (pygame) non disponible - Mode simulation activé")
            return
        
        try:
            pygame.mixer.init(frequency=22050, size=-16, channels=2, buffer=512)
            self.initialized = True
            logger.info("Audio initialisé avec succès")
        except Exception as e:
            logger.error(f"Erreur initialisation audio: {e}")
            logger.warning("Mode simulation activé (pas d'audio réel)")
    
    def play_success_sound(self, volume: float = 0.7):
        """Joue un son de succès
        
        Args:
            volume: Volume (0.0 à 1.0, défaut: 0.7)
        """
        if not self.initialized:
            logger.info("SIMULATION: Son de succès joué")
            return
        
        try:
            # Essayer de charger un fichier audio si disponible
            import os
            audio_file = "audio/success.wav"
            if os.path.exists(audio_file):
                sound = pygame.mixer.Sound(audio_file)
                sound.set_volume(volume)
                sound.play()
                logger.info(f"Son de succès joué depuis {audio_file}")
                return
            
            # Sinon, générer un son simple avec pygame
            # Créer un beep simple (800Hz, 0.2s)
            sample_rate = 22050
            duration = 0.2
            frames = int(sample_rate * duration)
            
            # Générer un ton sinusoïdal
            import math
            sound_array = []
            for i in range(frames):
                sample = int(32767 * volume * math.sin(2 * math.pi * 800 * i / sample_rate))
                sound_array.append([sample, sample])  # Stéréo
            
            # Convertir en array numpy si disponible, sinon utiliser une approche simple
            try:
                import numpy as np
                sound_data = np.array(sound_array, dtype=np.int16)
                sound = pygame.sndarray.make_sound(sound_data)
            except ImportError:
                # Fallback sans numpy : utiliser un beep système
                import os
                os.system('beep -f 800 -l 200 2>/dev/null || echo -e "\a"')
                logger.info("Son de succès joué (beep système)")
                return
            
            sound.play()
            logger.info("Son de succès joué (généré)")
        except Exception as e:
            logger.error(f"Erreur lecture son de succès: {e}")
            # Fallback : utiliser un simple beep système
            try:
                import os
                os.system('beep -f 800 -l 200 2>/dev/null || echo -e "\a"')
            except:
                pass

# Instance globale Audio
audio_controller = AudioController()


class VoskRecognitionController:
    """Contrôleur pour la reconnaissance vocale avec VOSK (offline)"""
    
    def __init__(self, model_path="vosk-model-small-fr-0.22"):
        self.model = None
        self.recognizer = None
        self.initialized = False
        self.audio_queue = queue.Queue()
        self.MIC_RATE = 16000
        self.VOSK_RATE = 16000
        
        if not VOSK_AVAILABLE:
            logger.warning("VOSK non disponible - Mode simulation activé")
            return
        
        try:
            # Charger le modèle VOSK
            self.model = Model(model_path)
            self.recognizer = KaldiRecognizer(self.model, self.VOSK_RATE)
            
            # Détecter le micro
            device_info = sd.query_devices(0, 'input')
            self.MIC_RATE = int(device_info['default_samplerate'])
            self.initialized = True
            logger.info(f"VOSK initialisé - Micro: {device_info['name']}, rate: {self.MIC_RATE}")
        except Exception as e:
            logger.error(f"Erreur initialisation VOSK: {e}")
            logger.warning("Mode simulation activé (pas de reconnaissance vocale réelle)")
    
    def audio_callback(self, indata, frames, time_info, status):
        """Callback pour capturer l'audio"""
        if status:
            logger.warning(f"Audio status: {status}")
        self.audio_queue.put(indata.copy())
    
    async def wait_for_answer(self, timeout: int = 30) -> Optional[str]:
        """Attend la réponse de l'utilisateur via reconnaissance vocale VOSK
        
        Args:
            timeout: Temps max d'attente (secondes)
        
        Returns:
            str: Texte reconnu ou None si échec/timeout
        """
        if not self.initialized:
            logger.warning("VOSK non disponible - Simulation")
            await asyncio.sleep(2)
            return "réponse simulée"
        
        try:
            # Attendre que GPIO 16 soit pressé
            logger.info("En attente du démarrage de l'enregistrement (GPIO 16)...")
            start_time = time.time()
            while not gpio.recording_started and (time.time() - start_time) < timeout:
                await asyncio.sleep(0.1)
            
            if not gpio.recording_started:
                logger.warning("Timeout: Pas d'enregistrement")
                return None
            
            # Démarrer l'enregistrement avec sounddevice
            logger.info("Enregistrement démarré (VOSK)...")
            self.audio_queue = queue.Queue()
            
            # Créer un nouveau recognizer pour chaque reconnaissance
            self.recognizer = KaldiRecognizer(self.model, self.VOSK_RATE)
            
            with sd.InputStream(
                device=0,
                channels=1,
                samplerate=self.MIC_RATE,
                dtype='float32',
                callback=self.audio_callback
            ):
                while gpio.is_recording and (time.time() - start_time) < timeout:
                    try:
                        audio_chunk = self.audio_queue.get(timeout=0.1)
                        
                        # Resample pour VOSK
                        audio_mono = audio_chunk[:, 0] if len(audio_chunk.shape) > 1 else audio_chunk
                        num_samples = int(len(audio_mono) * self.VOSK_RATE / self.MIC_RATE)
                        audio_resampled = resample(audio_mono, num_samples)
                        audio_bytes = (audio_resampled * 32767).astype(np.int16).tobytes()
                        
                        if self.recognizer.AcceptWaveform(audio_bytes):
                            result = json.loads(self.recognizer.Result())
                            text = result.get("text", "")
                            if text:
                                logger.info(f"VOSK: Réponse reconnue: {text}")
                                gpio.recording_started = False
                                gpio.is_recording = False
                                return text
                    except queue.Empty:
                        continue
                    except Exception as e:
                        logger.error(f"Erreur traitement audio VOSK: {e}")
            
            # Résultat final
            final_result = json.loads(self.recognizer.FinalResult())
            text = final_result.get("text", "")
            logger.info(f"VOSK: Résultat final: {text}")
            
            gpio.recording_started = False
            gpio.is_recording = False
            return text if text else None
                    
        except Exception as e:
            logger.error(f"Erreur lors de l'enregistrement VOSK: {e}")
            return None
        finally:
            # Réinitialiser les flags
            gpio.recording_started = False
            gpio.is_recording = False

# Instance globale VOSK Recognition
speech_controller = VoskRecognitionController()


class GPIOController:
    """Contrôleur GPIO pour LEDs, vibreur et buzzer"""
    
    def __init__(self):
        self.initialized = False
        self.recording_started = False
        self.is_recording = False
        
        if not GPIO_AVAILABLE:
            logger.warning("RPi.GPIO non disponible - Mode simulation activé")
            return
        
        try:
            GPIO.setmode(GPIO.BCM)
            GPIO.setwarnings(False)
            
            # Configuration des pins en sortie
            GPIO.setup(LED_PIN, GPIO.OUT)
            GPIO.setup(VIBRATOR_PIN, GPIO.OUT)
            GPIO.setup(BUZZER_PIN, GPIO.OUT)
            
            # Configuration du bouton d'enregistrement en entrée avec pull-up
            GPIO.setup(RECORD_BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)
            
            # Callback pour le bouton d'enregistrement
            GPIO.add_event_detect(RECORD_BUTTON_PIN, GPIO.FALLING, 
                                 callback=self._record_button_callback, 
                                 bouncetime=300)
            
            self.initialized = True
            logger.info("GPIO initialisé avec succès")
        except Exception as e:
            logger.error(f"Erreur initialisation GPIO: {e}")
            logger.warning("Mode simulation activé (pas de GPIO réel)")
    
    def _record_button_callback(self, channel):
        """Callback appelé quand le bouton d'enregistrement est pressé"""
        if not self.recording_started:
            # Démarrage de l'enregistrement
            self.recording_started = True
            self.is_recording = True
            logger.info("Bouton d'enregistrement pressé - Démarrage enregistrement")
            # LED clignote (enregistrement en cours)
            asyncio.create_task(self._start_recording_led())
        else:
            # Arrêt de l'enregistrement
            self.is_recording = False
            self.recording_started = False
            logger.info("Bouton d'enregistrement pressé - Arrêt enregistrement")
            # LED s'arrête
            asyncio.create_task(self._stop_recording_led())
            # Traitement de l'audio
            asyncio.create_task(self._process_recorded_audio())
    
    async def _start_recording_led(self):
        """Démarre la LED clignotante pour l'enregistrement"""
        # Implémenter le clignotement de la LED
        pass
    
    async def _stop_recording_led(self):
        """Arrête la LED clignotante"""
        pass
    
    async def _process_recorded_audio(self):
        """Traite l'audio enregistré"""
        # Cette fonction sera appelée par le VoskRecognitionController
        pass
    
    def good_answer(self):
        """Activer LED et son pour bonne réponse"""
        if not self.initialized:
            logger.info("SIMULATION: Bonne réponse (LED + Son)")
            # Jouer le son même en mode simulation
            audio_controller.play_success_sound()
            return
        
        try:
            # Allumer LED
            GPIO.output(LED_PIN, GPIO.HIGH)
            
            # Son de succès via pygame (dans le casque)
            audio_controller.play_success_sound(volume=0.7)
            
            # Éteindre LED après 1 seconde
            time.sleep(1.0)
            GPIO.output(LED_PIN, GPIO.LOW)
            
            logger.info("Bonne réponse: LED + Son activés")
        except Exception as e:
            logger.error(f"Erreur GPIO bonne réponse: {e}")
    
    def wrong_answer(self):
        """Activer vibreur pour mauvaise réponse"""
        if not self.initialized:
            logger.info("SIMULATION: Mauvaise réponse (Vibreur)")
            return
        
        try:
            # Activer vibreur
            GPIO.output(VIBRATOR_PIN, GPIO.HIGH)
            time.sleep(0.3)  # Vibrer pendant 0.3s
            GPIO.output(VIBRATOR_PIN, GPIO.LOW)
            
            logger.info("Mauvaise réponse: Vibreur activé")
        except Exception as e:
            logger.error(f"Erreur GPIO mauvaise réponse: {e}")
    
    def _play_tone(self, pin, frequency, duration):
        """Jouer un ton sur le buzzer"""
        # Simple implémentation avec PWM
        pwm = GPIO.PWM(pin, frequency)
        pwm.start(50)  # 50% duty cycle
        time.sleep(duration)
        pwm.stop()
    
    def cleanup(self):
        """Nettoyer les ressources GPIO"""
        if self.initialized and GPIO_AVAILABLE:
            GPIO.cleanup()
            logger.info("GPIO nettoyé")


class Database:
    """Gestionnaire de base de données SQLite"""
    
    def __init__(self, db_file: str):
        self.db_file = db_file
        self.init_database()
    
    def init_database(self):
        """Initialiser les tables de la base de données"""
        conn = sqlite3.connect(self.db_file)
        c = conn.cursor()
        
        # Table des questions (format Question-Réponse)
        c.execute('''
            CREATE TABLE IF NOT EXISTS questions (
                id TEXT PRIMARY KEY,
                question TEXT NOT NULL,
                correct_answer TEXT NOT NULL,
                category TEXT,
                difficulty TEXT,
                time_limit INTEGER,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # Migration : Si l'ancienne table existe avec l'ancien format, on la migre
        try:
            c.execute('SELECT answers, correct_answer_index FROM questions LIMIT 1')
            # Si on arrive ici, l'ancienne structure existe
            logger.info("Détection de l'ancienne structure - Migration nécessaire")
            # On ne migre pas automatiquement, il faut utiliser le script de migration
        except sqlite3.OperationalError:
            # Nouvelle structure, pas de migration nécessaire
            pass
        
        # Table des scores
        c.execute('''
            CREATE TABLE IF NOT EXISTS scores (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                player_name TEXT,
                score INTEGER NOT NULL,
                total_questions INTEGER NOT NULL,
                correct_answers INTEGER NOT NULL,
                game_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # Table des paramètres du jeu
        c.execute('''
            CREATE TABLE IF NOT EXISTS game_settings (
                key TEXT PRIMARY KEY,
                value TEXT NOT NULL,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
        # Paramètres par défaut
        c.execute('''
            INSERT OR IGNORE INTO game_settings (key, value) VALUES
            ('timer_seconds', '30'),
            ('sound_enabled', 'true'),
            ('vibration_enabled', 'true'),
            ('questions_per_game', '10')
        ''')
        
        conn.commit()
        conn.close()
        logger.info("Base de données initialisée")
    
    def add_question(self, question_data: Dict) -> bool:
        """Ajouter une question à la base de données (format Question-Réponse)"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                INSERT OR REPLACE INTO questions 
                (id, question, correct_answer, category, difficulty, time_limit)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (
                question_data['id'],
                question_data['question'],
                question_data['correctAnswer'],
                question_data.get('category', 'Général'),
                question_data.get('difficulty', 'MEDIUM'),
                question_data.get('timeLimit')
            ))
            
            conn.commit()
            conn.close()
            logger.info(f"Question ajoutée: {question_data['id']}")
            return True
        except Exception as e:
            logger.error(f"Erreur ajout question: {e}")
            return False
    
    def get_all_questions(self) -> list:
        """Récupérer toutes les questions (format Question-Réponse)"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            # Vérifier si l'ancienne structure existe
            try:
                c.execute('SELECT answers FROM questions LIMIT 1')
                # Ancienne structure détectée
                logger.warning("Ancienne structure détectée - Utilisez le script de migration")
                return []
            except sqlite3.OperationalError:
                # Nouvelle structure
                pass
            
            c.execute('SELECT id, question, correct_answer, category, difficulty, time_limit, created_at FROM questions ORDER BY created_at DESC')
            rows = c.fetchall()
            conn.close()
            
            questions = []
            for row in rows:
                questions.append({
                    'id': row[0],
                    'question': row[1],
                    'correctAnswer': row[2],
                    'category': row[3] or 'Général',
                    'difficulty': row[4] or 'MEDIUM',
                    'timeLimit': row[5],
                    'createdAt': row[6] if len(row) > 6 else None
                })
            
            return questions
        except Exception as e:
            logger.error(f"Erreur récupération questions: {e}")
            return []
    
    def delete_question(self, question_id: str) -> bool:
        """Supprimer une question"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('DELETE FROM questions WHERE id = ?', (question_id,))
            conn.commit()
            deleted = c.rowcount > 0
            conn.close()
            
            if deleted:
                logger.info(f"Question supprimée: {question_id}")
            return deleted
        except Exception as e:
            logger.error(f"Erreur suppression question: {e}")
            return False
    
    def save_score(self, player_name: str, score: int, total_questions: int, 
                   correct_answers: int, wrong_answers: int = 0) -> bool:
        """Sauvegarder un score dans la base de données"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                INSERT INTO scores 
                (player_name, score, total_questions, correct_answers, wrong_answers)
                VALUES (?, ?, ?, ?, ?)
            ''', (player_name, score, total_questions, correct_answers, wrong_answers))
            
            conn.commit()
            conn.close()
            logger.info(f"Score sauvegardé: {score}/{total_questions}")
            return True
        except Exception as e:
            logger.error(f"Erreur sauvegarde score: {e}")
            return False
    
    def get_recent_scores(self, limit: int = 10) -> list:
        """Récupérer les scores récents"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                SELECT * FROM scores 
                ORDER BY game_date DESC 
                LIMIT ?
            ''', (limit,))
            
            rows = c.fetchall()
            conn.close()
            
            scores = []
            for row in rows:
                scores.append({
                    'id': row[0],
                    'playerName': row[1] or 'Anonyme',
                    'score': row[2],
                    'totalQuestions': row[3],
                    'correctAnswers': row[4],
                    'gameDate': row[5]
                })
            
            return scores
        except Exception as e:
            logger.error(f"Erreur récupération scores: {e}")
            return []
    
    def update_setting(self, key: str, value: str) -> bool:
        """Mettre à jour un paramètre"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                INSERT OR REPLACE INTO game_settings (key, value, updated_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
            ''', (key, value))
            
            conn.commit()
            conn.close()
            logger.info(f"Paramètre mis à jour: {key} = {value}")
            return True
        except Exception as e:
            logger.error(f"Erreur mise à jour paramètre: {e}")
            return False
    
    def get_settings(self) -> Dict:
        """Récupérer tous les paramètres"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('SELECT key, value FROM game_settings')
            rows = c.fetchall()
            conn.close()
            
            settings = {}
            for row in rows:
                settings[row[0]] = row[1]
            
            return settings
        except Exception as e:
            logger.error(f"Erreur récupération paramètres: {e}")
            return {}


# Instances globales
gpio = GPIOController()
db = Database(DB_FILE)


# ============================================================================
# FONCTIONS HTTP POUR COMMUNIQUER AVEC FLASK
# ============================================================================

async def fetch_random_question_from_flask():
    """Récupère une question aléatoire depuis le serveur Flask"""
    if not AIOHTTP_AVAILABLE:
        logger.error("aiohttp non disponible - Impossible de communiquer avec Flask")
        return None
    
    try:
        async with aiohttp.ClientSession() as session:
            async with session.get(f"{FLASK_SERVER_URL}/questions/random") as resp:
                if resp.status == 200:
                    data = await resp.json()
                    logger.info(f"Question récupérée depuis Flask: {data['question']}")
                    return data
                else:
                    logger.error(f"Erreur Flask ({resp.status}): {await resp.text()}")
                    return None
    except aiohttp.ClientConnectorError as e:
        logger.error(f"Erreur connexion Flask (serveur non démarré?): {e}")
        return None
    except Exception as e:
        logger.error(f"Erreur récupération question Flask: {e}")
        return None


async def validate_answer_with_flask(question_id: int, user_answer: str):
    """Valide la réponse via le serveur Flask"""
    if not AIOHTTP_AVAILABLE:
        logger.error("aiohttp non disponible - Impossible de valider avec Flask")
        return False
    
    try:
        async with aiohttp.ClientSession() as session:
            payload = {"question_id": question_id, "answer": user_answer}
            async with session.post(f"{FLASK_SERVER_URL}/answers", json=payload) as resp:
                if resp.status == 200:
                    data = await resp.json()
                    is_correct = data.get('correct', False)
                    logger.info(f"Validation Flask: {'✅ Correct' if is_correct else '❌ Incorrect'}")
                    return is_correct
                else:
                    logger.error(f"Erreur validation Flask ({resp.status}): {await resp.text()}")
                    return False
    except Exception as e:
        logger.error(f"Erreur validation réponse Flask: {e}")
        return False


# ============================================================================
# FONCTIONS WEBSOCKET
# ============================================================================

async def broadcast_message(message: Dict):
    """Envoyer un message à tous les clients connectés"""
    if connected_clients:
        message_str = json.dumps(message)
        disconnected = set()
        
        for client in connected_clients:
            try:
                await client.send(message_str)
            except websockets.exceptions.ConnectionClosed:
                disconnected.add(client)
        
        # Nettoyer les clients déconnectés
        connected_clients.difference_update(disconnected)


async def handle_message(websocket: websockets.WebSocketServerProtocol, message: str):
    """Traiter un message reçu d'un client"""
    try:
        data = json.loads(message)
        message_type = data.get('type')
        
        logger.info(f"Message reçu: {message_type}")
        
        # Routeur de messages
        if message_type == 'ADD_QUESTION':
            success = db.add_question(data['data'])
            response = {
                'type': 'QUESTION_ADDED' if success else 'ERROR',
                'data': {'success': success, 'questionId': data['data'].get('id')},
                'message': 'Question ajoutée' if success else 'Erreur lors de l\'ajout'
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'GET_QUESTIONS':
            questions = db.get_all_questions()
            response = {
                'type': 'QUESTIONS_LIST',
                'data': questions
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'DELETE_QUESTION':
            success = db.delete_question(data['questionId'])
            response = {
                'type': 'QUESTION_DELETED' if success else 'ERROR',
                'data': {'success': success, 'questionId': data['questionId']}
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'UPDATE_SETTINGS':
            settings = data['data']
            for key, value in settings.items():
                db.update_setting(key, str(value))
            
            response = {
                'type': 'SETTINGS_UPDATED',
                'data': {'success': True}
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'GET_SETTINGS':
            settings = db.get_settings()
            response = {
                'type': 'SETTINGS',
                'data': settings
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'START_GAME':
            # Le jeu démarre via le bouton GPIO 25, pas via WebSocket
            # Ce message peut être utilisé pour initialiser depuis l'app si nécessaire
            await websocket.send(json.dumps({
                'type': 'INFO',
                'message': 'Le jeu démarre via le bouton GPIO 25 sur la Raspberry Pi'
            }))
        
        elif message_type == 'ANSWER_QUESTION':
            # Les réponses sont gérées automatiquement via reconnaissance vocale
            # Ce message peut être utilisé pour forcer une réponse depuis l'app si nécessaire
            await handle_answer(websocket, data['data'])
        
        elif message_type == 'GET_SCORES':
            scores = db.get_recent_scores(limit=data.get('limit', 10))
            response = {
                'type': 'SCORES_LIST',
                'data': scores
            }
            await websocket.send(json.dumps(response))
        
        elif message_type == 'PING':
            logger.info("Réponse PONG envoyée")
            await websocket.send(json.dumps({'type': 'PONG'}))
        
        else:
            logger.warning(f"Type de message inconnu: {message_type}")
            response = {
                'type': 'ERROR',
                'message': f'Type de message inconnu: {message_type}'
            }
            await websocket.send(json.dumps(response))
    
    except json.JSONDecodeError:
        logger.error("Erreur de décodage JSON")
        await websocket.send(json.dumps({
            'type': 'ERROR',
            'message': 'Format JSON invalide'
        }))
    except Exception as e:
        logger.error(f"Erreur traitement message: {e}")
        await websocket.send(json.dumps({
            'type': 'ERROR',
            'message': str(e)
        }))


async def start_game(websocket: websockets.WebSocketServerProtocol, game_data: Dict):
    """Démarrer une nouvelle partie"""
    global current_game, game_stats
    
    questions = db.get_all_questions()
    num_questions = game_data.get('numberOfQuestions', 10)
    
    if len(questions) < num_questions:
        await websocket.send(json.dumps({
            'type': 'ERROR',
            'message': f'Pas assez de questions. Disponibles: {len(questions)}, Demandées: {num_questions}'
        }))
        return
    
    # Sélectionner les questions
    import random
    selected_questions = random.sample(questions, num_questions)
    
    # Initialiser le jeu
    current_game = {
        'questions': selected_questions,
        'current_index': 0,
        'score': 0,
        'start_time': datetime.now().isoformat()
    }
    
    game_stats = {
        'total_questions': num_questions,
        'correct_answers': 0,
        'wrong_answers': 0,
        'current_score': 0
    }
    
    # Envoyer la première question
    await send_next_question(websocket)
    
    logger.info(f"Jeu démarré avec {num_questions} questions")


def normalize_answer(text: str) -> str:
    """Normalise une réponse pour comparaison (insensible à casse/accents/espaces)"""
    import unicodedata
    import re
    
    # Convertir en minuscules
    text = text.lower()
    
    # Supprimer les accents
    text = unicodedata.normalize('NFD', text)
    text = ''.join(c for c in text if unicodedata.category(c) != 'Mn')
    
    # Supprimer les espaces multiples et trim
    text = re.sub(r'\s+', ' ', text).strip()
    
    return text

def is_answer_correct(user_answer: str, correct_answer: str) -> bool:
    """Compare deux réponses (insensible à casse/accents/espaces)"""
    return normalize_answer(user_answer) == normalize_answer(correct_answer)

async def handle_timeout(websocket: websockets.WebSocketServerProtocol):
    """Gère le timeout d'une question"""
    global current_game, game_stats, question_timer_task, question_start_time
    
    if not current_game:
        return
    
    logger.warning("Timeout: Temps écoulé pour la question actuelle")
    
    # Traiter le timeout comme une réponse incorrecte
    question = current_game['questions'][current_game['current_index']]
    time_limit = question.get('timeLimit', 30)
    
    game_stats['wrong_answers'] += 1
    gpio.wrong_answer()  # Activer le vibreur
    
    # Envoyer le résultat avec timeout
    await websocket.send(json.dumps({
        'type': 'ANSWER_RESULT',
        'data': {
            'isCorrect': False,
            'correctAnswer': question['correctAnswer'],
            'userAnswer': '',
            'currentScore': game_stats['current_score'],
            'totalQuestions': game_stats['total_questions'],
            'questionNumber': current_game['current_index'] + 1,
            'timeElapsed': time_limit,
            'timeout': True
        }
    }))
    
    # Passer à la question suivante
    current_game['current_index'] += 1
    question_timer_task = None
    question_start_time = None
    
    # Attendre un peu avant la prochaine question
    await asyncio.sleep(2)
    await send_next_question(websocket)


async def wait_for_timeout(websocket: websockets.WebSocketServerProtocol, timeout_seconds: int):
    """Attend le timeout et gère l'expiration"""
    try:
        await asyncio.sleep(timeout_seconds)
        # Si on arrive ici, le timeout est expiré
        await handle_timeout(websocket)
    except asyncio.CancelledError:
        # Le timer a été annulé (réponse reçue avant le timeout)
        logger.info("Chronomètre annulé (réponse reçue)")
        pass


async def send_next_question(websocket: websockets.WebSocketServerProtocol):
    """Envoyer la question suivante (format Question-Réponse)"""
    global current_game, question_timer_task, question_start_time
    
    if not current_game:
        return
    
    # Annuler le timer précédent s'il existe
    if question_timer_task and not question_timer_task.done():
        question_timer_task.cancel()
        question_timer_task = None
    
    index = current_game['current_index']
    
    if index >= len(current_game['questions']):
        # Fin du jeu
        await end_game(websocket)
        return
    
    question = current_game['questions'][index]
    time_limit = question.get('timeLimit')
    
    # Ne pas envoyer la réponse correcte à l'app (sécurité)
    question_for_app = {
        'id': question['id'],
        'question': question['question'],
        'category': question.get('category', 'Général'),
        'difficulty': question.get('difficulty', 'MEDIUM'),
        'timeLimit': time_limit
    }
    
    await websocket.send(json.dumps({
        'type': 'CURRENT_QUESTION',
        'data': {
            'question': question_for_app,
            'questionNumber': index + 1,
            'totalQuestions': len(current_game['questions']),
            'currentScore': game_stats['current_score']
        }
    }))
    
    # Lire la question via TTS dans le casque
    question_text = question['question']
    logger.info(f"Lecture de la question via TTS: {question_text}")
    tts_controller.speak(question_text)
    
    # Démarrer le chronomètre si timeLimit est défini
    if time_limit and time_limit > 0:
        question_start_time = time.time()
        logger.info(f"Chronomètre démarré: {time_limit} secondes")
        question_timer_task = asyncio.create_task(
            wait_for_timeout(websocket, time_limit)
        )
    else:
        question_start_time = None
        logger.info("Chronomètre désactivé pour cette question")
    
    # Attendre la réponse via reconnaissance vocale
    # Note: Le jeu se déroule maintenant entièrement sur la Raspberry Pi
    # L'utilisateur répond via le bouton d'enregistrement + micro
    logger.info("En attente de la réponse de l'utilisateur...")
    user_answer = await speech_controller.wait_for_answer(
        timeout=time_limit or 30,
        phrase_time_limit=10
    )
    
    if user_answer:
        # Annuler le timer si une réponse est reçue
        if question_timer_task and not question_timer_task.done():
            question_timer_task.cancel()
            question_timer_task = None
        
        # Traiter la réponse
        answer_data = {
            'answer': user_answer,
            'timeElapsed': time.time() - question_start_time if question_start_time else 0
        }
        await handle_answer(websocket, answer_data)
    else:
        # Timeout ou erreur de reconnaissance
        logger.warning("Aucune réponse reçue ou reconnaissance échouée")
        # Le timeout sera géré par wait_for_timeout si le chronomètre est actif


async def handle_answer(websocket: websockets.WebSocketServerProtocol, answer_data: Dict):
    """Traiter une réponse (format Question-Réponse)"""
    global current_game, game_stats, question_timer_task, question_start_time
    
    if not current_game:
        await websocket.send(json.dumps({
            'type': 'ERROR',
            'message': 'Aucun jeu en cours'
        }))
        return
    
    # Annuler le timer si une réponse est reçue
    if question_timer_task and not question_timer_task.done():
        question_timer_task.cancel()
        question_timer_task = None
    
    question = current_game['questions'][current_game['current_index']]
    user_answer_text = answer_data.get('answer', '')  # Réponse textuelle
    correct_answer_text = question['correctAnswer']
    
    # Calculer le temps écoulé
    time_elapsed = 0
    if question_start_time:
        time_elapsed = time.time() - question_start_time
    else:
        time_elapsed = answer_data.get('timeElapsed', 0)
    
    # Comparaison textuelle normalisée
    is_correct = is_answer_correct(user_answer_text, correct_answer_text)
    
    # Mettre à jour les stats
    if is_correct:
        game_stats['correct_answers'] += 1
        # Score selon difficulté
        difficulty = question.get('difficulty', 'MEDIUM')
        if difficulty == 'EASY':
            game_stats['current_score'] += 1
        elif difficulty == 'MEDIUM':
            game_stats['current_score'] += 2
        elif difficulty == 'HARD':
            game_stats['current_score'] += 3
        else:
            game_stats['current_score'] += 1
        gpio.good_answer()
    else:
        game_stats['wrong_answers'] += 1
        gpio.wrong_answer()
    
    # Envoyer le résultat
    await websocket.send(json.dumps({
        'type': 'ANSWER_RESULT',
        'data': {
            'isCorrect': is_correct,
            'correctAnswer': correct_answer_text,
            'userAnswer': user_answer_text,
            'currentScore': game_stats['current_score'],
            'totalQuestions': game_stats['total_questions'],
            'questionNumber': current_game['current_index'] + 1,
            'timeElapsed': round(time_elapsed, 2)
        }
    }))
    
    # Réinitialiser le timer
    question_start_time = None
    
    # Passer à la question suivante
    current_game['current_index'] += 1
    
    # Attendre un peu avant la prochaine question
    await asyncio.sleep(2)
    await send_next_question(websocket)


async def end_game(websocket: websockets.WebSocketServerProtocol):
    """Terminer le jeu et sauvegarder le score"""
    global current_game, game_stats, question_timer_task, question_start_time
    
    if not current_game:
        return
    
    # Annuler le timer s'il existe
    if question_timer_task and not question_timer_task.done():
        question_timer_task.cancel()
        question_timer_task = None
    question_start_time = None
    
    # Calculer les statistiques finales
    final_score = game_stats['current_score']
    total_questions = game_stats['total_questions']
    correct_answers = game_stats['correct_answers']
    wrong_answers = game_stats['wrong_answers']
    
    # Sauvegarder le score dans la BDD
    game_id = f"game_{int(time.time())}"
    db.save_score(
        player_name="Player",
        score=final_score,
        total_questions=total_questions,
        correct_answers=correct_answers,
        wrong_answers=wrong_answers
    )
    
    # Envoyer les statistiques finales automatiquement à tous les clients
    await broadcast_message({
        'type': 'GAME_ENDED',
        'data': {
            'gameId': game_id,
            'startTime': current_game['start_time'],
            'endTime': datetime.now().isoformat(),
            'totalQuestions': total_questions,
            'correctAnswers': correct_answers,
            'wrongAnswers': wrong_answers,
            'finalScore': final_score,
            'totalTime': 0,  # À calculer si nécessaire
            'averageTime': 0  # À calculer si nécessaire
        }
    })
    
    # Réinitialiser le jeu
    current_game = None
    game_stats = {
        "total_questions": 0,
        "correct_answers": 0,
        "wrong_answers": 0,
        "current_score": 0
    }
    
    logger.info(f"Jeu terminé - Score: {final_score}/{total_questions}")


# ============================================================================
# BOUCLE DE JEU INFINIE (MODE FLASK)
# ============================================================================

async def infinite_game_loop():
    """Boucle de jeu infinie - Pose des questions continuellement via Flask"""
    global game_stats
    
    logger.info("🎮 Démarrage du mode jeu en boucle infinie")
    
    # Initialiser les stats
    game_stats = {
        "total_questions": 0,
        "correct_answers": 0,
        "wrong_answers": 0,
        "current_score": 0,
        "session_start": time.time()
    }
    
    question_count_in_session = 0
    QUESTIONS_PER_STATS = 10  # Envoyer les stats toutes les 10 questions
    
    await broadcast_message({
        "type": "GAME_STARTED",
        "data": {"message": "Mode jeu en boucle infinie démarré"}
    })
    
    # Boucle infinie
    while True:
        try:
            # Récupérer question depuis Flask
            question_data = await fetch_random_question_from_flask()
            
            if not question_data:
                logger.error("Impossible de récupérer une question, retry dans 5s...")
                await asyncio.sleep(5)
                continue
            
            question_count_in_session += 1
            game_stats["total_questions"] += 1
            
            logger.info(f"📝 Question #{game_stats['total_questions']}: {question_data['question']}")
            
            # Lire la question via TTS
            tts_controller.speak(question_data['question'])
            
            # Attendre la réponse vocale (VOSK + GPIO 16)
            logger.info("🎤 En attente de la réponse (appuyez sur GPIO 16)...")
            user_answer = await speech_controller.wait_for_answer(timeout=60)
            
            if user_answer:
                logger.info(f"💬 Réponse utilisateur: {user_answer}")
                
                # Valider avec Flask
                is_correct = await validate_answer_with_flask(
                    question_data['id'], 
                    user_answer
                )
                
                if is_correct:
                    game_stats["correct_answers"] += 1
                    game_stats["current_score"] += 10
                    logger.info("✅ Bonne réponse!")
                    gpio.good_answer()
                    await broadcast_message({
                        "type": "ANSWER_RESULT",
                        "data": {
                            "correct": True,
                            "user_answer": user_answer,
                            "score": game_stats["current_score"],
                            "total_questions": game_stats["total_questions"]
                        }
                    })
                else:
                    game_stats["wrong_answers"] += 1
                    logger.info(f"❌ Mauvaise réponse. Bonne réponse: {question_data.get('correct_answer', '?')}")
                    gpio.wrong_answer()
                    await broadcast_message({
                        "type": "ANSWER_RESULT",
                        "data": {
                            "correct": False,
                            "user_answer": user_answer,
                            "correct_answer": question_data.get('correct_answer', '?'),
                            "score": game_stats["current_score"],
                            "total_questions": game_stats["total_questions"]
                        }
                    })
            else:
                # Timeout ou pas de réponse
                game_stats["wrong_answers"] += 1
                logger.warning("⏱️ Timeout - Pas de réponse")
                gpio.wrong_answer()
            
            # Envoyer les stats périodiquement (toutes les 10 questions)
            if question_count_in_session >= QUESTIONS_PER_STATS:
                logger.info(f"📊 Envoi des statistiques (session de {QUESTIONS_PER_STATS} questions)")
                await broadcast_message({
                    "type": "GAME_STATS",
                    "data": {
                        "total_questions": game_stats["total_questions"],
                        "correct_answers": game_stats["correct_answers"],
                        "wrong_answers": game_stats["wrong_answers"],
                        "current_score": game_stats["current_score"],
                        "session_duration": time.time() - game_stats["session_start"]
                    }
                })
                # Réinitialiser le compteur de session
                question_count_in_session = 0
            
            # Petite pause entre les questions
            await asyncio.sleep(2)
            
        except Exception as e:
            logger.error(f"Erreur dans la boucle de jeu: {e}")
            await asyncio.sleep(5)  # Pause avant de réessayer


# ============================================================================
# GESTION DES CONNEXIONS WEBSOCKET
# ============================================================================

async def handle_client(websocket: websockets.WebSocketServerProtocol, path: str):
    """Gérer une connexion client"""
    client_address = websocket.remote_address
    logger.info(f"Nouveau client connecté: {client_address}")
    
    connected_clients.add(websocket)
    
    # Envoyer un message de bienvenue
    await websocket.send(json.dumps({
        'type': 'CONNECTED',
        'data': {
            'message': 'Connecté au serveur CultureG',
            'serverVersion': '1.0.0',
            'questionsCount': len(db.get_all_questions())
        }
    }))
    
    try:
        async for message in websocket:
            await handle_message(websocket, message)
    
    except websockets.exceptions.ConnectionClosed:
        logger.info(f"Client déconnecté: {client_address}")
    except Exception as e:
        logger.error(f"Erreur avec client {client_address}: {e}")
    finally:
        connected_clients.discard(websocket)


async def main():
    """Point d'entrée principal"""
    logger.info("=" * 50)
    logger.info("Serveur CultureG - Raspberry Pi")
    logger.info("=" * 50)
    logger.info(f"Mode: {'RÉEL' if GPIO_AVAILABLE else 'SIMULATION'} (GPIO {'disponible' if GPIO_AVAILABLE else 'non disponible'})")
    logger.info(f"Écoute sur ws://{WS_HOST}:{WS_PORT}")
    logger.info(f"Base de données: {DB_FILE}")
    logger.info(f"Flask server: {FLASK_SERVER_URL}")
    logger.info("🎮 Mode jeu en boucle infinie activé")
    logger.info("Appuyez sur Ctrl+C pour arrêter")
    logger.info("=" * 50)
    
    try:
        async with websockets.serve(handle_client, WS_HOST, WS_PORT):
            # Lancer la boucle de jeu infinie en tâche d'arrière-plan
            game_task = asyncio.create_task(infinite_game_loop())
            
            try:
                await asyncio.Future()  # Run forever
            except KeyboardInterrupt:
                logger.info("\n🛑 Arrêt du serveur...")
                game_task.cancel()
                
                # Envoyer les stats finales
                try:
                    await broadcast_message({
                        "type": "GAME_STATS_FINAL",
                        "data": {
                            "total_questions": game_stats["total_questions"],
                            "correct_answers": game_stats["correct_answers"],
                            "wrong_answers": game_stats["wrong_answers"],
                            "current_score": game_stats["current_score"],
                            "total_duration": time.time() - game_stats.get("session_start", time.time())
                        }
                    })
                except:
                    pass
    except KeyboardInterrupt:
        logger.info("\nArrêt du serveur...")
    finally:
        gpio.cleanup()
        logger.info("Serveur arrêté")


if __name__ == "__main__":
    try:
        asyncio.run(main())
    except KeyboardInterrupt:
        logger.info("Arrêt du serveur")
        gpio.cleanup()

