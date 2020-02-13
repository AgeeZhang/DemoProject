package com.zcitc.updatelibrary.service;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Vibrator;
import android.util.Log;
import android.webkit.DownloadListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.jess.arms.base.BaseService;
import com.jess.arms.integration.AppManager;
import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.LogUtils;
import com.zcitc.updatelibrary.DownloadTags;
import com.zcitc.updatelibrary.UpdateController;
import com.zcitc.updatelibrary.contract.UpdateContract;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;
import com.zcitc.updatelibrary.utils.FileOperateUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;

import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;
import okhttp3.ResponseBody;

public class DownloadService extends BaseService {

    private RxErrorHandler mErrorHandler;
    private String APK_dir = "";
    private String SAVE_PATH = File.separator + "Update" + File.separator + "APK" + File.separator;
    private final int NotificationID = 0x10000;
    private NotificationManager mNotificationManager = null;
    private NotificationCompat.Builder builder;

    @Override
    public void init() {
        initAPKDir();
        initError(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String apkUrl = intent.getStringExtra(DownloadTags.APK_URL);
        String fileMd5 = intent.getStringExtra(DownloadTags.FILE_MD5);
        startDownload(apkUrl, fileMd5);
        return super.onStartCommand(intent, flags, startId);
    }

    private synchronized UpdateThread updateThread() {
        return UpdateController.getInstance().updateThread();
    }

    private void initAPKDir() {
        APK_dir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + SAVE_PATH;
        File destDir = new File(APK_dir);
        if (!destDir.exists()) {// 判断文件夹是否存在
            destDir.mkdirs();
        }
    }

    public void startDownload(final String path, final String fileMD5) {
        final String appName = path.substring(path.lastIndexOf("/") + 1);
        ArmsUtils.obtainAppComponentFromContext(this)
                .repositoryManager()
                .obtainRetrofitService(UpdateService.class)
                .download(path)
                .subscribeOn(Schedulers.io())
                .subscribe(new ErrorHandleSubscriber<ResponseBody>(mErrorHandler) {
                    @Override
                    public void onNext(ResponseBody responseBody) {
                        FileOperateUtil.writeResponseBodyToDisk(responseBody, appName, fileMD5,
                                FileOperateUtil.getFolderPath(getApplicationContext(), FileOperateUtil.TYPE_OTHER, "apk"),
                                new FileOperateUtil.OnProgressRefresh() {
                                    @Override
                                    public void onStart() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        updateThread().downloadStart();
                                        startedDownload();
                                    }

                                    @Override
                                    public void onProgress(long p) {
                                        updateThread().downloading(p);
                                        loadingDownload(p);
                                    }

                                    @Override
                                    public void onFail() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        updateThread().downloadFail("");
                                        failDownload();
                                    }

                                    @Override
                                    public void onSuccess(String path) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        updateThread().downloadSuccess(path);
                                        successDownload();
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable t) {
                        super.onError(t);
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        updateThread().downloadFail("");
                        failDownload();
                    }
                });
    }


    private void initError(Context context) {
        mErrorHandler = RxErrorHandler.builder().with(context).responseErrorListener(new ResponseErrorListener() {
            @Override
            public void handleResponseError(Context context, Throwable t) {
                LogUtils.warnInfo(t.getMessage());
            }
        }).build();
    }

    private boolean checkNotification() {
        return NotificationManagerCompat.from(this).areNotificationsEnabled();
    }

    private void startedDownload() {
        if (checkNotification()) {
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            // 针对Android 8.0版本对于消息栏的限制，需要加入channel渠道这一概念
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {  //Android 8.0以上
                NotificationChannel mChannel = new NotificationChannel("10086", "Update download service", NotificationManager.IMPORTANCE_LOW);
                Log.i("DownAPKService", mChannel.toString());
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

    private void loadingDownload(long p) {
        if (mNotificationManager != null) {
            builder.setProgress(100, (int) p, false);
            builder.setContentInfo(AppUtils.getPercent((int) p, 100));
            mNotificationManager.notify(NotificationID, builder.build());
        }
    }

    private void successDownload() {
        if (mNotificationManager != null) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            assert vibrator != null;
            vibrator.vibrate(250L);// 参数是震动时间(long类型)
            stopSelf();
            mNotificationManager.cancel(NotificationID);
        }
    }

    private void failDownload() {
        if (mNotificationManager != null) {
            builder.setContentTitle("新版本下载失败");
            builder.setContentText("点击重试");
            mNotificationManager.notify(NotificationID, builder.build());
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
