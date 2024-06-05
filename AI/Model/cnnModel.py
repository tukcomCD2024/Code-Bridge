from keras.models import Sequential
from keras.layers import Dense, Conv2D, MaxPool2D, Flatten
from keras.preprocessing.image import ImageDataGenerator
from keras.callbacks import ModelCheckpoint, EarlyStopping
from tensorflow.keras.optimizers import RMSprop
import os

os.environ['KMP_DUPLICATE_LIB_OK'] = 'True'
imageSize = 128

trdata = ImageDataGenerator(zoom_range=[0.8, 1.3], shear_range=0.72, horizontal_flip=True, rotation_range=45)
traindata = trdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\대학교\졸업작품\project\Code-Bridge\AI\asset\image\svg",
                                       target_size=(imageSize, imageSize), class_mode='categorical', batch_size=20)
tsdata = ImageDataGenerator()
testdata = tsdata.flow_from_directory(directory=r"C:\Users\Ka\Desktop\Ka\대학교\졸업작품\project\Code-Bridge\AI\asset\image\svg", target_size=(imageSize, imageSize),
                                      class_mode='categorical')

imageCount = len(os.listdir('../asset/image/svg/'))


def cnnDepth4():
    model = Sequential()
    model.add(Conv2D(32, (3, 3), activation='relu', input_shape=(imageSize, imageSize, 3)))
    model.add(MaxPool2D((2, 2)))

    model.add(Flatten())
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=1024, activation="relu"))
    model.add(Dense(units=imageCount, activation="sigmoid"))

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
    model.add(Dense(units=imageCount, activation="sigmoid"))

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
    model.add(Dense(units=imageCount, activation="sigmoid"))

    return model


def createModel(model, lr, e, name):
    opt = RMSprop(lr=lr)
    model.compile(optimizer=opt, loss='categorical_crossentropy', metrics=['accuracy'])

    model.summary()

    checkpoint = ModelCheckpoint(name, monitor='val_accuracy', verbose=1, save_best_only=True,
                                 save_weights_only=False, mode='auto', period=1)
    early = EarlyStopping(monitor='val_accuracy', min_delta=0, patience=60, verbose=1, mode='auto')
    # hist = model.fit_generator(steps_per_epoch=len(traindata), generator=traindata, validation_data=testdata,
    #                            validation_steps=len(testdata), epochs=4, callbacks=[checkpoint, early])

    hist = model.fit(traindata, steps_per_epoch=len(traindata), validation_data=testdata, validation_steps=len(testdata),
                     epochs=e, callbacks=[checkpoint, early], batch_size=5)


for e in range(40, 100, 10):
    createModel(cnnDepth6(), 0.0001, e, f'cnn6e{e}v7.h5')
    createModel(cnnDepth5(), 0.0001, e, f'cnn5e{e}v7.h5')

# model = cnnDepth6()
# model.compile()
# model.summary()
#
# model = cnnDepth5()
# model.compile()
# model.summary()