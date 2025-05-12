# quiz.py
from flask import Flask, current_app, jsonify, request
from flask_cors import CORS
import google.generativeai as genai
import re
# Import des routes
import quiz_creator
import quiz_interaction
import quiz_ai 
import test # Renommé pour correspondre au nom du fichier

app = Flask(__name__)
CORS(app)

# Configuration de la base de données
DB_CONFIG = {
    'host': 'mysql-34e8ad04-chaimae-cf86.j.aivencloud.com',
    'port': 18011,
    'user': 'avnadmin',
    'password': 'AVNS_9ZeFEDBQLpI4cQZdPGP',
    'database': 'quiz'
}

# Initialiser la connexion à la base de données
try:
    app.db_connection = test.DatabaseHandler(DB_CONFIG)
except Exception as e:
    print(f"Erreur de connexion à la base de données : {e}")
    app.db_connection = None  # Important : Définir à None en cas d'échec





# Enregistrement des routes
app.add_url_rule('/create_manual', methods=['POST'], view_func=quiz_creator.create_manual)
app.add_url_rule('/quiz/<string:room_id>/questions', methods=['GET'], view_func=quiz_interaction.get_quiz_questions)
app.add_url_rule('/quiz/<string:room_id>/submit', methods=['POST'],view_func=quiz_interaction.submit_quiz)  # Correction de l'appel
app.add_url_rule('/generate_quiz', methods=['POST'], view_func=quiz_ai.generate_quiz)  # Ajout de la route pour quiz-AI
app.add_url_rule('/quiz/<string:room_id>/start', methods=['POST'],view_func=quiz_interaction.start_quiz_session)  # Correction de l'appel



@app.route('/quiz/<room_id>/status', methods=['PATCH'])
def update_quiz_status(room_id):
    # Met à jour le statut dans la base de données avec une requête SQL
    conn = current_app.db_connection.get_connection()
    cursor = conn.cursor()
    
    # Mise à jour du statut
    cursor.execute("UPDATE quizzes SET status = %s WHERE room_id = %s", ("running", room_id))
    
    # Commit des changements
    conn.commit()
    conn.close()

    # Réponds avec un message de succès
    return jsonify({'message': 'Statut mis à jour avec succès'}), 200

@app.route('/quiz/<room_id>/status', methods=['GET'])
def get_quiz_status(room_id):
    try:
        # Connexion à la base de données
        conn = current_app.db_connection.get_connection()
        cursor = conn.cursor()

        # Exécuter la requête SQL pour récupérer le statut de la room
        cursor.execute("SELECT status FROM quizzes WHERE room_id = %s", (room_id,))
        result = cursor.fetchone()
        print(result)
        if result:
            # Si la room est trouvée, renvoie le statut
            return jsonify({'status': result[0]}), 200
        else:
            # Si la room n'est pas trouvée, retourne une erreur 404
            return jsonify({'error': 'Room not found'}), 404

    except Exception as e:
        # Si une erreur se produit, renvoyer une erreur serveur
        return jsonify({'error': str(e)}), 500

    finally:
        # Assurez-vous que la connexion à la base de données est fermée
        if conn:
            conn.close()



if __name__ == '__main__':
    app.run(debug=True, port=5000)  # Spécifiez le port explicitement