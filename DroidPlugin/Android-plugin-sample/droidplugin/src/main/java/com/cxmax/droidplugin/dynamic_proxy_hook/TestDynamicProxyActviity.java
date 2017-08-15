package com.cxmax.droidplugin.dynamic_proxy_hook;

import android.app.Activity;
import android.content.Context;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class TestDynamicProxyActviity extends Activity {

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        try {
            HookHelper.attachContext();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
