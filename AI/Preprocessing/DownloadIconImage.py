import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By
import os
from urllib.request import urlretrieve


option = ['regular', 'outline', '']
cd_installer.install()

driver = webdriver.Chrome()


def getIconCount(driver):
    driver.get(url.format(keyword))
    icons = driver.find_elements(By.XPATH, '//*[@id="app"]/div[2]/div/div/div[2]/div[2]/div/div/div')
    return len(icons)


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


def icon(webURL, img_folder):
    if not os.path.isdir(img_folder):  # 없으면 새로 생성하는 조건문
        os.mkdir(img_folder)

    driver.get(webURL)
    n = getIconCount(driver)
    for i in range(n):
        downloadIcon()


url = "https://icon-sets.iconify.design/?query={}"
icon(url, '../asset/image/icon1/')

