import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By

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


def getTagCount(driver, i, j):
    iconPath = f'//*[@id="viewport"]/div[6]/div/section[2]/ul/li[{i}]/div/a'
    tagPath = '//*[@id="detail"]/div/div[2]/ul/li'
    driver.implicitly_wait(20)
    try:
        # 아이콘을 클릭했는데 에러가 나면
        driver.find_element(By.XPATH, iconPath).click()
    except:
        driver.get(f"https://www.flaticon.com/search/{j}?weight=thin&corner=rounded&type=uicon")
        try:
            # 다시 눌러보기
            driver.find_element(By.XPATH, iconPath).click()  # icon
        except:
            # 스크롤해서 다시 눌러 보기
            driver.get(f"https://www.flaticon.com/search/{j}?weight=thin&corner=rounded&type=uicon")
            element = driver.find_element(By.XPATH, iconPath)
            driver.execute_script("arguments[0].scrollIntoView();", element)
            element.click()

    tag = driver.find_elements(By.XPATH, tagPath)

    return len(tag)

def tagScanning(driver, tagSet, i):
    tagSet.add(driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text)
    return tagSet



def scanTags():
    detail = set()
    path = "https://www.flaticon.com/search/{}?weight=thin&corner=rounded&type=uicon"

    lastpage = getLastpage(path.format(2))
    for i in range(3, lastpage):
        detail = detail.union(getTag(path, i, 3, 99, set(), tagScanning))
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
    try:
        tagMap[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] += 1
    except:
        tagMap[driver.find_element(By.XPATH, f'//*[@id="detail"]/div/div[2]/ul/li[{i + 1}]/a').text] = 1
    return tagMap

def getTag(webURL, pageNum, from0, to0, tagSet, action):
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
                action(driver, tagSet, i)
            driver.back()
        driver.close()

    driver = getDriver(webURL.format(pageNum + 1))
    for j in range(to1, from0):
        for i in range(getTagCount(driver, j, pageNum + 1)):
            action(driver, tagSet, i)
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
    webURL = "https://www.flaticon.com/search/{}?weight=thin&corner=rounded&type=uicon"

    tagCount = initalTagCount()
    pageNum = getLastpage(webURL.format(1))

    print(tagCount)
    for i in range(pageNum):
        tagCount = getTag(webURL, i, 3, 99, tagCount, tagCounting)
        print(tagCount)

    printTagCount(tagCount)

countTags()
# icon7("https://www.flaticon.com/kr/icon-fonts-most-downloaded/{}?weight=thin&type=uicon", './asset/imgIcon7')
