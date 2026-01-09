#!/usr/bin/env python3
"""
Script de test pour le serveur CultureG
Teste les principales fonctionnalités du serveur WebSocket
"""

import asyncio
import websockets
import json
import sys


async def test_connection():
    """Test de connexion basique"""
    print("=" * 50)
    print("TEST 1: Connexion au serveur")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            # Recevoir message de connexion
            response = await websocket.recv()
            data = json.loads(response)
            print(f"✅ Connexion réussie!")
            print(f"   Message: {data.get('data', {}).get('message')}")
            print(f"   Questions disponibles: {data.get('data', {}).get('questionsCount')}")
            return True
    except Exception as e:
        print(f"❌ Erreur de connexion: {e}")
        return False


async def test_ping():
    """Test du ping/pong"""
    print("\n" + "=" * 50)
    print("TEST 2: Ping/Pong")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            # Envoyer ping
            await websocket.send(json.dumps({"type": "PING"}))
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'PONG':
                print("✅ Ping/Pong fonctionne!")
                return True
            else:
                print(f"❌ Réponse inattendue: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False


async def test_add_question():
    """Test d'ajout de question"""
    print("\n" + "=" * 50)
    print("TEST 3: Ajout de question")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            message = {
                "type": "ADD_QUESTION",
                "data": {
                    "id": "test-question-1",
                    "question": "Quelle est la capitale de la France ?",
                    "answers": ["Paris", "Lyon", "Marseille", "Toulouse"],
                    "correctAnswerIndex": 0,
                    "category": "Géographie",
                    "difficulty": "EASY"
                }
            }
            
            await websocket.send(json.dumps(message))
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'QUESTION_ADDED' and data.get('data', {}).get('success'):
                print("✅ Question ajoutée avec succès!")
                print(f"   ID: {data.get('data', {}).get('questionId')}")
                return True
            else:
                print(f"❌ Échec: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False


async def test_get_questions():
    """Test de récupération des questions"""
    print("\n" + "=" * 50)
    print("TEST 4: Récupération des questions")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            await websocket.send(json.dumps({"type": "GET_QUESTIONS"}))
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'QUESTIONS_LIST':
                questions = data.get('data', [])
                print(f"✅ {len(questions)} questions récupérées")
                if questions:
                    print(f"   Première question: {questions[0].get('question')}")
                return True
            else:
                print(f"❌ Réponse inattendue: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False


async def test_get_settings():
    """Test de récupération des paramètres"""
    print("\n" + "=" * 50)
    print("TEST 5: Récupération des paramètres")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            await websocket.send(json.dumps({"type": "GET_SETTINGS"}))
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'SETTINGS':
                settings = data.get('data', {})
                print("✅ Paramètres récupérés:")
                for key, value in settings.items():
                    print(f"   {key}: {value}")
                return True
            else:
                print(f"❌ Réponse inattendue: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False


async def test_update_settings():
    """Test de mise à jour des paramètres"""
    print("\n" + "=" * 50)
    print("TEST 6: Mise à jour des paramètres")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            message = {
                "type": "UPDATE_SETTINGS",
                "data": {
                    "timer_seconds": "45",
                    "sound_enabled": "true"
                }
            }
            
            await websocket.send(json.dumps(message))
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'SETTINGS_UPDATED':
                print("✅ Paramètres mis à jour avec succès!")
                return True
            else:
                print(f"❌ Échec: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        return False


async def test_game_flow():
    """Test du flux de jeu complet"""
    print("\n" + "=" * 50)
    print("TEST 7: Flux de jeu complet")
    print("=" * 50)
    
    try:
        uri = "ws://localhost:8765"
        async with websockets.connect(uri) as websocket:
            await websocket.recv()  # Ignorer CONNECTED
            
            # Démarrer le jeu
            print("   → Démarrage du jeu...")
            await websocket.send(json.dumps({
                "type": "START_GAME",
                "data": {"numberOfQuestions": 2}
            }))
            
            # Recevoir première question
            response = await websocket.recv()
            data = json.loads(response)
            
            if data.get('type') == 'CURRENT_QUESTION':
                question = data.get('data', {}).get('question', {})
                print(f"✅ Question reçue: {question.get('question')}")
                
                # Répondre (première réponse = bonne)
                print("   → Envoi de la réponse...")
                await websocket.send(json.dumps({
                    "type": "ANSWER_QUESTION",
                    "data": {"answerIndex": question.get('correctAnswerIndex', 0)}
                }))
                
                # Recevoir résultat
                response = await websocket.recv()
                data = json.loads(response)
                
                if data.get('type') == 'ANSWER_RESULT':
                    is_correct = data.get('data', {}).get('isCorrect')
                    score = data.get('data', {}).get('currentScore')
                    print(f"✅ Réponse traitée: {'Correcte' if is_correct else 'Incorrecte'}")
                    print(f"   Score actuel: {score}")
                    
                    # Attendre question suivante ou fin
                    await asyncio.sleep(2)
                    try:
                        response = await asyncio.wait_for(websocket.recv(), timeout=3)
                        data = json.loads(response)
                        if data.get('type') == 'GAME_ENDED':
                            print("✅ Jeu terminé!")
                            print(f"   Score final: {data.get('data', {}).get('finalScore')}")
                        elif data.get('type') == 'CURRENT_QUESTION':
                            print("✅ Question suivante reçue")
                    except asyncio.TimeoutError:
                        print("   (Timeout - normal si pas assez de questions)")
                    
                    return True
                else:
                    print(f"❌ Réponse inattendue: {data}")
                    return False
            else:
                print(f"❌ Pas de question reçue: {data}")
                return False
    except Exception as e:
        print(f"❌ Erreur: {e}")
        import traceback
        traceback.print_exc()
        return False


async def run_all_tests():
    """Exécuter tous les tests"""
    print("\n" + "=" * 50)
    print("SUITE DE TESTS - Serveur CultureG")
    print("=" * 50)
    print("\n⚠️  Assure-toi que le serveur est lancé sur ws://localhost:8765")
    print("   (python3 server.py)\n")
    
    tests = [
        ("Connexion", test_connection),
        ("Ping/Pong", test_ping),
        ("Ajout question", test_add_question),
        ("Récupération questions", test_get_questions),
        ("Récupération paramètres", test_get_settings),
        ("Mise à jour paramètres", test_update_settings),
        ("Flux de jeu", test_game_flow),
    ]
    
    results = []
    
    for name, test_func in tests:
        try:
            result = await test_func()
            results.append((name, result))
        except Exception as e:
            print(f"❌ Erreur dans {name}: {e}")
            results.append((name, False))
    
    # Résumé
    print("\n" + "=" * 50)
    print("RÉSUMÉ DES TESTS")
    print("=" * 50)
    
    passed = sum(1 for _, result in results if result)
    total = len(results)
    
    for name, result in results:
        status = "✅ PASS" if result else "❌ FAIL"
        print(f"{status} - {name}")
    
    print(f"\nTotal: {passed}/{total} tests réussis")
    
    if passed == total:
        print("\n🎉 Tous les tests sont passés!")
        return 0
    else:
        print(f"\n⚠️  {total - passed} test(s) ont échoué")
        return 1


if __name__ == "__main__":
    try:
        exit_code = asyncio.run(run_all_tests())
        sys.exit(exit_code)
    except KeyboardInterrupt:
        print("\n\nTests interrompus par l'utilisateur")
        sys.exit(1)
    except Exception as e:
        print(f"\n❌ Erreur fatale: {e}")
        import traceback
        traceback.print_exc()
        sys.exit(1)

