import json

from keras.models import load_model
import numpy as np
from PIL import Image
import operator, os
from app.services.LoadImage import readFromJsonToImage, downloadFromS3

# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample\heart.png"


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    listByDesc = sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40]
    return listByDesc


def getImage():
    img = Image.open(imgsrc)
    img = img.resize((224, 224))
    img = img.convert("RGB")
    img = np.asarray(img)
    img = np.expand_dims(img, axis=0)

    return img


def resize(image):
    image = image.resize((224, 224))
    image = np.array(image)
    image = np.expand_dims(image, axis=0)
    return image


def getPredict(img):
    saved_model = load_model(os.getcwd() + "/app/services/vgg16_sigmoid_RMS.h5")
    pre = saved_model.predict(img)

    return pre



def AI(json_data):
    dict_data = json.loads(json_data)
    type = dict_data['type']
    if type == "S3":
        image = downloadFromS3(dict_data['bucket'], dict_data['key'])
        pass
    elif type == "base64":
        image = readFromJsonToImage(json_data)
        pass
    preResult = getPredict(resize(image))
    return resultByDesc(preResult)


