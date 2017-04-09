package com.cxmax.droidplugin.intercept_activity;

import android.content.ComponentName;
import android.content.Intent;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class IActivityManagerHandler implements InvocationHandler {

    private static final String TAG = "IActivityManagerHandler";

    Object mBase;

    public IActivityManagerHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if ("startActivity".equals(method.getName())) {
            Intent raw;
            int index = 0;

            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            raw = (Intent) args[index];

            Intent newIntent = new Intent();

            String stubPackage ="com.cxmax.droidplugin.intercept_activity";
            ComponentName componentName = new ComponentName(stubPackage , StubActivity.class.getSimpleName());

            newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, raw);
            args[index] = newIntent;
            return method.invoke(mBase , args);
        }
        return  method.invoke(mBase , args);
    }
}
