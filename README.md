# PythonTest2
本项目简单整合了Chaquopy6.3.0以及UVCCamera。
源码中存在两个Python脚本，分别用于测试调用Python以及皮肤油脂检测。油脂检测所需图片很难用自带相机拍摄完成，故又整合了UVCCamera以方便使用USB相机拍摄图片。
***
## 1.Chaquopy [官网](https://chaquo.com/chaquopy/)
本项目整合了Chaquopy（Python SDK for Android），因Python脚本编写使用Python3.6.5，所以Chaquopy并为使用最新版，关于版本对应请参见[官方说明](https://chaquo.com/chaquopy/doc/current/versions.html)，关于使用方法请参阅[官方文档](https://chaquo.com/chaquopy/documentation/)。
***
## 2.UVCCamera
整合此工程在与可以访问USB Camera设备。详细使用及说明请参阅[作者Github](https://github.com/saki4510t/UVCCamera).
***
## 3.其他注意事项
+ Android studio版本：3.6.1
+ GRadle版本：5.1.1
+ Gradle插件版本：3.4.1
+ NDK版本：android-ndk-r20b（若需要重新编译UVCCamera的so库，请使用r14b版本）
