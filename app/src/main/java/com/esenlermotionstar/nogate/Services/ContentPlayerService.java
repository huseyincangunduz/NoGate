package com.esenlermotionstar.nogate.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentManager;
import com.esenlermotionstar.nogate.Helper.Settings;

import com.esenlermotionstar.nogate.Helper.TimeLimitManager;
import com.esenlermotionstar.nogate.LockScrReceiver;
import com.esenlermotionstar.nogate.R;
import com.esenlermotionstar.nogate.SystemControllers.NetworkController;
import com.esenlermotionstar.nogate.extend_time_activity;

public class ContentPlayerService extends Service {


    private static final int NOTIFICATION_ID_LOCKSCRPLY_ENABLED = 1;
    private static final int NOTIFICATION_ID_LOCKSCRPLY_DISABLED_TIME_EXCEED = 2;
    private NotificationManager notificationManager;
    private NetworkController networkController;
    LockScrReceiver lockScrReceiver;
    private ApprovedContentManager icerikYoneticisi;
    private android.content.Context applicationContext;
    private boolean isRunning = false;
    private boolean timeExceedNotificationIsShowing;

    private boolean controlIsNotExceedLimit(Context context) {
        return (!Settings.getIsTimeLimitEnabled(context) || TimeLimitManager.getTodayWatchedTimes() < TimeLimitManager.getTodayTimeLimit());
    }
    void ShowNotification() {
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= 26)  //Oreo'dan büyük ya da eşit
        {
            String id = "com.esenlermotionstar.nogate.notifications";// default_channel_id
            String title = "Esenler Motionstar NoGate"; // Default Channel

            NotificationChannel mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
            mBuilder = new NotificationCompat.Builder(this, id);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }


        mBuilder.setContentTitle(getString(R.string.activated_lockscr_player));
        mBuilder.setContentText(getString(R.string.will_be_played_at_ls));
        mBuilder.setSmallIcon(R.drawable.ic_lockscrplayer_name);
        mBuilder.setPriority(NotificationManager.IMPORTANCE_LOW);
        mBuilder.setAutoCancel(true);
        mBuilder.setOngoing(true); //Silinemez
        mBuilder.setWhen(System.currentTimeMillis());
        notificationManager.notify(NOTIFICATION_ID_LOCKSCRPLY_ENABLED, mBuilder.build());
    }


    void timeIsExceedNotification() {
        notificationManager = (NotificationManager)
                getSystemService(NOTIFICATION_SERVICE);

        NotificationCompat.Builder mBuilder;
        if (Build.VERSION.SDK_INT >= 26)  //Oreo'dan büyük ya da eşit
        {
            String id = "com.esenlermotionstar.nogate.notifications";// default_channel_id
            String title = "Morphosium NoGate"; // Default Channel

            NotificationChannel mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel = new NotificationChannel(id, title, NotificationManager.IMPORTANCE_DEFAULT);
            mChannel.enableVibration(true);
            notificationManager.createNotificationChannel(mChannel);
            mBuilder = new NotificationCompat.Builder(this, id);
        } else {
            mBuilder = new NotificationCompat.Builder(this);
        }


        mBuilder.setContentTitle(getString(R.string.TimesUp)).setContentText(getString(R.string.timeExpiredNotifMsg)).setSmallIcon(R.drawable.ic_time_limit_exceed);

        Intent notifyIntent = new Intent(this, extend_time_activity.class);
        //notifyIntent.setAction(TimeExtendDialogReceiver.TIME_EXCEED);
// Create the PendingIntent
        PendingIntent notifyPendingIntent = PendingIntent.getActivity(
                this, 0, notifyIntent, PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setContentIntent(notifyPendingIntent);

       // mBuilder.addAction(0, getString(R.string.extendTime), notifyPendingIntent);
        mBuilder.setAutoCancel(false);
        mBuilder.setOngoing(true); //Silinemez
        mBuilder.setWhen(System.currentTimeMillis());

        notificationManager.notify(NOTIFICATION_ID_LOCKSCRPLY_DISABLED_TIME_EXCEED, mBuilder.build());
        timeExceedNotificationIsShowing = true;
    }
    void closeTimeExceedNotification() {
        if (timeExceedNotificationIsShowing)
        {
            notificationManager.cancel(NOTIFICATION_ID_LOCKSCRPLY_DISABLED_TIME_EXCEED);
            timeExceedNotificationIsShowing = false;
        }

    }
    void CloseNotification() {
        notificationManager.cancel(NOTIFICATION_ID_LOCKSCRPLY_ENABLED);
    }

    private void StartLockScrPlayer() {
        try {
            lockScrReceiver = new LockScrReceiver(this);
            lockScrReceiver.registerThis(applicationContext);


            closeTimeExceedNotification();


        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onCreate() {
        applicationContext = this.getApplicationContext();
        icerikYoneticisi = ApprovedContentManager.getInstance(applicationContext);
        networkController = new NetworkController();
        networkController.registerThis(applicationContext);
        super.onCreate();
    }

    private void StopLockScrPlayer() {
        try {
            lockScrReceiver.unregisterThis(applicationContext);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        try{
            networkController.unregisterThis(applicationContext);
        } catch (Exception exx)
        {
            exx.printStackTrace();
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //ÇALIŞTIRMA DURUMU
        Log.i(null, "ContentPlayerService::onStartCommand: SERVİS BAŞLIYOR");
        if (isRunning == false) {
            TimeLimitManager.addOnChangeTimeLimitEvent(new TimeLimitManager.OnChangeTimeLimits() {
                @Override
                protected void run(int watched, int dbTodayTimeLimit, int defaultTimeLimit) {
                    startLockScrPlayerControlly();
                }
            });
                    startLockScrPlayerControlly();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void startLockScrPlayerControlly() {
        if(controlIsNotExceedLimit(getApplicationContext())){
            StartLockScrPlayer();
            ShowNotification();
            isRunning = true;
        }
        else timesExceed();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public boolean stopService(Intent name) {

        //SERVİSİ DURDURMA
        Log.i(null, "ContentPlayerService::onStartCommand: SERVİS KAPANIYOR");
        stopNoGateAllListenings();
        return super.stopService(name);
    }


    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopNoGateAllListenings();

    }

    private void stopLockScrPlayerListening() {

        if (isRunning == true) {
            StopLockScrPlayer();
            CloseNotification();
            isRunning = false;

        }
    }

    @Override
    public void onDestroy() {

        super.onDestroy();
        stopNoGateAllListenings();
    }


    public void timesExceed()
    {
        stopLockScrPlayerListening();
        timeIsExceedNotification();
    }

    void stopNoGateAllListenings()
    {

        try {
            icerikYoneticisi.saveApprovedContentsFromXML();
            Toast.makeText(this, R.string.chng_are_saved, Toast.LENGTH_SHORT).show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        stopLockScrPlayerListening();
        closeTimeExceedNotification();
        stopSelf();
    }
}
