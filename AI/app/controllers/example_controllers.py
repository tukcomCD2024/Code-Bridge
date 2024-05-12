from flask import Blueprint, request, jsonify, Response
from app.services import auto_draw
import json

bp = Blueprint(name='example',
               import_name=__name__,
               url_prefix='/example')

ai = Blueprint(name='ai',
               import_name=__name__,
               url_prefix='/ai')


@ai.route('/base64', methods=['POST'])
def draw()->str:
    return jsonify(auto_draw.AIbyBase64(json.loads(request.get_json())))

@ai.route('s3', methods=['POST'])
def drawByS3()-> Response:#str?????????????????????????????????????????????
    return jsonify(auto_draw.AIbyS3(json.loads(request.get_json())))

@ai.route('url', methods=['POST'])
def drawByS3()-> Response:
    return jsonify(auto_draw.AIbyURL(json.loads(request.get_json())))
