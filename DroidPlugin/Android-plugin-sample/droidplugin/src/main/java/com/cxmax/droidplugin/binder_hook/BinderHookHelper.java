package com.cxmax.droidplugin.binder_hook;

import android.os.IBinder;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class BinderHookHelper {

    public static void hookClipboardService() throws Exception {
        final String CLIPBOARD_SERVICE = "clipboard";

        Class<?> serviceManager = Class.forName("android.os.ServiceManager");
        Method getService = serviceManager.getDeclaredMethod("getService", String.class);

        IBinder rawBinder = (IBinder) getService.invoke(null , CLIPBOARD_SERVICE);

        IBinder hookedBinder = (IBinder) Proxy.newProxyInstance(serviceManager.getClassLoader(),
                new Class<?>[] { IBinder.class },
                new BinderProxyHookHandler(rawBinder));

        Field cacheField = serviceManager.getDeclaredField("sCache");
        cacheField.setAccessible(true);
        Map<String, IBinder> cache = (Map) cacheField.get(null);
        cache.put(CLIPBOARD_SERVICE, hookedBinder);
    }
}
