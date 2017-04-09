package com.cxmax.droidplugin.binder_hook;

import android.content.ClipData;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class BinderHookHandler implements InvocationHandler {

    private static final String TAG = "BinderHookHandler";

    Object base;

    public BinderHookHandler(IBinder base , Class<?> stubClass) {
        try {
            Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);
            this.base = asInterfaceMethod.invoke(null , base);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("getPrimaryClip".equals(method.getName())) {
            Log.e(TAG, "hook getPrimaryClip");
            return ClipData.newPlainText(null, "you are hooked");
        }
        if ("hasPrimaryClip".equals(method.getName())) {
            return true;
        }

        return method.invoke(base, args);
    }
}
