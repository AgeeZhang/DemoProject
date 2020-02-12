package com.zcitc.updatelibrary;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.zcitc.updatelibrary.contract.UpdateContract;
import com.zcitc.updatelibrary.service.DownloadService;
import com.zcitc.updatelibrary.thread.UpdateThread;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateController {

    private final static Logger log = LoggerFactory.getLogger(UpdateController.class);
    public final static String MUST_BE_UPDATED = "mustBeUpdated";
    public final static String NEW_VERSION = "newVersion";
    private static UpdateController singleton;

    private Context mContext;
    private AppCompatActivity mActivity;
    private UpdateThread mUpdateThread;
    private UpdateDialog mUpdateDialog;
    private Intent mIntent;

    private UpdateController() {
    }

    public static synchronized UpdateController getInstance() {
        if (singleton == null) {
            singleton = new UpdateController();
        }
        return singleton;
    }

    public void init(Context var1) {
        try {
            this.mContext = var1;
            initUpdateThread(var1);
            var1.startService(new Intent(var1, DownloadService.class));
        } catch (Exception var3) {
            log.error("UpdateController初始化失败！");
        }
    }

    public synchronized void initUpdateThread(Context var1) {
        if (mUpdateThread == null) {
            mUpdateThread = new UpdateThread(var1.getApplicationContext());
            mUpdateThread.start();
            mUpdateThread.waitForReady();
        }
    }

    public synchronized UpdateThread getUpdateThread() {
        return mUpdateThread;
    }

    public void NeedUpdate(AppCompatActivity activity, Intent intent) {
        mIntent = intent;
        mUpdateDialog = new UpdateDialog(activity, mustBeUpdated(), mIntent.getStringExtra(NEW_VERSION));
        mUpdateDialog.setUpdateClickListener(new UpdateContract.OnUpdateClickListener() {
            @Override
            public void OnClick() {
                sendDownloadSignal();
                // 非必须升级，后台下载安装
                if (!mustBeUpdated())
                    mUpdateDialog.dismiss();
            }
        });
    }

    public void showDialog() {
        mUpdateDialog.show();
    }

    public boolean mustBeUpdated() {
        return mIntent.getIntExtra(MUST_BE_UPDATED, 0) > BuildConfig.VERSION_CODE;
    }

    public void sendDownloadSignal() {
        getUpdateThread().sendDownloadSignal();
    }

//    public void sendInstallSignal(String path) {
//        handler() ?.sendMessage(Message.obtain().apply {
//            what = DIALOG_INSTALL_APK
//            obj = path
//        })
//    }
//
//    public void sendRetrySignal() {
//        handler() ?.sendMessage(Message.obtain().apply {
//            what = DIALOG_RETRY
//            obj = downloadIntent()
//        })
//    }
}
