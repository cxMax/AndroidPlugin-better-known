package com.cxmax.pluginhostdemo.classloader_hook;

import dalvik.system.DexClassLoader;

/**
 * @describe : 通过伪造LoadedApk 和 hook PMS来欺骗系统,插件已经安装, 从而达到装载插件的activity的目的
 * @usage :
 * Created by caixi on 18-1-7.
 */
public class CustomClassLoader extends DexClassLoader{

    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }

}
