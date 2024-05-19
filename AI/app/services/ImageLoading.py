import io, base64
from PIL import Image
import boto3
import requests


def decodeFromJsonToImage(json_data):
    img = json_data['img']
    img = base64.b64decode(img)
    return Image.open(io.BytesIO(img))


def downloadFromS3(bucket, key):
    AWS_ACCESS_KEY_ID = "ACCESS_KEY"
    AWS_SECRET_ACCESS_KEY = "SECRET_ACCESS_KEY"
    AWS_DEFAULT_REGION = "리전 코드"
    client = boto3.client('s3',
                          aws_access_key_id=AWS_ACCESS_KEY_ID,
                          aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
                          region_name=AWS_DEFAULT_REGION)

    file_name = 'downLoad.png'  # 다운될 이미지 이름
    # bucket = 'mufi-photo'  # 버켓 주소
    # key = 'test.jpg'  # s3 이미지

    client.download_file(bucket, key, file_name)
    return Image.open(file_name)


def downloadFromURL(url):
    # request.get 요청
    res = requests.get(url)

    # Img open
    return Image.open(io.BytesIO(res.content))
