from PIL import Image, ImageFilter
import cairosvg
import os
import numpy as np

tagsTemp = ['rabbit', 'bear', 'dog', 'cat', 'tiger', 'horse']
defaultRoute = '../asset/image/'


def getSubdirectoryList(directoryName='icon1'):
    directoryPath = defaultRoute + directoryName
    if not os.path.isdir(directoryPath):
        os.mkdir(directoryPath)
        return []
    return [f'{directoryPath}/{x}' for x in os.listdir(directoryPath)]


def getImageList(path):
    return os.listdir(path)


def convertSVGtoPNG():
    for i in getSubdirectoryList():
        for j in getImageList(i):
            imgPath = f'{i}/{j}'
            imgTo = i.replace('icon2', 'icon3')
            imgSavePath = f"{imgTo}/{j}.png"
            print(imgPath)
            print(imgSavePath)
            try:
                cairosvg.svg2png(url=imgPath, write_to=imgSavePath)
            except:
                print('fail')


def imageReformByAlpha(img):
    size = 224

    array = np.array(img)
    imageArray = []
    for i in array:
        for j in i:
            imageArray.append(255 if j[3] == 0 else 0)
    imageArray = np.resize(imageArray, [size, size])
    return Image.fromarray(imageArray)


def imageReform():
    directoryName = 'icon2'

    for i in getSubdirectoryList(directoryName):
        for imageFile in getImageList(i):
            imgPath = f'{i}/{imageFile}'

            img = Image.open(imgPath)

            resized = imageReformByAlpha(img)
            imageSavePath = f"{defaultRoute}resizedImage/{i}/{imageFile}"
            resized.save(imageSavePath)

            monochrome = resized.convert('RGB')
            reformImage = monochrome.filter(ImageFilter.BoxBlur(radius=2))
            reformImage = reformImage.convert('L')
            imageSavePath = f"{defaultRoute}reformImage/{i}/{imageFile}"
            reformImage.save(imageSavePath)


def svgImageResize():
    for i in getSubdirectoryList():
        for imageName in getImageList(i):
            imgPath = f'{i}/{imageName}'

            img = open(imgPath, 'r')
            imgSource = img.read()
            img.close()

            print(imgSource)
            imgSource = imgSource.replace('"1em"', '"224"')
            print(imgSource)

            img = open(imgPath, 'w')
            img.write(imgSource)
            img.close()


# svgImageResize
convertSVGtoPNG()
imageReform()
