package org.openalpr;

/**
 * Open ALPR wrapper.
 */
public class AlprJNIWrapper implements OpenALPR {

    static {
        System.loadLibrary("openalpr-native");
    }

    /* (non-Javadoc)
     * @see org.openalpr.Alpr#recognize(java.lang.String, int)
     */
    @Override
    public native String recognize(String imgFilePath, int topN);

    /* (non-Javadoc)
     * @see org.openalpr.Alpr#recognizeWithCountryNRegion(java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public native String recognizeWithCountryNRegion(String country, String region,
                                                     String imgFilePath, int topN);

    /* (non-Javadoc)
     * @see org.openalpr.Alpr#recognizeWithCountryRegionNConfig(java.lang.String, java.lang.String, java.lang.String, java.lang.String, int)
     */
    @Override
    public native String recognizeWithCountryRegionNConfig(String country,
                                                           String region, String imgFilePath, String configFilePath, int topN);

    /*
     * (non-Javadoc)
     * @see org.openalpr.Alpr#version()
     */
    @Override
    public native String version();
}