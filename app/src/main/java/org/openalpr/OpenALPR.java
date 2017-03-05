package org.openalpr;

import android.content.Context;

import com.openalpr.Alpr23;

import org.openalpr.util.Utils;

import java.io.File;

public class OpenALPR {

    static OpenALPR instance;

    private static Alpr23 privateInstance;

    private OpenALPR(String androidDataDir, String configurationFile, String country, String region, int maxPlateNumbers) {
        privateInstance = new Alpr23();

        privateInstance.initialize(country, configurationFile, androidDataDir + File.separatorChar + "runtime_data");
        privateInstance.set_default_region(region);
        privateInstance.set_top_n(maxPlateNumbers);
    }

    public synchronized static OpenALPR create(Context context, String androidDataDir, String configurationFile, String country, String region, int maxPlateNumbers) {
        if (instance == null) {
            Utils.copyAssetFolder(context.getAssets(), "runtime_data", androidDataDir + File.separatorChar + "runtime_data");

            instance = new OpenALPR(androidDataDir, configurationFile, country, region, maxPlateNumbers);
        }

        return instance;
    }

    public String recognize(String image) {
        return privateInstance.native_recognize(image);
    }

    public String getVersion() {
        return privateInstance.get_version();
    }

    public void dispose() {
        privateInstance.dispose();
    }
}
