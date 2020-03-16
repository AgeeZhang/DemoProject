package com.zcitc.updatelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.provider.Settings;

import com.zcitc.updatelibrary.contract.DownloadTags;
import com.zcitc.updatelibrary.contract.UpdateInterface;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;
import com.zdf.activitylauncher.ActivityLauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

public abstract class BaseUpdateController implements UpdateInterface.OnDownloadCallback {

    private final static Logger log = LoggerFactory.getLogger(BaseUpdateController.class);
    protected Context mContext;
    protected Class<?> mService;
    protected UpdateThread mUpdateThread;
    protected TempData mTempData;
    private String mPath;

    abstract public void init(Context var1, Class<?> cls);

    abstract public void needUpdate(AppCompatActivity activity, Intent intent);

    abstract public void showDialog();

    abstract public void OnDownloadEvent(Message msg);

    abstract public void OnNotInstallPermission();

    abstract public void OnInstallPermission(String path);

    protected synchronized void initUpdateThread(Context var1) {
        if (mUpdateThread == null) {
            mUpdateThread = new UpdateThread(var1.getApplicationContext());
            mUpdateThread.start();
            mUpdateThread.waitForReady();
        }
    }

    public synchronized UpdateThread updateThread() {
        return mUpdateThread;
    }

    protected boolean mustBeUpdated() {
        return mTempData.NewVersionCode > BuildConfig.VERSION_CODE;
    }

    protected void sendDownloadSignal() {
        Intent intent = new Intent(mContext, mService);
        intent.putExtra(DownloadTags.APK_URL, mTempData.APKPath);
        intent.putExtra(DownloadTags.FILE_MD5, mTempData.APKMd5);
        intent.putExtra(DownloadTags.ICON_ID, mTempData.IconId);
        mContext.startService(intent);
    }

    protected void sendRetrySignal() {
        Intent intent = new Intent(mContext, mService);
        intent.putExtra(DownloadTags.APK_URL, mTempData.APKPath);
        intent.putExtra(DownloadTags.FILE_MD5, mTempData.APKMd5);
        intent.putExtra(DownloadTags.ICON_ID, mTempData.IconId);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mContext.startForegroundService(intent);
        } else {
            mContext.startService(intent);
        }
    }

    @Override
    public void onDownLoadStart() {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_START;
        envelop.obj = 0;
        OnDownloadEvent(envelop);
    }

    @Override
    public void onProgress(long progress) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOADING;
        envelop.obj = Integer.valueOf(String.valueOf(progress));
        OnDownloadEvent(envelop);
    }

    @Override
    public void onFail(String msg) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_FAIL;
        envelop.obj = msg;
        OnDownloadEvent(envelop);
    }

    @Override
    public void onSuccess(String path) {
        Message envelop = new Message();
        envelop.what = DownloadTags.DOWNLOAD_SUCCESS;
        envelop.obj = path;
        OnDownloadEvent(envelop);
    }

    /**
     * 开始安装（判断安装权限）
     *
     * @param path
     */
    public void sendInstallSignal(String path) {
        this.mPath = path;
        if (checkInstallPermission(mContext)) {//判断是否是8.0或以上
            OnNotInstallPermission();
        } else {
            OnInstallPermission(path);
        }
    }

    /**
     * 判断根据版本判断安装权限
     *
     * @param var1
     * @return
     */
    private boolean checkInstallPermission(Context var1) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return var1.getPackageManager().canRequestPackageInstalls();
        } else {
            return true;
        }
    }

    /**
     * 申请安装权限
     */
    public void OnNotInstallPermission(Activity currentActivity) {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + AppUtils.getPackageName(currentActivity)));
        ActivityLauncher.init(currentActivity).startActivityForResult(intent, (resultCode, data) -> {
            if (resultCode == currentActivity.RESULT_OK) {
                if (mPath != null)
                    sendInstallSignal(mPath);
                else
                    log.debug("文件被清理，请重新检查更新！");
            } else {
                log.debug("未打开'安装未知来源应用'开关,无法安装,请打开后重试");
            }
        });
    }

    /**
     * 开始安装Apk
     *
     * @param path
     */
    public void OnInstallPermission(Activity currentActivity, String path) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {   //Android7.0开始安装路径发生改变
            Uri apkUri = FileProvider.getUriForFile(currentActivity, BuildConfig.APPLICATION_ID + ".fileProvider", new File(path));
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        } else {
            Uri apkUri = Uri.fromFile(new File(path));
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        }
        currentActivity.startActivity(intent);
    }


    protected class TempData {

        public int NewVersionCode;
        public String NewVersion;
        public String APKPath;
        public String APKMd5;
        public int IconId;

        public TempData(Intent intent) {
            NewVersionCode = intent.getIntExtra(DownloadTags.MUST_BE_UPDATED, 0);
            NewVersion = intent.getStringExtra(DownloadTags.NEW_VERSION);
            APKPath = intent.getStringExtra(DownloadTags.APK_URL);
            APKMd5 = intent.getStringExtra(DownloadTags.FILE_MD5);
            IconId = intent.getIntExtra(DownloadTags.ICON_ID, 0);
        }
    }

}
