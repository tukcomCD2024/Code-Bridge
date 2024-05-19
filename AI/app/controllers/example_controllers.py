from flask import Blueprint, request, jsonify
from app.services import auto_draw
from app.services import example_service

bp = Blueprint(name='example',
               import_name=__name__,
               url_prefix='/example')

ai = Blueprint(name='draw',
               import_name=__name__,
               url_prefix='/draw')

@bp.route('/', methods=['GET'])
def mroute() -> str:
    data = 'hello world'
    result = example_service.mroute(data=data)
    return jsonify(result=result)
#
# @bp.route('/<int:user_number>', methods=['GET'])
# def mroute_add_param(user_number: int) -> str:
#     result = example_service.mroute_add_param(user_number)
#     return jsonify(result=result)

@ai.route('/', methods=['GET', 'POST'])
def draw()->str:
    return jsonify(auto_draw.AI(request.get_json()))
