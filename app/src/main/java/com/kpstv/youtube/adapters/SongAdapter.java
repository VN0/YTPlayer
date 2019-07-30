package com.kpstv.youtube.adapters;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import com.kpstv.youtube.models.DiscoverModel;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTSearch;
import com.kpstv.youtube.utils.YTutils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class SongAdapter extends RecyclerView.Adapter<SongAdapter.MyViewHolder> {

    private ArrayList<DiscoverModel> dataSet;
    private ArrayList<String> yturls;
    Context con;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        TextView AuthorText;
        ImageView imageView;
        LinearLayout mainLayout;

        public MyViewHolder(View itemView) {
            super(itemView);
            this.titleText = itemView.findViewById(R.id.aTitle);
            this.AuthorText = itemView.findViewById(R.id.aAuthor);
            this.imageView = itemView.findViewById(R.id.aImage);
            this.mainLayout = itemView.findViewById(R.id.mainlayout);
        }
    }

    public SongAdapter(ArrayList<DiscoverModel> data, Context context) {
        this.dataSet = data;
        this.con = context;
        yturls = new ArrayList<>();
        for (DiscoverModel model: data)
            yturls.add(0,model.getYtUrl());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent,
                                           int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_item, parent, false);

        MyViewHolder myViewHolder = new MyViewHolder(view);

        return myViewHolder;
    }
    @Override
    public void onBindViewHolder(final MyViewHolder holder, final int listPosition) {

        final DiscoverModel discoverModel = dataSet.get(listPosition);
/*
        final ArrayList<String> urls = yturls;
        urls.remove(searchModel.getYturl());
        urls.add(0,searchModel.getYturl());
        final String[] stringarray = YTutils.ConvertToStringArray(urls);*/

        holder.titleText.setText(discoverModel.getTitle());
        holder.AuthorText.setText(discoverModel.getAuthor());

        Glide.with(con).load(discoverModel.getImgUrl()).addListener(new RequestListener<Drawable>() {
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

        holder.mainLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Activity activity = (Activity) con;
                if (discoverModel.getYtUrl()==null)
                    new layoutListener(activity,discoverModel).execute();
                else {
                    RunLink(discoverModel.getYtUrl(),activity);
                }

            }
        });

    }

    public class layoutListener extends AsyncTask<Void, Void, Void> {

        private ProgressDialog dialog;
        private Activity activity;
        private DiscoverModel discoverModel;
        private String videoId;

        public layoutListener(Activity activity, DiscoverModel model) {
            this.activity = activity;
            this.discoverModel = model;
            dialog = new ProgressDialog(activity);
        }

        protected void onPreExecute() {
            this.dialog.setMessage("Parsing Url...");
            this.dialog.show();
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            dialog.dismiss();
            RunLink("https://www.youtube.com/watch?v="+videoId,activity);
            super.onPostExecute(aVoid);
        }

        protected Void doInBackground(Void... voids) {
            String search_text = discoverModel.getTitle()
                    + "+by+" + discoverModel.getAuthor();

            YTSearch ytSearch = new YTSearch(search_text);

            videoId = ytSearch.getVideoIDs().get(0);

            return null;
        }
    }

    void RunLink(String link, Activity activity) {
        Intent intent = new Intent(con,PlayerActivity.class);
        intent.putExtra("youtubelink",new String[] {link});
        con.startActivity(intent);
        activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
    }

    @Override
    public int getItemCount() {

        return dataSet.size();
    }
}