package com.cxmax.pluginhostdemo.resource_hook;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cxmax.pluginhostdemo.R;

import org.xmlpull.v1.XmlPullParser;

/**
 * @describe : 加载插件的资源
 * @usage : 原理 : 通过AssetManager , 把插件apk通过反射addAssetPath()添加到AssetManager里面, 在通过resource获取对应的插件资源
 * <p>
 * </p>
 * Created by caixi on 18-1-13.
 */

public class ResourceHookHelper {

    private AssetManager createAssetManager(String apkPath) {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            AssetManager.class.getDeclaredMethod("addAssetPath", String.class).invoke(
                    assetManager, apkPath);
            return assetManager;
        } catch (Throwable th) {
            th.printStackTrace();
        }
        return null;
    }

    public Resources getPluginResource(Context context, String apkPath){
        AssetManager assetManager = createAssetManager(apkPath);
        return new Resources(assetManager, context.getResources().getDisplayMetrics(), context.getResources().getConfiguration());
    }

    public void hookPluginResourceUsage(Context context, View root) {
        ViewGroup contentWrapper = (ViewGroup) root.findViewById(R.id.plugin_content);

        /**
         *  插件apk路径
         */
        String apkPath = Environment.getExternalStorageDirectory()+"/apkbeloaded-debug.apk";
        /**
         *  插件资源对象
         */
        Resources resources = getPluginResource(context,apkPath);
        /**
         *获取图片资源
         */
        Drawable drawable = resources.getDrawable(resources.getIdentifier("icon_be_load", "drawable",
                "laodresource.demo.com.apkbeloaded"));
        /**
         *  获取文本资源
         */
        String text = resources.getString(resources.getIdentifier("text_beload","string",
                "laodresource.demo.com.apkbeloaded"));

        /**
         * 获取布局资源
         */
        XmlPullParser xmlResourceParser = resources.getLayout(resources.getIdentifier("layout_be_load", "layout",
                "laodresource.demo.com.apkbeloaded"));
        View viewFromPlugin = LayoutInflater.from(context).inflate(xmlResourceParser,null);
        contentWrapper.addView(viewFromPlugin);
    }
}
