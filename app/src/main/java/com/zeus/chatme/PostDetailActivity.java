package com.zeus.chatme;

import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.ortiz.touchview.TouchImageView;

public class PostDetailActivity extends AppCompatActivity {
    private PlayerView playerView;
    private ImageView btn_fullscreen;
    private TouchImageView imageView;
    private String postType;
    private long playbackPosition = 0;
    private boolean flag = false;
    private ProgressBar loadingBar;
    private SimpleExoPlayer simpleExoPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        btn_fullscreen = findViewById(R.id.bt_fullscreen);
        imageView = findViewById(R.id.postIV);
        playerView = findViewById(R.id.video_added);
        loadingBar = findViewById(R.id.loadingBar);

        //make activity fullscreen
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        String postImage = getIntent().getStringExtra("Post_");
        postType = getIntent().getStringExtra("Type_");

        try {
            if (postType.equals("iv")) {
                loadingBar.setVisibility(View.GONE);
                imageView.setVisibility(View.VISIBLE);
                playerView.setVisibility(View.GONE);
                if (postImage.equals("default")) {
                    imageView.setImageResource(R.drawable.default_dp_2);
                } else {
                    Glide.with(this).load(postImage).placeholder(R.drawable.placeholder_1).into(imageView);
                }
            } else {
                imageView.setVisibility(View.GONE);
                playerView.setVisibility(View.VISIBLE);

                //Implement exo player

                //initialize load control
                LoadControl loadControl = new DefaultLoadControl();
                //initialize bandwidth meter
                BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
                //Initialize track selector
                TrackSelector trackSelector = new DefaultTrackSelector(new AdaptiveTrackSelection.Factory(bandwidthMeter));

                //initialize simple exo player
                simpleExoPlayer = ExoPlayerFactory.newSimpleInstance(PostDetailActivity.this, trackSelector , loadControl);

                //Initialize data source factory
                DefaultHttpDataSourceFactory factory = new DefaultHttpDataSourceFactory("exoplayer_video");
                //initialize extractors factory
                ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
                //Initialize media source
                MediaSource mediaSource = new ExtractorMediaSource(Uri.parse(postImage), factory, extractorsFactory , null , null);

                //set player
                playerView.setPlayer(simpleExoPlayer);
                //keep screen on
                playerView.setKeepScreenOn(true);
                //prepare media
                simpleExoPlayer.prepare(mediaSource);
                //play video when ready
                simpleExoPlayer.setPlayWhenReady(true);
                simpleExoPlayer.addListener(new Player.EventListener() {
                    @Override
                    public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {

                    }

                    @Override
                    public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {

                    }

                    @Override
                    public void onLoadingChanged(boolean isLoading) {

                    }

                    @Override
                    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                        if (playbackState == Player.STATE_BUFFERING) {
                            loadingBar.setVisibility(View.VISIBLE);
                        } else if (playbackState == Player.STATE_READY || playbackState == Player.STATE_ENDED) {
                            loadingBar.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onRepeatModeChanged(int repeatMode) {

                    }

                    @Override
                    public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

                    }

                    @Override
                    public void onPlayerError(ExoPlaybackException error) {
                        Toast.makeText(PostDetailActivity.this, "An error occurred.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onPositionDiscontinuity(int reason) {

                    }

                    @Override
                    public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

                    }

                    @Override
                    public void onSeekProcessed() {

                    }
                });

            }
        } catch (Exception e) {
            Toast.makeText(this, "Error occurred", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

        btn_fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {
                    btn_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen));
                    //set portrait orientation
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
                    flag = false;
                } else {
                    btn_fullscreen.setImageDrawable(getResources().getDrawable(R.drawable.ic_fullscreen_exit));
                    setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // landscape
                    flag = true;
                }
            }
        });

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (postType.equals("vv")) {
            //stop video when ready
            simpleExoPlayer.setPlayWhenReady(false);
            simpleExoPlayer.getPlaybackState();
            playbackPosition = simpleExoPlayer.getCurrentPosition();
//            simpleExoPlayer.release();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (postType.equals("vv")) {
            //play video when ready
            simpleExoPlayer.seekTo(playbackPosition);
            simpleExoPlayer.setPlayWhenReady(true);
            simpleExoPlayer.getPlaybackState();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}