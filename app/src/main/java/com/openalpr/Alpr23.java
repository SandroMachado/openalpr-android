package com.openalpr;

/**
 * Open ALPR wrapper.
 */

public class Alpr23 {

    static {
        System.loadLibrary("openalprjni");
    }

    public native void initialize(String country, String configFile, String runtimeDir);

    public native String native_recognize(String imgFilePath);

    public native void set_top_n(int topN);

    public native void set_default_region(String region);

    public native String get_version();

    public native void dispose();
}