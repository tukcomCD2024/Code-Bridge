import csv
import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By

cd_installer.install()


def getReaderTXT(url, token):
    f = open(url)
    return f.readline().split(token)


def getReaderCSV(url):
    f = open(url)
    return csv.reader(f)

def availableTag(reader):
    driver = webdriver.Chrome()
    checkMsgPath = '//*[@id="app"]/div[2]/div/div/div[2]/div[1]'

    f = open('../asset/tag/tagsFinal5.txt', 'w')
    print(len(reader))
    for row in reader:
        driver.get(f"https://icon-sets.iconify.design/?query={row}")
        driver.implicitly_wait(200)
        try:
            el = driver.find_element(By.XPATH, checkMsgPath)
            if el.text != 'No icon sets match your search':
                f.write(row+"\n")
        except Exception as e:
            pass

    f = open('../asset/tag/sampleTag.txt', 'r')

    availableTags = []
    for i in f.readlines():
        availableTags.append(i[:-1])
    writer = csv.DictWriter(open('../asset/tag/sampleTags.csv', 'w', newline='\n'), fieldnames=['tag', 'count'])
    writer.writeheader()
    for row in reader:
        if row[0] in availableTags:
            writer.writerow({'tag': row[0], 'count': row[1]})


availableTag(getReaderTXT('../asset/tag/sampleTag.txt', ', '))
