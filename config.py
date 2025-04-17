import os
from dotenv import load_dotenv

load_dotenv()

class Config:
    '''
    Конфиг сервера. Путь к базе данных, секретный ключ
    '''
    SECRET_KEY = os.getenv("SECRET_KEY", "1111")
    DB_PATH = os.getenv("DB_PATH", "data.db")
    ACCESS_TOKEN_EXPIRES = 60 * 24 * 30  # В минутах