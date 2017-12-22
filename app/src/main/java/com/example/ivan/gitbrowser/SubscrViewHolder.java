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

class SubscrViewHolder extends RecyclerView.ViewHolder {
    CardView cv;
    TextView login;
    ImageView imageView;

    SubscrViewHolder(View itemView) {
        super(itemView);
        cv = (CardView) itemView.findViewById(R.id.cardView);
        login = (TextView) itemView.findViewById(R.id.login);
        imageView = (ImageView) itemView.findViewById(R.id.imageView);
    }

}
