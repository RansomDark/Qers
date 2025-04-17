import hashlib
import secrets

def generate_random_hash():
    return secrets.token_hex(15)

# Хеш пароля
def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()

# Проверка пароля
def verify_password(plain, hashed):
    return hash_password(plain) == hashed
