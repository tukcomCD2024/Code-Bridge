import numpy as np
def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        if result[i] > 0.001:
            x[i] = result[i]

    print(x)
    import operator
    print(sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:40])

from PIL import Image
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\bank\bank.png"
# imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size64Image01\security\security.png"
imgsrc = r"C:\Users\Ka\Desktop\Ka\programming\AI\sample\heart.png"

img = Image.open(imgsrc)
img = img.resize((224,224))
img = img.convert("RGB")
img = np.asarray(img)
img = np.expand_dims(img, axis=0)
from keras.models import load_model
saved_model = load_model("vgg16_softmax.h5")
output = saved_model.predict(img)

print(output)

resultByDesc(output[0])
