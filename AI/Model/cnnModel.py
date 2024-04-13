from keras.models import Sequential
from keras.layers import Dense, Conv2D, MaxPool2D, Flatten
from keras.preprocessing.image import ImageDataGenerator
import os
from keras.callbacks import ModelCheckpoint, EarlyStopping

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
imageSize = 128

trdata = ImageDataGenerator(zoom_range=[0.9, 1.3], height_shift_range=0.3, width_shift_range=0.2, horizontal_flip=True, rotation_range=0.3, validation_split=0.1)
traindata = trdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\대학교\졸업작품\project\Code-Bridge\AI\asset\image\icon1",
                                       target_size=(imageSize, imageSize),
                                       class_mode='categorical', batch_size=20)
tsdata = ImageDataGenerator()
testdata = tsdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\대학교\졸업작품\project\Code-Bridge\AI\asset\image\icon1", target_size=(imageSize, imageSize),
                                      class_mode='categorical')


def cnnDepth3():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(imageSize, imageSize, 3)))
    model.add(MaxPool2D((2, 2)))

    model.add(Flatten())
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=116, activation="sigmoid"))

    return model


def cnnDepth4():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(imageSize, imageSize, 3)))
    model.add(MaxPool2D((2, 2)))

    model.add(Flatten())
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=116, activation="sigmoid"))

    return model


def cnnDepth5():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(imageSize, imageSize, 3)))
    model.add(MaxPool2D((2, 2)))
    model.add(Conv2D(64, (3, 3), activation='relu'))
    model.add(MaxPool2D((2, 2)))

    model.add(Flatten())
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=116, activation="sigmoid"))

    return model


def cnnDepth6():
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
    model.add(Dense(units=116, activation="sigmoid"))

    return model


model = cnnDepth3()

from keras.optimizers import RMSprop

opt = RMSprop(lr=0.001)
model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])

model.summary()

checkpoint = ModelCheckpoint("cnn3Depth32.h5", monitor='val_accuracy', verbose=1, save_best_only=True,
                             save_weights_only=False, mode='auto', period=1)
early = EarlyStopping(monitor='val_accuracy', min_delta=0, patience=20, verbose=1, mode='auto')
# hist = model.fit_generator(steps_per_epoch=len(traindata), generator=traindata, validation_data=testdata,
#                            validation_steps=len(testdata), epochs=4, callbacks=[checkpoint, early])

hist = model.fit(traindata, steps_per_epoch=len(traindata), validation_data=testdata, validation_steps=len(testdata),
                 epochs=30, callbacks=[checkpoint, early], batch_size=5)

import matplotlib.pyplot as plt

plt.plot(hist.history["accuracy"])
plt.plot(hist.history['val_accuracy'])
plt.plot(hist.history['val_loss'])
plt.title("model accuracy")
plt.ylabel("Accuracy")
plt.xlabel("Epoch")
plt.ylim(0, 5)
plt.legend(["Accuracy", "Validation Accuracy", "Validation Loss"])
plt.show()
