import keras, os
import numpy
from keras.models import Sequential
from keras.layers import Dense, Conv2D, MaxPool2D, Flatten
import numpy as np
from keras.preprocessing.image import ImageDataGenerator
import os

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
trdata = ImageDataGenerator(validation_split=0.2)
traindata = trdata.flow_from_directory(directory=f"../asset/image/animals", target_size=(224, 224), subset='training', class_mode='categorical', batch_size=20)
tsdata = ImageDataGenerator(validation_split=0.2)
testdata = tsdata.flow_from_directory(directory=f"../asset/image/animals", target_size=(224, 224), subset='validation', class_mode='categorical')


# traindata = ImageDataGenerator.train_generator
# testdata = ImageDataGenerator.test_generator


def vgg16():
    model = Sequential()
    model.add(Conv2D(input_shape=(224, 224, 3), filters=64, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=64, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(MaxPool2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Conv2D(filters=128, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=128, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(MaxPool2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Conv2D(filters=256, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=256, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=256, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(MaxPool2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(MaxPool2D(pool_size=(2, 2), strides=(2, 2)))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(Conv2D(filters=512, kernel_size=(3, 3), padding="same", activation="relu"))
    model.add(MaxPool2D(pool_size=(2, 2), strides=(2, 2)))

    model.add(Flatten())
    model.add(Dense(units=4096, activation="relu"))
    model.add(Dense(units=4096, activation="relu"))
    model.add(Dense(units=90, activation="softmax"))

    return model


model = vgg16()

from keras.optimizers import SGD

opt = SGD(lr=0.1)
model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy', 'categorical_crossentropy'])

model.summary()

from keras.callbacks import ModelCheckpoint, EarlyStopping

checkpoint = ModelCheckpoint("vgg16_1.h5", monitor='val_accuracy', verbose=1, save_best_only=True,
                             save_weights_only=False, mode='auto', period=1)
early = EarlyStopping(monitor='val_accuracy', min_delta=0, patience=12, verbose=1, mode='auto')
# hist = model.fit_generator(steps_per_epoch=len(traindata), generator=traindata, validation_data=testdata,
#                            validation_steps=len(testdata), epochs=4, callbacks=[checkpoint, early])

hist = model.fit(traindata, steps_per_epoch=traindata.samples//20, validation_data=testdata, validation_steps=len(testdata),
                 epochs=8, callbacks=[checkpoint, early])
try:
    m = model.predict(testdata)

    print('====', numpy.argmax(m, axis=1), '====')
    print('====', m, '====')
    print(len(m))
except:
    print('error')

import matplotlib.pyplot as plt

plt.plot(hist.history["accuracy"])
plt.plot(hist.history['val_accuracy'])
plt.plot(hist.history['loss'])
plt.plot(hist.history['val_loss'])
plt.title("model accuracy")
plt.ylabel("Accuracy")
plt.xlabel("Epoch")
plt.legend(["Accuracy", "Validation Accuracy", "loss", "Validation Loss"])
plt.show()

# import tensorflow as tf
# gpus = tf.config.experimental.list_physical_devices('GPU')
# tf.config.experimental.set_memory_growth(gpus[0], True)

# 1. dense unit = 6, fit
# 2. dense unit = 1, fit
# 3. dense unit = 1, fit_generate
# 4. dense unit = 6, fit_generate
# 4-1. dense unit = 6, fit_generate
