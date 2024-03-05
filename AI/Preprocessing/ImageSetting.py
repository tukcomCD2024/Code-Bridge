from PIL import Image, ImageFilter, ImageOps
import cairosvg
import os
import numpy as np

tagsTemp = ['rabbit', 'bear', 'dog', 'cat', 'tiger', 'horse']
defaultRoute = '../asset/image/sample2/'


def getSubdirectoryList(defaultRoute=defaultRoute, directoryName='svg'):
    directoryPath = defaultRoute + directoryName
    if not os.path.isdir(directoryPath):
        os.mkdir(directoryPath)
        return []
    return [f'{directoryPath}/{x}' for x in os.listdir(directoryPath)]


def getImageList(path):
    return os.listdir(path)


def convertSVGtoPNG():
    for i in getSubdirectoryList():
        imgTo = i.replace('svg', 'png')
        for j in getImageList(i):
            imgPath = f'{i}/{j}'
            imgSavePath = f"{imgTo}/{j[:-4]}.png"
            try:
                cairosvg.svg2png(url=imgPath, write_to=imgSavePath)
            except:
                print('fail')


def convertColor2Mono():
    for i in getSubdirectoryList('../asset/image/', 'animals'):
        imgTo = i.replace('animals', 'monoAnimals')
        if not os.path.isdir(imgTo):
            os.mkdir(imgTo)
        for j in getImageList(i):
            imgPath = f'{i}/{j}'
            imgSavePath = f"{imgTo}/{j}"

            image = Image.open(imgPath)
            image = image.filter(ImageFilter.FIND_EDGES)
            image = image.filter(ImageFilter.SHARPEN)
            image = image.convert("L")
            image = image.convert("RGB")
            image = image.resize((224, 224))

            image.save(imgSavePath)


def boldLine():
    for dir, subdir, files in os.walk("../asset/image/animalsMono"):
        directory = dir.replace('animalsMono', 'animalsFilter')
        if not os.path.isdir(directory):
            os.mkdir(directory)

        for img in files:
            imgpath = f'{dir}/{img}'
            image = Image.open(imgpath)
            fn = lambda x: 255-x if x > 0 else 0
            image = ImageOps.invert(image)
            image = image.convert('L')
            image.save(imgpath.replace('animalsMono', 'animalsFilter'))

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
    directoryName = 'png'

    for i in getSubdirectoryList(directoryName):
        for imageFile in getImageList(i):
            imgPath = f'{i}/{imageFile}'

            img = Image.open(imgPath)

            resized = imageReformByAlpha(img).convert('L')
            imageSavePath = i.replace(directoryName, 'resizedImage')
            # imageSavePath = f"{defaultRoute}resizedImage/{i}/{imageFile}"
            resized.save(f'{imageSavePath}/{imageFile}')

            monochrome = resized.convert('RGB')
            reformImage = monochrome.filter(ImageFilter.BoxBlur(radius=2))
            reformImage = reformImage.convert('L')
            imageSavePath = i.replace(directoryName, 'reformImage')
            # imageSavePath = f"{defaultRoute}reformImage/{i}/{imageFile}"
            reformImage.save(f'{imageSavePath}/{imageFile}')


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


# svgImageResize()
# convertSVGtoPNG()
# imageReform()

# convertColor2Mono()
boldLine()

for dir, subdir, files in os.walk(defaultRoute):
    print(dir, subdir, files)
