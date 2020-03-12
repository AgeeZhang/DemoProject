package com.zcitc.updatelibrary.contract;

public interface UpdateInterface {

    interface OnUpdateClickListener {
        void OnClick();
    }

    interface OnDownloadCallback {

        void onDownLoadStart();

        void onProgress(long progress);

        void onFail(String msg);

        void onSuccess(String path);
    }
}
