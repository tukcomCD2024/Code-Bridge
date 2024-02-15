import os


# 그냥 연습삼아 만들어본 코드
# 안쓰면 나중에 지우셈
def getAllImage(path='../asset/image/icon1'):
    print('directory path:', path)

    for i in os.listdir(path):
        if os.path.isdir(f'{path}/{i}'):
            print(f'sub', end='')
            getAllImage(f'{path}/{i}')
        else:
            print(i)
    print('-' * 4, 'directory over', '-' * 4)
    print()


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


print(getImageListByHash('../asset/image/icon1/'))
