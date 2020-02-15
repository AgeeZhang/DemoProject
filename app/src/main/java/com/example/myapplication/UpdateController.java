package com.example.myapplication;

import android.content.Context;
import android.content.Intent;
import android.os.Message;

import androidx.appcompat.app.AppCompatActivity;

import com.zcitc.updatelibrary.BaseUpdateController;
import com.zcitc.updatelibrary.DownloadTags;
import com.zcitc.updatelibrary.contract.UpdateContract;

public class UpdateController extends BaseUpdateController {

    private static UpdateController singleton;
    private AppCompatActivity mActivity;
    private UpdateDialog mUpdateDialog;

    public static synchronized UpdateController getInstance() {
        if (singleton == null) {
            singleton = new UpdateController();
        }
        return singleton;
    }

    @Override
    public void init(Context var1, Class<?> cls) {
        this.mContext = var1;
        this.mService = cls;
        initUpdateThread(var1);
    }

    @Override
    public void needUpdate(AppCompatActivity activity, Intent intent) {
        this.mActivity = activity;
        this.mIntent = intent;
        mUpdateDialog = new UpdateDialog(activity, mustBeUpdated(), mIntent.getStringExtra(DownloadTags.NEW_VERSION));
        mUpdateDialog.setUpdateClickListener(() -> {
            sendDownloadSignal();
            // 非必须升级，后台下载安装
            if (!mustBeUpdated())
                mUpdateDialog.dismiss();
        });
    }

    @Override
    public void showDialog() {
        mUpdateDialog.show();
    }

    @Override
    public void OnDownloadEvent(Message msg) {
        switch (msg.what) {
            case DownloadTags.DOWNLOAD_START:
                mUpdateDialog.onDownloadStart();
                break;
            case DownloadTags.DOWNLOADING:
                mUpdateDialog.onDownloading((int) msg.obj);
                break;
            case DownloadTags.DOWNLOAD_SUCCESS:
                mUpdateDialog.onDownloadSuccess(() -> sendInstallSignal((String) msg.obj));
                break;
            case DownloadTags.DOWNLOAD_FAIL:
                mUpdateDialog.onDownloadFail(() -> sendRetrySignal());
                break;
        }
    }
}