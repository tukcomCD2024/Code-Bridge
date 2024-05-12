from keras.models import load_model
import numpy as np
from PIL import Image
import operator, os
from app.services.LoadImage import decodeFromJsonToImage, downloadFromS3, downloadFromURL

# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample\heart.png"


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    listByDesc = sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:6]
    return listByDesc


def getImage():
    img = Image.open(imgsrc)
    img = img.resize((224, 224))
    img = img.convert("RGB")
    img = np.asarray(img)
    img = np.expand_dims(img, axis=0)

    return img


def resize(image):
    image = image.resize((128, 128))
    image = np.array(image)
    image = np.expand_dims(image, axis=0)
    return image


def getPredict(img):
    saved_model = load_model(os.getcwd() + "/app/services/cnn1.h5")
    pre = saved_model.predict(img)
    return pre


def AI_process(image):
    resized = resize(image)
    pre_result = getPredict(resized)
    return resultByDesc(pre_result)

def AI_by_Base64(json_data):
    image = decodeFromJsonToImage(json_data)
    return AI_process(image)

def AI_by_S3(json_data):
    image = downloadFromS3(json_data['bucket'], json_data['key'])
    return AI_process(image)

def AI_by_URL(json_data):
    image = downloadFromURL(json_data['url'])
    return AI_process(image)
