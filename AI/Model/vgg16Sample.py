from keras.models import Sequential
from keras.layers import Dense, Conv2D, MaxPool2D, Flatten
import numpy as np
from keras.preprocessing.image import ImageDataGenerator
import os

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
imageSize = 128

trdata = ImageDataGenerator(zoom_range=[0.9, 1.3], height_shift_range=0.3, width_shift_range=0.2, horizontal_flip=True, rotation_range=0.1)
traindata = trdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size224Image01", target_size=(imageSize, imageSize), class_mode='categorical', batch_size=20)
tsdata = ImageDataGenerator()
testdata = tsdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\programming\AI\AI2\asset\size224Image01", target_size=(imageSize, imageSize),  class_mode='categorical')

def cnnSample():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(imageSize, imageSize, 3)))
    model.add(MaxPool2D((2, 2)))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(MaxPool2D((2, 2)))
    model.add(Conv2D(128, (3, 3), activation='relu'))
    model.add(MaxPool2D((2, 2)))

    model.add(Flatten())
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=129, activation="sigmoid"))

    return model


model = cnnSample()

from keras.optimizers import RMSprop

opt = RMSprop(lr=0.001)
model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])

model.summary()

from keras.callbacks import ModelCheckpoint, EarlyStopping

checkpoint = ModelCheckpoint("vgg16_sigmoid.h5", monitor='val_accuracy', verbose=1, save_best_only=True,
                             save_weights_only=False, mode='auto', period=1)
early = EarlyStopping(monitor='val_accuracy', min_delta=0, patience=20, verbose=1, mode='auto')
# hist = model.fit_generator(steps_per_epoch=len(traindata), generator=traindata, validation_data=testdata,
#                            validation_steps=len(testdata), epochs=4, callbacks=[checkpoint, early])

hist = model.fit(traindata, steps_per_epoch=len(traindata), validation_data=testdata, validation_steps=len(testdata),
                 epochs=120, callbacks=[checkpoint, early], batch_size=5)

import matplotlib.pyplot as plt

plt.plot(hist.history["accuracy"])
plt.plot(hist.history['val_accuracy'])
plt.plot(hist.history['val_loss'])
plt.title("model accuracy")
plt.ylabel("Accuracy")
plt.xlabel("Epoch")
plt.legend(["Accuracy", "Validation Accuracy", "Validation Loss"])
plt.show()
