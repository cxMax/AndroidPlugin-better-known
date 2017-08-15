package com.cxmax.droidplugin.classloader_hook.ams_hook;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.cxmax.droidplugin.intercept_activity.AMSHookHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class ActivityThreadHandlerCallback implements Handler.Callback {

    Handler mBase;

    public ActivityThreadHandlerCallback(Handler base) {
        mBase = base;
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case 100:
                handleLaunchActivity(msg);
                break;
        }
        mBase.handleMessage(msg);
        return true;
    }

    private void handleLaunchActivity(Message msg) {
        Object obj = msg.obj;
        try {
            // 把替身恢复成真身
            Field intent = obj.getClass().getDeclaredField("intent");
            intent.setAccessible(true);
            Intent raw = (Intent) intent.get(obj);

            Intent target = raw.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);
            raw.setComponent(target.getComponent());

            Field activityInfoField = obj.getClass().getDeclaredField("activityInfo");
            activityInfoField.setAccessible(true);

            // 根据 getPackageInfo 根据这个 包名获取 LoadedApk的信息; 因此这里我们需要手动填上, 从而能够命中缓存
            ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(obj);

            activityInfo.applicationInfo.packageName = target.getPackage() == null ?
                    target.getComponent().getPackageName() : target.getPackage();

            hookPackageManager();
        } catch (Exception e) {
            throw new RuntimeException("hook launch activity failed", e);
        }
    }

    private static void hookPackageManager() throws Exception {
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);
        // 获取ActivityThread里面原始的 sPackageManager
        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        // 准备好代理对象, 用来替换原始的对象
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[] { iPackageManagerInterface },
                new IPackageManagerHookHandler(sPackageManager));

        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        sPackageManagerField.set(currentActivityThread, proxy);
    }
}
