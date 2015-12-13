package org.openalpr.app;

/**
 * Created by sujay on 28/11/14.
 */
public interface AppConstants {
    static final String BITMAP_STORAGE_KEY = "viewbitmap";
    static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";

    static final int REQUEST_IMAGE_CAPTURE = 1;
    static final String RUNTIME_DATA_DIR_ASSET = "runtime_data";
    static final String ANDROID_DATA_DIR = "/data/data/org.openalpr.app";
    static final String OPENALPR_CONF_FILE = "openalpr.conf";
    static final String PREF_INSTALLED_KEY = "installed";
    static final String JPEG_FILE_PREFIX = "IMG_";
    static final String JPEG_FILE_SUFFIX = ".jpg";
    static final String ALPR_FRAGMENT_TAG = "alpr";
    static final String ALPR_ARGS = "alprargs";

    static final String DLG_MESSAGE = "dlgMessage";

    static final String JSON_RESULT_ARRAY_NAME = "results";
}
