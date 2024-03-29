import numpy as np
from PIL import Image
from keras.models import load_model
import operator

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


def getPredict(img):
    saved_model = load_model("vgg16_softmax_sigmoid_RMS.h5")
    pre = saved_model.predict(img)

    return pre


def imageMatching():
    image = getImage()
    predict = getPredict(image)
    result = resultByDesc(predict[0])
    print(result)
