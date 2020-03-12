package com.zcitc.updatelibrary.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateThread extends Thread {

    private final static Logger log = LoggerFactory.getLogger(UpdateThread.class);

    private static final int ACTION_UPDATE_THREAD_QUIT = 0X1010; // quit this thread
    private static final int ACTION_UPDATE_DOWLOAD = 0X2010; // quit this thread
    private static final int ACTION_UPDATE_INSTALL = 0X2011; // quit this thread

    private Context mContext;
    private UpdateThreadHandler mUpdateHandler;
    private final DownloadEventHandler mDownloadEventHandler;
    private boolean mReady;

    private static final class UpdateThreadHandler extends Handler {

        private UpdateThread mUpdateThread;

        UpdateThreadHandler(UpdateThread thread) {
            this.mUpdateThread = thread;
        }

        public void release() {
            mUpdateThread = null;
        }

        @Override
        public void handleMessage(Message msg) {
            if (this.mUpdateThread == null) {
                log.warn("handler is already released! " + msg.what);
                return;
            }
            switch (msg.what) {
                case ACTION_UPDATE_THREAD_QUIT:
                    mUpdateThread.exit();
                    break;
            }
        }
    }

    public UpdateThread(Context context) {
        this.mContext = context;
        this.mDownloadEventHandler = new DownloadEventHandler(mContext);
    }

    @Override
    public void run() {
        log.trace("start to run");
        Looper.prepare();
        mUpdateHandler = new UpdateThreadHandler(this);
        mReady = true;
        Looper.loop();
    }

    public final void exit() {
        if (Thread.currentThread() != this) {
            mUpdateHandler.sendEmptyMessage(ACTION_UPDATE_THREAD_QUIT);
            return;
        }
        mReady = false;
        Looper.myLooper().quit();
        mUpdateHandler.release();
    }

    public final void waitForReady() {
        while (!mReady) {
            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            log.debug("wait for " + UpdateThread.class.getSimpleName());
        }
    }

    public DownloadEventHandler eventHandler() {
        return mDownloadEventHandler;
    }

    public void downloadStart() {
        eventHandler().mDownloadEventHandler.onDownLoadStart();
    }

    public void downloading(long p) {
        eventHandler().mDownloadEventHandler.onProgress(p);
    }

    public void downloadSuccess(String path) {
        eventHandler().mDownloadEventHandler.onSuccess(path);
    }

    public void downloadFail(String msg) {
        eventHandler().mDownloadEventHandler.onFail(msg);
    }


}
