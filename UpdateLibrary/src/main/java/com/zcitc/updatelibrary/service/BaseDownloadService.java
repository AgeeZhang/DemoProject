package com.zcitc.updatelibrary.service;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.zcitc.updatelibrary.BaseUpdateController;
import com.zcitc.updatelibrary.contract.UpdateContract;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;

public abstract class BaseDownloadService extends Service implements UpdateContract.OnDownloadCallback {

    protected final String TAG = this.getClass().getSimpleName();
    protected final int NotificationID = 0x10000;
    protected NotificationManager mNotificationManager = null;
    protected NotificationCompat.Builder builder;

    abstract public void init();

    abstract public void startDownload(final String path, final String fileMD5);

    abstract public void notifyDownloadState(Message msg);

    abstract public UpdateThread updateThread();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        init();
        updateThread().eventHandler().addEventHandler(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    @Override
    public void onDownLoadStart() {
        if (checkNotification()) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //Android 8.0以上
                NotificationChannel mChannel = new NotificationChannel("10086", "Update download service", NotificationManager.IMPORTANCE_LOW);
                mNotificationManager.createNotificationChannel(mChannel);
            }
            builder = new NotificationCompat.Builder(getApplicationContext());
            builder.setOngoing(true);
            builder.setAutoCancel(false);
            builder.setContentIntent(null);
            builder.setContentText("0%");
            builder.setContentTitle("正在下载新版本");
            builder.setProgress(100, 0, false);
            mNotificationManager.notify(NotificationID, builder.build());
        }
    }

    @Override
    public void onProgress(long progress) {
        if (mNotificationManager != null) {
            builder.setProgress(100, (int) progress, false);
            builder.setContentInfo(AppUtils.getPercent((int) progress, 100));
            mNotificationManager.notify(NotificationID, builder.build());
        }
    }

    @Override
    public void onFail(String msg) {
        if (mNotificationManager != null) {
            builder.setContentTitle("新版本下载失败");
            builder.setContentText("点击重试");
            mNotificationManager.notify(NotificationID, builder.build());
        }
    }

    @Override
    public void onSuccess(String path) {
        if (mNotificationManager != null) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            assert vibrator != null;
            vibrator.vibrate(250L);// 参数是震动时间(long类型)
            stopSelf();
            mNotificationManager.cancel(NotificationID);
        }
    }

    private boolean checkNotification() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

}
