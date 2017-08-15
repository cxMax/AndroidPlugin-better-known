package com.cxmax.droidplugin.dynamic_proxy_hook;

import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class HookHelper {

    public static void attachContext() throws Exception{
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        Field instrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        instrumentationField.setAccessible(true);

        Instrumentation instrumentation = (Instrumentation) instrumentationField.get(currentActivityThread);

        Instrumentation instrumentation1Proxy = new EvilInstrumentation(instrumentation);

        instrumentationField.set(currentActivityThread ,instrumentation1Proxy );
    }
}
