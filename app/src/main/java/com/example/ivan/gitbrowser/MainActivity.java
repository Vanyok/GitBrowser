package com.example.ivan.gitbrowser;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.SearchRecentSuggestions;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
    private SearchView mSearchView;
    private MenuItem searchMenuItem;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        statusET = (TextView) findViewById(R.id.status_tv);
        mNetworkFragment = NetworkFragment.getInstance(getSupportFragmentManager(), getString(R.string.gitUrl));
        SharedPreferences sharedPref = getPreferences(Context.MODE_PRIVATE);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        String response = sharedPref.getString(getString(R.string.gitResKey), "");
        if (!response.equals("")) {
            updateList(response);
        }
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        String searchQuery = "";
        //store the search and display it
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            //get search query
            searchQuery = intent.getStringExtra(SearchManager.QUERY);

            //save search
            SearchRecentSuggestions suggestions = new SearchRecentSuggestions(this,
                    GbSuggestionProvider.AUTHORITY, GbSuggestionProvider.MODE);
            suggestions.saveRecentQuery(searchQuery, null);

            //set search to the textview
            progressBar.setVisibility(View.VISIBLE);
            doSearchGits(searchQuery);

        }else if (Intent.ACTION_VIEW.equals(intent.getAction())) {
            // Handle a suggestions click (because the suggestions all use ACTION_VIEW)
            searchQuery = intent.getStringExtra(SearchManager.QUERY);

            //set search term to the textview
            progressBar.setVisibility(View.VISIBLE);
            doSearchGits(searchQuery);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_search, menu);
        SearchManager searchManager =
                (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.action_search);
        mSearchView = (SearchView) searchMenuItem.getActionView();
       // mSearchView.setOnQueryTextListener(this);
        mSearchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        mSearchView.setIconifiedByDefault(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_search:
                // User chose the "Settings" item, show the app settings UI...
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    /**
     * Called when user taps search button
     */
    public void doSearchGits(String keyword) {
        //build query string and pass it to network fragment
        String filters = null;
        try {
            filters = "?q=" + URLEncoder.encode(keyword, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
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
        progressBar.setVisibility(View.INVISIBLE);
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
