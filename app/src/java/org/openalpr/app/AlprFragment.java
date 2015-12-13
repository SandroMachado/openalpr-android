package org.openalpr.app;

import android.app.Activity;
import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.openalpr.OpenALPR;
import static org.openalpr.app.AppConstants.ALPR_ARGS;
import static org.openalpr.app.AppConstants.JSON_RESULT_ARRAY_NAME;

import java.io.File;



/**
 * Created by sujay on 27/09/14.
 */
public class AlprFragment extends Fragment {

    private static final String LOG_TAG = AlprFragment.class.getName();

    AsyncListener<AlprResult> listener;
    AlprTask alprTask;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        Bundle arguments = getArguments();
        String[] parameters = arguments.getStringArray(ALPR_ARGS);
        alprTask = new AlprTask();
        alprTask.execute(parameters);

    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        listener = (AsyncListener<AlprResult>)activity;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    class AlprTask extends AsyncTask<String, Void, AlprResult> {

        OpenALPR openALPR;

        @Override
        protected void onPreExecute() {
            if(openALPR == null){
                openALPR = OpenALPR.Factory.create();
            }
            if(listener != null){
                listener.onPreExecute();
            }
        }

        @Override
        protected AlprResult doInBackground(String... parameter) {
            if(parameter.length == 5) {
                String country = parameter[0];
                String region = parameter[1];
                String filePath = parameter[2];
                String confFile = parameter[3];
                String stopN = parameter[4];

                int topN = Integer.parseInt(stopN);

                String result = openALPR.recognizeWithCountryRegionNConfig(country, region, filePath,
                        confFile, topN);
                AlprResult alprResult = processJsonResult(result);

                deleteImageFile(filePath);

                return alprResult;
            }
            return new AlprResult();
        }

        @Override
        protected void onProgressUpdate(Void... values) {

            if(listener != null){
                listener.onProgressUpdate();
            }
        }

        @Override
        protected void onPostExecute(AlprResult alprResult) {
            if(listener != null){
                listener.onPostExecute(alprResult);
            }
        }

        private AlprResult processJsonResult(String result) {
            AlprResult alprResult = new AlprResult();
            try {
                JSONObject jsonObject = new JSONObject(result);
                addResult(jsonObject, alprResult);
            } catch (JSONException e) {
                Log.e(LOG_TAG, "Exception parsing JSON result", e);
                alprResult.setRecognized(false);
            }
            return alprResult;
        }

        private void addResult(JSONObject jsonObject, AlprResult alprResult) throws JSONException {
            JSONArray resultArray = jsonObject.getJSONArray(JSON_RESULT_ARRAY_NAME);
            alprResult.setProcessingTime(jsonObject.getLong("processing_time_ms"));
            AlprResultItem alprResultItem = null;

            for (int i = 0; i < resultArray.length(); i++) {
                JSONObject resultObject = resultArray.getJSONObject(i);
                alprResultItem = new AlprResultItem();
                alprResultItem.setPlate(resultObject.getString("plate"));
                alprResultItem.setProcessingTime(resultObject.getLong("processing_time_ms"));
                alprResultItem.setConfidence(resultObject.getDouble("confidence"));
                alprResult.addResultItem(alprResultItem);
            }
        }

        private void deleteImageFile(final String filePath){
            File file = new File(filePath);
            if(file.exists()){
                file.delete();
                Log.i(LOG_TAG, String.format("Deleted file %s", filePath));
            }
        }
    }
}

