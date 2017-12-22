package com.example.ivan.gitbrowser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by ivan on 20.12.17.
 * Helper which convertToProjects Json string to list of projects
 */

class JsonToProjectList {
    static ArrayList<GitProject> convertToProjects(String jsonString) throws JSONException {
        JSONObject obj = new JSONObject(jsonString);
        JSONArray array = obj.getJSONArray("items");
        ArrayList<GitProject> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(new GitProject(
                    array.getJSONObject(i).getInt("id"),
                    array.getJSONObject(i).getJSONObject("owner").getString("avatar_url"),
                    array.getJSONObject(i).getString("full_name"),
                    array.getJSONObject(i).getString("description"),
                    array.getJSONObject(i).getInt("forks"),
                    array.getJSONObject(i).getString("subscribers_url")
            ));
        }
        return list;
    }

    static ArrayList<Subscriber> convertToSubscribers(String jsonString) throws JSONException {
        JSONArray array = new JSONArray(jsonString);
        ArrayList<Subscriber> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(new Subscriber(
                    array.getJSONObject(i).getString("login"),
                    array.getJSONObject(i).getString("avatar_url")
            ));
        }
        return list;
    }

}
