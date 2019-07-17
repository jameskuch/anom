package com.appspot.autoanom;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;


public class MainScreen extends Activity implements View.OnClickListener {




    long startTime = 0;
    int numberofPresses = 0;
    Handler timerMoveToHiddenSettings_handler = new Handler();
    Runnable timerMoveToHiddenSettings_runnable = new Runnable() {
        @Override
        public void run() {
            long millis = System.currentTimeMillis() - startTime;


            timerMoveToHiddenSettings_handler.postDelayed(this, 25);
            if (millis >= 250) {
                numberofPresses = 0;
                //txtNumPresses.setText(Integer.toString(numberofPresses));
            }
        }
    };
    //
    private ImageButton btn_NewSubject;
    private ImageButton btn_RetestSubject;
    private ImageView toHiddenSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        initUI();
    }

    private void initUI() {
        toHiddenSettings = (ImageView) findViewById(R.id.ToHiddenSettings);
        toHiddenSettings.setOnClickListener(this);

        btn_NewSubject = (ImageButton) findViewById(R.id.ibtn_newsubject);
        btn_RetestSubject = (ImageButton) findViewById(R.id.ibtn_retestsubject);
        btn_NewSubject.setOnClickListener(this);
        btn_RetestSubject.setOnClickListener(this);
        //txtNumPresses = (TextView) findViewById(R.id.txtNumPresses);


    }

    public void onClick(View v) {

        if (v == toHiddenSettings) {
            // we will need to hit this hidden button 5x in a row before 1 second timeout
            // if we already started the timer, stop it
            timerMoveToHiddenSettings_handler.removeCallbacks(timerMoveToHiddenSettings_runnable);
            // reset the start time
            startTime = System.currentTimeMillis();
            // start the timer again
            timerMoveToHiddenSettings_handler.postDelayed(timerMoveToHiddenSettings_runnable, 0);

            // increment the number of presses
            numberofPresses += 1;
            //txtNumPresses.setText(Integer.toString(numberofPresses));
            if (numberofPresses >= 5) {
                Intent k = new Intent(this, Settings.class);
                startActivity(k);

            }
        } else if (v == btn_NewSubject) {


            Intent k = new Intent(this, AutoAnom.class);
            startActivity(k);

        } else if (v == btn_RetestSubject) {

        }
    }


    @Override
    public void onPause() {
        super.onPause();
        timerMoveToHiddenSettings_handler.removeCallbacks(timerMoveToHiddenSettings_runnable);
        numberofPresses = 0;
        //Button b = (Button)findViewById(R.id.button);
        //b.setText("start");
    }

}
