import os
from PIL import Image

import numpy as np


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    import operator
    return sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40]


# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample\{}.png"
images = {'arrow': 4, 'heart': 51, 'heart2': 51, 'cloud': 28, 'sword': 91, 'skirt': 83, 'star': 87, 'star2': 87, 'bread':17, 'bread2':17, 'candy':26}

from keras.models import load_model

for i in os.listdir('./'):
    if not 'f32' in i:
        continue
    print(i)

    saved_model = load_model(i)

    for i in images.keys():
        img = Image.open(imgsrc.format(i))
        img = img.resize((128, 128))
        img = img.convert("RGB")
        img = np.array(img)
        img = np.expand_dims(img, axis=0)

        output = saved_model.predict(img)

        result = resultByDesc(output[0])[:8]
        for j in range(5):
            try:
                if images[i] in result[j]:
                    print(i, j)
                    break
            except:
                break
