package edu.strathmore.serc.sercopenenergymonitorv3;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Bob on 17/03/2017.
 * Android does not allow Network IO to happen in the Main Thread,
 * we need to run it in its own Thread and a common way to do this is with AsyncTask.
 */

public class EmonCmsApiCall extends AsyncTask<String, Void, String> {

    private Context mContext;
    private ProgressDialog dialog;
    public AsyncResponse delegate = null;
    private boolean hasError = false;

    public interface AsyncResponse {
        void processFinish(String output) throws JSONException;
    }

    public EmonCmsApiCall(Context context ,AsyncResponse delegate){
        this.delegate = delegate;
        mContext = context;
    }

    public EmonCmsApiCall(Context context) {
        mContext = context;

    }

    @Override
    protected void onPreExecute() {
        dialog = new ProgressDialog(mContext);
        super.onPreExecute();
        Log.i("SERC Log:", "Starting onPreExecute");
        dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        dialog.setMessage("Loading. Please wait...");
        dialog.setIndeterminate(true);
        dialog.setCanceledOnTouchOutside(false);
        Log.i("SERC Log:", "Showing ProgressBar");
        dialog.show();

    }


    @Override
    protected String doInBackground(String... params) {
        String result = "";
        try {
            String urlstring = params[0];
            Log.i("SERC Log:", "HTTP Connecting: " + urlstring);

            // Recommended way of making http requests is HttpURLConnection
            URL url = new URL(urlstring);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            try {
                InputStream reader = new BufferedInputStream(urlConnection.getInputStream());
                Log.i("SERC Log:", "Starting to read text");
                String text = "";
                int i = 0;
                while ((i = reader.read()) != -1) {
                    text += (char) i;
                }
                Log.i("SERC Log:", "HTTP Response: " + text);
                result = text;

            } catch (Exception e) {
                Log.i("SERC Log:", "HTTP Exception: " + e);
                hasError = true;

            } finally {
                Log.i("SERC Log:", "HTTP Disconnecting");
                urlConnection.disconnect();
            }

        } catch (Exception e) {
            e.printStackTrace();
            Log.i("SERC Log:", "HTTP Exception: " + e);
            hasError = true;

        }

        return result;
    }

    @Override
    protected void onPostExecute(String s) {
        super.onPostExecute(s);
        if (hasError){
            Toast.makeText(mContext, "Error. Could not fetch data", Toast.LENGTH_SHORT).show();
        }
        try {
            delegate.processFinish(s);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        Log.i("SERC Log", "Loading Dialog onPostExecute exists: " + String.valueOf(dialog.isShowing()));
        if(dialog.isShowing()) {
            dialog.dismiss();
        }

    }



}

