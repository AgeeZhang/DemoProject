package com.zcitc.updatelibrary.thread;

import android.content.Context;

import com.zcitc.updatelibrary.contract.UpdateContract;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadEventHandler {

    private final Context mContext;
    private final ConcurrentHashMap<UpdateContract.OnDownloadCallback, Integer> mDownloadEventHandlerList = new ConcurrentHashMap<>();

    public DownloadEventHandler(Context ctx) {
        this.mContext = ctx;
    }

    public void addEventHandler(UpdateContract.OnDownloadCallback handler) {
        this.mDownloadEventHandlerList.put(handler, 0);
    }

    public void removeEventHandler(UpdateContract.OnDownloadCallback handler) {
        this.mDownloadEventHandlerList.remove(handler);
    }

    final UpdateContract.OnDownloadCallback mDownloadEventHandler = new UpdateContract.OnDownloadCallback() {

        @Override
        public void onDownLoadStart() {
            Iterator<UpdateContract.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateContract.OnDownloadCallback handler = it.next();
                handler.onDownLoadStart();
            }
        }

        @Override
        public void onProgress(long progress) {
            Iterator<UpdateContract.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateContract.OnDownloadCallback handler = it.next();
                handler.onProgress(progress);
            }
        }

        @Override
        public void onFail(String msg) {
            Iterator<UpdateContract.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateContract.OnDownloadCallback handler = it.next();
                handler.onFail(msg);
            }
        }

        @Override
        public void onSuccess(String path) {
            Iterator<UpdateContract.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateContract.OnDownloadCallback handler = it.next();
                handler.onSuccess(path);
            }
        }
    };
}
