package com.zcitc.updatelibrary.thread;

import android.content.Context;

import com.zcitc.updatelibrary.contract.UpdateInterface;

import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

public class DownloadEventHandler {

    private final Context mContext;
    private final ConcurrentHashMap<UpdateInterface.OnDownloadCallback, Integer> mDownloadEventHandlerList = new ConcurrentHashMap<>();

    public DownloadEventHandler(Context ctx) {
        this.mContext = ctx;
    }

    public void addEventHandler(UpdateInterface.OnDownloadCallback handler) {
        this.mDownloadEventHandlerList.put(handler, 0);
    }

    public void removeEventHandler(UpdateInterface.OnDownloadCallback handler) {
        this.mDownloadEventHandlerList.remove(handler);
    }

    final UpdateInterface.OnDownloadCallback mDownloadEventHandler = new UpdateInterface.OnDownloadCallback() {

        @Override
        public void onDownLoadStart() {
            Iterator<UpdateInterface.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateInterface.OnDownloadCallback handler = it.next();
                handler.onDownLoadStart();
            }
        }

        @Override
        public void onProgress(long progress) {
            Iterator<UpdateInterface.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateInterface.OnDownloadCallback handler = it.next();
                handler.onProgress(progress);
            }
        }

        @Override
        public void onFail(String msg) {
            Iterator<UpdateInterface.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateInterface.OnDownloadCallback handler = it.next();
                handler.onFail(msg);
            }
        }

        @Override
        public void onSuccess(String path) {
            Iterator<UpdateInterface.OnDownloadCallback> it = mDownloadEventHandlerList.keySet().iterator();
            while (it.hasNext()) {
                UpdateInterface.OnDownloadCallback handler = it.next();
                handler.onSuccess(path);
            }
        }
    };
}
