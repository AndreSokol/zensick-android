package andresokol.zensick;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity implements AsyncResponseInterface {
    private JSONArray ParsedData;
    private SwipeRefreshLayout mSwipe;
    private DownloadData mDownload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

        mSwipe = (SwipeRefreshLayout) findViewById(R.id.update_swipe);
        mSwipe.setColorSchemeColors(R.color.blue, R.color.green, R.color.yellow, R.color.red);

        mSwipe.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.i("NO SHIT", "onRefresh called from SwipeRefreshLayout");
                StartUpdate();
            }
        });

        ParsedData = null;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void StartUpdate() {
        mDownload = new DownloadData(this);
        mDownload.delegate = this;
        mDownload.execute();
    }

    public void AsyncResponse(String data) {
        /*DownloadData dataRetriever = new DownloadData(this);
        dataRetriever.execute();

        String data = "Something gone wrong";

        try {
            data = dataRetriever.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }*/

        try {
            ParsedData = new JSONArray(data);
        } catch (JSONException e) {
            Log.d("SHIT", e.toString());
            return;
        }

        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout body = (LinearLayout) findViewById(R.id.main_page_body);
        RelativeLayout newPost;
        TextView titleView, bodyView, authorView, timeView;
        JSONObject parsedPost;
        String postTitle, postBody, postAuthor, postTime;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

        body.removeAllViewsInLayout();

        for (int i = 0; i < ParsedData.length(); i++) {
            newPost = (RelativeLayout) inflater.inflate(R.layout.post_template, null);
            //RelativeLayout.LayoutParams params = newPost.generateLayoutParams((AttributeSet) 3);
            //newPost.setTop(100);

            //RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(
            //        ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            //params.addRule(RelativeLayout.LayoutParams.);


            titleView = (TextView) newPost.findViewById(R.id.post_title);
            bodyView = (TextView) newPost.findViewById(R.id.post_body);
            authorView = (TextView) newPost.findViewById(R.id.post_author);
            timeView = (TextView) newPost.findViewById(R.id.post_time);

            try {
                parsedPost = ParsedData.getJSONObject(i);
                postTitle = parsedPost.getString("title");
                postAuthor = parsedPost.getString("author");
                postBody = parsedPost.getString("body");
                postTime = parsedPost.getString("created");
            } catch (JSONException e) {
                Log.d("SHIT", e.toString());
                break;
            }

            titleView.setText(postTitle);

            postBody = (postBody.split("<c>"))[0];
            bodyView.setText(postBody);

            authorView.setText(postAuthor);

            postTime = postTime.replaceAll("Z", "+0003");
            try {
                timeView.setText((dateFormat.parse(postTime)).toString().substring(4, 16));
            } catch (ParseException e) {
                timeView.setText(postTime);
            }

            newPost.setId(i + 10000);

            body.addView(newPost);
        }


        mSwipe.setRefreshing(false);
        //setContentView(body);
    }

    public void ShowPost(View view) {
        Intent intent = new Intent(MainActivity.this, ShowArticle.class);

        try {
            intent.putExtra("data", ParsedData.getJSONObject(view.getId() - 10000).toString());
        } catch(JSONException e) {
            intent.putExtra("data", "");
        }

        startActivity(intent);
    }
}
