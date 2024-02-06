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


def getLastpage(webURL):
    driver = getDriver(webURL)
    driver.implicitly_wait(10)
    lastpage = driver.find_element(By.XPATH, '//*[@id="pagination-total"]').text
    driver.close()

    return int(lastpage.replace(',', ''))


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


def getTagCount(driver, i, j):
    iconPath = f'//*[@id="viewport"]/div[6]/div/section[2]/ul/li[{i}]/div/a'
    tagPath = '//*[@id="detail"]/div/div[2]/ul/li'
    driver.implicitly_wait(20)
    try:
        driver.find_element(By.XPATH, iconPath).click()  # icon
    except:
        driver.get(f"https://www.flaticon.com/search/{j}?weight=thin&corner=rounded&type=uicon")
        try:
            driver.find_element(By.XPATH, iconPath).click()  # icon
        except:
            driver.get(f"https://www.flaticon.com/search/{j}?weight=thin&corner=rounded&type=uicon")
            element = driver.find_element(By.XPATH, iconPath)
            driver.execute_script("arguments[0].scrollIntoView();", element)
            element.click()

    tag = driver.find_elements(By.XPATH, tagPath)

    return len(tag)

def tagScan(driver, webURL, tagSet, i):
    try:
        tagSet.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text)
    except:
        driver.get(webURL)
        tagSet.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text)



def scanTag(webURL, pageNum, from0, to0, tagSet, action):
    iconPath = '//*[@id="viewport"]/div[6]/div/section[2]/ul/li[{}]/div/a'
    tagPath = '//*[@id="detail"]/div/div[2]/ul/li'
    detail = set()

    step = (to0 - from0) // 5
    for j in range(5):
        from1 = step * j + from0
        to1 = from1 + step

        driver = getDriver(webURL.format(pageNum + 1))
        for k in range(from1, to1):
            for i in range(getTagCount(driver, k, pageNum + 1)):
                detail.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text)
            driver.implicitly_wait(20)

            driver.back()
        driver.close()

    driver = getDriver(webURL.format(pageNum + 1))
    for j in range(to1, from0):
        for i in range(getTagCount(driver, j)):
            detail.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text)
        driver.implicitly_wait(20)

        driver.back()
    driver.close()
    return detail


def icon7(webURL, img_folder):
    if not os.path.isdir(img_folder):  # 없으면 새로 생성하는 조건문
        os.mkdir(img_folder)

    for i in range(53):
        scanIcon(webURL, img_folder, i + 1, 3, 99)
    scanIcon(webURL, img_folder, 54, 3, 37)


def scanTags():
    detail = set()
    path = "https://www.flaticon.com/kr/search/1?word=동물&weight=thin&type=uicon"
    path = "https://www.flaticon.com/search/{}?weight=thin&corner=rounded&type=uicon"

    # iconPath = '//*[@id="viewport"]/div[6]/div/section[3]/ul/li[{}]/div/a'

    lastpage = getLastpage(path.format(2))
    for i in range(3, lastpage):
        detail = detail.union(scanTag(path, i, 3, 99))
        print(detail)

    print(detail)


def combineTags():
    path = '../asset/tag/'
    tags = set()
    for i in os.listdir(path):
        f = open(path + i, 'r', encoding='UTF-8')
        temp = f.readline().replace("'", '').replace('\n', '').split(', ')
        f.close()
        print(len(temp))
        tags = tags.union(set(temp))
    print(len(tags))
    print(tags)
    f = open("../asset/tag/tagsFinal.txt", "w")
    t = ''
    for i in tags:
        t += i + ","
    print(t)
    f.write(t)


def initalTagCount():
    f = open("../asset/tag/tagsFinal.txt", "r")
    tags = f.readline().split(',')
    tagCount = {}
    for i in tags:
        tagCount[i] = 0
    tagCount['Piñata'] = 0

    return tagCount


def tagCounting(driver, webURL, tagMap, i):
    tagMap[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1

def scanTagCount(webURL, pageNum, from0, to0, tagSet, action):
    n = 4
    step = (to0 - from0) // n
    path = webURL.format(pageNum + 1)
    print(path)
    for j in range(n):
        from1 = step * j + from0
        to1 = from1 + step

        driver = getDriver(path)
        for k in range(from1, to1):
            for i in range(getTagCount(driver, k, pageNum + 1)):
                # action(driver, webURL, tagSet, i)
                try:
                    tagSet[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1
                except:

                    driver.get(path)
                    tagSet[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1

            driver.back()
        driver.close()

    driver = getDriver(webURL.format(pageNum + 1))
    for j in range(to1, from0):
        for i in range(getTagCount(driver, j, pageNum + 1)):
            # action(driver, webURL, tagSet, i)
            try:
                tagSet[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1
            except:
                driver.get(webURL)
                tagSet[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1
        driver.back()
    driver.close()
    return tagSet


def printTagCount(tagMap):
    num = ""
    for i in tagMap:
        print(i)
        num += str(tagMap[i]) + ","
    print(num)


def countTag():
    webURL = "https://www.flaticon.com/search/{}?weight=thin&corner=rounded&type=uicon"

    tagCount = initalTagCount()
    pageNum = getLastpage(webURL.format(0))

    print(tagCount)
    for i in range(pageNum):
        tagCount = scanTagCount(webURL, i, 3, 99, tagCount, tagCounting)
        print(tagCount)

    printTagCount(tagCount)

countTag()
# icon7("https://www.flaticon.com/kr/icon-fonts-most-downloaded/{}?weight=thin&type=uicon", './asset/imgIcon7')
