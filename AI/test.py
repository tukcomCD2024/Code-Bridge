from PIL import Image
import requests
import json
import base64
import cv2
import io


image_name = 'test.png'
img = cv2.imread(image_name)
jpg_img = cv2.imencode('.png', img)
b64_string = base64.b64encode(jpg_img[1]).decode('utf-8')

files = {
            "img": b64_string,
        }
print(files)
r = requests.post('http://127.0.0.1:5000/draw', json=json.dumps(files))

print(r)