from PIL import Image
import numpy as np

# 이미지를 흑백으로 변환
def convertMono(img: Image):
    fn = lambda x: 255 if x > 0 else 0
    img = img.convert('L').point(fn, mode='1')
    return img


# 이미지 크기를 128*128로 변경 후 이미지를 배열화
def resizing(img):
    img = img.resize((128, 128))
    img = img.convert('RGB')
    return img


# 이미지를 배열화
def imageToArray(img):
    img = np.array(img)
    img = np.expand_dims(img, axis=0)
    return img


# 이미지를 AI에 맞게 전환
def imageProcessing(img: Image):
    img = convertMono(img)
    img = resizing(img)
    img = imageToArray(img)
    return img
