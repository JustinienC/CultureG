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

# Configuration du logging
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Configuration GPIO
LED_PIN = 18          # GPIO 18 pour LED (bonne réponse)
VIBRATOR_PIN = 23     # GPIO 23 pour vibreur (mauvaise réponse)
BUZZER_PIN = 24       # GPIO 24 pour buzzer/haut-parleur
BUTTON_PIN = 25       # GPIO 25 pour bouton démarrage

# Configuration WebSocket
WS_HOST = "0.0.0.0"   # Écouter sur toutes les interfaces
WS_PORT = 8765        # Port WebSocket

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


class GPIOController:
    """Contrôleur GPIO pour LEDs, vibreur et buzzer"""
    
    def __init__(self):
        self.initialized = False
        
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
            
            # Configuration du bouton en entrée avec pull-up
            GPIO.setup(BUTTON_PIN, GPIO.IN, pull_up_down=GPIO.PUD_UP)
            
            # Callback pour le bouton
            GPIO.add_event_detect(BUTTON_PIN, GPIO.FALLING, 
                                 callback=self._button_callback, 
                                 bouncetime=300)
            
            self.initialized = True
            logger.info("GPIO initialisé avec succès")
        except Exception as e:
            logger.error(f"Erreur initialisation GPIO: {e}")
            logger.warning("Mode simulation activé (pas de GPIO réel)")
    
    def _button_callback(self, channel):
        """Callback appelé quand le bouton est pressé"""
        logger.info("Bouton démarrage pressé")
        # Envoyer événement à tous les clients connectés
        asyncio.create_task(self._notify_button_pressed())
    
    async def _notify_button_pressed(self):
        """Notifier les clients que le bouton a été pressé"""
        message = {
            "type": "BUTTON_PRESSED",
            "timestamp": datetime.now().isoformat()
        }
        await broadcast_message(message)
    
    def good_answer(self):
        """Activer LED et son pour bonne réponse"""
        if not self.initialized:
            logger.info("SIMULATION: Bonne réponse (LED + Son)")
            return
        
        try:
            # Allumer LED
            GPIO.output(LED_PIN, GPIO.HIGH)
            
            # Son de succès (buzzer)
            self._play_tone(BUZZER_PIN, 1000, 0.2)  # 1000Hz pendant 0.2s
            
            # Éteindre LED après 0.5s
            time.sleep(0.5)
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
        
        # Table des questions
        c.execute('''
            CREATE TABLE IF NOT EXISTS questions (
                id TEXT PRIMARY KEY,
                question TEXT NOT NULL,
                answers TEXT NOT NULL,
                correct_answer_index INTEGER NOT NULL,
                category TEXT,
                difficulty TEXT,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
            )
        ''')
        
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
        """Ajouter une question à la base de données"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                INSERT OR REPLACE INTO questions 
                (id, question, answers, correct_answer_index, category, difficulty)
                VALUES (?, ?, ?, ?, ?, ?)
            ''', (
                question_data['id'],
                question_data['question'],
                json.dumps(question_data['answers']),
                question_data['correctAnswerIndex'],
                question_data.get('category', 'Général'),
                question_data.get('difficulty', 'MEDIUM')
            ))
            
            conn.commit()
            conn.close()
            logger.info(f"Question ajoutée: {question_data['id']}")
            return True
        except Exception as e:
            logger.error(f"Erreur ajout question: {e}")
            return False
    
    def get_all_questions(self) -> list:
        """Récupérer toutes les questions"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('SELECT * FROM questions ORDER BY created_at DESC')
            rows = c.fetchall()
            conn.close()
            
            questions = []
            for row in rows:
                questions.append({
                    'id': row[0],
                    'question': row[1],
                    'answers': json.loads(row[2]),
                    'correctAnswerIndex': row[3],
                    'category': row[4] or 'Général',
                    'difficulty': row[5] or 'MEDIUM'
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
    
    def save_score(self, score_data: Dict) -> bool:
        """Sauvegarder un score"""
        try:
            conn = sqlite3.connect(self.db_file)
            c = conn.cursor()
            
            c.execute('''
                INSERT INTO scores (player_name, score, total_questions, correct_answers)
                VALUES (?, ?, ?, ?)
            ''', (
                score_data.get('playerName', 'Anonyme'),
                score_data['score'],
                score_data['totalQuestions'],
                score_data['correctAnswers']
            ))
            
            conn.commit()
            conn.close()
            logger.info(f"Score sauvegardé: {score_data['score']}/{score_data['totalQuestions']}")
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
            await start_game(websocket, data.get('data', {}))
        
        elif message_type == 'ANSWER_QUESTION':
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


async def send_next_question(websocket: websockets.WebSocketServerProtocol):
    """Envoyer la question suivante"""
    global current_game
    
    if not current_game:
        return
    
    index = current_game['current_index']
    
    if index >= len(current_game['questions']):
        # Fin du jeu
        await end_game(websocket)
        return
    
    question = current_game['questions'][index]
    
    await websocket.send(json.dumps({
        'type': 'CURRENT_QUESTION',
        'data': {
            'question': question,
            'questionNumber': index + 1,
            'totalQuestions': len(current_game['questions'])
        }
    }))


async def handle_answer(websocket: websockets.WebSocketServerProtocol, answer_data: Dict):
    """Traiter une réponse"""
    global current_game, game_stats
    
    if not current_game:
        await websocket.send(json.dumps({
            'type': 'ERROR',
            'message': 'Aucun jeu en cours'
        }))
        return
    
    question = current_game['questions'][current_game['current_index']]
    user_answer = answer_data.get('answerIndex')
    correct_answer = question['correctAnswerIndex']
    
    is_correct = user_answer == correct_answer
    
    # Mettre à jour les stats
    if is_correct:
        game_stats['correct_answers'] += 1
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
            'correctAnswerIndex': correct_answer,
            'currentScore': game_stats['current_score'],
            'totalQuestions': game_stats['total_questions']
        }
    }))
    
    # Passer à la question suivante
    current_game['current_index'] += 1
    
    # Attendre un peu avant la prochaine question
    await asyncio.sleep(2)
    await send_next_question(websocket)


async def end_game(websocket: websockets.WebSocketServerProtocol):
    """Terminer le jeu et sauvegarder le score"""
    global current_game, game_stats
    
    if not current_game:
        return
    
    # Sauvegarder le score
    db.save_score({
        'playerName': 'Joueur',
        'score': game_stats['current_score'],
        'totalQuestions': game_stats['total_questions'],
        'correctAnswers': game_stats['correct_answers']
    })
    
    # Envoyer le résultat final
    await websocket.send(json.dumps({
        'type': 'GAME_ENDED',
        'data': {
            'finalScore': game_stats['current_score'],
            'totalQuestions': game_stats['total_questions'],
            'correctAnswers': game_stats['correct_answers'],
            'wrongAnswers': game_stats['wrong_answers']
        }
    }))
    
    logger.info(f"Jeu terminé. Score: {game_stats['current_score']}/{game_stats['total_questions']}")
    
    current_game = None


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
    logger.info(f"Écoute sur ws://{WS_HOST}:{WS_PORT}")
    logger.info(f"Base de données: {DB_FILE}")
    logger.info("Appuyez sur Ctrl+C pour arrêter")
    logger.info("=" * 50)
    
    try:
        async with websockets.serve(handle_client, WS_HOST, WS_PORT):
            await asyncio.Future()  # Run forever
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

