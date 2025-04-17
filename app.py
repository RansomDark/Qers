from flask import Flask
from routes import routes
from databases import init_db

app = Flask(__name__)
app.register_blueprint(routes)
init_db()

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5001, debug=True)
