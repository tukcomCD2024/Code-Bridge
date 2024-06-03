import os
import operator
import numpy as np
from PIL import Image
from keras.models import load_model


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    return sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40]


# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample2\{}.png"
images = {'heart': 54, 'heart2': 54, 'cloud': 31, 'sword': 96, 'skirt': 86, 'star': 92, 'star2': 92, 'bread': 15, 'bread2': 15, 'candy': 24,
          'flask': 47, 'dice': 37, 'dice2': 37, 'house': 56, 'house2': 56, 'cylinder2': 36, 'cylinder3': 36, 'school': 83, 'school2': 83, }
modellist = ("cnn5e58v2.h5,cnn5e59v3.h5,cnn5e80vc.h5,cnn5f32e60u1024.h5,cnn6e40vc.h5,cnn6e47v2.h5,cnn6e50v3.h5,cnn6e50vc.h5,cnn6e51v2.h5,cnn6e57v2.h5,cnn6e5" +
             "9v2.h5,cnn6e60v3.h5,cnn6e61v3.h5,cnn6e62v2.h5,cnn6e63v2.h5,cnn6e63v3.h5,cnn6e65v3.h5,cnn6e66v2.h5,cnn6e67v2.h5,cnn6e68v2.h5,cnn6e80v3.h5").split(
    ',')


def allModels():
    models = []
    for m in modellist:
        models.append(load_model(m))
    return models


def imagePredictMono():
    for i in modellist:
        if not '.h5' in i:
            continue
        print(i)

        saved_model = load_model("./" + i)

        total = 0
        correct = 0
        for key in images.keys():
            total += 40

            img = Image.open(imgsrc.format(key))
            img = img.resize((128, 128))
            img = img.convert("RGB")
            img = np.array(img)
            img = np.expand_dims(img, axis=0)

            output = saved_model.predict(img)

            result = resultByDesc(output[0])[:40]
            for j in range(40):
                try:
                    if images[key] in result[j]:
                        # print(key, j)
                        total += j
                        total -= 40
                        correct += 1
                        break
                except:
                    # print("outOfBound")
                    break

        print("total:", total)
        # print("correct:", correct)


def imagePredictPoly(models):
    for key in images.keys():
        img = Image.open(imgsrc.format(key))
        img = img.resize((128, 128))
        img = img.convert("RGB")
        img = np.array(img)
        img = np.expand_dims(img, axis=0)

        total1 = {}
        total2 = {}

        for m in models:
            predict = m.predict(img)[0]
            result = resultByDesc(predict)[:40]
            for r in result:
                i = r[0]
                if i in total1.keys():
                    total1[i] += 1
                    total2[i] += result.index(r)
                else:
                    total1[i] = 1
                    total2[i] = result.index(r)
        # result = resultByDesc(result)

        result1 =  sorted(total1.items(), key=operator.itemgetter(1), reverse=True)[:40]
        result2 =  sorted(total2.items(), key=operator.itemgetter(1), reverse=True)[:40]
        print(key, images[key])
        print(result1)
        print(result2)


# imagePredictMono()
imagePredictPoly(allModels())