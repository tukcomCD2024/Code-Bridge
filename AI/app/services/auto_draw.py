from keras.models import load_model
import operator, os
from app.services.ImageLoading import decodeFromJsonToImage, downloadFromS3, downloadFromURL
from app.services.ImagePreprocessing import imageProcessing

tags = 'airplane,apartment,apple,arm,arrow,axe,bag,baseball,basketball,bath,bed,bench,book,bottle,box,bread,broom,brush,bucket,bulb,cake,calendar,candle,candy,castle,chair,cherry,clock,cloud,coffee,coin,cookie,cup,cylinder,dice,dress,ear,earth,envelope,eraser,eye,finger,flag,flame,flask,flower,gamecontroller,ghost,gift,grape,hammer,heart,hospital,house,industry,injection,ladder,lake,leaf,leg,lightning,meat,megaphone,mic,money,monitor,mouse,mushroom,nail,nose,officebuilding,pants,peanut,pencil,police,pumpkin,rain,ribbon,rocket,ruler,school,shield,shirt,skirt,soccer,speaker,sprout,star,stethoscope,sun,swim,sword,television,tennis,tree,truck,umbrella,vehicle,volleyball,watch,wheel,windmill,zoom'.split(
    ',')


# AI를 이용하여 유사하다고 예상되는 이미지(이름) 출력
def getPredict(img):
    saved_model = load_model(os.getcwd() + "/app/services/cnn1.h5")
    pre = saved_model.predict(img)
    return pre


# AI 결과를 정확도에 따라 오름차 순으로 정렬
def resultByDesc(result):
    x = {}
    for i in range(len(result)):
        x[i] = result[i]
    return sorted(x.items(), key=operator.itemgetter(1), reverse=True)[:6]


def getImageNameList(result):
    names = []
    for k, v in result:
        print(k,tags[k])
        names.append(tags[k])
    return names


def AI_process(image):
    imgData = imageProcessing(image)
    predict = getPredict(imgData)
    result = resultByDesc(predict[0])
    return getImageNameList(result)


def AIbyBase64(json_data):
    image = decodeFromJsonToImage(json_data)
    return AI_process(image)


def AIbyS3(json_data):
    image = downloadFromS3(json_data['bucket'], json_data['key'])
    return AI_process(image)


def AIbyURL(json_data):
    image = downloadFromURL(json_data['url'])
    return AI_process(image)
