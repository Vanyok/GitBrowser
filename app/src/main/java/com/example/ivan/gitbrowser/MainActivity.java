package com.example.ivan.gitbrowser;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements DownloadCallback {

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;

    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;
    private ArrayList<GitProject> projects = new ArrayList<>();
    RecyclerViewAdapter adapter;
    // Reference to the TextView showing additional info
    private TextView statusET;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        statusET = (TextView) findViewById(R.id.status_tv);
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), getString(R.string.gitUrl));
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        String response = sharedPref.getString(getString(R.string.gitResKey), "");
        if (!response.equals("")) {
            updateList(response);
        }

    }

    /**
     * Called when user taps search button
     */
    public void searchGits(View view) {

        EditText langET = (EditText) findViewById(R.id.lang_input);
        EditText keyWordsET = (EditText) findViewById(R.id.keywords_input);
        String keyword = keyWordsET.getText().toString();
        String lang = langET.getText().toString();
        if (!lang.equals(""))
            lang = " language:" + lang;
        //build query string and pass it to network fragment
        String filters = "?q=" + keyword + lang;
        mNetworkFragment.setUrlString(getString(R.string.gitUrl));
        mNetworkFragment.setmQueryParams(filters);
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
        }

    }

    /**
     * Called when the user taps on the project item from list
     */
    public void viewDetails(View view, int position) {
        GitProject project = projects.get(position);
        Intent intent = new Intent(this, DetailsActivity.class);
        // pass project variables to the detail activity
        intent.putExtra("subscrLink", project.getLinkToSubscr());
        intent.putExtra("projName", project.getRepositoryName());
        startActivity(intent);
    }


    @Override
    public void updateFromDownload(String result) {
         /* extract to other class*/
        updateList(result);
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(getString(R.string.gitResKey), result);
        editor.apply();
    }

    private void updateList(String result) {
        try {
            projects = JsonToProjectList.convertToProjects(result);
            Resources res = getResources();
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerView);
            adapter = new RecyclerViewAdapter(projects, getApplication(), new ItemClickListener() {
                @Override
                public void onItemClick(View view, int position) {
                    viewDetails(view, position);
                }
            });
            adapter.setResources(res);
            recyclerView.setAdapter(adapter);
            Integer num = adapter.getItemCount();
            String projectsFound = res.getQuantityString(R.plurals.numberOfProjectsAvailable, num, num);
            statusET.setText(projectsFound);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public NetworkInfo getActiveNetworkInfo() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }

    @Override
    public void onProgressUpdate(int progressCode, int percentComplete) {

        switch (progressCode) {
            // You can add UI behavior for progress updates here.
            case Progress.ERROR:
                statusET.setText(R.string.errrorTryLater);
                break;
            case Progress.CONNECT_SUCCESS:
                statusET.setText(R.string.searching);
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                //   ...
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
                statusET.setText(R.string.downloading);
                break;
            case Progress.PROCESS_INPUT_STREAM_SUCCESS:
                //  ...
                break;
        }
    }

    @Override
    public void finishDownloading() {
        mDownloading = false;
        if (mNetworkFragment != null) {
            mNetworkFragment.cancelDownload();
        }
    }
}
