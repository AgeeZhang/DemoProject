package com.example.myapplication;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.zcitc.updatelibrary.contract.UpdateContract.*;

public class UpdateDialog extends Dialog {

    private TextView updateHint;
    private TextView confirmBtn;
    private TextView closeBtn;
    private ProgressBar progressBar;

    private boolean isForceUpdate;
    private String mVersionCode;
    private OnUpdateClickListener updateClickListener;
    private OnUpdateClickListener closeClickListener;

    public UpdateDialog(@NonNull Context context, boolean forceUpdate, String versionCode) {
        super(context, com.zcitc.updatelibrary.R.style.UpdateDialogStyle);
        isForceUpdate = forceUpdate;
        mVersionCode = versionCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zcitc.updatelibrary.R.layout.dialog_update);
        updateHint = findViewById(com.zcitc.updatelibrary.R.id.update_hint);
        confirmBtn = findViewById(com.zcitc.updatelibrary.R.id.update_btn);
        closeBtn = findViewById(com.zcitc.updatelibrary.R.id.update_close);
        progressBar = findViewById(com.zcitc.updatelibrary.R.id.update_progress);

        progressBar.setVisibility(View.GONE);
        confirmBtn.setVisibility(View.VISIBLE);
        closeBtn.setVisibility(isForceUpdate ? View.GONE : View.VISIBLE);

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (updateClickListener != null) updateClickListener.OnClick();
            }
        });
        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!isForceUpdate) dismiss();
                if (closeClickListener != null) closeClickListener.OnClick();
            }
        });

        setCancelable(!isForceUpdate);
        String msg = "　　有新的版本 v" + mVersionCode + " 可用，" + (isForceUpdate ? "您必须更新到此版本才能继续使用。" : "建议您更新到此版本。") + "是否更新到最新版本？";
        updateHint.setText(msg);
        setCanceledOnTouchOutside(!isForceUpdate);
    }

    public void setUpdateClickListener(OnUpdateClickListener listener) {
        this.updateClickListener = listener;
    }

    public void setCloseClickListener(OnUpdateClickListener closeClickListener) {
        this.closeClickListener = closeClickListener;
    }

    public void onDownloadStart() {
        confirmBtn.setVisibility(View.GONE);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setProgress(0);
    }

    public void onDownloading(int progress) {
        progressBar.setProgress(progress);
    }

    public void onDownloadSuccess(OnUpdateClickListener listener) {
        updateClickListener = listener;
        confirmBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        confirmBtn.setText("立即安装");
    }

    public void onDownloadFail(OnUpdateClickListener listener) {
        updateClickListener = listener;
        confirmBtn.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);
        confirmBtn.setText("重　　试");
    }

}
