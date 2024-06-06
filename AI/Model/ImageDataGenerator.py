from keras.preprocessing.image import ImageDataGenerator
import matplotlib.pylab as plt

data_file_path = r"C:\Users\Ka\Desktop\Ka\대학교\졸업작품\project\Code-Bridge\AI\asset\image"

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
    zoom_range=[0.7, 1.1],
    horizontal_flip=True,  # randomly flip images
    vertical_flip=False,
    validation_split=0.1)  # randomly flip images

test_data_generator = ImageDataGenerator(rescale=1. / 255)

train_generator = train_data_generator.flow_from_directory(
    data_file_path + "/animals",
    target_size=(224, 224),
    batch_size=3,
    class_mode='categorical',
    subset='training')

test_generator = train_data_generator.flow_from_directory(
    data_file_path + "/animals",
    target_size=(224, 224),
    batch_size=3,
    class_mode='categorical',
    subset='validation')

# x_train, y_train = train_generator.next()
# for idx in range(len(x_train)):
#     print(x_train[idx].shape)
#     print(y_train[idx])
#     plt.imshow(x_train[idx])
#     plt.show()
#
# print(len(x_train))
# print(train_generator.n)
#
# for i in range(40):
#     x_train, y_train = train_generator.next()
#     for idx in range(len(x_train)):
#         # print(x_train[idx].shape)
#         # print(y_train[idx])
#         plt.imshow(x_train[idx])
#         plt.show()
