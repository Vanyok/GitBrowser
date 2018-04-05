package com.example.ivan.gitbrowser;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by ivan on 20.12.17.
 * an adapter for recyclerView widget
 */

public class SubscribersViewAdapter extends RecyclerView.Adapter<SubscrViewHolder> {

    private List<Subscriber> list = Collections.emptyList();
    private Context context;


    SubscribersViewAdapter(List<Subscriber> list, Context context) {
        this.list = list;
        this.context = context;
    }

    @Override
    public SubscrViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_subscriber, parent, false);

        return new SubscrViewHolder(v);
    }

    public void onBindViewHolder(SubscrViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.login.setText(list.get(position).getLogin());
        //Use ImageDownloader to bind image.
        ImageDownloader downloader = new ImageDownloader();
        downloader.download(list.get(position).getAvatar(), holder.imageView);

    }

    @Override
    public int getItemCount() {
        //returns the number of elements the RecyclerView will display
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }


}
