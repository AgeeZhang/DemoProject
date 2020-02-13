package com.example.myapplication;

import com.jess.arms.base.BaseApplication;
import com.zcitc.updatelibrary.UpdateController;

public class MainApplication extends BaseApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        UpdateController.getInstance().init(this);
    }

}
