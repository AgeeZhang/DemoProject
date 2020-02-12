package com.example.myapplication;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.zcitc.updatelibrary.UpdateController;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onClick(View v) {
        Intent intent=new Intent();
        intent.putExtra(UpdateController.MUST_BE_UPDATED,99);
        intent.putExtra(UpdateController.NEW_VERSION,"1.2.3");
        UpdateController.getInstance().NeedUpdate(this,intent);
        UpdateController.getInstance().showDialog();
    }
}
