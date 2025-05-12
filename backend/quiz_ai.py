# quiz_ai.py
from flask import request, jsonify
import google.generativeai as genai
import re

genai.configure(api_key="AIzaSyBh81Gzj69wK7_lhmhLaJzpRi4lYDMiOZU")
modele = genai.GenerativeModel("models/gemini-1.5-flash-latest")

def generer_qcm(theme, niveau, nb_questions):
    prompt = f"""Génère {nb_questions} QCM sur le thème "{theme}".
Niveau : {niveau}.
Format :
Q: [question]
A. [choix A]
B. [choix B]
C. [choix C]
D. [choix D]
Réponse: [Lettre correcte]"""
    try:
        reponse = modele.generate_content(prompt)
        return reponse.text
    except Exception as e:
        print(f"Erreur lors de l'appel à l'API Gemini: {e}")
        return None

def parser_qcm(texte_qcm):
    # Supprimer le 'Q: ' initial si présent
    if texte_qcm.startswith('Q: '):
        texte_qcm = texte_qcm[3:]
    
    # Diviser le texte en questions
    questions = re.split(r'\nQ: ', texte_qcm)
    qcm_data = []
    
    for q in questions:
        if not q.strip():  # Ignorer les questions vides
            continue
            
        lignes = q.strip().split('\n')
        if len(lignes) < 6:
            print(f"Erreur de formatage pour la question: {q}")
            continue
            
        question = lignes[0].strip()
        choix = {}
        try:
            # Extraire les choix
            for i in range(1, 5):
                if i < len(lignes) and lignes[i].startswith(('A.', 'B.', 'C.', 'D.')):
                    lettre = lignes[i][0]
                    choix[lettre] = lignes[i][3:].strip()
            
            # Extraire la réponse
            reponse = None
            for ligne in lignes[4:]:
                if ligne.startswith('Réponse:'):
                    reponse = ligne.split(':')[-1].strip().upper()
                    break
            
            if not reponse or len(choix) != 4:
                print(f"Format de réponse invalide pour la question: {q}")
                continue
                
            qcm_data.append({
                'question': question,
                'choix': choix,
                'reponse': reponse
            })
        except Exception as e:
            print(f"Erreur lors du parsing de la question: {q}")
            print(f"Erreur: {str(e)}")
            continue
            
    return qcm_data

def generate_quiz():
    """
    Route pour générer un quiz à partir des paramètres fournis.
    Returns:
        jsonify: Un objet JSON contenant les données du quiz, ou un message d'erreur.
    """
    data = request.get_json()  # Utiliser directement request.get_json()
    theme = data.get('theme')
    niveau = data.get('niveau')
    nb_questions = data.get('nb_questions')
    if not theme or not niveau or not nb_questions:
        return jsonify({'error': 'Paramètres manquants : theme, niveau et nb_questions sont requis.'}), 400
    if not isinstance(nb_questions, int):
        try:
            nb_questions = int(nb_questions)  # Convertir en entier si possible
        except ValueError:
            return jsonify({'error': 'nb_questions doit être un entier.'}), 400
    texte_qcm = generer_qcm(theme, niveau, nb_questions)
    if not texte_qcm:
        return jsonify({'error': 'La génération du quiz a échoué.'}), 500
    qcm_data = parser_qcm(texte_qcm)
    if not qcm_data:
        return jsonify({'error': 'Erreur lors du parsing du quiz généré.'}), 500
    return jsonify({'quiz': qcm_data}), 200  # Retourner un seul objet JSON