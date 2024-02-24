from keras.preprocessing.image import ImageDataGenerator
import matplotlib.pylab as plt

data_file_path = "../asset/image/sample/"

train_data_generator = ImageDataGenerator(
    rescale=1. / 255,
    featurewise_center=False,  # set input mean to 0 over the dataset
    samplewise_center=False,  # set each sample mean to 0
    featurewise_std_normalization=False,  # divide inputs by std of the dataset
    samplewise_std_normalization=False,  # divide each input by its std
    zca_whitening=False,  # apply ZCA whitening
    rotation_range=0,  # randomly rotate images in the range (degrees, 0 to 180)
    width_shift_range=0.2,  # randomly shift images horizontally (fraction of total width)
    height_shift_range=0.2,  # randomly shift images vertically (fraction of total height)
    zoom_range=[0.5, 1.2],
    horizontal_flip=True,  # randomly flip images
    vertical_flip=True,)  # randomly flip images

test_data_generator = ImageDataGenerator(rescale=1. / 255)

train_generator = train_data_generator.flow_from_directory(
    data_file_path + "/reformImage",
    target_size=(224, 224),
    batch_size=1,
    class_mode='binary')

test_generator = test_data_generator.flow_from_directory(
    data_file_path + "/resizedImage",
    target_size=(224, 224),
    batch_size=1,
    class_mode='binary')

x_train, y_train = train_generator.next()
for idx in range(len(x_train)):
    print(x_train[idx].shape)
    print(y_train[idx])
    plt.imshow(x_train[idx])
    plt.show()

