import sqlite3
from config import Config

def get_db_connection():
    return sqlite3.connect(Config.DB_PATH)

def init_db():
    with get_db_connection() as conn:
        cursor = conn.cursor()
        cursor.execute('''
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                password_hash TEXT NOT NULL,
                username TEXT NOT NULL UNIQUE,
                email TEXT NOT NULL UNIQUE,
                is_pressed INTEGER DEFAULT 0,
                hash TEXT NOT NULL
            )
        ''')
        conn.commit()
