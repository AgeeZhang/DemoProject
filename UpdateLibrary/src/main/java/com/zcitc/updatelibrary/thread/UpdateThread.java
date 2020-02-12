package com.zcitc.updatelibrary.thread;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.zcitc.updatelibrary.UpdateEventHandler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UpdateThread extends Thread {
    private final static Logger log = LoggerFactory.getLogger(UpdateThread.class);

    private Context mContext;
    private UpdateThreadHandler mUpdateHandler;
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

            }
        }
    }

    public UpdateThread(Context context) {
        this.mContext = context;
    }

    @Override
    public void run() {
        log.trace("start to run");
        Looper.prepare();
        mUpdateHandler = new UpdateThreadHandler(this);
        // enter thread looper
        mReady = true;
        Looper.loop();
    }

    public final void exit() {
        if (Thread.currentThread() != this) {
            log.warn("exit() - exit app thread asynchronously");
            mUpdateHandler.sendEmptyMessage(0);
            return;
        }
        mReady = false;
        log.debug("exit() > start");
        Looper.myLooper().quit();
        mUpdateHandler.release();
        log.debug("exit() > end");
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

    public void sendDownloadSignal(){
        Message envelop = new Message();
//        envelop.what = ACTION_WORKER_CONNECT_TO_RTM_SERVICE;
//        envelop.obj = new String[]{uid};
        mUpdateHandler.handleMessage(envelop);
    }


}
