package com.cxmax.droidplugin.intercept_activity;

import android.app.Activity;
import android.content.Context;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class TestInterceptActivity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            AMSHookHelper.hookActivityManagerNative();
            AMSHookHelper.hookActivityThreadHandler();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
