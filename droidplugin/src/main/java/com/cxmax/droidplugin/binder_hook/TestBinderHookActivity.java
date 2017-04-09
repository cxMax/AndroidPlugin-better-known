package com.cxmax.droidplugin.binder_hook;

import android.app.Activity;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

/**
 * @describe :
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class TestBinderHookActivity extends Activity {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
        try {
            BinderHookHelper.hookClipboardService();
        } catch (Exception e) {
            e.printStackTrace();
        }

        EditText editText = new EditText(this);
        setContentView(editText);
    }
}
