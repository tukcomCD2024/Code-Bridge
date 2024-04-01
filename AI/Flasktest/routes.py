from Flasktest.controllers import example_controllers

def routelist(app):
    return app.register_blueprint(example_controllers.ai)