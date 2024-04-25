import os

import numpy as np

models = ['30cnn4Depth32.h5', 'cnn4-2Depth32.h5', '30cnn5Depth32.h5', '30cnn6Depth32.h5','512cnn4Depth32.h5', '512cnn5Depth32.h5', '512cnn6Depth32.h5']


def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    print(x)
    import operator
    return sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40]


from PIL import Image

# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r" "

img = Image.open(imgsrc)
img = img.resize((128, 128))
img = img.convert("RGB")
img = np.asarray(img)
img = np.expand_dims(img, axis=0)
from keras.models import load_model

for i in os.listdir('./'):
    print(i)
    if not '.h5' in i:
        continue

    saved_model = load_model(i)
    output = saved_model.predict(img)

    print(resultByDesc(output[0]))
    print()