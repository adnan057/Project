package com.example.adnan.project;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.view.Window;
import android.view.WindowManager;

public class LoadActivity extends AppCompatActivity {

    private static int TIME_OUT = 3000;
    private ProgressDialog progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_load);
        getSupportActionBar().hide();

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {

                // This method will be executed once the timer is over
                // Start your app main activity
                Intent i = new Intent(LoadActivity.this, MainActivity.class);
                startActivity(i);

                // close this activity
                finish();
            }
        }, TIME_OUT);

    }
}
