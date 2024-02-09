import csv
import chromedriver_autoinstaller as cd_installer
from selenium import webdriver
from selenium.webdriver.common.by import By

f = open('../asset/tag/tags2차 작업.csv','r')
reader = csv.reader(f)

cd_installer.install()

driver = webdriver.Chrome()
checkMsgPath = '//*[@id="app"]/div[2]/div/div/div[2]/div[1]'

i = 0
for row in reader:
    if i>4401:
        driver.get(f"https://icon-sets.iconify.design/?query={row[0]}")
        driver.implicitly_wait(200)
        try:
            el = driver.find_element(By.XPATH, checkMsgPath)
            if el.text != 'No icon sets match your search':
                print(row[0])
        except Exception as e:
            pass
    i+=1





