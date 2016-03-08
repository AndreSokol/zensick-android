package andresokol.zensick;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by sokol_000 on 07.02.2016.
 */
public class DownloadData extends AsyncTask<Void, Void, String> {
    private String sURL;
    private Context mContext;
    public AsyncResponseInterface delegate = null;

    public DownloadData(Context context) {
        mContext = context;
        sURL = "http://" + mContext.getString(R.string.server_url) + "/ajax/post/";
    }

    @Override
    protected String doInBackground(Void... params) {
        ConnectivityManager connMgr = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            try {
                return downloadData();
            } catch(IOException e) {
                return "Not able to load data";
            }
        } else {
            return "No network connection available.";
        }
    }

    private String downloadData() throws IOException {
        InputStream is = null;
        // Only display the first 500 characters of the retrieved
        // web page content.
        int len = 50000;

        try {
            URL url = new URL(sURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            int response = conn.getResponseCode();
            Log.d("SHIT?", "The response is: " + response);
            is = conn.getInputStream();

            Log.d("READ", is.toString());

            // Convert the InputStream into a string
            String contentAsString = readContent(is, len);
            Log.d("READ", contentAsString);
            return contentAsString;

            // Makes sure that the InputStream is closed after the app is
            // finished using it.
        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

    private String readContent(InputStream stream, int len) throws IOException, UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        String line;
        while((line = reader.readLine()) != null){
            builder.append(line);
        }
        return builder.toString();
    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        delegate.AsyncResponse(result);
    }
}
