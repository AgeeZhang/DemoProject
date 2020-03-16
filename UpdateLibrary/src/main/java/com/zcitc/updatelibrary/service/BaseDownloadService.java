package com.zcitc.updatelibrary.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.Message;

import com.zcitc.updatelibrary.contract.DownloadTags;
import com.zcitc.updatelibrary.contract.UpdateInterface;
import com.zcitc.updatelibrary.thread.UpdateThread;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

public abstract class BaseDownloadService extends Service implements UpdateInterface.OnDownloadCallback {

    protected final int NotificationID = 0x10000;
    protected final String channelId = "10086";
    protected NotificationManager mNotificationManager = null;
    protected NotificationCompat.Builder builder;
    protected Notification notification;

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
        initNotification();
        updateThread().eventHandler().addEventHandler(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }

    protected void initNotification() {
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //Android 8.0以上
            NotificationChannel mChannel = new NotificationChannel(channelId, getPackageName(), NotificationManager.IMPORTANCE_LOW);
            mChannel.setDescription("下载");
            mChannel.enableLights(false);
            mChannel.enableVibration(false);
            mNotificationManager.createNotificationChannel(mChannel);
        }
        builder = new NotificationCompat.Builder(this, getPackageName());
    }

    @Override
    public void onDownLoadStart() {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_START;
        envelop.obj = 0;
        notifyDownloadState(envelop);
    }

    @Override
    public void onProgress(long progress) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOADING;
        envelop.obj = Integer.valueOf(String.valueOf(progress));
        notifyDownloadState(envelop);
    }

    @Override
    public void onFail(String msg) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_FAIL;
        envelop.obj = msg;
        notifyDownloadState(envelop);
    }

    @Override
    public void onSuccess(String path) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_SUCCESS;
        envelop.obj = path;
        notifyDownloadState(envelop);
    }

}
