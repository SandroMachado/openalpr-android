package com.sandro.openalprsample;

import android.Manifest;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
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
    private TextView resultTextView;
    private EditText txtCountry;
    private EditText txtRegion;
    private EditText txtCandidatesNum;
    private TableLayout resultTable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appCtx = this;
        ANDROID_DATA_DIR = this.getApplicationInfo().dataDir;

        txtCandidatesNum = (EditText) findViewById(R.id.txtCandidatesNum);
        txtCountry = (EditText) findViewById(R.id.txtCountry);
        txtRegion = (EditText) findViewById(R.id.txtRegion);

        resultTable = (TableLayout) findViewById(R.id.resultTable);
        resultTextView = (TextView) findViewById(R.id.textView);
        imageView = (ImageView) findViewById(R.id.imageView);

        resultTextView.setText("Supported countries: empty, eu, us");

        findViewById(R.id.btnTakePicture).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                checkPermission();
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
                resultTextView.setText("");
                imageView.setVisibility(View.GONE);
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
            final ProgressDialog progress = ProgressDialog.show(this, "Loading", "Parsing result...", true);
            final String openAlprConfFile = ANDROID_DATA_DIR + File.separatorChar + "runtime_data" + File.separatorChar + "openalpr.conf";
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 10;

            if (requestCode == REQUEST_FILE) {
                if (data != null) {
                    String path = Environment.getExternalStorageDirectory().getPath() + "/" + data.getData().getLastPathSegment().split(":")[1];
                    imageFile = new File(path);
                }
            }

            // Picasso requires permission.WRITE_EXTERNAL_STORAGE
            Picasso.with(MainActivity.this).load(imageFile).fit().centerCrop().into(imageView);
            resultTextView.setText("Processing");

            AsyncTask.execute(new Runnable() {
                @Override
                public void run() {
                    resultTable.setVisibility(View.VISIBLE);
                    int candidates = txtCandidatesNum.getText().toString().isEmpty()? 5 : Integer.parseInt((txtCandidatesNum.getText().toString()));
                    String result = OpenALPR.Factory.create(MainActivity.this, ANDROID_DATA_DIR).recognizeWithCountryRegionNConfig(txtCountry.getText().toString(), txtRegion.getText().toString(), imageFile.getAbsolutePath(), openAlprConfFile, candidates);
                    Log.d("OPEN ALPR", result);

                    try {
                        final Results results = new Gson().fromJson(result, Results.class);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                imageView.setVisibility(View.VISIBLE);
                                if (results == null || results.getResults() == null || results.getResults().size() == 0) {
                                    Toast.makeText(MainActivity.this, "It was not possible to detect the licence plate.", Toast.LENGTH_LONG).show();
                                    resultTextView.setText("It was not possible to detect the licence plate.");
                                } else {
                                    TableLayout.LayoutParams rowLayoutParams = new TableLayout.LayoutParams(TableLayout.LayoutParams.FILL_PARENT, TableLayout.LayoutParams.WRAP_CONTENT);
                                    TableRow.LayoutParams cellLayoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT);

                                    for(Result loResult : results.getResults()) {
                                        TableRow tableRow = new TableRow(appCtx);
                                        tableRow.setLayoutParams(rowLayoutParams);

                                        if (loResult.getConfidence() < 60)
                                            tableRow.setBackgroundColor(Color.RED);
                                        else if (loResult.getConfidence() < 85)
                                            tableRow.setBackgroundColor(Color.YELLOW);
                                        else if (loResult.getConfidence() >= 85)
                                            tableRow.setBackgroundColor(Color.GREEN);

                                        TextView cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(loResult.getPlate());
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", loResult.getConfidence())+"%");
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(txtCountry.getText().toString()+"-"+txtRegion.getText().toString());
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", loResult.getRegionConfidence())+"%");
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        cellValue = new TextView(appCtx);
                                        cellValue.setTypeface(null, Typeface.BOLD);
                                        cellValue.setText(String.format("%.2f", ((loResult.getProcessingTimeMs() / 1000.0) % 60)) + " s");
                                        cellValue.setLayoutParams(cellLayoutParams);
                                        tableRow.addView(cellValue);

                                        resultTable.addView(tableRow);
                                        for (Candidate loCandidate : loResult.getCandidates()) {
                                            tableRow = new TableRow(appCtx);
                                            tableRow.setLayoutParams(rowLayoutParams);
                                            tableRow.setBackgroundColor(Color.LTGRAY);

                                            cellValue = new TextView(appCtx);
                                            cellValue.setText(loCandidate.getPlate());
                                            cellValue.setLayoutParams(cellLayoutParams);
                                            tableRow.addView(cellValue, 0);

                                            cellValue = new TextView(appCtx);
                                            cellValue.setText(String.format("%.2f", loCandidate.getConfidence())+"%");
                                            cellValue.setLayoutParams(cellLayoutParams);
                                            tableRow.addView(cellValue, 1);
                                            resultTable.addView(tableRow);
                                        }
                                    }
                                    resultTable.invalidate();
                                    resultTextView.setText("");
                                }
                            }
                        });

                    } catch (JsonSyntaxException exception) {
                        final ResultsError resultsError = new Gson().fromJson(result, ResultsError.class);

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                resultTextView.setText(resultsError.getMsg());
                            }
                        });
                    }

                    progress.dismiss();
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
        } else { // We already have permissions, so handle as normal
            takePicture();
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
                    takePicture();
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
        resultTable.setVisibility(View.VISIBLE);

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
