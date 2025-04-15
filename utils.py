import hashlib
import secrets

def generate_random_hash():
    return secrets.token_hex(15)

def hash_password(password):
    return hashlib.sha256(password.encode()).hexdigest()

def verify_password(plain, hashed):
    return hash_password(plain) == hashed
