from flask import Blueprint, request, jsonify
from app.services import auto_draw

bp = Blueprint(name='example',
               import_name=__name__,
               url_prefix='/example')

ai = Blueprint(name='draw',
               import_name=__name__,
               url_prefix='/draw')


@ai.route('/', methods=['GET', 'POST'])
def draw()->str:
    return jsonify(auto_draw.AI(request.get_json()))
