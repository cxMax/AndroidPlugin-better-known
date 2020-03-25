# Android手机获取栈顶应用的activity

## 场景
* adb shell dumpsys activity 指令失效

## 需要解决的问题
* 我们要拿当前系统某个前台应用正在运行的activity名称， 如果用以上指令非常简单

## Android提供的API
1. ActivityManager.getRunningTasks
    * 这个在Android 5.0以上的版本已经弃用， 第三方应用无法获取到进程包名
2. ActivityManager.getRunningAppProcesses
    * 这个在Android 5.0以上的版本，对于非系统应用 ，getRunningAppProcesses() 方法已经只能获取到自身应用和桌面应用的进程信息了
3. UsageStatsManager，在5.0以上系统能用。 但是需要系统权限
    * 在5.0以上系统能用。 但是需要系统权限
4. ActivityLifecycleCallbacks
    * 只能判断自身应用


## 特殊操作
1. 读取/proc目录
    * 首先来介绍一下/proc目录，它是一个虚拟的目录，其下面的文件和目录也都是虚拟的，不占用实际的存储空间，而是存在于系统内存中。proc以文件系统的方式为访问系统内核的操作提供接口，它是动态从系统内核中读出所需信息的
    * 频繁的文件 IO 操作
    * 具体原理 ： https://github.com/jaredrummler/AndroidProcesses

2. 使用AccessibilityService
    * 每一次窗口变化， 会走进这个回调， onAccessibilityEvent
    * 具体原理 ： https://effmx.com/articles/tong-guo-android-fu-zhu-gong-neng-accessibility-service-jian-ce-ren-yi-qian-tai-jie-mian/

3. 通过反射去拿
    * 结论 ： dumpsys activity 最终调用的ActivityThread#dumpActivity()
    * 参考 ： https://github.com/huanglongyu/Demos/blob/master/Android/GraphicDemo
    * 具体原理 ：

## dumpsys原理
* 参考 ：http://gityuan.com/2015/08/22/tool-dumpsys/
1. dumpsys.cpp中会在defaultServiceManager()，获取系统中已经注册的ServiceManager, 常见的例如ActivityManagerService,WindowManagerService等等
2. 那么framework中，是在哪里注册呢， 答案就是。ActivityManangerService#setSystemProcess()
```
2424            ServiceManager.addService(Context.ACTIVITY_SERVICE, this, true);
2425            ServiceManager.addService(ProcessStats.SERVICE_NAME, mProcessStats);
2426            ServiceManager.addService("meminfo", new MemBinder(this));
2427            ServiceManager.addService("gfxinfo", new GraphicsBinder(this));
2428            ServiceManager.addService("dbinfo", new DbBinder(this));
2429            if (MONITOR_CPU_USAGE) {
2430                ServiceManager.addService("cpuinfo", new CpuBinder(this));
2431            }
2432            ServiceManager.addService("permission", new PermissionController(this));
2433            ServiceManager.addService("processinfo", new ProcessInfoService(this));
```
3. 可见dumpsys activity指令，最终调用的还是ActivityManagerService提供的函数
4. 其他例如'gfxinfo'，具体实现在GraphicsBinder中， 而'activity'却在this中，
5. 因此直接看dumpActivity这个函数
6. 可见最终还是调用的r.app.thread.dumpActivity
7. ActivityThread.dumpActivity
8. 因此通过系统包名，可以直接通过反射拿到。参考 ：https://github.com/huanglongyu/Demos/blob/master/Android/GraphicDemo/app/src/main/java/com/example/hly/graphicdemo/Dump.java


## 参考
* https://effmx.com/articles/tong-guo-android-fu-zhu-gong-neng-accessibility-service-jian-ce-ren-yi-qian-tai-jie-mian/
* adb shell dumpsys 命令用法 : https://www.jianshu.com/p/d859480613a4
