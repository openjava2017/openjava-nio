package com.openjava.nio.util;

public final class ClassUtils
{
    public static ClassLoader getDefaultClassLoader()
    {
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if (loader == null) {
            loader = ClassUtils.class.getClassLoader();
            
            // getClassLoader() may return null if the class was loaded by
            // the bootstrap ClassLoader
            if (loader == null) {
                loader = ClassLoader.getSystemClassLoader();
            }
        }
        
        return loader;
    }
}
