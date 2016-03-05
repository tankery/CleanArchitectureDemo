package me.tankery.demo.cleanarchitecture;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ProgressBar progressBar;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        listView = (ListView) findViewById(R.id.list_view);

        listView.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);

        new AsyncTask<String, Object, List<String>>() {
            @Override
            protected List<String> doInBackground(String... params) {
                List<String> repositories = null;
                try {
                    repositories = search(params[0]);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return repositories;
            }

            @Override
            protected void onPostExecute(List<String> repositories) {
                if (repositories == null || repositories.isEmpty())
                    return;

                ArrayAdapter adapter = new ArrayAdapter<>(
                        MainActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, repositories
                );
                listView.setAdapter(adapter);
                listView.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        }.execute("cleanarchitecture");
    }

    private List<String> search(String urlStr) throws IOException {
        List<String> repositories = new ArrayList<>();

        HttpURLConnection urlConnection = null;
        InputStream in = null;
        StringBuilder responseStrBuilder = null;
        try {
            URL url = new URL("https://api.github.com/search/repositories?q=" + urlStr);
            urlConnection = (HttpURLConnection) url.openConnection();
            in = new BufferedInputStream(urlConnection.getInputStream());

            BufferedReader streamReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
            responseStrBuilder = new StringBuilder();

            String inputStr;
            while ((inputStr = streamReader.readLine()) != null) {
                responseStrBuilder.append(inputStr);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }

        if (responseStrBuilder == null) {
            return null;
        }

        try {
            JSONObject json = new JSONObject(responseStrBuilder.toString());

            JSONArray array = json.getJSONArray("items");

            for (int i = 0; i < array.length(); i++) {
                JSONObject item = array.getJSONObject(i);
                String name = item.getString("full_name");
                repositories.add(name);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }


        return repositories;
    }

}
