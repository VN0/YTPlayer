package com.kpstv.youtube.fragments;


import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.kpstv.youtube.R;
import com.kpstv.youtube.DiscoverActivity;
import com.kpstv.youtube.SearchActivity;
import com.kpstv.youtube.adapters.SearchAdapter;
import com.kpstv.youtube.models.SearchModel;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.YTSearch;

import java.util.ArrayList;

public class SearchFragment extends Fragment {

    View v;
    RecyclerView recyclerView;
    static RecyclerView.LayoutManager layoutManager;
    SearchAdapter adapter; boolean networkCreated; ArrayList<String> images;
    ArrayList<SearchModel> models; RelativeLayout progresslayout;
    ArrayList<Drawable> drawables; Activity activity; TextView moreTrend;
    CardView discoverViral, searchCard; boolean istrendloaded,isdiscoverloaded;

    AsyncTask<Void,Void,Void> trendTask, discoverTask;

    private static String SpotifyTrendsCSV, SpotifyViralCSV;

    ImageView imageView1;
    ImageView imageView2;
    ImageView imageView3;
    ImageView imageView4;

    public SearchFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        if (!networkCreated) {
            v = inflater.inflate(R.layout.fragment_search, container, false);

            activity = getActivity();

            models = new ArrayList<>();
            drawables = new ArrayList<>();
            images = new ArrayList<>();

            searchCard = v.findViewById(R.id.cardView_search);
            imageView1 = v.findViewById(R.id.dImage1);
            moreTrend = v.findViewById(R.id.moreTrending);
            imageView2 = v.findViewById(R.id.dImage2);
            imageView3 = v.findViewById(R.id.dImage3);
            imageView4 = v.findViewById(R.id.dImage4);

            recyclerView = v.findViewById(R.id.my_recycler_view);
            discoverViral = v.findViewById(R.id.discoverViral);
            progresslayout = v.findViewById(R.id.progressLayout);
            layoutManager = new LinearLayoutManager(getContext(),
                    LinearLayoutManager.HORIZONTAL,true);
            recyclerView.setLayoutManager(layoutManager);

            searchCard.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity,SearchActivity.class);
                    intent.putExtra("data_csv",SpotifyViralCSV);
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.right_enter,R.anim.left_exit);
                }
            });

            moreTrend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity,DiscoverActivity.class);
                    intent.putExtra("data_csv",SpotifyTrendsCSV);
                    intent.putExtra("title","Discover Trends");
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                }
            });

            discoverViral.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity,DiscoverActivity.class);
                    intent.putExtra("data_csv",SpotifyViralCSV);
                    intent.putExtra("title","Discover Viral");
                    activity.startActivity(intent);
                    activity.overridePendingTransition(R.anim.slide_up,R.anim.slide_down);
                }
            });

            networkCreated = true;

            trendTask = new getTrending();
            trendTask.execute();

            discoverTask = new loadDiscoverImages();
            discoverTask.execute();
        }

        return v;
    }

    @Override
    public void onResume() {
        if (!istrendloaded && trendTask.getStatus() != AsyncTask.Status.RUNNING)
        {
            trendTask = new getTrending();
            trendTask.execute();
        }
        if (!isdiscoverloaded && discoverTask.getStatus() != AsyncTask.Status.RUNNING)
        {
            discoverTask = new loadDiscoverImages();
            discoverTask.execute();
        }
        if (drawables.size()>3) {
            imageView1.setImageDrawable(drawables.get(0));
            imageView2.setImageDrawable(drawables.get(1));
            imageView3.setImageDrawable(drawables.get(2));
            imageView4.setImageDrawable(drawables.get(3));
        }
        super.onResume();
    }

    @Override
    public void onPause() {
        trendTask.cancel(true);
        discoverTask.cancel(true);
        super.onPause();
    }

    class getTrending extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {

            recyclerView.setItemAnimator(new DefaultItemAnimator());
            adapter = new SearchAdapter(models,activity);
            recyclerView.setAdapter(adapter);
            recyclerView.getLayoutManager().scrollToPosition(models.size()-1);
            progresslayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);

            istrendloaded = true;
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (SpotifyTrendsCSV==null) {
                HttpHandler handler = new HttpHandler();
                SpotifyTrendsCSV = handler.makeServiceCall(
                        "https://spotifycharts.com/regional/global/daily/latest/download");
            }
            if (models.size()<10) {
                models.clear();
                String[] csvlines = SpotifyTrendsCSV.split("\n|\r");
                for (int i=2;i<12;i++) {
                    String line = csvlines[i];
                    String title = line.split(",")[1].replace("\"","");
                    String author = line.split(",")[2].replace("\"","");

                    String search_text = title.replace(" ","+")
                            + "+by+" + author.replace(" ","+");

                    YTSearch ytSearch = new YTSearch(search_text);

                    final String videoId = ytSearch.getVideoIDs().get(0);
                    String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";

                    Log.e("TrendingLines",line.split(",")[1].replace("\"",""));
                    models.add(0,new SearchModel(
                            title, imgurl, "https://www.youtube.com/watch?v="+videoId
                    ));
                }
            }
            return null;
        }
    }

    class loadDiscoverImages extends AsyncTask<Void,Void,Void> {

        @Override
        protected void onPostExecute(Void aVoid) {
            loadImageGlide(images.get(0),imageView1);
            loadImageGlide(images.get(1),imageView2);
            loadImageGlide(images.get(2),imageView3);
            loadImageGlide(images.get(3),imageView4);

            isdiscoverloaded=true;
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            if (SpotifyViralCSV==null) {
                HttpHandler handler = new HttpHandler();
                SpotifyViralCSV = handler.makeServiceCall(
                        "https://spotifycharts.com/viral/global/daily/latest/download"
                );
            }
            if (images.size()<4) {
                images.clear();
                String[] csvlines = SpotifyViralCSV.split("\n|\r");
                for (int i=1;i<5;i++) {
                    String line = csvlines[i];
                    String title = line.split(",")[1].replace("\"","");
                    String author = line.split(",")[2].replace("\"","");

                    String search_text = title.replace(" ","+")
                            + "+by+" + author.replace(" ","+");

                    YTSearch ytSearch = new YTSearch(search_text);

                    final String videoId = ytSearch.getVideoIDs().get(0);
                    String imgurl = "https://i.ytimg.com/vi/"+videoId+"/mqdefault.jpg";

                    images.add(imgurl);
                }
            }
            return null;
        }
    }

    void loadImageGlide(String url,final ImageView imageView) {
        Glide.with(v).load(url).addListener(new RequestListener<Drawable>() {
            @Override
            public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                return false;
            }

            @Override
            public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                drawables.add(resource);
                imageView.setImageDrawable(resource);
                return true;
            }
        }).into(imageView);
    }

}