package com.kpstv.youtube;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.github.hiteshsondhi88.libffmpeg.Util;
import com.kpstv.youtube.adapters.DownloadAdapter;
import com.kpstv.youtube.receivers.SongBroadCast;
import com.kpstv.youtube.services.DownloadService;
import com.kpstv.youtube.utils.YTutils;
import com.mikhaellopez.circularprogressbar.CircularProgressBar;

import static com.kpstv.youtube.services.DownloadService.downloadTask;
import static com.kpstv.youtube.services.DownloadService.process;

public class DownloadActivity extends AppCompatActivity {

    private static final String TAG = "DownloadActivity";
    RecyclerView recyclerView;
    LinearLayoutManager manager;
    CircularProgressBar currentProgress;
    ImageView currentImageView, moreButton;
    TextView txtTitle, txtSize, txtPercent,pendingText,currentText;
    DownloadAdapter adapter; ConstraintLayout itemConstraint;
    LinearLayout emptyLayout;
    Context context; int lastJobUpdate;
    Handler mHandler = new Handler();

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_download);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("Downloads");

        context = DownloadActivity.this;

        currentText = findViewById(R.id.currentText);
        itemConstraint = findViewById(R.id.item_constraint);
        emptyLayout = findViewById(R.id.emptyLayout);
        recyclerView = findViewById(R.id.recyclerView);
        manager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(manager);

        currentProgress = findViewById(R.id.DProgress);
        pendingText = findViewById(R.id.pendingText);
        currentImageView = findViewById(R.id.DImage);
        moreButton = findViewById(R.id.DMore);
        txtTitle = findViewById(R.id.DTitle);
        txtPercent = findViewById(R.id.DPercentText);
        txtSize = findViewById(R.id.DSizeText);

        if (DownloadService.currentModel !=null) {

            int accentColor = ContextCompat.getColor(this,R.color.colorAccent);

            PopupMenu popupMenu = new PopupMenu(context,moreButton);
            popupMenu.inflate(R.menu.service_menu);
            popupMenu.setOnMenuItemClickListener(menuItem -> {
                switch (menuItem.getItemId()) {
                    case R.id.cancel_action:
                        if (DownloadService.pendingJobs.size()<=0)
                        {
                            Intent newintent = new Intent(context, SongBroadCast.class);
                            newintent.setAction("com.kpstv.youtube.STOP_SERVICE");

                            PendingIntent stopservicePending =
                                    PendingIntent.getBroadcast(context, 5, newintent, 0);

                            try {
                                stopservicePending.send();
                            } catch (PendingIntent.CanceledException e) {
                                e.printStackTrace();
                            }

                            return true;
                        }
                        if (downloadTask.getStatus()== AsyncTask.Status.RUNNING)
                        {
                            Log.e(TAG, "onCreate: Cancelling current task");
                            downloadTask.cancel(true);
                        }
                        if (!Util.isProcessCompleted(DownloadService.process))
                        {
                            Log.e(TAG, "onCreate: Destroying process" );
                            process.destroy();
                        }
                        DownloadService.currentsize=0;
                        DownloadService.totalsize=0;
                        DownloadService.progress=0;
                        break;
                }
                return true;
            });

            moreButton.setOnTouchListener((v, motionEvent) -> {
                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN: {
                        ImageView view = (ImageView ) v;
                        view.setColorFilter(accentColor);
                        v.invalidate();
                        break;
                    }
                    case MotionEvent.ACTION_UP:

                        if (DownloadService.currentModel==null)
                            return false;

                        popupMenu.show();

                    case MotionEvent.ACTION_CANCEL: {
                        ImageView view = (ImageView) v;
                        view.clearColorFilter();
                        view.invalidate();
                        break;
                    }
                }
                return true;
            });

            runTask();
            lastJobUpdate = DownloadService.pendingJobs.size();
            adapter = new DownloadAdapter(DownloadService.pendingJobs,this);
            recyclerView.setAdapter(adapter);
            mHandler.postDelayed(mUpdateTimeTask, 1000);
        }else finish();
    }

    void runTask() {
        if (DownloadService.currentModel !=null) {
            txtTitle.setText(DownloadService.currentModel.getTitle()+" - "+DownloadService.currentModel.getChannelTitle());
           switch (DownloadService.currentModel.getTaskExtra()) {
               case "mp3task":
                   currentImageView.setImageDrawable(getDrawable(R.drawable.ic_audio_download));
                   break;
               case "mergetask":
                   currentImageView.setImageDrawable(getDrawable(R.drawable.ic_movie_download));
                   break;
            }
            String currentSize = YTutils.getSize(DownloadService.currentsize);
            String totalSIze = YTutils.getSize(DownloadService.totalsize);
            //int percent = ((int) DownloadService.currentsize*100 / (int)DownloadService.totalsize);

            int percent = DownloadService.progress;

            if (percent==-1) {
                txtPercent.setText(percent+"%");
                currentProgress.setIndeterminateMode(true);
            }else {
                currentProgress.setIndeterminateMode(false);
                currentProgress.setProgressWithAnimation(percent);
                txtPercent.setText(percent+"%");
            }

            txtSize.setText(String.format("%s / %s", currentSize, totalSIze));

            if (DownloadService.pendingJobs.size() <= 0)
            {
                recyclerView.setVisibility(View.GONE);
                pendingText.setVisibility(View.GONE);
                pendingText.setText(" ");
            }else {
                recyclerView.setVisibility(View.VISIBLE);
                pendingText.setVisibility(View.VISIBLE);
                pendingText.setText("Pending");
            }

            if(adapter!=null && (DownloadService.pendingJobs.size() != lastJobUpdate))
            {
                adapter.notifyDataSetChanged();
                lastJobUpdate = DownloadService.pendingJobs.size();
            }
            /*if (adapter!=null && (DownloadService.pendingJobs.size() != adapter.getItemCount())) {
                Log.e(TAG, "runTask: Need to change the data");
                adapter.notifyDataSetChanged();
            }*/
        }
    }

    public Runnable mUpdateTimeTask = new Runnable() {
        public void run() {
            if (DownloadService.currentModel !=null) {
                runTask();
                mHandler.postDelayed(this,1000);
            }else {
                emptyLayout.setVisibility(View.VISIBLE);
                itemConstraint.setVisibility(View.GONE);
                pendingText.setVisibility(View.GONE);
                recyclerView.setVisibility(View.GONE);
                currentText.setVisibility(View.GONE);
            }
        }
    };

    @Override
    protected void onDestroy() {
        mHandler.removeCallbacks(mUpdateTimeTask);

        super.onDestroy();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}