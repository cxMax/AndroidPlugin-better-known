package com.cxmax.droidplugin.ams_pms_hook;

import android.app.Activity;
import android.content.Context;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class TestAMS_PMSActivity extends Activity {


    @Override
    protected void attachBaseContext(Context newBase) {
        HookHelper.hookActivityManager();
        HookHelper.hookPackageManager(newBase);
        super.attachBaseContext(newBase);
    }
}
