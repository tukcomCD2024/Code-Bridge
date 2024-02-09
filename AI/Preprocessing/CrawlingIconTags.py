import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By

cd_installer.install()

webURL = "https://www.flaticon.com/search/{}?weight=thin&corner=rounded&type=uicon"


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


def getTagCount(driver, i, j):
    iconPath = f'//*[@id="viewport"]/div[6]/div/section[2]/ul/li[{i}]/div/a'
    tagPath = '//*[@id="detail"]/div/div[2]/ul/li'
    driver.implicitly_wait(20)
    try:
        # 아이콘을 클릭했는데 에러가 나면
        driver.find_element(By.XPATH, iconPath).click()
    except:
        driver.get(webURL.format(j))
        try:
            # 다시 눌러보기
            driver.find_element(By.XPATH, iconPath).click()  # icon
        except:
            # 스크롤해서 다시 눌러 보기
            driver.get(webURL.format(j))
            element = driver.find_element(By.XPATH, iconPath)
            driver.execute_script("arguments[0].scrollIntoView();", element)
            element.click()

    tag = driver.find_elements(By.XPATH, tagPath)

    return len(tag)

def tagScanning(driver, tagSet, i):
    tagSet.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i}]/a').text)
    return tagSet



def scanTags():
    detail = set()

    lastpage = getLastpage(webURL.format(2))
    for i in range(3, lastpage):
        detail = detail.union(getTag(i, 3, 99, set(), tagScanning))
        print(detail)

    print(detail)


def initalTagCount():
    f = open("../asset/tag/tagsFinal.txt", "r")
    tags = f.readline().split(',')
    tagCount = {}
    for i in tags:
        tagCount[i] = 0
    tagCount['Piñata'] = 0

    return tagCount


def tagCounting(driver, tagMap, i):
    tagName = driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i}]/a').text
    try:
        tagMap[tagName] += 1
    except:
        tagMap[tagName] = 1
    return tagMap


def getTag(pageNum, from0, to0, tagSet, action):
    n = 4
    step = (to0 - from0) // n
    path = webURL.format(pageNum + 1)
    endpoint = 0
    for j in range(n):
        from1 = step * j + from0
        endpoint = to1 = from1 + step

        driver = getDriver(path)
        for k in range(from1, to1):
            for i in range(getTagCount(driver, k, pageNum + 1)):
                action(driver, tagSet, i + 1)
            driver.back()
        driver.close()

    driver = getDriver(webURL.format(pageNum + 1))
    for j in range(endpoint, from0):
        for i in range(getTagCount(driver, j, pageNum + 1)):
            action(driver, tagSet, i + 1)
        driver.back()
    driver.close()
    return tagSet


def printTagCount(tagMap):
    num = ""
    for i in tagMap:
        print(i)
        num += str(tagMap[i]) + ","
    print(num)


def countTags():
    tagCount = initalTagCount()
    pageNum = getLastpage(webURL.format(1))

    print(tagCount)
    for i in range(pageNum):
        tagCount = getTag(i, 3, 99, tagCount, tagCounting)
        print(tagCount)

    printTagCount(tagCount)

countTags()
