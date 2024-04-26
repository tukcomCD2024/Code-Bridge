from flask import Blueprint, request, jsonify, Response
from app.services import auto_draw

bp = Blueprint(name='example',
               import_name=__name__,
               url_prefix='/example')

ai = Blueprint(name='ai',
               import_name=__name__,
               url_prefix='/ai')


@ai.route('/base64', methods=['POST'])
def draw()->str:
    return jsonify(auto_draw.AI(request.get_json()))

@ai.route('s3', methods=['POST'])
def drawByS3()-> Response:#str?????????????????????????????????????????????
    return jsonify((auto_draw.AI(request.get_json())))