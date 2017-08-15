package com.cxmax.droidplugin.binder_hook;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @describe :this class simply hooks the method named queryLocalInterface
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class BinderProxyHookHandler implements InvocationHandler {

    private static final String TAG = "BinderProxyHookHandler";

    IBinder base;
    Class<?> stub;
    Class<?> iinterface;

    public BinderProxyHookHandler(IBinder base) {
        this.base = base;
        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("queryLocalInterface".equals(method.getName())) {

            Log.d(TAG, "hook queryLocalInterface");

            return Proxy.newProxyInstance(proxy.getClass().getClassLoader(),
                    new Class[] { this.iinterface },
                    new BinderHookHandler(base, stub));
        }
        Log.d(TAG, "method:" + method.getName());
        return method.invoke(base, args);
    }
}
