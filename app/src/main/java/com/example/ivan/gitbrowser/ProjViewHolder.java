package com.example.ivan.gitbrowser;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * Created by ivan on 20.12.17.
 *
 */

class ProjViewHolder extends RecyclerView.ViewHolder {
    CardView cv;
    TextView title;
    TextView description;
    TextView forks;
    ImageView imageView;

    ProjViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        title = (TextView) itemView.findViewById(R.id.title);
        description = (TextView) itemView.findViewById(R.id.description);
        forks = (TextView) itemView.findViewById(R.id.forks_num);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }

}
