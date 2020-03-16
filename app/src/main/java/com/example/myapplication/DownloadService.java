package com.example.myapplication;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;

import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.LogUtils;
import com.zcitc.updatelibrary.BuildConfig;
import com.zcitc.updatelibrary.contract.DownloadTags;
import com.zcitc.updatelibrary.service.BaseDownloadService;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;

import java.io.File;

import androidx.core.app.NotificationCompat;
import androidx.core.content.FileProvider;
import io.reactivex.schedulers.Schedulers;
import me.jessyan.rxerrorhandler.core.RxErrorHandler;
import me.jessyan.rxerrorhandler.handler.ErrorHandleSubscriber;
import okhttp3.ResponseBody;

public class DownloadService extends BaseDownloadService {

    private RxErrorHandler mErrorHandler;
    private PendingIntent mPendingIntent;
    private Intent mIntent;
    private String APK_dir = "";
    private String SAVE_PATH = File.separator + "Update" + File.separator + "APK" + File.separator;

    private int mSmallIcon = 0;
    private String mApkUrl;
    private String mFileMd5;

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
        mApkUrl = intent.getStringExtra(DownloadTags.APK_URL);
        mFileMd5 = intent.getStringExtra(DownloadTags.FILE_MD5);
        mSmallIcon = intent.getIntExtra(DownloadTags.ICON_ID, 0);
        initNotification();
        startDownload(mApkUrl, mFileMd5);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initApkDir() {
        APK_dir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + SAVE_PATH;
        File destDir = new File(APK_dir);
        if (!destDir.exists()) {
            destDir.mkdirs();
        }
    }

    private void initError(Context context) {
        mErrorHandler = RxErrorHandler.builder().with(context).responseErrorListener((context1, t) -> LogUtils.warnInfo(t.getMessage())).build();
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
                                        updateThread().downloadStart();
                                    }

                                    @Override
                                    public void onProgress(long p) {
                                        updateThread().downloading(p);
                                    }

                                    @Override
                                    public void onFail() {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        updateThread().downloadFail("下载失败，请重试！");
                                    }

                                    @Override
                                    public void onSuccess(String path) {
                                        try {
                                            Thread.sleep(500);
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
                                        updateThread().downloadSuccess(path);
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
                        updateThread().downloadFail("下载失败，请重试！");
                    }
                });
    }

    @Override
    public void notifyDownloadState(Message msg) {
        switch (msg.what) {
            case DownloadTags.DOWNLOAD_START:
                builder.setOngoing(true);
                builder.setSmallIcon(mSmallIcon);
                builder.setChannelId(channelId);
                builder.setDefaults(NotificationCompat.DEFAULT_VIBRATE); //悬浮通知设置
                builder.setPriority(NotificationCompat.PRIORITY_MAX);  //悬浮通知设置
                builder.setAutoCancel(false);
                builder.setContentIntent(null);
                builder.setContentText("0%");
                builder.setContentTitle("正在下载新版本");
                builder.setProgress(100, 0, false);
                break;
            case DownloadTags.DOWNLOADING:
                builder.setProgress(100, (int) msg.obj, false);
                builder.setContentText(AppUtils.getPercent((int) msg.obj, 100));
                break;
            case DownloadTags.DOWNLOAD_SUCCESS:
                mIntent = new Intent(Intent.ACTION_VIEW);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //Android7.0开始安装路径发生改变
                    Uri apkUri = FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".fileProvider", new File((String) msg.obj));
                    mIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    mIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                } else {
                    Uri apkUri = Uri.fromFile(new File((String) msg.obj));
                    mIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
                }
                mPendingIntent = PendingIntent.getActivity(this, 0, mIntent, 0);
                builder.setContentIntent(mPendingIntent);
                builder.setContentTitle("新版本下载完成");
                builder.setContentText("点击安装");
                break;
            case DownloadTags.DOWNLOAD_FAIL:
                mIntent = new Intent(this, DownloadService.class);
                mIntent.putExtra(DownloadTags.APK_URL, mApkUrl);
                mIntent.putExtra(DownloadTags.FILE_MD5, mFileMd5);
                mIntent.putExtra(DownloadTags.ICON_ID, mSmallIcon);
                mPendingIntent = PendingIntent.getService(this, 0, mIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                builder.setContentIntent(mPendingIntent);
                builder.setContentTitle("新版本下载失败");
                builder.setContentText("点击重试");
                break;
        }
        notification = builder.build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;//设置通知栏常驻
        mNotificationManager.notify(NotificationID, notification);
    }
}
