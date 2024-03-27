import os
import csv


def numCheck(tag):
    for i in range(10):
        if str(i) in tag:
            return False
    return True


def includeNo():
    f = open("../asset/tag/tags5차 작업.csv", "r")
    g = open("../asset/tag/tags6차 작업.csv", "w", newline='\n')
    read = csv.reader(f)
    write = csv.writer(g)

    for i in read:
        if numCheck(i[0]):
            write.writerow(i)


def checkTagLength(l):
    f = open("../asset/tag/tags5차 작업.csv", "r")
    read = csv.reader(f)

    arr = []
    for i in read:
        if len(i[0]) > l:
            arr.append(i[0])
    return arr


def tagReform():
    f = open("../asset/tag/animals.txt")
    # readline_test.py
    line = f.readline()
    line = line.replace("'", '')
    for i in line.split(', '):
        print(i.lower(), end=', ')
    f.close()


includeNo()
