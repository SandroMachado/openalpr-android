package com.sandro.openalprsample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.transition.Explode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.squareup.picasso.Picasso;

import org.openalpr.OpenALPR;
import org.openalpr.model.Candidate;
import org.openalpr.model.Coordinate;
import org.openalpr.model.Result;
import org.openalpr.model.Results;
import org.openalpr.model.ResultsError;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private final int REQUEST_IMAGE = 100;
    private final int REQUEST_FILE = 42;
    private final int STORAGE = 1;

    private String ANDROID_DATA_DIR;
    private File imgFolder;
    private File imageFile;

    private Context appCtx;
    private ImageView imageView;
    private EditText txtCountry;
    private EditText txtRegion;
    private EditText txtCandidatesNum;
    private TableLayout resultTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();

        appCtx = this;
        ANDROID_DATA_DIR = this.getApplicationInfo().dataDir;

        txtCandidatesNum = (EditText) findViewById(R.id.txtCandidatesNum);
        txtCountry = (EditText) findViewById(R.id.txtCountry);
        txtRegion = (EditText) findViewById(R.id.txtRegion);

        resultTable = (TableLayout) findViewById(R.id.resultTable);
        imageView = (ImageView) findViewById(R.id.imageView);

        findViewById(R.id.btnTakePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePicture();
            }
        });

        findViewById(R.id.btnLoad).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadPicture();
            }
        });

        findViewById(R.id.btnClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                resultTable.setVisibility(View.GONE);
                int count = resultTable.getChildCount();
                for (int i = 1; i < count; i++) {
                    View child = resultTable.getChildAt(i);
                    if (child instanceof TableRow) ((ViewGroup) child).removeAllViews();
                }
                Toast.makeText(appCtx, "Result table cleared!", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.btnFlushDir).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int cont = 0;
                for (File file : imgFolder.listFiles()) {
                    if (file.delete()) ++cont;
                }
                Toast.makeText(appCtx, cont + " files deleted successfully!", Toast.LENGTH_LONG).show();
            }
        });

        imgFolder = new File(Environment.getExternalStorageDirectory() + "/OpenALPR/");
        if (!imgFolder.exists()) {
            imgFolder.mkdir();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == REQUEST_IMAGE || requestCode == REQUEST_FILE) && resultCode == Activity.RESULT_OK) {
            final long startTime = System.currentTimeMillis();
            final long[] endTime = new long[1];
            final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Parsing result...", true);
            final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;

            if (requestCode == REQUEST_FILE) {
                if (data != null && data.getData() != null) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/" + data.getData().getLastPathSegment().split(":")[1];
                    imageFile = new File(path);
                    Picasso.with(MainActivity.this).invalidate(imageFile);
                }
            }

            final int[] x1 = { 0 };
            final int[] x2 = { 0 };
            final int[] y1 = { 0 };
            final int[] y2 = { 0 };
            final String[] plate = {""};

            AsyncTask.execute(new Runnable() {

                @Override
                public void run() {

                    int candidates = txtCandidatesNum.getText().toString().isEmpty()? 5 : Integer.parseInt((txtCandidatesNum.getText().toString()));
                    String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig(txtCountry.getText().toString(), txtRegion.getText().toString(), imageFile.getAbsolutePath(), openAlprConfFile, candidates);
                    Log.d("OPEN ALPR", result);

                    try {
                        final Results results = new Gson().fromJson(result, Results.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTable.setVisibility(View.VISIBLE);
                                if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                                    Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
                                } else {
                                    endTime[0] = System.currentTimeMillis();
                                    TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    TableRow.LayoutParams cellLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                                    List<Result> resultsList = results.getResults();
                                    for(int i = 0; i < resultsList.size(); ++i) {
                                        Result result = resultsList.get(i);

                                        if (i == 0) { // save rectangle coordinates and plate of best result
                                            x1[0] = result.getCoordinates().get(0).getX();
                                            y1[0] = result.getCoordinates().get(0).getY();
                                            x2[0] = result.getCoordinates().get(2).getX();
                                            y2[0] = result.getCoordinates().get(2).getY();
                                            plate[0] = result.getPlate();
                                        }

                                        TableRow tableRow = new TableRow(appCtx);
                                        tableRow.setLayoutParams(rowLayoutParams);

                                        if (result.getConfidence() < 60)
                                            tableRow.setBackgroundColor(Color.RED);
                                        else if (result.getConfidence() < 85)
                                            tableRow.setBackgroundColor(Color.YELLOW);
                                        else if (result.getConfidence() >= 85)
                                            tableRow.setBackgroundColor(Color.GREEN);

                                        TextView cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(result.getPlate());
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", result.getConfidence())+"%");
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        String region = txtCountry.getText().toString()+"_"+txtRegion.getText().toString();
                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(region.length() == 1? "n/a" : region);
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", result.getMatchesTemplate()));
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", ((result.getProcessingTimeMs() / 1000.0) % 60)) + " s");
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        resultTable.addView(tableRow);
                                        List<Candidate> candidates = result.getCandidates();
                                        for (int j = 1; j < candidates.size(); ++j) {
                                            Candidate candidate = candidates.get(j);
                                            tableRow = new TableRow(appCtx);
                                            tableRow.setLayoutParams(rowLayoutParams);
                                            tableRow.setBackgroundColor(Color.LTGRAY);

                                            cellValue = new TextView(appCtx);
                                            cellValue.setText(candidate.getPlate());
                                            cellValue.setLayoutParams(cellLayoutParams);
                                            tableRow.addView(cellValue, 0);

                                            cellValue = new TextView(appCtx);
                                            cellValue.setText(String.format("%.2f", candidate.getConfidence())+"%");
                                            cellValue.setLayoutParams(cellLayoutParams);
                                            tableRow.addView(cellValue, 1);

                                            tableRow.addView(new TextView(appCtx), 2);

                                            cellValue = new TextView(appCtx);
                                            cellValue.setText(String.valueOf(candidate.getMatchesTemplate()));
                                            cellValue.setLayoutParams(cellLayoutParams);
                                            tableRow.addView(cellValue, 3);
                                            resultTable.addView(tableRow);
                                        }
                                    }
                                    resultTable.invalidate();
                                    Toast.makeText(appCtx, "Processing time: " + String.format("%.2f", (((endTime[0]-startTime) / 1000.0) % 60)) + " s", Toast.LENGTH_LONG).show();
                                }
                            }
                        });

                    } catch (JsonSyntaxException exception) {
                        final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(appCtx, resultsError.getMsg(), Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    progress.dismiss();

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            // Picasso requires permission.WRITE_EXTERNAL_STORAGE
                            Picasso.with(MainActivity.this).load(imageFile).fit().centerCrop().into(imageView);
                            if (imageView.getDrawable() != null) {
                                Bitmap bitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
                                Bitmap originalBitmap = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), new BitmapFactory.Options());

                                float viewWidth = bitmap.getWidth();
                                float viewHeigth = bitmap.getHeight();
                                float originalWidth = originalBitmap.getWidth();
                                float originalHeigth = originalBitmap.getHeight();

                                Canvas canvas = new Canvas(bitmap);
                                Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
                                paint.setColor(Color.GREEN);
                                paint.setStyle(Paint.Style.STROKE);
                                paint.setStrokeWidth(8);

                                // map rectangle coordinates to imageview
                                int p1_x = (int)((x1[0] * viewWidth) / originalWidth);
                                int p1_y = (int)((y1[0] * viewHeigth) / originalHeigth);
                                int p2_x = (int)((x2[0] * viewWidth) / originalWidth);
                                int p2_y = (int)((y2[0] * viewHeigth) / originalHeigth);
                                canvas.drawRect(new Rect(p1_x, p1_y, p2_x, p2_y), paint);

                                paint.setTextSize(75);
                                paint.setStyle(Paint.Style.FILL);
                                paint.setTypeface(Typeface.DEFAULT_BOLD);
                                paint.setColor(Color.YELLOW);
                                canvas.drawText(plate[0], p1_x, p1_y-10, paint);
                                imageView.setImageBitmap(bitmap);
                            }
                        }
                    });
                }
            });
        }
    }

    private void checkPermission() {
        List<String> permissions = new ArrayList<>();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
        if (!permissions.isEmpty()) {
            Toast.makeText(this, "Storage access needed to manage the picture.", Toast.LENGTH_LONG).show();
            String[] params = permissions.toArray(new String[permissions.size()]);
            ActivityCompat.requestPermissions(this, params, STORAGE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case STORAGE:{
                Map<String, Integer> perms = new HashMap<>();
                // Initial
                perms.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, PackageManager.PERMISSION_GRANTED);
                // Fill with results
                for (int i = 0; i < permissions.length; i++)
                    perms.put(permissions[i], grantResults[i]);
                // Check for WRITE_EXTERNAL_STORAGE
                Boolean storage = perms.get(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED;
                if (storage) {
                    // permission was granted, yay!
                } else {
                    // Permission Denied
                    Toast.makeText(this, "Storage permission is needed to analyse the picture.", Toast.LENGTH_LONG).show();
                }
            }
            default:
                break;
        }
    }

    public String dateToString(Date date, String format) {
        SimpleDateFormat df = new SimpleDateFormat(format, Locale.getDefault());

        return df.format(date);
    }

    public void takePicture() {
        // Generate the path for the next photo
        String name = dateToString(new Date(), "yyyy-MM-dd-hh-mm-ss");
        imageFile = new File(imgFolder, name + ".jpg");

        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(imageFile));
        startActivityForResult(intent, REQUEST_IMAGE);
    }

    public void loadPicture() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, REQUEST_FILE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (imageFile != null) {// Picasso does not seem to have an issue with a null value, but to be safe
            Picasso.with(MainActivity.this).load(imageFile).fit().centerCrop().into(imageView);
        }
    }

}
