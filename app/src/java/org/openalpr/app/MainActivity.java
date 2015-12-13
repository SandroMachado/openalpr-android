package org.openalpr.app;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static org.openalpr.app.AppConstants.*;


public class MainActivity extends Activity implements AsyncListener<AlprResult> {

    private static final String LOG_TAG = MainActivity.class.getName();

    private String mCurrentPhotoPath;
    private ImageView mImageView;

    private EditText plate;
    private EditText processingTime;

    private TextView errorText;

    private ProgressDialog progressDialog;

    Button.OnClickListener takePhotoBtnClickListener = new Button.OnClickListener(){

        @Override
        public void onClick(View view) {
            dispatchTakePictureIntent();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (!PreferenceManager.getDefaultSharedPreferences(
                getApplicationContext())
                .getBoolean(PREF_INSTALLED_KEY, false)) {

            PreferenceManager.getDefaultSharedPreferences(
                    getApplicationContext())
                    .edit().putBoolean(PREF_INSTALLED_KEY, true).commit();

            /*PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).
                    edit().putString(RUNTIME_DATA_DIR_ASSET, ANDROID_DATA_DIR).commit();*/

            copyAssetFolder(getAssets(), RUNTIME_DATA_DIR_ASSET,
                    ANDROID_DATA_DIR + File.separatorChar + RUNTIME_DATA_DIR_ASSET);
        }

        setContentView(R.layout.activity_main);

        mImageView = (ImageView)findViewById(R.id.imageView);

        plate = (EditText)findViewById(R.id.plateNumberId);
        processingTime = (EditText)findViewById(R.id.processingTimeId);


        errorText = (TextView)findViewById(R.id.errorTextView);

        Button takePicBtn = (Button)findViewById(R.id.button);

        setBtnListenerOrDisable(takePicBtn, takePhotoBtnClickListener,
                MediaStore.ACTION_IMAGE_CAPTURE);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private static boolean copyAssetFolder(AssetManager assetManager,
                                           String fromAssetPath, String toPath) {
        try {
            String[] files = assetManager.list(fromAssetPath);
            new File(toPath).mkdirs();
            boolean res = true;
            for (String file : files)
                if (file.contains("."))
                    res &= copyAsset(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
                else
                    res &= copyAssetFolder(assetManager,
                            fromAssetPath + "/" + file,
                            toPath + "/" + file);
            return res;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean copyAsset(AssetManager assetManager,
                                     String fromAssetPath, String toPath) {
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
        } catch(Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void copyFile(InputStream in, OutputStream out) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
        while((read = in.read(buffer)) != -1){
            out.write(buffer, 0, read);
        }
    }

    private File getStorageDir(){
        File storageDir = null;

        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {

            storageDir = getExternalFilesDir(null);
            /*if (storageDir != null) {
                if (! storageDir.mkdirs()) {
                    if (! storageDir.exists()){
                        Log.d("camera-app-photos", "failed to create directory");
                        return null;
                    }
                }
            }*/

        } else {
            Log.v(getString(R.string.app_name), "External storage is not mounted READ/WRITE.");
        }

        return storageDir;
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = JPEG_FILE_PREFIX + timeStamp + "_";
        File storageDir = getStorageDir();
        File imageF = File.createTempFile(imageFileName, JPEG_FILE_SUFFIX, storageDir);
        return imageF;
    }

    private File setUpPhotoFile() throws IOException {

        File f = createImageFile();
        mCurrentPhotoPath = f.getAbsolutePath();

        return f;
    }

    private void handleBigCameraPhoto() {

        if (mCurrentPhotoPath != null) {
            setPic();
            //mCurrentPhotoPath = null;
        }
    }

    private void setPic() {

		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the ImageView */
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((targetW > 0) || (targetH > 0)) {
            scaleFactor = Math.min(photoW/targetW, photoH/targetH);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        Bitmap bitmap = BitmapFactory.decodeFile(mCurrentPhotoPath, bmOptions);

		/* Associate the Bitmap to the ImageView */
        mImageView.setImageBitmap(bitmap);
        mImageView.setVisibility(View.VISIBLE);
    }

    private void dispatchTakePictureIntent() {
        setErrorText("");
        clearData();
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        File f = null;

        try {
            f = setUpPhotoFile();
            mCurrentPhotoPath = f.getAbsolutePath();
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(f));
        } catch (IOException e) {
            e.printStackTrace();
            f = null;
            mCurrentPhotoPath = null;
        }

        startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode == RESULT_OK) {

            String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar +
                    RUNTIME_DATA_DIR_ASSET + File.separatorChar +OPENALPR_CONF_FILE;
            handleBigCameraPhoto();
            String parameters[] = {"us", "", this.mCurrentPhotoPath, openAlprConfFile, "1"};
            Bundle args = new Bundle();
            args.putStringArray(ALPR_ARGS, parameters);
            AlprFragment alprFragment = (AlprFragment)getFragmentManager()
                    .findFragmentByTag(ALPR_FRAGMENT_TAG);

            if(alprFragment == null){
                alprFragment = new AlprFragment();
                alprFragment.setArguments(args);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.add(alprFragment, ALPR_FRAGMENT_TAG);
                transaction.commitAllowingStateLoss();
            }
        }
    }


    public static boolean isIntentAvailable(Context context, String action) {
        final PackageManager packageManager = context.getPackageManager();
        final Intent intent = new Intent(action);
        List<ResolveInfo> list =
                packageManager.queryIntentActivities(intent,
                        PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    private void setBtnListenerOrDisable(
            Button btn,
            Button.OnClickListener onClickListener,
            String intentName
    ) {
        if (isIntentAvailable(this, intentName)) {
            btn.setOnClickListener(onClickListener);
        }
    }


    public void setPlate(String plate) {
        this.plate.setText(plate);
    }

    public void setProcessingTime(long processingTime){
        this.processingTime.setText(String.format("%d %s", processingTime, "ms"));
    }

    private void setErrorText(String text) {
        errorText.setText(text);
    }

    private void clearData(){
        plate.setText("");
        processingTime.setText("");
    }

    @Override
    public void onPreExecute() {
        onProgressUpdate();
    }

    @Override
    public void onProgressUpdate() {
        if(progressDialog == null){
            prepareProgressDialog();
        }
    }

    @Override
    public void onPostExecute(AlprResult alprResult) {
        if(alprResult.isRecognized()) {
            List<AlprResultItem> resultItems = alprResult.getResultItems();
            if (resultItems.size() > 0) {
                AlprResultItem resultItem = resultItems.get(0);
                setPlate(resultItem.getPlate());
                setProcessingTime(alprResult.getProcessingTime());
            }
            cleanUp();
        }else {
            setErrorText(getString(R.string.recognition_error));
            cleanUp();
        }
    }

    private void cleanUp() {
        progressDialog.dismiss();
        progressDialog = null;
        FragmentManager fm = getFragmentManager();
        AlprFragment alprFragment = (AlprFragment) fm.findFragmentByTag(ALPR_FRAGMENT_TAG);
        fm.beginTransaction().remove(alprFragment).commitAllowingStateLoss();
    }

    private void prepareProgressDialog(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMessage("Processing Image data");
        progressDialog.show();
    }
}
