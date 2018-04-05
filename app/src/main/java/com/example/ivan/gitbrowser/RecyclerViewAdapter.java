package com.example.ivan.gitbrowser;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

/**
 * Created by ivan on 20.12.17.
 * an adapter fo recyclerView widget
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<ProjViewHolder> {

    private List<GitProject> list = Collections.emptyList();
    private Resources resources;
    private ItemClickListener clickListener;

    RecyclerViewAdapter(List<GitProject> list, Context context, ItemClickListener listener) {
        this.list = list;
        this.clickListener = listener;
    }

    @Override
    public ProjViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //Inflate the layout, initialize the View Holder
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_project, parent, false);
        final ProjViewHolder projViewHolder = new ProjViewHolder(v);
        v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onItemClick(v, projViewHolder.getAdapterPosition());
            }
        });
        return projViewHolder;
    }

    public Resources getResources() {
        return resources;
    }

    void setResources(Resources resources) {
        this.resources = resources;
    }

    public void onBindViewHolder(ProjViewHolder holder, int position) {

        //Use the provided View Holder on the onCreateViewHolder method to populate the current row on the RecyclerView
        holder.title.setText(list.get(position).getRepositoryName());
        holder.description.setText(list.get(position).getDescription());
        Integer forksNum = list.get(position).getNumOfForks();
        String forksTxt = resources.getQuantityString(R.plurals.numberOfForks, forksNum, forksNum);
        holder.forks.setText(forksTxt);
        //Use ImageDownloader to bind image.
        ImageDownloader downloader = new ImageDownloader();
        downloader.download(list.get(position).getAvatar(), holder.imageView);
        holder.cv.setId(list.get(position).getId());
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

    public void setClickListener(ItemClickListener itemClickListener) {
        this.clickListener = itemClickListener;
    }

}
