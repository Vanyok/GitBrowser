package com.example.ivan.gitbrowser;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.widget.TextView;

import org.json.JSONException;

import java.util.ArrayList;

public class DetailsActivity extends AppCompatActivity implements DownloadCallback {

    // Keep a reference to the NetworkFragment, which owns the AsyncTask object
    // that is used to execute network ops.
    private NetworkFragment mNetworkFragment;
    SubscribersViewAdapter adapter;
    // Boolean telling us whether a download is in progress, so we don't trigger overlapping
    // downloads with consecutive button clicks.
    private boolean mDownloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //get passed variables
        Intent intent = getIntent();
        String name = intent.getStringExtra("projName");
        String subsrLink = intent.getStringExtra("subscrLink");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        TextView nameTv = findViewById(R.id.projNameTitle);
        nameTv.setText(name);
        //download subscribers list
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), subsrLink);
        mNetworkFragment.setUrlString(subsrLink);
        if (!mDownloading && mNetworkFragment != null) {
            // Execute the async download.
            mNetworkFragment.startDownload();
            mDownloading = true;
        }
    }


    @Override
    public void updateFromDownload(String result) {
        try {
            ArrayList<Subscriber> list = JsonToProjectList.convertToSubscribers(result);
            Resources res = getResources();
            RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerSubView);
            adapter = new SubscribersViewAdapter(list, getApplication());
            recyclerView.setAdapter(adapter);
            Integer num = adapter.getItemCount();
            String subscrFound = res.getQuantityString(R.plurals.numberOfSubscribers, num, num);
            TextView numTv = findViewById(R.id.subscrCntTv);
            numTv.setText(subscrFound);
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
                break;
            case Progress.CONNECT_SUCCESS:
                break;
            case Progress.GET_INPUT_STREAM_SUCCESS:
                //   ...
                break;
            case Progress.PROCESS_INPUT_STREAM_IN_PROGRESS:
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
