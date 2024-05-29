from PIL import Image
import numpy as np

# 이미지를 흑백으로 변환
def convertMono(img: Image):
    fn = lambda x: 255 if x > 240 else 0
    img = img.convert('L').point(fn, mode='1')
    return img.convert('RGB')


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




# 이미지 크기를 128*128로 변경 후 이미지를 배열화
def resizing(img):
    img = img.resize((128, 128))
    img = img.convert('RGB')
    return img


# 이미지를 배열화
def imageToArray(img):
    img = np.array(img)
    img = np.expand_dims(img, axis=0)
    return img


# 이미지를 AI에 맞게 전환
def imageProcessing(img: Image):
    img = convertMono(img)
    img = trim_white_borders(img)
    img = resizing(img)
    img = imageToArray(img)
    return img
