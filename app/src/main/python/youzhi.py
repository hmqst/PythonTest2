import cv2 as cv
import numpy as np
from skimage import measure,color,segmentation
from time import time
import datetime
from java import jclass

def image_processing(path):
    tt1 = time()
    image=cv.imread(path)
    img1 = cv.cvtColor(image,cv.COLOR_BGR2GRAY)
    th,tw=img1.shape[:2]
    for i in range (th):
        for j in range (tw):
            if img1[i][j]>200:
                img1[i][j]=255
            else:
                img1[i][j]=0
    # cv.imshow("r0",img1)
    #OpenCV定义的结构元素
    kernel = cv.getStructuringElement(cv.MORPH_ELLIPSE,(2, 2))
    kerne2 = cv.getStructuringElement(cv.MORPH_ELLIPSE,(9, 9))
    #腐蚀图像
    r = cv.erode(img1,kernel)
    #膨胀图像
    r = cv.dilate(r,kerne2)
    number=0
    labels=measure.label(r,connectivity=2)  #2=8连通区域标记 1=4
    #print('regions number:',labels.max()+1)  #显示连通区域块数(从0开始标记)
    for region in measure.regionprops(labels):
        minr, minc, maxr, maxc = region.bbox#r行=纵坐标 c列=横坐标
        r0=int((minc+maxc)/2);r1=int((minr+maxr)/2)
        rad=max(int((maxr-minr)/2),int((maxc-minc)/2))
        if region.area>15:
          if rad>5:
            cv.circle(image,(r0,r1),rad,(255,0,0),2)
            number+=1
    # print('number=',number)
    tt2 = time()
    tt = tt2 - tt1
    # print('time=%.5f'%tt)
    nowTime=datetime.datetime.now().strftime('%Y-%m-%d %H:%M:%S')#现在
    # print('nowtime is :',nowTime)

    # cv.imshow("r",r)
    # cv.imshow("image",image)
    # cv.waitKey(0)
    # cv.destroyAllWindows()
    return {'number':number, 'time':tt, 'this_time':nowTime}

def get_youzhi(path):
    result = image_processing(r''+ path)
    JavaBean = jclass("com.test.pythontest2.pythonToJava.YouZhi")#用自己的包名
    jb = JavaBean()
    jb.setNumber(result['number'])
    jb.setTime(result['time'])
    jb.setThis_time(result['this_time'])
    return jb



