import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By
import os
from urllib.request import urlretrieve
from PIL import Image, ImageFilter
import cairosvg


category = ['fluent-emoji-high-contrast/', 'ph/', 'mdi/', 'material-symbols-light/', 'bi/', 'teenyicons/', 'clarity/', 'ci/', 'icon-park-outline/', 'mingcute/', 'tabler/']
additional = {'fluent/': 'regular', 'healthicons/': 'outline 24'}
tagsTemp = ['rabbit', 'bear', 'dog', 'cat', 'tiger','horse']

url = "https://icon-sets.iconify.design/{}/?query={}"

cd_installer.install()

driver = webdriver.Chrome()


def timeout():
    print("next")


def getIconCount():
    try:
        icons = driver.find_elements(By.XPATH, '//*[@id="app"]/div[2]/div/div/div[2]/div[2]/div/div/div[1]/a/iconify-icon')
        return len(icons)
    except:
        return 0


def scanIcon(webURL, imgFolder, pageNum, from0, to0):
    iconPath = '//*[@id="viewport"]/div[6]/div/section[4]/ul/li[{}]/div/a'
    imagePath = '//*[@id="uicons__detail-img"]'

    driver.get(webURL.format(pageNum + 1))

    for j in range(from0, to0):
        driver.find_element(By.XPATH, iconPath.format(j)).click()  # icon
        img = driver.find_element(By.XPATH, imagePath)
        url = img.get_attribute('src')
        urlretrieve(url, imgFolder + f'/{pageNum}-{j}.svg')
        driver.implicitly_wait(300)
        driver.back()  # icon
    driver.close()


def downloadIcon():
    img_folder = '../asset/image/icon1/'

    if not os.path.isdir(img_folder):  # 없으면 새로 생성하는 조건문
        os.mkdir(img_folder)

    for c in category:
        for t in tagsTemp:
            driver.get(url.format(c, t))
            driver.implicitly_wait(100)

            imgSubFolder = img_folder + t + '/'

            n = getIconCount()
            print(n)
            for i in range(n):
                driver.find_element(By.XPATH, f'//*[@id="app"]/div[2]/div/div/div[2]/div[2]/div/div/div/a[{i+1}]/iconify-icon').click()
                driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/div/div[3]/div/div[1]/section[1]/button[1]').click()
                source = driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/div/div[3]/div/div[3]/div/textarea').get_attribute("value")

                imgSource = imgSubFolder+str(i)+".svg"
                f = open(imgSource, 'w')
                f.write('<?xml version="1.0" encoding="UTF-8"?>')
                f.write(source)
                print(source)
                driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/button').click()


def makeDirectory():
    img_folder = '../asset/image/icon1/'

    for t in tagsTemp:
        imgSubFolder = img_folder + t + '/'
        if not os.path.isdir(imgSubFolder):  # 없으면 새로 생성하는 조건문
            os.mkdir(imgSubFolder)


def icon():
    makeDirectory()
    downloadIcon()


