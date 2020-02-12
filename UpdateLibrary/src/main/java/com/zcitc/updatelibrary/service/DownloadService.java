package com.zcitc.updatelibrary.service;

import android.Manifest;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.webkit.DownloadListener;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.jess.arms.utils.ArmsUtils;
import com.jess.arms.utils.LogUtils;
import com.zcitc.updatelibrary.UpdateController;
import com.zcitc.updatelibrary.contract.UpdateContract;
import com.zcitc.updatelibrary.thread.UpdateThread;
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

public class DownloadService extends Service {

    private RxErrorHandler mErrorHandler;
    private String APK_dir = "";
    private String SAVE_PATH = File.separator + "Update" + File.separator + "APK" + File.separator;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initAPKDir();
        initError(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String APK_URL = intent.getStringExtra("apk_url");
//        DownFile(APK_URL, APK_dir + "Club.apk");
        startDownload(intent);
        return super.onStartCommand(intent, flags, startId);
    }

    private void initAPKDir() {
        APK_dir = getApplicationContext().getExternalFilesDir(null).getAbsolutePath() + SAVE_PATH;
        File destDir = new File(APK_dir);
        if (!destDir.exists()) {// 判断文件夹是否存在
            destDir.mkdirs();
        }
    }

    public void startDownload(Intent intent) {

        String path = "";//intent.getStringExtra(IntentTags.UPDATE_URL);
        String fileMD5 = "";// intent.getStringExtra(IntentTags.UPDATE_FILE_MD5);

//        ArmsUtils.obtainAppComponentFromContext(this)
//                .repositoryManager()
//                .obtainRetrofitService(UpdateService.class)
//                .download(path)
//                .subscribeOn(Schedulers.io())
//                .subscribe(new ErrorHandleSubscriber<ResponseBody>(mErrorHandler) {
//                    @Override
//                    public void onNext(ResponseBody responseBody) {
//                        String appName = path.substring(path.lastIndexOf("/") + 1);
//                        FileOperateUtil.writeResponseBodyToDisk(responseBody, appName, fileMD5,
//                                FileOperateUtil.getFolderPath(getApplicationContext(), FileOperateUtil.TYPE_OTHER, "apk"),
//                                new FileOperateUtil.OnProgressRefresh() {
//                                    @Override
//                                    public void onStart() {
//                                        try {
//                                            Thread.sleep(500);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                        downloadStart();
//                                    }
//
//                                    @Override
//                                    public void onProgress(long p) {
//                                        downloading((int) p);
//                                    }
//
//                                    @Override
//                                    public void onFail() {
//                                        try {
//                                            Thread.sleep(500);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                        downloadFail();
//                                    }
//
//                                    @Override
//                                    public void onSuccess(String path) {
//                                        try {
//                                            Thread.sleep(500);
//                                        } catch (InterruptedException e) {
//                                            e.printStackTrace();
//                                        }
//                                        downloadSuccess(path);
//                                    }
//                                });
//                    }
//
//                    @Override
//                    public void onError(Throwable t) {
//                        super.onError(t);
//                        try {
//                            Thread.sleep(500);
//                        } catch (InterruptedException e) {
//                            e.printStackTrace();
//                        }
//                        downloadFail();
//                    }
//                });
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
    public void onDestroy() {
        super.onDestroy();
        stopSelf();
    }
}
