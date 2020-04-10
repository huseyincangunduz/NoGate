package com.esenlermotionstar.nogate;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.widget.Toast;
import androidx.core.app.NotificationCompat;
import com.esenlermotionstar.nogate.ContentApproval.ApprovedContentManager;
import com.esenlermotionstar.nogate.DataManager.NoGateDb;
import com.esenlermotionstar.nogate.Helper.Settings;
import com.esenlermotionstar.nogate.Services.ContentPlayerService;


public class LockScrReceiver extends BroadcastReceiver {
    static boolean registered = false;

    private final ContentPlayerService PlayerService;
    NotificationManager notificationManager;
    public LockScrReceiver(ContentPlayerService service)
    {
        PlayerService = service;
    }

    public static boolean isRegistered() {
        return registered;
    }

    public void registerThis(Context ctx) {
        if (!registered) {
            IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
            filter.addAction(Intent.ACTION_SCREEN_OFF);
            ctx.registerReceiver(this, filter);
            registered = true;
        }
    }

    public void unregisterThis(Context ctx) {
        if (registered) {
            try {
                ctx.unregisterReceiver(this);
                registered = false;
            } catch (Exception ex)
            {
                ex.printStackTrace();
                registered = false;
            }

        }

    }
    private void startLockScrActivity(Context context) {
        LockScrActivity.setOnTimeIsUp(new LockScrActivity.OnTimeIsUp() {
            @Override
            void run() {
                PlayerService.timesExceed();
            }
        });
        Intent itd = new Intent(context, LockScrActivity.class);
        itd.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(itd);
    }


    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (ApprovedContentManager.ReadyListLength() > 0 && LockScrActivity.isWorking == false) {
                if (controlIsNotExceedLimit(context))
                    startLockScrActivity(context);
            }

        }
    }


    private boolean controlIsNotExceedLimit(Context context) {
        return (!Settings.getIsTimeLimitEnabled(context) || NoGateDb.getTodayWatchingTimeMinutes() < NoGateDb.getTodayMaxMinutes());
    }


}
