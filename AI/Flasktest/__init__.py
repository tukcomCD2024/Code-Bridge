from flask import Flask

def create_app():
    app = Flask(__name__)

    #route list
    from Flasktest.routes import routelist
    routelist(app)

    @app.route('/')
    def hello_world():
        return "hello world"

    @app.route('/ai')
    def imageSupport():
        return ""

    return app