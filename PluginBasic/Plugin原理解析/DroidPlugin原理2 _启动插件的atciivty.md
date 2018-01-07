## reference :
* Android源码分析-Activity的启动过程 : http://blog.csdn.net/singwhatiwanna/article/details/18154335
* Activity生命周期管理 : http://weishu.me/2016/03/21/understand-plugin-framework-activity-management/

## 需要解决的问题 :
* 启动插件activity : 未注册Activity真身替换坑位 : https://github.com/tiann/understand-plugin-framework/blob/master/intercept-activity/src/main/java/com/weishu/intercept_activity
* 动态加载插件

## 启动未在宿主mainifest注册的插件atciivty :

* 需要理解activity的启动流程 :
    * 启动流程重要的方法节点 : 
        * ActivityThread.performLaunchActivity()
            * 收集要启动的Activity的相关信息,package,component
            * ClassLoader加载Activity
            * 创建并解析Application,parse manifest
            * 创建Activity
 ![image](https://github.com/cxMax/AndroidPlugin-better-known/blob/master/PluginBasic/AndroidSource/Assets/android_start_activity_process.jpg)

* 核心类介绍 :
    * Instrumentation：用来辅助Activity完成启动Activity的过程
    * ActivityThread（包含ApplicationThread + ApplicationThreadNative + IApplicationThread）：真正启动Activity的实现都在这里
      * ApplicationThread实际上是一个Binder对象，是App所在的进程与AMS所在进程system_server通信的桥梁
    * H : handler对象, 存在于ActivityThread类, system_server与App主线程进行通信 ,

* 大致流程 :
  * ActivityManagerService的startActivity
  * Instrumentation的checkStartActivityResult , 这里会校验目标activity是否在Manifestfest已注册, 所以插件化需要占坑位
  * Activity的真正创建位于 ActivityThread的performLaunchActivity , 两点 :
    * 使用ClassLoader加载并通过反射创建Activity对象
    * 如果Application还没有创建，那么创建Application对象并回调相应的生命周期方法
* hook点 :
  * ActivityManagerService解析manifest的启动activity, 这里需要把真身保存进去,为了后面的替换
  * H处理system_server传递的消息, 启动真身activity , 需要进行替换