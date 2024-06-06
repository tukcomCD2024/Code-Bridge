import os
import shutil


def getImageListByHash(path):
    img = [{}]  # imagesWithTag
    for i in os.listdir(path):
        if os.path.isdir(f'{path}/{i}'):
            img[0][i] = getImageListByHash(f'{path}/{i}')
        else:
            img.append(i)
    if len(img[0]) == 0:
        img.pop(0)
    if len(img) == 1:
        img = img[0]
    return img


# print(getImageListByHash('../asset/image/sample/svg/'))

def imageCategorical():
    src = 'C:/Users/Ka/Desktop/Ka/programming/AI/AI2/asset/size224Image01'
    for routes, dirs, files in os.walk(src):
        print(routes, files, dirs)
        for image in files:
            os.mkdir(routes + '/' + image[:-4])
            shutil.move(routes + '/' + image, routes + '/' + image[:-4] + '/' + image)


imageCategorical()
