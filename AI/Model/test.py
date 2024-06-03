import os
import numpy as np
from PIL import Image
from keras.models import load_model


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    import operator
    return sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40]


# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample2\{}.png"
images = {'heart': 54, 'heart2': 54, 'cloud': 31, 'sword': 96, 'skirt': 86, 'star': 92, 'star2': 92, 'bread': 15, 'bread2': 15, 'candy': 24,
          'flask': 47, 'dice': 37, 'dice2': 37, 'house': 56, 'house2': 56, 'cylinder2': 36, 'cylinder3': 36, 'school': 83, 'school2': 83, }


def imagePredictMono():
    for i in os.listdir('./'):
        if not '.h5' in i:
            continue
        print(i)

        saved_model = load_model(i)

        for key in images.keys():
            img = Image.open(imgsrc.format(key))
            img = img.resize((128, 128))
            img = img.convert("RGB")
            img = np.array(img)
            img = np.expand_dims(img, axis=0)

            output = saved_model.predict(img)

            result = resultByDesc(output[0])[:8]
            for j in range(5):
                try:
                    if images[key] in result[j]:
                        print(key, j)
                        break
                except:
                    print("outOfBound")
                    break


def imagePredictPoly(models):
    for key in images.keys():
        img = Image.open(imgsrc.format(key))
        img = img.resize((128, 128))
        img = img.convert("RGB")
        img = np.array(img)
        img = np.expand_dims(img, axis=0)

        total = {}

        for m in models:
           result = resultByDesc(m.predict(img)[0])[:6]
           for r in result:
               if r in total.keys():
                   total[r] += 1
               else:
                   total[r] = 1
        result = resultByDesc(result)

        print(result)

imagePredictMono()
