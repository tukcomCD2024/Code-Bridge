from flask import Flask

app = Flask(__name__)


def create_app():

    if __name__ == "__main__":
        app.run()

    #route list
    from app.routes import routelist
    routelist(app)

    @app.route('/')
    def hello_world():
        return "hello world"

    @app.route('/ai')
    def imageSupport():
        return ""

    return app

create_app()