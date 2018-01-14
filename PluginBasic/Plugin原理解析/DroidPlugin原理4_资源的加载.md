## reference :
* 资源加载 : http://blog.csdn.net/maplejaw_/article/details/51507954
* Android应用程序插件化研究之AssetManager : https://www.jianshu.com/p/c8c03bdd11e3

* 手机中, 已安装apk的dex文件存储在哪里 ?
    * 已安装的APK会将dex解压到了/data/dalvik-cache/目录下，PathClassLoader会到这里去找

* 手机中, 已安装的apk存储位置在哪里 ?
    * /data/app/package_name/base-1.apk

* AndroidClassLoader委派机制：DexClassLoader->PathClassLoader->BootClassLoader。

* 方案 :
    * 使用AssetManager的addAssetPath , 将插件的res资源添加进去, 从而达到宿主加载插件的资源的目的
* 相关知识点 :
    * appt生成R文件, R文件的生成规则 :
      * 最高的1字节表示Package ID，次高1个字节表示Type ID，最低2字节表示Entry ID
    
    * aapt2次打包 :
    
      * 第一次是生成R.java，参与javac编译
    
      * 第二次是对res里面的资源文件进行编译