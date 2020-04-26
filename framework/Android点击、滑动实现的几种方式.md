# Android点击、滑动实现的几种方式
> 需要解决的问题 ： 如何实现Android内的点击、滑动的实现

## 常规实现方法
* 使用adb命令，adb shell input .... (原理使用的InputManagerService)
* 借助UiAutomator, 其实原理是用的去注册借助AccessBilityService服务完成的, Instrumentation的sendPointerSync()
* 借助AccessBilityService， 使用gesturedescription, 但是有缺陷， 只能7.0以上的系统才能使用
* 使用InputManagerService， 通过反射的形式调用injectInputEvent()

### 思考 : 要通过InputManagerService实现AccessBilityService和UiAutomator一样的效果？
* 需要解决的就是系统应用权限， 第三方应用内部去调用InputManagerService会有权限检查这个环节
    * 可以通过hook的方式
    * 360黑科技启动权限这种形式

### adb shell input
1. 输入文本：
```
adb shell input text 12345
输入12345文本
````

2. 功能键：
```
adb shell input keyevent 4 返回
adb shell input keyevent 66 确定
adb shell input keyevent 67 删除；更多的keyevent键对应code值参考 http://www.cnblogs.com/chengchengla1990/p/4515108.html
```

3. 点击：
```
adb shell input tap 20 1000
点击位置(20,1000)
```

4. 滑动(长按)
```
adb shell input swipe 10 20 100 200
从(10,20)滑动到(100,200)
```
5. 长按
```
最后加一个参数，表示操作的时间ms，如
adb shell input swipe 100 200 500 600 900 从(100,200)滑动到(500,600)总花费900ms
adb shell input swipe 1400 400 1400 400 900 长按(1400,400) 900ms
```

## 参考
* 十分钟了解Android触摸事件原理（InputManagerService） : https://juejin.im/post/5a291aca51882531926e9e3d
* android adb shell input各种妙用 : https://www.cnblogs.com/zzugyl/p/7515004.html
* uiautomator 和 accessbilityservice 源码解析 ： https://www.drunkdream.com/2019/07/19/uiautomator-accessibilityservice/