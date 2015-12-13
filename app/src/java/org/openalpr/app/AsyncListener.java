package org.openalpr.app;

/**
 * Created by sujay on 27/09/14.
 */
public interface AsyncListener<Result> {
    void onPreExecute();
    void onProgressUpdate();
    void onPostExecute(Result result);
}
