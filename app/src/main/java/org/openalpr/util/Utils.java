package org.openalpr.util;

import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OpenALPR utils.
 */
public class Utils {

    private Utils() {}

    /**
     * Copies the assets folder.
     *
     * @param assetManager The assets manager.
     * @param fromAssetPath The from assets path.
     * @param toPath The to assets path.
     *
     * @return A boolean indicating if the process went as expected.
     */
    public static boolean copyAssetFolder(AssetManager assetManager, String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);

            new File(toPath).mkdirs();

            boolean res = true;

            for (String file : files)

                if (file.contains(".")) {
                    res &= copyAsset(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                } else {
                    res &= copyAssetFolder(assetManager, fromAssetPath + "/" + file, toPath + "/" + file);
                }

            return res;
        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    /**
     * Copies an asset to the application folder.
     *
     * @param assetManager The asset manager.
     * @param fromAssetPath The from assets path.
     * @param toPath The to assests path.
     *
     * @return A boolean indicating if the process went as expected.
     */
    private static boolean copyAsset(AssetManager assetManager, String fromAssetPath, String toPath) {
        InputStream in = null;
        OutputStream out = null;

        try {
            in = assetManager.open(fromAssetPath);

            new File(toPath).createNewFile();

            out = new FileOutputStream(toPath);

            copyFile(in, out);
            in.close();

            in = null;

            out.flush();
            out.close();

            out = null;

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Copies a file.
     *
     * @param in The input stream.
     * @param out The output stream.
     *
     * @throws IOException
     */
    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];

        int read;

        while ((read = in.read(buffer)) != -1) {
            out.write(buffer, 0, read);
        }
    }

}
