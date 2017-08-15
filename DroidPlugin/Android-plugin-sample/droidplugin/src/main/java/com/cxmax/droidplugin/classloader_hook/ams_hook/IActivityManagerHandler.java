package com.cxmax.droidplugin.classloader_hook.ams_hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.cxmax.droidplugin.classloader_hook.UPFApplication;
import com.cxmax.droidplugin.intercept_activity.AMSHookHelper;
import com.cxmax.droidplugin.intercept_activity.StubActivity;

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
            String stubPackage = UPFApplication.getContext().getPackageName();
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            newIntent.setComponent(componentName);

            newIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, raw);
            args[index] = newIntent;
            Log.d(TAG, "hook success");
            return method.invoke(mBase, args);
        }
        return method.invoke(mBase, args);
    }
}
