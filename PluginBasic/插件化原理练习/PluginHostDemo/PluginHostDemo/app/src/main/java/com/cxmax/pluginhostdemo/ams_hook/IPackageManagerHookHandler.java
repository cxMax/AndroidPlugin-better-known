package com.cxmax.pluginhostdemo.ams_hook;

import android.content.pm.PackageInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @describe :
 * @usage :
 * Created by caixi on 18-1-7.
 */
public class IPackageManagerHookHandler implements InvocationHandler {

    private Object mBase;

    /**
     *
     * @param base  IPackageManager
     */
    public IPackageManagerHookHandler(Object base) {
        mBase = base;
    }

    /**
     * 只hook这个getPackageInfo()函数, 欺骗系统,插件已经安装了
     * @param proxy
     * @param method
     * @param args
     * @return
     * @throws Throwable
     */
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
