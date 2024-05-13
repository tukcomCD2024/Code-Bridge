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
def draw() -> str:
    return jsonify(auto_draw.AIbyBase64(json.loads(request.get_json())))


@ai.route('/s3', methods=['POST'])
def drawByS3() -> str:  # str?????????????????????????????????????????????
    return jsonify(auto_draw.AIbyS3(json.loads(request.get_json())))


@ai.route('/url', methods=['POST'])
def drawByURL() -> str:
    s = request.get_data()
    l = json.loads(s)
    return jsonify(auto_draw.AIbyURL(l))


String.format("https://ap-northeast-2.console.aws.amazon.com/s3/object/ai-icons?region=ap-northeast-2&bucketType=general&prefix=svg/{}/{}-outline.png", 이름, 이름)