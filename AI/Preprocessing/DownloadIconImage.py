import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By
import os
from urllib.request import urlretrieve

cd_installer.install()

def getDriver(path):
    driver = webdriver.Chrome()
    driver.get(path)
    return driver


def scanIcon(webURL, imgFolder, pageNum, from0, to0):
    iconPath = '//*[@id="viewport"]/div[6]/div/section[4]/ul/li[{}]/div/a'
    imagePath = '//*[@id="uicons__detail-img"]'

    driver = getDriver(webURL.format(pageNum + 1))

    for j in range(from0, to0):
        driver.find_element(By.XPATH, iconPath.format(j)).click()  # icon
        img = driver.find_element(By.XPATH, imagePath)
        url = img.get_attribute('src')
        urlretrieve(url, imgFolder + f'/{pageNum}-{j}.svg')
        driver.implicitly_wait(300)
        driver.back()  # icon
    driver.close()


def icon7(webURL, img_folder):
    if not os.path.isdir(img_folder):  # 없으면 새로 생성하는 조건문
        os.mkdir(img_folder)

    for i in range(53):
        scanIcon(webURL, img_folder, i + 1, 3, 99)
    scanIcon(webURL, img_folder, 54, 3, 37)
