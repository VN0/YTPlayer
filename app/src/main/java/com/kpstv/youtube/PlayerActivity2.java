package com.kpstv.youtube;

import android.Manifest;
import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.graphics.Palette;
import android.text.Spanned;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.TransitionOptions;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.coremedia.iso.boxes.Container;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.InterstitialAd;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.kpstv.youtube.adapters.PlayerAdapter;
import com.kpstv.youtube.fragments.LyricBottomSheet;
import com.kpstv.youtube.models.LyricModel;
import com.kpstv.youtube.models.YTConfig;
import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.utils.HttpHandler;
import com.kpstv.youtube.utils.SoundCloud;
import com.kpstv.youtube.utils.YTMeta;
import com.kpstv.youtube.utils.YTStatistics;
import com.kpstv.youtube.utils.YTutils;
import com.spyhunter99.supertooltips.ToolTip;
import com.spyhunter99.supertooltips.ToolTipManager;
import com.tonyodev.fetch2.Fetch;
import com.warkiz.widget.IndicatorSeekBar;
import com.warkiz.widget.OnSeekChangeListener;
import com.warkiz.widget.SeekParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;

public class PlayerActivity2 extends AppCompatActivity implements AppInterface {

    static String YouTubeUrl;
    static ImageView backImage,backImage1, viewImage;

    static ConstraintLayout mainlayout;

    static TextView mainTitle, viewCount, currentDuration, totalDuration, channelTitle;

    static Activity activity; public static ProgressBar progressBar;

    static ImageButton previousFab, playFab, nextFab, repeatButton, downloadButton, playlistButton, youTubeButton,lyricButton;

    ImageButton navigationDown, shareButton; //static VisualizerView visualizerView;

    static ViewPager mainPager;
    static PlayerAdapter adapter;

    static ImageView mainImageView;

   // static ProgressBar mprogressBar;

    static IndicatorSeekBar indicatorSeekBar;
    private static InterstitialAd mInterstitialAd;

    public static ImageView favouriteButton, addToPlaylist;

    static Handler mHandler = new Handler();

    AsyncTask<String, String, String> mergeTask, cutTask, mp3Task;

    SharedPreferences preferences; boolean supportFFmpeg=false;

    static AsyncTask<Void,Void,Void> setData; static Spanned lyricText;

    int accentColor;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels;
        Log.e("DisplayMatrix",height+"");

        activity = this;

        setContentView(R.layout.activity_player_new);

        Rect rectangle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectangle);
        int statusBarHeight = rectangle.top;
        int contentViewTop =
                window.findViewById(Window.ID_ANDROID_CONTENT).getTop();
        int titleBarHeight= contentViewTop - statusBarHeight;

        accentColor = ContextCompat.getColor(this,R.color.colorAccent);

        preferences = getSharedPreferences("settings", MODE_PRIVATE);

        setTitle("");

        getAllViews();

        playFab.setOnClickListener(v -> changePlayBack(!MainActivity.isplaying));

        nextFab.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    MainActivity.playNext();
                   /* if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                        setData.cancel(true);
                    if (!MainActivity.localPlayBack)
                        setData = new loadData();
                    else setData = new loadData_Offline(MainActivity.videoID);
                    setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        previousFab.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    MainActivity.playPrevious();
                   /* if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                        setData.cancel(true);
                    setData = new loadData();
                    setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        lyricButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    Bundle args = new Bundle();
                    args.putSerializable("model",new LyricModel(MainActivity.videoTitle,lyricText));
                    LyricBottomSheet bottomSheet = new LyricBottomSheet();
                    bottomSheet.setArguments(args);
                    bottomSheet.show(getSupportFragmentManager(),"");

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        shareButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                   // shareButton.setVisibility();
                    Log.e(TAG, "onCreate: ShareButtonState:"+shareButton.getVisibility() );

                   if (MainActivity.localPlayBack) {
                       YTutils.shareFile(PlayerActivity2.activity,new File(MainActivity.videoID));
                   }else {
                       Intent intent = new Intent(Intent.ACTION_SEND);
                       intent.setType("text/plain");
                       intent.putExtra(Intent.EXTRA_TEXT,"Listen to "+MainActivity.videoTitle+" by "+MainActivity.channelTitle+" "+
                               YTutils.getYtUrl(MainActivity.videoID));
                       activity.startActivity(Intent.createChooser(intent,"Share"));
                   }

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        indicatorSeekBar.setOnSeekChangeListener(new OnSeekChangeListener() {
            @Override
            public void onSeeking(SeekParams seekParams) {

            }

            @Override
            public void onStartTrackingTouch(IndicatorSeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);
            }

            @Override
            public void onStopTrackingTouch(IndicatorSeekBar seekBar) {
                mHandler.removeCallbacks(mUpdateTimeTask);

                long progresstoSeek = YTutils.progressToTimer(seekBar.getProgress(), MainActivity.total_duration);
                Log.e("ProgresstoSeek", progresstoSeek + "");
                MainActivity.mMediaSessionCallback.onSeekTo(progresstoSeek);
               // MainActivity.player.seekTo(progresstoSeek);

                updateProgressBar();
            }
        });

        mainPager.addOnPageChangeListener(mainPageListener);

        adapter = new PlayerAdapter(activity,MainActivity.yturls);
        mainPager.setAdapter(adapter);

        loadAgain();

        Palette.generateAsync(MainActivity.bitmapIcon, palette -> {
            MainActivity.nColor = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));
            Log.e(TAG, "onCreate: Changing nColor: "+MainActivity.nColor );
            backImage.setColorFilter(MainActivity.nColor);
            backImage1.setColorFilter(MainActivity.nColor);
        });

        addToPlaylist.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    if (MainActivity.total_seconds==0)
                    {
                        Toast.makeText(activity, "Player is still processing!", Toast.LENGTH_SHORT).show();
                    }else
                    YTutils.addToPlayList(activity,MainActivity.videoID,MainActivity.videoTitle,
                            MainActivity.channelTitle,MainActivity.imgUrl,MainActivity.total_seconds);

                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        favouriteButton.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageView view = (ImageView ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                   MainActivity.actionFavouriteClicked();

                case MotionEvent.ACTION_CANCEL: {
                    ImageView view = (ImageView) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        downloadButton.setOnTouchListener((v, motionEvent) -> {
            switch (motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    /** For local playback stuff */
                    if (MainActivity.localPlayBack) {
                        if (MainActivity.yturls.size()>1)
                            startActivity(new Intent(activity,NPlaylistActivity.class));
                    }else {
                        if (Build.VERSION.SDK_INT>Build.VERSION_CODES.M) {
                            if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED) {
                                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                        100);
                            } else showListDialog();
                        }else showListDialog();

                    }

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        downloadButton.setOnLongClickListener(view -> {
            if (DownloadService.pendingJobs.size()>0) {
                Intent intent = new Intent(PlayerActivity2.this,DownloadActivity.class);
                startActivity(intent);
            }
            return true;
        });

        youTubeButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    YTutils.StartURLIntent(YouTubeUrl, this);

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        playlistButton.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN: {
                    ImageButton view = (ImageButton ) v;
                    view.setColorFilter(accentColor);
                    v.invalidate();
                    break;
                }
                case MotionEvent.ACTION_UP:

                    if (MainActivity.yturls.size()>1)
                        startActivity(new Intent(this,NPlaylistActivity.class));

                case MotionEvent.ACTION_CANCEL: {
                    ImageButton view = (ImageButton) v;
                    view.clearColorFilter();
                    view.invalidate();
                    break;
                }
            }
            return true;
        });

        repeatButton.setOnClickListener(v1->{
            MainActivity.isLoop = !MainActivity.isLoop;
            makeRepeat(MainActivity.isLoop);
        });

        navigationDown.setOnClickListener(view -> {
            callFinish();
        });

        if (!MainActivity.localPlayBack) {
            String url = MainActivity.yturls.get(MainActivity.ytIndex);
            if (url.contains("soundcloud.com"))
                setSoundCloudData(url);
        }

    }

    public static void setLyricData(Spanned text) {
        if (text!=null) {
            Log.e(TAG, "setLyricData: Show data here..." +AppSettings.showLyricTooltip );
            lyricText = text;
            lyricButton.setVisibility(View.VISIBLE);
        }else {
            lyricText=null;
            lyricButton.setVisibility(View.GONE);
        }
    }

    static void setSoundCloudData(String url) {
        if (url.contains("soundcloud.com")) {
            if (setData != null && setData.getStatus() == AsyncTask.Status.RUNNING)
                setData.cancel(true);
            setData = new soundcloud_data(url);
            setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    static ViewPager.OnPageChangeListener mainPageListener = new ViewPager.OnPageChangeListener() {
        @Override
        public void onPageScrolled(int i, float v, int i1) {

        }

        @Override
        public void onPageSelected(int pos) {
            try {
                Log.e(TAG, "onPageSelected: "+MainActivity.yturls.get(pos) );
                if (MainActivity.localPlayBack)
                    MainActivity.ChangeVideoOffline(pos);
                else
                    MainActivity.ChangeVideo(pos);
                String url = MainActivity.yturls.get(pos);
                setSoundCloudData(url);
              /*  if (setData!=null && setData.getStatus() == AsyncTask.Status.RUNNING)
                    setData.cancel(true);
                if (!MainActivity.localPlayBack) {
                    setData = new loadData();
                }
                else
                    setData = new loadData_Offline(MainActivity.yturls.get(pos));
                setData.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);*/
            }catch (Exception e) {
                e.printStackTrace();
            }
        }



        @SuppressLint("StaticFieldLeak")
        @Override
        public void onPageScrollStateChanged(int i) {
            /** When song is changed automatically this will keep background image...*/
           /* String imageUri = YTutils.getImageUrl(MainActivity.yturls.get(MainActivity.ytIndex));
            if (MainActivity.videoID.contains("soundcloud.com")) {

            }
                imageUri = MainActivity.imgUrl;
            Log.e(TAG, "onPageScrollStateChanged: Loading Image: "+imageUri);
            Glide.with(activity).asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.generateAsync(resource, palette -> {
                                int color = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));
                                MainActivity.bitmapIcon = resource;
                                MainActivity.nColor = color;
                                Log.e(TAG, "onPageScrolled: Changing nColor: "+MainActivity.nColor +
                                        ", ImageUri: "+MainActivity.imgUrl );
                                ChangeBackgroundColor(color);
                                MainActivity.rebuildNotification();
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });*/
        }
    };

    public static void loadAgain() {

        player_common();

        mainPager.removeOnPageChangeListener(mainPageListener);
        if (mainPager.getChildCount()!=MainActivity.yturls.size()) {
            adapter.notifyDataSetChanged();
        }
        if (mainPager.getCurrentItem()!=MainActivity.ytIndex)
        {
            mainPager.setCurrentItem(MainActivity.ytIndex,true);
        }
        mainPager.addOnPageChangeListener(mainPageListener);
    }

    static void player_common() {
        mainlayout.setVisibility(View.VISIBLE);
      //  mprogressBar.setVisibility(View.GONE);
        mainTitle.setText(MainActivity.videoTitle);
        channelTitle.setText(MainActivity.channelTitle);
        if (!MainActivity.localPlayBack)
        {
            viewCount.setVisibility(View.VISIBLE);
            viewCount.setText(MainActivity.viewCounts);
            viewImage.setVisibility(View.VISIBLE);
            favouriteButton.setVisibility(View.VISIBLE);
            addToPlaylist.setVisibility(View.VISIBLE);
            downloadButton.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_file_download));
            youTubeButton.setVisibility(View.VISIBLE);
            playlistButton.setVisibility(View.VISIBLE);

            YouTubeUrl = YTutils.getYtUrl(MainActivity.videoID);

            String data = YTutils.readContent(activity,"favourite.csv");
            if (data!=null && data.contains(MainActivity.videoID)) {
                MainActivity.isFavourite = true;
                favouriteButton.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite_full));
            }else {
                MainActivity.isFavourite=false;
                favouriteButton.setImageDrawable(activity.getDrawable(R.drawable.ic_favorite));
            }
        }else {
            viewCount.setVisibility(View.GONE);
            viewImage.setVisibility(View.GONE);
            favouriteButton.setVisibility(View.GONE);
            addToPlaylist.setVisibility(View.GONE);

            downloadButton.setImageDrawable(ContextCompat.getDrawable(activity,R.drawable.ic_playlist));

            youTubeButton.setVisibility(View.GONE);
            playlistButton.setVisibility(View.GONE);
        }

        if (MainActivity.viewCounts.equals("-1")) {
            viewCount.setVisibility(View.GONE);
            viewImage.setVisibility(View.GONE);
        }else {
            viewCount.setVisibility(View.VISIBLE);
            viewImage.setVisibility(View.VISIBLE);
        }

        if (MainActivity.yturls.size()>1)
            playlistButton.setEnabled(true);
        else playlistButton.setEnabled(false);

        // Loading color with animation...

        int colorTo = MainActivity.nColor;
        if (backImage.getTag()!=null) {
            ChangeBackgroundColor(colorTo);
        }else {
            backImage.setColorFilter(MainActivity.nColor);
            backImage1.setColorFilter(MainActivity.nColor);
        }
        backImage.setTag(colorTo);

        makeRepeat(MainActivity.isLoop);

        setLyricData(MainActivity.lyricText);

        totalDuration.setText(YTutils.milliSecondsToTimer(MainActivity.total_duration));
        detectPlayback();
        updateProgressBar();
    }

    static void ChangeBackgroundColor(int colorTo) {
        Log.e("Animation","I am animating bro...");
        int colorFrom = (int) backImage.getTag();
        ValueAnimator colorAnimation = ValueAnimator.ofObject(new ArgbEvaluator(), colorFrom, colorTo);
        colorAnimation.setDuration(300);
        colorAnimation.addUpdateListener(animator -> backImage.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.addUpdateListener(animator -> backImage1.setColorFilter((int) animator.getAnimatedValue()));
        colorAnimation.start();
    }

    private static final String TAG = "PlayerActivity2";

    static class soundcloud_data extends AsyncTask<Void,Void,Void> {
        SoundCloud soundCloud;String url;

        public soundcloud_data(String url) {
            this.url = url;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (soundCloud.getViewCount()!=null && !soundCloud.getViewCount().isEmpty()) {
                MainActivity.viewCounts = YTutils.getViewCount(Long.parseLong(soundCloud.getViewCount()));
                viewImage.setVisibility(View.VISIBLE);
                viewCount.setVisibility(View.VISIBLE);
                viewCount.setText(MainActivity.viewCounts);
            }
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            soundCloud = new SoundCloud(url);
            soundCloud.captureViews();
            return null;
        }
    }

    static class loadData_Offline extends AsyncTask<Void,Void,Void> {
        String filePath;

        public loadData_Offline(String filePath) {
            this.filePath = filePath;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            Palette.generateAsync(MainActivity.bitmapIcon, palette -> {
                MainActivity.nColor = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));

                loadAgain();
                MainActivity.rebuildNotification();
            });
            super.onPostExecute(aVoid);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            File f = new File(filePath);
            Uri uri = Uri.fromFile(f);
            try {
                MediaMetadataRetriever mmr = new MediaMetadataRetriever();
                mmr.setDataSource(activity,uri);
                String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                String artist = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                String title = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);

                byte [] data = mmr.getEmbeddedPicture();

                if (artist==null) artist ="Unknown artist";
                if (title==null) title = YTutils.getVideoTitle(f.getName());

                if (title.contains("."))
                    title = title.split("\\.")[0];

                MainActivity.videoTitle = title;
                MainActivity.channelTitle = artist;
                MainActivity.likeCounts = -1; MainActivity.dislikeCounts = -1;
                MainActivity.viewCounts = "-1";

                MainActivity.total_seconds = Integer.parseInt(durationStr);

            }catch (Exception e) {
                // TODO: Do something when cannot played...
            }
            return null;
        }
    }

    static class loadData extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPostExecute(Void aVoid) {
            String imageUri = YTutils.getImageUrl(MainActivity.yturls.get(MainActivity.ytIndex));
            if (MainActivity.videoID.contains("soundcloud.com"))
                imageUri = soundCloud.getModel().getImageUrl();
            Glide.with(activity)
                    .asBitmap()
                    .load(imageUri)
                    .into(new CustomTarget<Bitmap>() {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            Palette.generateAsync(resource, palette -> {
                                MainActivity.bitmapIcon = resource;
                                MainActivity.nColor = palette.getVibrantColor(activity.getResources().getColor(R.color.light_white));
                                Log.e(TAG, "loadData: Changing nColor: "+MainActivity.nColor +
                                        ", ImageUri: "+MainActivity.imgUrl );
                                loadAgain();
                                MainActivity.rebuildNotification();
                            });
                        }

                        @Override
                        public void onLoadCleared(@Nullable Drawable placeholder) {

                        }
                    });

            super.onPostExecute(aVoid);
        }

        String jsonResponse(String videoID, int apinumber) {
            HttpHandler httpHandler = new HttpHandler();
            String link = "https://www.googleapis.com/youtube/v3/videos?id=" + videoID + "&key=" + API_KEYS[apinumber] + "&part=statistics";
            return httpHandler.makeServiceCall(link);
        }
        SoundCloud soundCloud;
        @Override
        protected Void doInBackground(Void... voids) {
            String videoID = MainActivity.videoID;

            if (videoID.contains("soundcloud.com")) {
                soundCloud = new SoundCloud(videoID);
                if (soundCloud.getModel()==null || soundCloud.getModel().getStreamUrl()==null) {
                    return null;
                }
              //  soundCloud.captureViews();
                MainActivity.soundCloudPlayBack=true;
                MainActivity.videoTitle = soundCloud.getModel().getTitle();
                MainActivity.channelTitle = soundCloud.getModel().getAuthorName();
                MainActivity.imgUrl = soundCloud.getModel().getImageUrl();
                MainActivity.likeCounts = -1; MainActivity.dislikeCounts = -1;
                MainActivity.viewCounts = "-1";
                /*if (soundCloud.getViewCount()!=null && !soundCloud.getViewCount().isEmpty())
                    MainActivity.viewCounts =YTutils.getViewCount( Long.parseLong(soundCloud.getViewCount()));*/
                return null;
            }

            int i=0;
            int apiLength = API_KEYS.length;
            String json;
            do {
                json = jsonResponse(videoID, i);
                i++;
            }while (json.contains("\"error\":") && i<apiLength);

            YTMeta ytMeta = new YTMeta(videoID);
            if (ytMeta.getVideMeta() != null) {
                MainActivity.channelTitle = YTutils.getChannelTitle(ytMeta.getVideMeta().getTitle(),
                        ytMeta.getVideMeta().getAuthor());
                MainActivity.videoTitle = YTutils.setVideoTitle(ytMeta.getVideMeta().getTitle());
                MainActivity.imgUrl = ytMeta.getVideMeta().getImgUrl();
            }


            if (json.contains("\"error\":")) {
                YTStatistics ytStatistics = new YTStatistics(videoID);
                MainActivity.viewCounts = ytStatistics.getViewCount();
                MainActivity.likeCounts = Integer.parseInt(ytStatistics.getLikeCount());
                MainActivity.dislikeCounts = Integer.parseInt(ytStatistics.getDislikeCount());
                json = null;
            }

            if (json != null) {
                try {
                    JSONObject statistics = new JSONObject(json).getJSONArray("items")
                            .getJSONObject(0).getJSONObject("statistics");
                    MainActivity.viewCounts = YTutils.getViewCount(Long.parseLong(statistics.getString("viewCount")));
                    MainActivity.likeCounts = 100;
                    MainActivity.dislikeCounts = 0;
                    try {
                        MainActivity.likeCounts = Integer.parseInt(statistics.getString("likeCount"));
                        MainActivity.dislikeCounts = Integer.parseInt(statistics.getString("dislikeCount"));
                    }catch (Exception e){e.printStackTrace();}

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e("PlayerActivity_JSON", e.getMessage());
                }
            }
            return null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 100: {
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showListDialog();
                } else {
                    Toast.makeText(PlayerActivity2.this, "Permission denied!",
                            Toast.LENGTH_SHORT).show();
                }
                return;
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAgain();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);
        super.onDestroy();
    }

    /*@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.player_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == android.R.id.home) {
            callFinish();
            return true;
        } else if (itemId == R.id.action_youtube) {
            YTutils.StartURLIntent(YouTubeUrl, this);
        } else if (itemId == R.id.action_share) {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, YouTubeUrl);
            startActivity(Intent.createChooser(shareIntent, "Share using..."));
        } else if (itemId == R.id.action_add) {
            YTutils.addToPlayList(this, YouTubeUrl, MainActivity.total_duration / 1000);
        } else if (itemId == R.id.action_loop) {
            MainActivity.isLoop = !MainActivity.isLoop;
            item.setChecked(MainActivity.isLoop);
        }

        return super.onOptionsItemSelected(item);
    }*/

   /* @Override
    public boolean onSupportNavigateUp() {
        Log.e("OnSupportFinished","called");
        callFinish();
        return false;
    }*/

    public void startService(YTConfig model) {
        Intent serviceIntent = new Intent(this, DownloadService.class);
        serviceIntent.putExtra("addJob", model);

        ContextCompat.startForegroundService(this, serviceIntent);
    }

    public static void detectPlayback() {
        if (MainActivity.isplaying)
            makePause();
        else makePlay();
    }

    public void changePlayBack(boolean isplay) {
        MainActivity.changePlayBack(isplay);
        Log.e("PlayingState", "Playing State: " + MainActivity.isplaying + ", isPlay:" + isplay);
        if (isplay) {
            makePause();
        } else {
            makePlay();
        }
        Log.e("CurrentDur", MainActivity.player.getCurrentPosition() + "");
    }

    public static void hidePlayButton() {
        playFab.setEnabled(false);
        playFab.setColorFilter(activity.getResources().getColor(R.color.white));
    }

    public static void showPlayButton() {
        playFab.setEnabled(true);
        playFab.clearColorFilter();
    }

    public static void makePlay() {
        playFab.setImageDrawable(activity.getResources().getDrawable(R.drawable.play));
    }

    public static void makePause() {
        playFab.setImageDrawable(activity.getResources().getDrawable(R.drawable.pause));
    }

    public static void makeRepeat(boolean value) {
        if (value)
            repeatButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_repeat_true));
        else
            repeatButton.setImageDrawable(activity.getResources().getDrawable(R.drawable.ic_repeat));
    }

    private void getAllViews() {
        viewImage = findViewById(R.id.imageView3);
        mainPager = findViewById(R.id.viewPager);
        lyricButton = findViewById(R.id.lyricButton);
        shareButton = findViewById(R.id.shareButton);
//        visualizerView = findViewById(R.id.visualizerView);
        favouriteButton = findViewById(R.id.favourite_button);
        progressBar = findViewById(R.id.progressBar);
        addToPlaylist = findViewById(R.id.addPlaylist_button);
        navigationDown = findViewById(R.id.navigation_down);
      //  mprogressBar = findViewById(R.id.mainprogress);
        mainTitle = findViewById(R.id.maintitle);
        viewCount = findViewById(R.id.mainviews);
        currentDuration = findViewById(R.id.currentDur);
        totalDuration = findViewById(R.id.totalDur);
        mainImageView = findViewById(R.id.mainImage);
        indicatorSeekBar = findViewById(R.id.seekBar);
        mainlayout = findViewById(R.id.mainlayout);
        backImage = findViewById(R.id.background_image);
        backImage1 = findViewById(R.id.background_image1);
        channelTitle = findViewById(R.id.channelTitle);
        youTubeButton = findViewById(R.id.youtube_IButton);

        previousFab = findViewById(R.id.previous_IButton);
        playFab = findViewById(R.id.playPause_IButton);
        nextFab = findViewById(R.id.forward_IButton);
        repeatButton = findViewById(R.id.repeat_IButton);
        downloadButton = findViewById(R.id.download_IButton);
        playlistButton = findViewById(R.id.currentPlaylist_IButton);
    }

    void callFinish() {
        String toput = "true";
        if (!MainActivity.isplaying) toput = "false";
        Intent i = new Intent(this, MainActivity.class);
        i.putExtra("videoID",YouTubeUrl);
        i.putExtra("is_playing",toput);
        i.putExtra("b_title",mainTitle.getText().toString());
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(i);
    }

    public static void showAd() {
        if (!AppSettings.showAds)
            return;
        //TODO: Change ad unit ID, Sample ca-app-pub-3940256099942544/1033173712
        mInterstitialAd = new InterstitialAd(activity);
        mInterstitialAd.setAdUnitId("ca-app-pub-1164424526503510/4801416648");
        mInterstitialAd.loadAd(new AdRequest.Builder().build());
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                Log.e(TAG, "onAdFailedToLoad: Ad failed to load: " + i);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                mInterstitialAd.show();
            }
        });
    }

    void showListDialog() {

        //     Log.e("YOUTUBEURL",YouTubeUrl);
        ArrayList<String> tmplist = new ArrayList<>();
        final ArrayList<YTConfig> configs = new ArrayList<>();

        for (int i = 0; i < MainActivity.ytConfigs.size(); i++) {
            String text = MainActivity.ytConfigs.get(i).getText();
            boolean isalreadyadded = false;
            for (int j = 0; j < tmplist.size(); j++) {
                if (tmplist.get(j).contains(text))
                    isalreadyadded = true;
            }
            if (!isalreadyadded) {
                tmplist.add(MainActivity.ytConfigs.get(i).getText());
                configs.add(MainActivity.ytConfigs.get(i));
            }
        }

        final String[] arrays = new String[configs.size()];
        for (int i = 0; i < configs.size(); i++) {
            arrays[i] = configs.get(i).getText();
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(PlayerActivity2.this);
        builder.setTitle("Select Media Codec");

        builder.setItems(arrays, (dialog, which) -> {

            if (AppSettings.downloadCount<=0 && AppSettings.setDownloads) {
                View v = getLayoutInflater().inflate(R.layout.alert_buy_premium,null);
                new AlertDialog.Builder(this)
                        .setView(v).setPositiveButton("Purchase",(dialogInterface, i) -> {
                    YTutils.openPurchaseActivity(this);
                        }).setNegativeButton("Cancel",null)
                        .show();
                return;
            }else if (AppSettings.setDownloads) AppSettings.downloadCount--;
            YTConfig config = configs.get(which);
            config.setVideoID(MainActivity.videoID);
            config.setAudioUrl(MainActivity.audioLink);
            String filename;
            if (config.getText().length() > 55) {
                filename = config.getTitle().substring(0, 55).trim() + "." + config.getExt();
            } else {
                filename = config.getChannelTitle().trim() +" - " + config.getTitle() + "." + config.getExt();
            }
            filename = filename.replaceAll("[\\\\><\"|*?%:#/]", "");
            final String fileCurrent = filename; // Using this since current filename cannot be placed as final
            if (arrays[which].contains("(no audio)")) {

                config.setTargetName(fileCurrent.split("\\.")[0]+".mp4");
                config.setTaskExtra("mergetask");
                startService(config);

               /* int icon = android.R.drawable.ic_dialog_info;
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity2.this);
                alert.setIcon(icon);
                alert.setTitle("Merge");
                alert.setMessage("The current sample you selected does not contain audio stream.\n\nDo you want to merge the audio with it?");
                alert.setPositiveButton("Yes", (dialog1, which1) -> {
                    showAd();
                    mergeTask = new MergeAudioVideo(PlayerActivity2.this,"/sdcard/Download/"+fileCurrent);
                    mergeTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,MainActivity.audioLink,config.getUrl());
                });
                alert.setNegativeButton("No", (dialog12, which12) -> {
                    downloadFromUrl(fileCurrent, config);

                    Toast.makeText(PlayerActivity2.this, "Download started",
                            Toast.LENGTH_SHORT).show();
                    showAd();
                });
                alert.setNeutralButton("Cancel",null);
                alert.show();*/

            } else if (arrays[which].contains("Audio ")) {

                config.setTargetName(fileCurrent.split("\\.")[0]+".mp3");
                config.setTaskExtra("mp3task");
                startService(config);

               /* int icon = android.R.drawable.ic_dialog_info;
                final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity2.this);
                alert.setIcon(icon);
                alert.setTitle("Edit Sample");
                alert.setMessage("Do you want to download and cut sample in editor?\n\nIf so select \"Cut\" else \"Normal\" to begin usual download.");
                alert.setPositiveButton("Cut", (dialog1, which1) -> {
                    showAd();
                    cutTask = new cutTask(PlayerActivity2.this,
                            "Download/"+fileCurrent);
                    cutTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,config.getUrl());
                });
                alert.setNeutralButton("Cancel",null);
                alert.setNegativeButton("Normal", (dialog12, which12) -> {
                    downloadFromUrl(fileCurrent, config);

                    showAd();
                });
                alert.show();*/

            }else
                downloadFromUrl(fileCurrent, config);

            Toast.makeText(PlayerActivity2.this, "Download started",
                    Toast.LENGTH_SHORT).show();
            showAd();
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private Fetch fetch;
    private void downloadFromUrl(String fileName, YTConfig config) {

        if (config.isAudio() && supportFFmpeg && !config.getExt().equals(".mp3")) {

            final AlertDialog.Builder alert= new AlertDialog.Builder(PlayerActivity2.this);
            alert.setIcon( android.R.drawable.ic_dialog_info);
            alert.setTitle("MP3 Conversion");
            alert.setMessage("The current sample you've selected is not an mp3 stream.\n\nDo you want to convert song into mp3?");
            alert.setPositiveButton("Yes", (dialog1, which1) -> {
                showAd();

                String targetName = fileName.split("\\.")[0]+".mp3";

                mp3Task = new MP3Task(PlayerActivity2.this,targetName,config, MainActivity.videoID);
                mp3Task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR,config.getUrl());
            });
            alert.setNegativeButton("No", (dialog12, which12) -> {
                //   downloadFromUrl(fileCurrent, config);

                Toast.makeText(PlayerActivity2.this, "Download started",
                        Toast.LENGTH_SHORT).show();
                showAd();
                downloadNormal(fileName,config);
            });
            alert.setNeutralButton("Cancel",null);
            alert.show();
        }else {
            downloadNormal(fileName,config);
        }
    }

    private void downloadNormal(String fileName, YTConfig config) {
        Uri uri = Uri.parse(config.getUrl());
        DownloadManager.Request request = new DownloadManager.Request(uri);
        request.setTitle(config.getTitle()+" - "+config.getChannelTitle());

        request.allowScanningByMediaScanner();
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        manager.enqueue(request);
    }

    class MP3Task extends AsyncTask<String, String, String> {

        AlertDialog alertdialog;
        View dialogView;
        TextView tview, dtview;
        ProgressBar bar; YTConfig config;
        Context con; String fileLengthString="0";YTMeta ytMeta;
        String targetName; String videoID;boolean isConverted=false;

        public MP3Task(Context context, String fileName, YTConfig config, String videoID) {
            this.con = context;
            this.targetName = fileName;
            this.config = config;
            this.videoID = videoID;
        }

        @Override
        protected void onPreExecute() {
            Log.e("ExecutingTask","true");
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_merger, null);
            tview = dialogView.findViewById(R.id.textView);
            dtview = dialogView.findViewById(R.id.textView_Download);
            bar = dialogView.findViewById(R.id.progressBar);
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity2.this);
            alert.setTitle("Download");
            alert.setMessage("This could take a while depending upon length of audio!");
            alert.setCancelable(false);
            alert.setView(dialogView);
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                mp3Task.cancel(true);
            });
            alertdialog = alert.create();
            alertdialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tview.setText(values[1]);
            if (Integer.parseInt(values[0])==-1) {
                bar.setIndeterminate(true);
                return;
            }
            bar.setIndeterminate(false);
            bar.setProgress(Integer.parseInt(values[0]));
            dtview.setText(YTutils.getSize(Long.parseLong(values[2]))+" / "+fileLengthString);
        }

        @Override
        protected void onPostExecute(String s) {
            alertdialog.dismiss();
            if (YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+targetName).exists())
                Toast.makeText(con, "Download Completed", Toast.LENGTH_LONG).show();
            else Toast.makeText(con, "Download Failed", Toast.LENGTH_LONG).show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {

                String prefixName = config.getTitle()+"_"+config.getChannelTitle();

                File mp3 = YTutils.getFile("YTPlayer/"+prefixName+".mp3");
                if (mp3.exists()) mp3.delete();
                File f = YTutils.getFile("YTPlayer/"+prefixName+".file");
                if (f.exists()) f.delete();


                String audioUrl = sUrl[0];

                // Download audio file first...
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                long fileLength = connection.getContentLength();
                fileLengthString = YTutils.getSize(fileLength);
                File root = Environment.getExternalStorageDirectory();

                DataInputStream input = new DataInputStream(url.openStream());
                DataOutputStream output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/"+prefixName+".file"));

                byte data[] = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "",
                            "Downloading Audio... 1/2",total+"");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Convert the audio file to mp3...

                publishProgress((-1) + "",
                        "Converting to mp3... 2/2");


              /*  FFmpeg ffmpeg = FFmpeg.getInstance(con);
                try {
                    String cmd[] = new String[] { "-y","-i",f.getPath(),mp3.getPath() };
                    // to execute "ffmpeg -version" command you just need to pass "-version"
                    ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                        @Override
                        public void onStart() {}

                        @Override
                        public void onProgress(String message) {}

                        @Override
                        public void onFailure(String message) {
                            isConverted=true;
                            Log.e("FailedToDownload","true");
                        }

                        @Override
                        public void onSuccess(String message) {
                            isConverted=true;
                            // We will set tag here...
                            MusicMetadataSet src_set = null;
                            try {
                                src_set = new MyID3().read(mp3);
                            } catch (IOException e1) {
                                e1.printStackTrace();
                            }

                            if (src_set == null)
                            {
                                Log.i("NULL", "NULL");
                            }
                            else
                            {
                                URL uri = null; ImageData imageData=null;
                                try {
                                    uri = new URL(YTutils.getImageUrlID(videoID));
                                    Bitmap bitmap = BitmapFactory.decodeStream(uri.openConnection().getInputStream());
                                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                                    byte[] bitmapdata = stream.toByteArray();
                                    imageData = new ImageData(bitmapdata,"image/jpeg","arun photo",1);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }

                                File dst = YTutils.getFile(Environment.DIRECTORY_DOWNLOADS+"/"+targetName);
                                MusicMetadata meta = new MusicMetadata(YTutils.getVideoTitle(config.getTitle()));
                                if (imageData!=null) {
                                    meta.addPicture(imageData);
                                }

                                meta.setAlbum(ytMeta.getVideMeta().getAuthor());
                                meta.setArtist(YTutils.getChannelTitle(config.getTitle(),config.getChannelTitle()));
                                try {
                                    new MyID3().write(mp3, dst, src_set, meta);
                                    mp3.delete();
                                } catch (UnsupportedEncodingException e) {
                                    e.printStackTrace();
                                } catch (ID3WriteException e) {
                                    e.printStackTrace();
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                        }

                        @Override
                        public void onFinish() {}
                    });
                } catch (FFmpegCommandAlreadyRunningException e) {
                    // Handle if FFmpeg is already
                    Log.e("PlayerActivity2", "doInBackground: Already Running" );
                    e.printStackTrace();
                }*/


              /*  IConvertCallback callback = new IConvertCallback() {
                    @Override
                    public void onSuccess(File convertedFile) {

                    }

                    @Override
                    public void onFailure(Exception error) {
                        isConverted=true;
                        Log.e("FailedToDownload","true");
                    }
                };*/

               /* AndroidAudioConverter.with(PlayerActivity2.this)
                        .setFile(f)
                        .setFormat(AudioFormat.MP3)
                        .setCallback(callback)
                        .convert();*/

                ytMeta = new YTMeta(videoID);

                do {
                    if (mp3.exists()) {
                        total = mp3.length();
                        publishProgress(((int) (total * 100 / fileLength)) + "",
                                "Converting... 2/2",total+"");
                    }
                }while (!isConverted);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class cutTask extends AsyncTask<String, String, String> {

        AlertDialog alertdialog;
        View dialogView;
        TextView tview, dtview;
        ProgressBar bar;
        Context con; String fileLengthString="0";
        String target,fileName;

        public cutTask(Context context, String targetfile) {
            this.con = context;
            this.target = targetfile;
        }

        @Override
        protected void onPreExecute() {
            Log.e("ExecutingTask","true");
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_merger, null);
            tview = dialogView.findViewById(R.id.textView);
            dtview = dialogView.findViewById(R.id.textView_Download);
            bar = dialogView.findViewById(R.id.progressBar);
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity2.this);
            alert.setTitle("Download");
            alert.setMessage("This could take a while depending upon length of audio!");
            alert.setCancelable(false);
            alert.setView(dialogView);
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                cutTask.cancel(true);
            });
            alertdialog = alert.create();
            alertdialog.show();
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tview.setText(values[1]);
            if (Integer.parseInt(values[0])==-1) {
                bar.setIndeterminate(true);
                return;
            }
            bar.setIndeterminate(false);
            bar.setProgress(Integer.parseInt(values[0]));
            dtview.setText(YTutils.getSize(Long.parseLong(values[2]))+" / "+fileLengthString);
        }

        @Override
        protected void onPostExecute(String s) {
            //   Toast.makeText(PlayerActivity.this, "Saved at /sdcard/"+target, Toast.LENGTH_LONG).show();
            Log.e("FileName",fileName);
            alertdialog.dismiss();
            startEditor("file:/"+YTutils.getFile("YTPlayer/"+fileName).toString());
        }

        private void startEditor(String filePathUri) {
            Intent intent = new Intent(con, RingdroidEditActivity.class);
            intent.putExtra("FILE_PATH", filePathUri);
            startActivity(intent);
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                fileName = YTutils.getFile(target).getName();

                String audioUrl = sUrl[0];

                // Download audio file first...
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                long fileLength = connection.getContentLength();
                fileLengthString = YTutils.getSize(fileLength);
                File root = Environment.getExternalStorageDirectory();

                DataInputStream input = new DataInputStream(url.openStream());
                DataOutputStream output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/"+fileName));

                byte data[] = new byte[8192];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "",
                            "Downloading Audio... 1/2",total+"");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Trimming audio

                /*publishProgress((-1) + "", "Trimming media... 2/2");
                Mp4Cutter mp4Cutter = new Mp4Cutter();
                mp4Cutter.startTrim(
                        YTutils.getFile("YTPlayer/audio.download"),
                        YTutils.getFile(target),
                        currentDuration, totalDuration

                );*/
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class MergeAudioVideo extends AsyncTask<String, String, String> {

        AlertDialog alertdialog;
        View dialogView;
        TextView tview, dtview;
        ProgressBar bar; String fileLengthString;
        Context con;
        String target;

        public MergeAudioVideo(Context context, String targetfile) {
            this.con = context;
            this.target = targetfile;
        }

        @Override
        protected void onPreExecute() {
            Log.e("ExecutingTask","true");
            LayoutInflater inflater = getLayoutInflater();
            dialogView = inflater.inflate(R.layout.alert_merger, null);
            tview = dialogView.findViewById(R.id.textView);
            dtview = dialogView.findViewById(R.id.textView_Download);
            bar = dialogView.findViewById(R.id.progressBar);
            AlertDialog.Builder alert = new AlertDialog.Builder(PlayerActivity2.this);
            alert.setTitle("Merging");
            alert.setMessage("This could take a while depending upon length of video!");
            alert.setCancelable(false);
            alert.setView(dialogView);
            alert.setNegativeButton("Cancel", (dialog, which) -> {
                mergeTask.cancel(true);
            });
            alertdialog = alert.create();
            alertdialog.show();
        }

        @Override
        protected String doInBackground(String... sUrl) {
            try {
                String audioUrl = sUrl[0];
                String videoUrl = sUrl[1];

                // Download audio file first...
                URL url = new URL(audioUrl);
                URLConnection connection = url.openConnection();
                connection.connect();

                long fileLength = connection.getContentLength();
                fileLengthString = YTutils.getSize(fileLength);
                File root = Environment.getExternalStorageDirectory();

                DataInputStream input = new DataInputStream(url.openStream());
                DataOutputStream output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/audio.download"));


                byte data[] = new byte[4096];
                long total = 0;
                int count;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "",
                            "Downloading Audio... 1/3",total+"");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Download video file second...
                url = new URL(videoUrl);
                connection = url.openConnection();
                connection.connect();

                fileLength = connection.getContentLength();
                fileLengthString = YTutils.getSize(fileLength);
                input = new DataInputStream(url.openStream());
                output = new DataOutputStream(new FileOutputStream(
                        root.getAbsolutePath() + "/YTPlayer/video.download"));

                total = 0;
                while ((count = input.read(data)) != -1) {
                    total += count;
                    publishProgress(((int) (total * 100 / fileLength)) + "", "Downloading Video... 2/3",total+"");
                    output.write(data, 0, count);
                    output.flush();
                }
                output.flush();
                output.close();
                input.close();

                // Merging audio and video third
                publishProgress((-1) + "", "Merging media... 3/3");
                mux("/sdcard/YTPlayer/video.download","/sdcard/YTPlayer/audio.download",
                        target);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onProgressUpdate(String... values) {
            tview.setText(values[1]);
            if (Integer.parseInt(values[0])==-1) {
                bar.setIndeterminate(true);
                return;
            }
            bar.setIndeterminate(false);
            bar.setProgress(Integer.parseInt(values[0]));
            dtview.setText(YTutils.getSize(Long.parseLong(values[2]))+" / "+fileLengthString);
        }

        @Override
        protected void onPostExecute(String s) {
            Toast.makeText(PlayerActivity2.this, "Saved at "+target, Toast.LENGTH_LONG).show();
            alertdialog.dismiss();
        }

        public boolean mux(String videoFile, String audioFile, String outputFile) {
            Movie video;
            try {
                video = new MovieCreator().build(videoFile);
            } catch (RuntimeException e) {
                e.printStackTrace();
                return false;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }

            Movie audio;
            try {

                audio = new MovieCreator().build(audioFile);
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (NullPointerException e) {
                e.printStackTrace();
                return false;
            }

            Track audioTrack = audio.getTracks().get(0);
            video.addTrack(audioTrack);
            Container out = new DefaultMp4Builder().build(video);
            FileOutputStream fos;
            try {
                fos = new FileOutputStream(outputFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                return false;
            }

            BufferedWritableFileByteChannel byteBufferByteChannel = new BufferedWritableFileByteChannel(fos);
            try {
                out.writeContainer(byteBufferByteChannel);
                byteBufferByteChannel.close();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            }
            return true;
        }


    }

    class BufferedWritableFileByteChannel implements WritableByteChannel {
        //    private static final int BUFFER_CAPACITY = 1000000;
        private static final int BUFFER_CAPACITY = 10000000;

        private boolean isOpen = true;
        private final OutputStream outputStream;
        private final ByteBuffer byteBuffer;
        private final byte[] rawBuffer = new byte[BUFFER_CAPACITY];

        private void dumpToFile() {
            try {
                outputStream.write(rawBuffer, 0, byteBuffer.position());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private BufferedWritableFileByteChannel(OutputStream outputStream) {
            this.outputStream = outputStream;
            this.byteBuffer = ByteBuffer.wrap(rawBuffer);
        }

        @Override
        public int write(ByteBuffer inputBuffer) {
            int inputBytes = inputBuffer.remaining();

            if (inputBytes > byteBuffer.remaining()) {
                dumpToFile();
                byteBuffer.clear();

                if (inputBytes > byteBuffer.remaining()) {
                    throw new BufferOverflowException();
                }
            }

            byteBuffer.put(inputBuffer);

            return inputBytes;
        }

        @Override
        public boolean isOpen() {
            return isOpen;
        }

        @Override
        public void close() throws IOException {
            dumpToFile();
            isOpen = false;
        }
    }

    public static void updateProgressBar() {
        mHandler.postDelayed(mUpdateTimeTask, 100);
    }

    public static Runnable mUpdateTimeTask = new Runnable() {
        public void run() {

            long totalDuration = MainActivity.player.getDuration();
            long currentDur = MainActivity.player.getCurrentPosition();

            // Displaying time completed playing
            currentDuration.setText("" + YTutils.milliSecondsToTimer(currentDur));

            // Updating progress bar
            int progress = (YTutils.getProgressPercentage(currentDur, totalDuration));
            //Log.d("Progress", ""+progress);
            indicatorSeekBar.setProgress(progress);

            // Running this thread after 100 milliseconds
            mHandler.postDelayed(this, 100);
        }
    };
}
