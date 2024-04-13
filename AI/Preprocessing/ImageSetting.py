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
    for dir, subdir, files in os.walk("../asset/image/icon1"):
        # imgTo = i.replace('svg', 'png')
        for j in files:
            imgPath = f'{dir}/{j}'
            imgSavePath = f"{dir}/{j[:-4]}.png"
            if '.png' in j:
                print("png file")
                continue
            try:
                cairosvg.svg2png(url=imgPath, write_to=imgSavePath)
                os.remove(imgPath)
            except:
                print('fail')


def convertColor2Mono():
    for dir, subdirs, files in os.walk('../asset/image/animals'):
        # imgTo = dir.replace('animals', 'monoAnimals')
        # if not os.path.isdir(imgTo):
        #     os.mkdir(imgTo)
        for j in files:
            imgPath = f'{dir}/{j}'
            # imgSavePath = f"{imgTo}/{j}"

            image = Image.open(imgPath)
            image = image.filter(ImageFilter.FIND_EDGES)
            image = image.filter(ImageFilter.SHARPEN)
            image = image.convert("L")
            image = image.convert("RGB")
            image = image.resize((224, 224))

            # image.save(imgSavePath)
            image.save(imgPath)


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
    img = img.convert("RGBA")

    array = np.array(img)
    imageArray = []
    for i in array:
        for j in i:
            imageArray.append(255 if j[3] == 0 else 0)
    imageArray = np.resize(imageArray, [size, size])
    return Image.fromarray(imageArray)


def imageReform():
    directoryName = 'png'

    for dir, subdir, files in os.walk("../asset/image/icon1"):
        for imageFile in files:
            imgPath = f'{dir}/{imageFile}'

            img = Image.open(imgPath)

            resized = imageReformByAlpha(img).convert('L')
            imageSavePath = dir.replace(directoryName, 'resizedImage')
            # imageSavePath = f"{defaultRoute}resizedImage/{i}/{imageFile}"
            resized.save(f'{dir}/{imageFile}')

            # monochrome = resized.convert('RGB')
            # reformImage = monochrome.filter(ImageFilter.BoxBlur(radius=2))
            # reformImage = reformImage.convert('L')
            # imageSavePath = dir.replace(directoryName, 'reformImage')
            # # imageSavePath = f"{defaultRoute}reformImage/{i}/{imageFile}"
            # reformImage.save(f'{imageSavePath}/{imageFile}')


def svgImageResize():
    for dir, subdir, files in os.walk("../asset/image/icon1"):
        for imageName in files:
            imgPath = f'{dir}/{imageName}'

            if not '.svg' in imageName:
                continue
            img = open(imgPath, 'r')
            imgSource = img.read()
            img.close()

            print(imgSource)
            imgSource = imgSource.replace('"1em"', '"224"')
            print(imgSource)

            img = open(imgPath, 'w')
            img.write(imgSource)
            img.close()


svgImageResize()
convertSVGtoPNG()
imageReform()

# convertColor2Mono()
# boldLine()

# for dir, subdir, files in os.walk(defaultRoute):
#     print(dir, subdir, files)
