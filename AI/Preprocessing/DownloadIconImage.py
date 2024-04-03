import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By
import os
from urllib.request import urlretrieve
from PIL import Image, ImageFilter
import cairosvg

# category = ['fluent-emoji-high-contrast/', 'ph/', 'mdi/', 'material-symbols-light/', 'bi/', 'teenyicons/', 'clarity/', 'ci/', 'icon-park-outline/', 'mingcute/', 'tabler/']
# additional = {'fluent/': 'regular', 'healthicons/': 'outline 24'}

# url = "https://icon-sets.iconify.design/{}/?query={}"
url = "https://icon-sets.iconify.design/?query={}+{}&prefixes=material-symbols%2Cmaterial-symbols-light%2Cic%2Cmdi%2Cph%2Csolar%2Ctabler%2Cmingcute%2Cri%2Cbi%2Ccarbon%2Ciconamoon%2Ciconoir%2Cion%2Clucide%2Cuil%2Ctdesign%2Cteenyicons%2Cclarity%2Cbx%2Cbxs%2Cmajesticons%2Cant-design%2Cgg%2Cgravity-ui%2Cocticon%2Cmemory%2Ccil%2Cflowbite%2Cmynaui%2Cbasil%2Cpixelarticons%2Cakar-icons%2Cci%2Csystem-uicons%2Ctypcn%2Cradix-icons%2Czondicons%2Cep%2Ccircum%2Cmdi-light%2Cfe%2Ceos-icons%2Cbitcoin-icons%2Ccharm%2Cprime%2Chumbleicons%2Cuiw%2Cuim%2Cuit%2Cuis%2Cmaki%2Cgridicons%2Cmi%2Cquill%2Cgala%2Clets-icons%2Cf7%2Cmage%2Cfluent%2Cicon-park-outline%2Cicon-park-solid%2Cicon-park-twotone%2Cjam%2Cheroicons%2Ccodicon%2Cpajamas%2Cpepicons-pop%2Cpepicons-print%2Cpepicons-pencil%2Cbytesize%2Cei%2Cstreamline%2Cguidance%2Cfa6-solid%2Cfa6-regular%2Cooui%2Cnimbus%2Coui%2Cformkit"

cd_installer.install()

driver = webdriver.Chrome()


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


def downloadIcon(tags):
    img_folder = '../asset/image/icon1/'
    category = ['outline', 'regular', 'thin', 'line']
    iconExistPath = '//*[@id="app"]/div[2]/div/div/div[2]/div[1]'

    if not os.path.isdir(img_folder):  # 없으면 새로 생성하는 조건문
        os.mkdir(img_folder)

    for c in category:
        for t in tags:
            driver.get(url.format(c, t))
            driver.implicitly_wait(100)

            imgSubFolder = img_folder + t + '/'

            n = getIconCount()
            print(n)
            for i in range(n):
                el = driver.find_element(By.XPATH, iconExistPath)
                if el.text != 'No icon sets match your search':
                    driver.find_element(By.XPATH, f'//*[@id="app"]/div[2]/div/div/div[2]/div[2]/div/div/div/a[{1}]/iconify-icon').click()
                    driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/div/div[3]/div/div[1]/section[1]/button[1]').click()
                    source = driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/div/div[3]/div/div[3]/div/textarea').get_attribute("value")
                    source = source.replace('"1em"', '"224"')

                    imgSource = imgSubFolder + t + "-" + c + ".svg"
                    f = open(imgSource, 'w')
                    f.write('<?xml version="1.0" encoding="UTF-8"?>')
                    f.write(source)
                    print(source)
                    driver.find_element(By.XPATH, '//*[@id="app"]/dialog/div/button').click()
                    break


def makeDirectory(tags):
    img_folder = '../asset/image/icon1/'

    for t in tags:
        imgSubFolder = img_folder + t + '/'
        if not os.path.isdir(imgSubFolder):  # 없으면 새로 생성하는 조건문
            os.mkdir(imgSubFolder)


def icon():
    tags = open("../asset/tag/sampleTag.txt", 'r').read().split(', ')
    makeDirectory(tags)
    downloadIcon(tags)


icon()
