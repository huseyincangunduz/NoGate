package com.esenlermotionstar.nogate;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;


import android.util.Log;
import android.view.View;

import android.view.Window;
import android.view.WindowManager;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.esenlermotionstar.nogate.ContentApproval.*;
import com.esenlermotionstar.nogate.DataManager.NoGateDb;
import com.esenlermotionstar.nogate.Helper.Settings;
import com.esenlermotionstar.nogate.Helper.TimeLimitManager;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.IFramePlayerOptions;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.PlayerConstants;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayer;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.YouTubePlayerView;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.AbstractYouTubePlayerListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.player.listeners.YouTubePlayerInitListener;
import com.pierfrancescosoffritti.androidyoutubeplayer.ui.PlayerUIController;

import java.io.File;
import java.time.DateTimeException;
import java.util.Timer;
import java.util.TimerTask;

public class LockScrActivity extends AppCompatActivity {
    YouTubePlayerView youtubePlayerView;

    VideoView oynatici;

    RelativeLayout contentLayout;


    void closeLockscrPlayer() {
        isWorking = false;
        if (youtubePlayerView != null) {
            try {
                youtubePlayerView.release();
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        finish();

    }

    boolean closeAtEnd = false;
    int current_ = 0;
    int duration_ = 0;


    //Zaten çalışıyorsa tekrar açmaması için
    static boolean isWorking = false;
    //boolean playing = true;
    boolean timerIsRunning = false;
    Timer timeController;
    Integer times = 0,
            timeElapsed = 0, startedTime;
    TimerTask timerControllingFunction = new TimerTask() {
        @Override
        public void run() {
            if (timerIsRunning && ((oynatici != null && oynatici.isPlaying()) || youtubePlayerView != null)) {
                times++;
                if (times == 60) {
                    timeElapsed++;
                    //NoGateDb.getInstance(getApplicationContext());

                    //NoGateDb.setTodayWatchingTimeMinutes(startedTime + timeElapsed);
                    TimeLimitManager.setMinsWatchedInToday(startedTime + timeElapsed, getApplicationContext());
                    times = 0;
                }
                Log.i(null, "run@timerControllingFunction: " + String.valueOf(timeElapsed) + " minutes, " + String.valueOf(times) + " seconds" +
                        " Times of playing content: " + current_ + "/" + duration_);

                Boolean timeLimitEnabled_ = Settings.getIsTimeLimitEnabled(getApplicationContext());
                Boolean maxMinIsLarge = NoGateDb.getTodayMaxMinutes() <= (startedTime + timeElapsed);

                if (oynatici != null) {
                    current_ = oynatici.getCurrentPosition() / 1000;
                    duration_ = oynatici.getDuration() / 1000;
                }


                if (timeLimitEnabled_ && maxMinIsLarge) {

                    timeExceedNotify();
                    /*OnTimeIsUp onTimeIsUp1 = LockScrActivity.getOnTimeIsUp();
                    if ( onTimeIsUp1 != null) onTimeIsUp1.run();*/
                }
            }
        }
    };

    private void timeExceedNotify() {
        Log.i(null, "kalınan süre/toplam: " + current_ + "/" + duration_);
        if (TimeLimitManager.getAllowingEndVideo(getApplicationContext(), current_, duration_)) {
            Log.i(null, "timeExceedNotify: zaman aşıldı ancak son çeyreğe girildiği için sondan kapatılacak");

            waitForVideoEnd();
        } else {
            closeLockscrPlayer();
            ApprovedContentManager.decreaseSira();
        }
    }

    private void waitForVideoEnd() {
        closeAtEnd = true;
        pauseTimer();
    }

    private YouTubePlayer ytPlayerInner;
    private boolean isOpening;


    void initializeTimer() {
        timeController = new Timer();
        startedTime = NoGateDb.getTodayWatchingTimeMinutes();
        timeController.schedule(timerControllingFunction, 0, 1000);
    }

    void startTimer() {
        timerIsRunning = true;

    }

    void pauseTimer() {
        timerIsRunning = false;

    }

    public static final String
            LOCKSCREEN_CONTENT_TYPE_EXTRA = "approvedContentType",
            LOCKSCREEN_CONTENT_URL_EXTRA = "approvedContentType",
            APPRCNTNT_YOUTUBE = "youtube",
            APPRCNTNT_FILE = "file";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // requestWindowFeature(Window.FEATURE_NO_TITLE);
        if (!isWorking) {


            setContentView(R.layout.activity_lock_scr);
            if (Build.VERSION.SDK_INT >= 28) {
                setShowWhenLocked(true);
                // setTurnScreenOn(true);
            } else {
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
            }
            contentLayout = findViewById(R.id.contentContainer);
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);

            getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

            InitializeLockScrPlayer(savedInstanceState);


        }

    }

    @SuppressLint("NewApi")
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && hasFocus) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //
        if (!(timeElapsed == 0 && times < 2)) {
            closeLockscrPlayer();
            /*if (oynatici != null)
            {
                oynatici.pause();
            }
            else if (youtubePlayerView != null && ytPlayerInner != null)
            {
                ytPlayerInner.pause();
            }*/
        }
    }

    private void InitializeLockScrPlayer(Bundle b) {
        isWorking = true;
        //  int Rotasyon = getWindowManager().getDefaultDisplay().getRotation();
        openContentInOrder(b);
        initializeTimer();

    }

    private void openContentInOrder(Bundle b) {
        if (b == null)
            openApprovedContent(ApprovedContentManager.iterate());
        else
            openApprovedContent(ApprovedContentManager.lastApprovedContent());
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putInt("sira", ApprovedContentManager.getSira());
        super.onRestoreInstanceState(savedInstanceState);
    }

    void openApprovedContent(ApprovedContent content) {
        if (closeAtEnd) {
            closeLockscrPlayer();
        } else {
            isOpening = true;
            if (content.isTypeThat(ApprovedContent.TYPE_ATTR_YOUTUBE)) {

                OpenYoutubeContent(content.getDataPath());
            } else if (content.isTypeThat(ApprovedContent.TYPE_ATTR_FILE)) {
                OpenVideoFile(content.getDataPath());
                //Telefondaki mp4 dosyası açılır
            }
            isOpening = false;
        }


    }

    private void disposeOfflinePlayers() {
        if (oynatici != null) {
            contentLayout.removeView(oynatici);
        }
        oynatici = null;
        //contentLayout.removeAllViews();
    }

    private void disposeYoutubePlayers() {
        try {
            if (youtubePlayerView != null) {
                youtubePlayerView.release();
                contentLayout.removeView(youtubePlayerView);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        youtubePlayerView = null;
        ytPlayerInner = null;

    }

    private void OpenVideoFile(String dataPath) {
        disposeYoutubePlayers();
        Uri uri = Uri.fromFile(new File(dataPath));
        if (oynatici == null) {
            oynatici = new VideoView(this);

            contentLayout.addView(oynatici);
            MediaController controller = new MediaController(this);
            controller.setAnchorView(oynatici);
            oynatici.setMediaController(controller);
            oynatici.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    openContentInOrder(null);
                }
            });

            oynatici.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {

                }
            });
        }


        oynatici.setVideoURI(uri);
        oynatici.start();
        startTimer();
    }


    void OpenYoutubeContent(String videocode_) {


        /*if (youtubePlayerView != null)
            youtubePlayerView.release();
        youtubePlayerView = null;*/
        disposeOfflinePlayers();
        final String videoCode = videocode_;
        if (isWorking) {
            IFramePlayerOptions parametreler = new IFramePlayerOptions.Builder().rel(0).controls(0).showInfo(0).ivLoadPolicy(3).build();

            //View olarak contentLayout'un içine yerleştiriyoruz
            if (youtubePlayerView == null || ytPlayerInner == null) {
                youtubePlayerView = new YouTubePlayerView(this);
                contentLayout.addView(youtubePlayerView);
                PlayerUIController controller = youtubePlayerView.getPlayerUIController();


                controller.showYouTubeButton(false);

                controller.showVideoTitle(true);
                controller.showCustomAction1(false);
                controller.showCustomAction2(false);
                controller.enableLiveVideoUI(false);
                controller.showUI(true);
                controller.showPlayPauseButton(true);


                youtubePlayerView.initialize(new YouTubePlayerInitListener() {

                    public void onInitSuccess(@NonNull final YouTubePlayer initializedYouTubePlayer) {
                        ytPlayerInner = initializedYouTubePlayer;
                        initializedYouTubePlayer.addListener(new AbstractYouTubePlayerListener() {

                            public void onReady() {
                                String videoId = videoCode;
                                initializedYouTubePlayer.loadVideo(videoId, 0);
                                //Butonlara görev ver, (durdur oynat gibi vs. )
                            }

                            @Override
                            public void onStateChange(@NonNull PlayerConstants.PlayerState state) {
                                super.onStateChange(state);
                                if (state == PlayerConstants.PlayerState.PLAYING) {
                                    startTimer();
                                } else if (state == PlayerConstants.PlayerState.PAUSED) {
                                    pauseTimer();
                                } else if (state == PlayerConstants.PlayerState.ENDED) {
                                    pauseTimer();
                                    openContentInOrder(null);
                                }
                            }

                            @Override
                            public void onVideoDuration(float duration) {
                                super.onVideoDuration(duration);
                                duration_ = (int) duration;

                            }

                            @Override
                            public void onCurrentSecond(float second) {
                                super.onCurrentSecond(second);
                                current_ = (int) second;
                            }
                        });

                    }
                }, true, parametreler);
            } else {
                ytPlayerInner.loadVideo(videoCode, 0);
            }


        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (youtubePlayerView != null)
            youtubePlayerView.release();
        pauseTimer();
        isWorking = false;

    }


    public static abstract class OnTimeIsUp {
        abstract void run();
    }

    private static OnTimeIsUp onTimeIsUp;

    public static void setOnTimeIsUp(OnTimeIsUp onTimeIsUp) {
        LockScrActivity.onTimeIsUp = onTimeIsUp;
    }

    public static OnTimeIsUp getOnTimeIsUp() {
        return onTimeIsUp;
    }

}
