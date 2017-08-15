package com.cxmax.droidplugin.dynamic_proxy_hook;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class EvilInstrumentation extends Instrumentation {

    private static final String TAG = EvilInstrumentation.class.getSimpleName();

    Instrumentation base;

    public EvilInstrumentation(Instrumentation base) {
        this.base = base;
    }

    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {
        Log.e(TAG, "execStartActivity: hook success");

        try {
            Method execStartActivity = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class, IBinder.class, IBinder.class, Activity.class,
                    Intent.class, int.class, Bundle.class);
            execStartActivity.setAccessible(true);
            return (ActivityResult) execStartActivity.invoke(base, who,
                    contextThread, token, target, intent, requestCode, options);
        } catch (Exception e) {
            throw new RuntimeException("warning ! no execStartActivity method in this rom");
        }
    }
}
