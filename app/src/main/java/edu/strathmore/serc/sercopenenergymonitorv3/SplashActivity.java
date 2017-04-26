package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by Bob on 21/03/2017.
 */

public class SplashActivity extends AppCompatActivity {

    //Duration of wait *
    //private final int SPLASH_DISPLAY_LENGTH = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

       /* *//* New Handler to start the Menu-Activity
         * and close this Splash-Screen after some seconds.*//*
                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                /*//* Create an Intent that will start the Menu-Activity. *//**//*
                        Intent mainIntent = new Intent(SplashActivity.this,Menu.class);
                        SplashActivity.this.startActivity(mainIntent);
                        SplashActivity.this.finish();
                    }
                }, SPLASH_DISPLAY_LENGTH);
    };*/

        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        boolean helpPageShown = appSettings.getBoolean("help_page_shown",false);


        if (helpPageShown) {
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        } else {
            Intent intent = new Intent(SplashActivity.this, HelpActivity.class);
            startActivity(intent);
            finish();
        }
    }
}

