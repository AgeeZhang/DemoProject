package com.example.myapplication;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Message;

import androidx.core.app.NotificationCompat;

import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.LogUtils;
import com.zcitc.updatelibrary.DownloadTags;
import com.zcitc.updatelibrary.service.BaseDownloadService;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;

import java.io.File;

import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import me.jessyan.rxerrorhandler.handler.listener.ResponseErrorListener;
import okhttp3.ResponseBody;

public class DownloadService extends BaseDownloadService {

    private RxErrorHandler mErrorHandler;
    private String APK_dir = "";
    private String SAVE_PATH = File.separator + "Update" + File.separator + "APK" + File.separator;

    @Override
    public void init() {
        initApkDir();
        initError(this);
    }

    @Override
    public UpdateThread updateThread() {
        return UpdateController.getInstance().updateThread();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String apkUrl = intent.getStringExtra(DownloadTags.APK_URL);
        String fileMd5 = intent.getStringExtra(DownloadTags.FILE_MD5);
        startDownload(apkUrl, fileMd5);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initApkDir() {
        APK_dir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + SAVE_PATH;
        File destDir = new File(APK_dir);
        if (!destDir.exists()) {// 判断文件夹是否存在
            destDir.mkdirs();
        }
    }

    private void initError(Context context) {
        mErrorHandler = RxErrorHandler.builder().with(context).responseErrorListener(new ResponseErrorListener() {
            @Override
            public void handleResponseError(Context context, Throwable t) {
                LogUtils.warnInfo(t.getMessage());
            }
        }).build();
    }

    @Override
    public void startDownload(String path, String fileMD5) {
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
//                                        updateThread().downloadStart();
//                                        startedDownload();
                                    }

                                    @Override
                                    public void onProgress(long p) {
//                                        updateThread().downloading(p);
//                                        loadingDownload(p);
                                    }

                                    @Override
                                    public void onFail() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
//                                        updateThread().downloadFail("");
//                                        failDownload();
                                    }

                                    @Override
                                    public void onSuccess(String path) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
//                                        updateThread().downloadSuccess(path);
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
//                        updateThread().downloadFail("");
                    }
                });
    }

    @Override
    public void notifyDownloadState(Message msg) {
        switch (msg.what) {
            case DownloadTags.DOWNLOAD_START:
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
                break;
            case DownloadTags.DOWNLOADING:
                builder.setProgress(100, (int) msg.obj, false);
                builder.setContentInfo(AppUtils.getPercent((int) msg.obj, 100));
                break;
            case DownloadTags.DOWNLOAD_SUCCESS:
                builder.setContentTitle("新版本下载完成");
                builder.setContentText("点击安装");
                break;
            case DownloadTags.DOWNLOAD_FAIL:
                builder.setContentTitle("新版本下载失败");
                builder.setContentText("点击重试");
                break;
        }
        mNotificationManager.notify(NotificationID, builder.build());
    }
}
