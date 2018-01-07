package com.cxmax.pluginhostdemo;

import android.app.Application;
import android.content.Context;

/**
 * @describe :
 * @usage :
 * Created by caixi on 18-1-7.
 */
public class UPFApplication extends Application {

    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;
    }

    public static Context getContext() {
        return sContext;
    }

}
