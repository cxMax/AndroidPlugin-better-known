package com.cxmax.atlasdemo;

import android.content.Context;
import android.taobao.atlas.runtime.AtlasPreLauncher;
import android.util.Log;

/**
 * @describe :
 * @usage :
 * <p>
 * </p>
 * Created by caixi on 17-8-20.
 */

public class PreLaunch implements AtlasPreLauncher{
    @Override
    public void initBeforeAtlas(Context context) {
        Log.d("info", "prelaunch invokded");

    }
}
