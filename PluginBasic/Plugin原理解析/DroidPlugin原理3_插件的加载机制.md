## reference :
* Android 插件化原理解析——插件加载机制 : http://weishu.me/2016/04/05/understand-plugin-framework-classloader/
* ClassLoader相关基础知识 :
    * https://juejin.im/post/59752eb1f265da6c3f70eed9
    * https://liuzhengyang.github.io/2016/09/28/classloader/
    * https://www.jianshu.com/p/4b4f1fa6633c
    * https://www.jianshu.com/p/a8371d26f848
## 需要解决的问题 :
* ClassLoader动态加载插件的类(非宿主的类,未安装的apk)
    
## 原理 :
* apk安装后, 文件路径 : /data/app/package_name/base-1.apkAndroid
## android源码相关类 :
* LoadedApk : APK文件在内存中的表示
* Application
    
## 解决方案 :
* 方案1 :
    * 总结 :
        * 通过伪造LoadedApk 和 ApplicationInfo, 使用自定义的ClassLoader去将插件信息添加进mPackages中，进而完成了类的加载过程
        * 把插件的apk放在已安装宿主apk的/data/data/<package>/files/plugin/ 路径下, 从而可以获取到插件的apk相关信息
    * 步骤 :
       * hook AMS&PMS, 解决pm.getPackageInfo,获取ClassLoader, 因为插件没有安装,因此获取失败,抛出异常
       * LoadedApk : 将插件apk信息命中到内存里面, 可以获取到
    * 流程 :
       * 在ActivityThread接收到IApplication的 scheduleLaunchActivity远程调用之后，将消息转发给H
       * H类在handleMessage的时候，调用了getPackageInfoNoCheck方法来获取待启动的组件信息。在这个方法中会优先查找mPackages中的缓存信息，而我们已经手动把插件信息添加进去；因此能够成功命中缓存，获取到独立存在的插件信息。
       * H类然后调用handleLaunchActivity最终转发到performLaunchActivity方法；这个方法使用从getPackageInfoNoCheck中拿到LoadedApk中的mClassLoader来加载Activity类，进而使用反射创建Activity实例；接着创建Application，Context等完成Activity组件的启动
* 方案2 :
    * 使用宿主的ClassLoader, 将插件的dex文件添加到DexPathList, 从而完成了插件类的加载.