# Android动态权限判断
> 背景 : Android 6.0 (target=23) 新增动态判断
> 如果6.0以下的系统，要获取权限，应该怎么获取呢？

## 常规适配方法
* target version/ compile verison = 23
* 通过ContextCompt申请

## 源码
### 几个重要的类
* ContextCompat.checkSelfPermission
    * 检查应用是否具有某个危险权限。如果应用具有此权限，方法将返回 PackageManager.PERMISSION_GRANTED，并且应用可以继续操作。如果应用不具有此权限，方法将返回 PackageManager.PERMISSION_DENIED，且应用必须明确向用户要求权限。

* ActivityCompat.requestPermissions
    * 应用可以通过这个方法动态申请权限，调用后会弹出一个对话框提示用户授权所申请的权限。

* ActivityCompat.shouldShowRequestPermissionRationale
    * 如果应用之前请求过此权限但用户拒绝了请求，此方法将返回 true。如果用户在过去拒绝了权限请求，并在权限请求系统对话框中选择了 Don't ask again 选项，此方法将返回 false。如果设备规范禁止应用具有该权限，此方法也会返回 false。

* onRequestPermissionsResult
    * 当应用请求权限时，系统将向用户显示一个对话框。当用户响应时，系统将调用应用的 onRequestPermissionsResult() 方法，向其传递用户响应，处理对应的场景。

### 获取和请求流程
* 权限申请
    *（1）App调用requestPermissions发起动态权限申请；
    *（2）requestPermissions方法通过广播的形式启动PackageInstaller的GrantPermissionsActivity界面让用户选择是否授权；
    *（3）经由PackageManagerService把相关信息传递给PermissionManagerService处理；
    *（4）PermissionManagerService处理结束后回调PackageManagerService中onPermissionGranted方法把处理结果返回；
    *（5）PackageManagerService通知观察者权限变化并调用writeRuntimePermissionsForUserLPr方法让PackageManager的settings记录下相关的权限授予状态。
* 权限检查
    *（1）App调用checkSelfPermission方法检测是否具有权限；
    *（2）通过实现类ContextImpl的checkPermission方法经由ActivityManager和ActivityManagerService处理；
    *（3）经过ActivityManager处理后会调用PackageManagerService的checkUidPermission方法把数据传递给PermissionManagerService处理；
    *（4）最终经过一系列查询返回权限授权的状态。

* 前面分析过，申请过的动态权限会被记录在xml文件中，这些文件在PackageManagerService构造方法中会被重新读取并缓存到PackageManager的Settings里面。

## 6.0以下（5.x）系统如何获取权限？
> 5.x的ContextCompat是没有checkSelfPermission函数的

* 通过AppOpsManager去获取
···
public int checkPermission(Context context,String permissionName){
    if (Build.VERSION.SDK_INT >= 19){
        try {
            AppOpsManager mAppOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            String pkg = context.getApplicationContext().getPackageName();

            int uid = context.getApplicationInfo().uid;

            Class appOpsClass = Class.forName(AppOpsManager.class.getName());

            Method checkOpNoThrowMethod = appOpsClass.getMethod(CHECK_OP_NO_THROW, Integer.TYPE, Integer.TYPE, String.class);

            Field opPostNotificationValue = appOpsClass.getDeclaredField(permissionName);

            int value = (int) opPostNotificationValue.get(Integer.class);

            return (int) checkOpNoThrowMethod.invoke(mAppOps, value, uid, pkg);
        } catch (Throwable e) {
            e.printStackTrace();
            return 1;
        }
    }else{
        return 1;
    }
}
···

## 参考
* Android 6.0动态权限申请 : https://www.jianshu.com/p/2fe4fb3e8ce0
* (Android 9.0)动态权限运行机制源码分析 : https://www.jianshu.com/p/5ab996f54eea