from PIL import Image, ImageFilter, ImageOps
import cairosvg
import os
import numpy as np

tagsTemp = ['rabbit', 'bear', 'dog', 'cat', 'tiger', 'horse']
defaultRoute = "../asset/image/svg"


def getImageList(path):
    return os.listdir(path)


def convertSVGtoPNG():
    for dir, subdir, files in os.walk(defaultRoute):
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
            fn = lambda x: 255 - x if x > 0 else 0
            image = ImageOps.invert(image)
            image = image.convert('L')
            image.save(imgpath.replace('animalsMono', 'animalsFilter'))


def removeAlpha(img):
    size = 224
    img = img.convert("RGBA")
    img = img.resize((224, 224))

    array = np.array(img)
    imageArray = []
    for i in array:
        for j in i:
            imageArray.append(255 if j[3] == 0 or j[0] == 255 else 0)
    imageArray = np.resize(imageArray, [size, size])
    return Image.fromarray(imageArray)

def trim_white_borders(image, threshold=240):
    # 이미지 불러오기
    image_np = np.array(image)

    # 흰색(또는 거의 흰색) 픽셀 마스크 생성
    if image_np.ndim == 3:  # RGB 이미지
        mask = np.all(image_np > threshold, axis=-1)
    else:  # 흑백 이미지
        mask = image_np > threshold

    coords = np.argwhere(~mask)

    # 흰색이 아닌 픽셀의 최소/최대 좌표 찾기
    if coords.size == 0:
        raise ValueError("The image is completely white!")

    y_min, x_min = coords.min(axis=0)
    y_max, x_max = coords.max(axis=0) + 1  # 슬라이싱을 위해 +1

    # 이미지 자르기
    trimmed_image = image_np[y_min:y_max, x_min:x_max]

    # 잘라낸 이미지 저장
    trimmed_pil_image = Image.fromarray(trimmed_image)
    return trimmed_pil_image


def imageReform():
    for dir, subdir, files in os.walk(defaultRoute):
        for imageFile in files:
            imgPath = f'{dir}/{imageFile}'

            img = Image.open(imgPath)

            resized = removeAlpha(img).convert('L')
            trimImage = trim_white_borders(resized)
            trimImage.save(f'{dir}/{imageFile}')


def svgImageResize():
    for dir, subdir, files in os.walk(defaultRoute):
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



imageReform()

# convertColor2Mono()
# boldLine()

# for dir, subdir, files in os.walk(defaultRoute):
#     print(dir, subdir, files)
