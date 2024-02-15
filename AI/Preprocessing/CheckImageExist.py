import csv
import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By

cd_installer.install()


def availableTag():
    f = open('../asset/tag/tags2차 작업.csv', 'r')
    reader = csv.reader(f)

    driver = webdriver.Chrome()
    checkMsgPath = '//*[@id="app"]/div[2]/div/div/div[2]/div[1]'

    f = open('../asset/tag/tagsFinal4.txt', 'w')
    for row in reader:
        driver.get(f"https://icon-sets.iconify.design/?query={row[0]}")
        driver.implicitly_wait(200)
        try:
            el = driver.find_element(By.XPATH, checkMsgPath)
            if el.text != 'No icon sets match your search':
                print(row[0])
                f.write(row[0])
        except Exception as e:
            pass

    f = open('../asset/tag/tagsFinal4.txt', 'r')

    availableTags = []
    for i in f.readlines():
        availableTags.append(i[:-1])
    print(availableTags)
    writer = csv.DictWriter(open('../asset/tag/tags4차 작업.csv', 'w', newline='\n'), fieldnames=['tag', 'count'])
    writer.writeheader()
    for row in reader:
        if row[0] in availableTags:
            writer.writerow({'tag': row[0], 'count': row[1]})
