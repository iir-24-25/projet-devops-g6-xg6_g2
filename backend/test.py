import mysql.connector

class DatabaseHandler:
    def __init__(self, config):
        self.config = config

    def get_connection(self):
        """Établit une connexion à la base de données MySQL."""
        try:
            conn = mysql.connector.connect(**self.config)
            return conn
            
        except mysql.connector.Error as err:
            print(f"Erreur de connexion à la base de données : {err}")
            return None