package com.kpstv.youtube.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.PlayerActivity;
import com.kpstv.youtube.R;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.MyViewHolder> {

    private ArrayList<SearchModel> dataSet;

    Context con;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        ImageView imageView;
        CardView mainCard;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.sTitle);
            this.imageView = itemView.findViewById(R.id.sImage);
            this.mainCard = itemView.findViewById(R.id.cardView);
        }
    }

    public SearchAdapter(ArrayList<SearchModel> data, Context context) {
        this.dataSet = data;
        this.con = context;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.search_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final SearchModel searchModel = dataSet.get(listPosition);

        holder.titleText.setText(searchModel.getTitle());

        Glide.with(con).load(searchModel.getImageUrl()).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                holder.imageView.setImageDrawable(resource);
                return true;
            }
        }).into(holder.imageView);

        holder.mainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) con;
                Intent intent = new Intent(con,PlayerActivity.class);
                intent.putExtra("youtubelink",new String[]{
                        searchModel.getYturl()
                });
                con.startActivity(intent);
                activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
            }
        });

    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}
