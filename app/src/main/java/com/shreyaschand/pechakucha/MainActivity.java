package com.shreyaschand.pechakucha;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    EditText timerDisplay;
    CountDownTimer countDownTimer;
    private Button stopButton;
    private Button startButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        timerDisplay = (EditText) findViewById(R.id.time_remaining);
        startButton = (Button) findViewById(R.id.start_button);
        stopButton = (Button) findViewById(R.id.stop_button);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               startCountdown(20);
            }
        });
        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                    countDownTimer = null;
                }
            }
        });
    }

    public void startCountdown(final int seconds) {
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }

        timerDisplay.setTextColor(Color.BLACK);
        countDownTimer = new CountDownTimer(seconds * 1000 * 6, 1000) {

            public void onTick(long millisUntilFinished) {
                timerDisplay.setText(Integer.toString((((int) millisUntilFinished / 1000) % seconds) + 1));
            }

            public void onFinish() {
                timerDisplay.setText(Integer.toString(0));
                timerDisplay.setTextColor(Color.RED);
            }
        }.start();
    }
}
