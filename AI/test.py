from PIL import Image
import requests
import json
import base64
import cv2, os, io, numpy as np
import urllib.request
from app.services.ImageProcessing import imageProcessing


image_name = r'C:\Users\Ka\Desktop\Ka\programming\AI\s3sample.png'
img = cv2.imread(image_name)
jpg_img = cv2.imencode('.png', img)
b64_string = base64.b64encode(jpg_img[1]).decode('utf-8')

files = {
            "img": b64_string,
        }
file = {'url':'https://flaskbuckettest.s3.ap-northeast-2.amazonaws.com/free-icon-font-bolt-6853834.png'}
r = requests.post('http://127.0.0.1:5000/ai/url', json=json.dumps(file))
print((json.dumps({'url':''})))

# r = app.services.auto_draw.AIbyURL(json.loads(json.dumps(file)))
print(r)