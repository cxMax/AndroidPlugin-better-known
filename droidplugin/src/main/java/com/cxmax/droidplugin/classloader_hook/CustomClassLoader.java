package com.cxmax.droidplugin.classloader_hook;

import dalvik.system.DexClassLoader;

/**
 * @describe : this is used to load resource and codes
 * @usage :
 * <p>
 * <p>
 * Created by cxmax on 2017/4/9.
 */

public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
