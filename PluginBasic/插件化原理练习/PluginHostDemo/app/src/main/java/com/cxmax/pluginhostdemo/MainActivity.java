package com.cxmax.pluginhostdemo;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.cxmax.pluginhostdemo.ams_hook.AMSHookHelper;
import com.cxmax.pluginhostdemo.classloader_hook.BaseDexClassLoaderHookHelper;
import com.cxmax.pluginhostdemo.classloader_hook.LoadedApkClassLoaderHookHelper;

import java.io.File;

public class MainActivity extends Activity {

    private static final int PATCH_BASE_CLASS_LOADER = 1;

    private static final int CUSTOM_CLASS_LOADER = 2;

    private static final int HOOK_METHOD = PATCH_BASE_CLASS_LOADER;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        hookPluginUnRecordActivity(newBase);
//        hookHostUnRecordActivity();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.e("info", "onCreate: getName = " + getClass().getName() );
        Log.e("info", "onCreate: getSimpleName = " + getClass().getSimpleName() );
        Log.e("info", "onCreate: getCanonicalName = " + getClass().getCanonicalName() );
        Button button = new Button(this);
        button.setText("启动TargetActivity");

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startPluginUnRecordActivity();
//                startHostUnRecordActivity();
            }
        });
        setContentView(button);

    }


    private void hookPluginUnRecordActivity(Context newBase) {
        try {
            Utils.extractAssets(newBase, "plugin-release.apk");

            if (HOOK_METHOD == PATCH_BASE_CLASS_LOADER) {
                File dexFile = getFileStreamPath("plugin-release.apk");
                File optDexFile = getFileStreamPath("plugin-release.apk");
                BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(), dexFile, optDexFile);
            } else {
                LoadedApkClassLoaderHookHelper.hookLoadedApkInActivityThread(getFileStreamPath("plugin-release.apk"));
            }

            AMSHookHelper.hookActivityManagerNative();
            AMSHookHelper.hookActivityThreadHandler();
            AMSHookHelper.hookPackageManager();

        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private void hookHostUnRecordActivity() {
        try {
            AMSHookHelper.hookActivityManagerNative();
            AMSHookHelper.hookActivityThreadHandler();
        } catch (Throwable throwable) {
            throw new RuntimeException("hook failed", throwable);
        }
    }

    private void startPluginUnRecordActivity() {
        try {
            Intent t = new Intent();
            if (HOOK_METHOD == PATCH_BASE_CLASS_LOADER) {
                t.setComponent(new ComponentName("com.caixi.plugindemo",
                        "com.caixi.plugindemo.MainActivity"));
            } else {
                t.setComponent(new ComponentName("com.caixi.plugindemo",
                        "com.caixi.plugindemo.MainActivity"));
            }
            startActivity(t);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
    private void startHostUnRecordActivity() {
        // 启动目标Activity; 注意这个Activity是没有在AndroidManifest.xml中显式声明的
        // 但是调用者并不需要知道, 就像一个普通的Activity一样
        startActivity(new Intent(MainActivity.this, TargetActivity.class));
    }
}
