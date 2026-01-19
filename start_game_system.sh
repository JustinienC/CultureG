#!/bin/bash
# Script de démarrage du système CultureG
# Démarre le serveur Flask et le serveur WebSocket en parallèle

set -e

echo "=========================================="
echo " Démarrage du système CultureG"
echo "=========================================="

# Vérifier si on est dans le bon répertoire
if [ ! -d "serveur" ] || [ ! -d "raspberry-pi" ]; then
    echo " Erreur: Ce script doit être exécuté depuis la racine du projet"
    echo "   Structure attendue:"
    echo "   - serveur/"
    echo "   - raspberry-pi/"
    exit 1
fi

# Fonction de nettoyage
cleanup() {
    echo ""
    echo " Arrêt du système CultureG..."
    if [ ! -z "$FLASK_PID" ]; then
        kill $FLASK_PID 2>/dev/null || true
        echo "   Flask arrêté"
    fi
    if [ ! -z "$WEBSOCKET_PID" ]; then
        kill $WEBSOCKET_PID 2>/dev/null || true
        echo "   WebSocket arrêté"
    fi
    echo " Système arrêté"
    exit 0
}

# Capturer Ctrl+C et autres signaux
trap cleanup SIGINT SIGTERM

# Vérifier Python
if ! command -v python3 &> /dev/null; then
    echo " Python 3 n'est pas installé"
    exit 1
fi

# Démarrer le serveur Flask en arrière-plan
echo ""
echo "🔧 Démarrage du serveur Flask..."
cd serveur
python3 webServer.py > ../flask.log 2>&1 &
FLASK_PID=$!
cd ..
echo "   ✅ Flask démarré (PID: $FLASK_PID)"
echo "   📝 Logs: flask.log"

# Attendre que Flask soit prêt
echo "   ⏳ Attente du démarrage de Flask (3 secondes)..."
sleep 3

# Vérifier que Flask tourne toujours
if ! kill -0 $FLASK_PID 2>/dev/null; then
    echo "   ❌ Flask a crashé au démarrage. Voir flask.log"
    exit 1
fi

# Démarrer le serveur WebSocket
echo ""
echo "🔧 Démarrage du serveur WebSocket..."
cd raspberry-pi
python3 server.py &
WEBSOCKET_PID=$!
cd ..
echo "   ✅ WebSocket démarré (PID: $WEBSOCKET_PID)"

echo ""
echo "=========================================="
echo " ✅ Système CultureG opérationnel"
echo "=========================================="
echo " Flask:     http://localhost:5000"
echo " WebSocket: ws://0.0.0.0:8765"
echo ""
echo " Appuyez sur Ctrl+C pour arrêter"
echo "=========================================="

# Attendre que le serveur WebSocket se termine (ou Ctrl+C)
wait $WEBSOCKET_PID
