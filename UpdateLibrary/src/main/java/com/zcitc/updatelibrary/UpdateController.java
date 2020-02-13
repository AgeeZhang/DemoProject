package com.zcitc.updatelibrary;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import com.jess.arms.integration.AppManager;
import com.zcitc.updatelibrary.contract.UpdateContract;
import com.zcitc.updatelibrary.service.DownloadService;
import com.zcitc.updatelibrary.thread.UpdateThread;
import com.zcitc.updatelibrary.utils.AppUtils;
import com.zdf.activitylauncher.ActivityLauncher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class UpdateController implements UpdateContract.OnDownloadCallback {

    private final static Logger log = LoggerFactory.getLogger(UpdateController.class);
    private static UpdateController singleton;

    private Context mContext;
    private AppCompatActivity mActivity;
    private Intent mIntent;
    private UpdateThread mUpdateThread;
    private UpdateDialog mUpdateDialog;
    private String mPath;

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
        log.error("ces ");
    }

    public synchronized UpdateThread updateThread() {
        return mUpdateThread;
    }

    public void NeedUpdate(AppCompatActivity activity, Intent intent) {
        updateThread().eventHandler().addEventHandler(this);
        mActivity = activity;
        mIntent = intent;
        mUpdateDialog = new UpdateDialog(activity, mustBeUpdated(), mIntent.getStringExtra(DownloadTags.NEW_VERSION));
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
        return mIntent.getIntExtra(DownloadTags.MUST_BE_UPDATED, 0) > BuildConfig.VERSION_CODE;
    }

    public void sendDownloadSignal() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadTags.APK_URL, "https://dldir1.qq.com/weixin/android/weixin7010android1580.apk");
        intent.putExtra(DownloadTags.FILE_MD5, "414ead6cfed19db527894f05ace44702");
        intent.putExtra(DownloadTags.ICON_ID, mIntent.getIntExtra(DownloadTags.ICON_ID, 0));
        mContext.startService(intent);
    }

    public void sendRetrySignal() {
        Intent intent = new Intent(mContext, DownloadService.class);
        intent.putExtra(DownloadTags.APK_URL, "https://dldir1.qq.com/weixin/android/weixin7010android1580.apk");
        intent.putExtra(DownloadTags.FILE_MD5, "414ead6cfed19db527894f05ace44702");
        intent.putExtra(DownloadTags.ICON_ID, mIntent.getIntExtra(DownloadTags.ICON_ID, 0));
        mContext.startService(intent);
    }

    @Override
    public void onDownLoadStart() {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpdateDialog.onDownloadStart();
            }
        });
    }

    @Override
    public void onProgress(long progress) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpdateDialog.onDownloading((int) progress);
            }
        });
    }

    @Override
    public void onFail(String msg) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpdateDialog.onDownloadFail(new UpdateContract.OnUpdateClickListener() {
                    @Override
                    public void OnClick() {
                        sendRetrySignal();
                    }
                });
            }
        });
    }

    @Override
    public void onSuccess(final String path) {
        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUpdateDialog.onDownloadSuccess(new UpdateContract.OnUpdateClickListener() {
                    @Override
                    public void OnClick() {
                        sendInstallSignal(mContext, path);
                    }
                });
            }
        });
    }

    /**
     * 开始安装（判断安装权限）
     *
     * @param context
     * @param path
     */
    public void sendInstallSignal(Context context, String path) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//判断是否是8.0或以上
                boolean haveInstallPermission = context.getPackageManager().canRequestPackageInstalls();
                if (!haveInstallPermission) {
                    onNotInstallPermission();
                } else {
                    OnProviderInstallPermission(path);
                }
            } else {
                OnProviderInstallPermission(path);
            }
        } else {
            OnInstallPermission(path);
        }
    }

    /**
     * 申请安装权限
     */
    public void onNotInstallPermission() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:" + AppUtils.getPackageName(AppManager.getAppManager().getCurrentActivity())));
        ActivityLauncher.init(AppManager.getAppManager().getCurrentActivity()).startActivityForResult(intent, new ActivityLauncher.Callback() {
            @Override
            public void onActivityResult(int resultCode, Intent data) {
                if (resultCode == AppManager.getAppManager().getCurrentActivity().RESULT_OK) {
                    if (mPath != null)
                        sendInstallSignal(mContext, mPath);
                    else
                    {

                    }
//                        showMessage("文件被清理，请重新检查更新！");
                } else {
//                    showMessage("未打开'安装未知来源应用'开关,无法安装,请打开后重试");
                }
            }
        });
    }

    /**
     * Android7.0以上安装
     *
     * @param path
     */
    public void OnProviderInstallPermission(String path) {
        Uri apkUri = FileProvider.getUriForFile(AppManager.getAppManager().getCurrentActivity(), BuildConfig.APPLICATION_ID + ".fileProvider", new File(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        AppManager.getAppManager().getCurrentActivity().startActivity(intent);
    }

    /**
     * Android7.0以下安装
     *
     * @param path
     */
    public void OnInstallPermission(String path) {
        Uri apkUri = Uri.fromFile(new File(path));
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        AppManager.getAppManager().getCurrentActivity().startActivity(intent);
    }


}
