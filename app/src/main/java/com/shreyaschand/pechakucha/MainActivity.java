package com.shreyaschand.pechakucha;

import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.evernote.android.state.State;
import com.evernote.android.state.StateSaver;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {

    @BindView(R.id.period_time_remaining) EditText periodTimeDisplay;
    @BindView(R.id.periods_remaining) EditText periodsDisplay;

    @BindView(R.id.start_button) Button startButton;
    @BindView(R.id.pause_button) Button pauseButton;
    @BindView(R.id.resume_button) Button resumeButton;

    @State int totalPeriods;
    @State int periodLength;
    @State int timeLeftOnPause;
    @State int timerRunning = -1; // Used to pass time left on to new activity instance after rotation
    private PechaKuchaTimer countDownTimer;
    private TextWatcher textWatcher;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (countDownTimer != null && countDownTimer.isTimerRunning()) {
            countDownTimer.cancel();
            timerRunning = countDownTimer.getSecondsLeft();
        } else {
            timerRunning = -1;
        }
        StateSaver.saveInstanceState(this, outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        StateSaver.restoreInstanceState(this, savedInstanceState);

        textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                setupInitialInterface();
                periodsDisplay.removeTextChangedListener(this);
                periodTimeDisplay.removeTextChangedListener(this);
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        };

        if (timerRunning > -1) {
            countDownTimer = new PechaKuchaTimer(timerRunning);
            countDownTimer.start();
            setupRunningTimerInterface();
        }
    }

    @OnClick(R.id.start_button)
    public void onStartStopButtonClick(Button b) {
        // if we're just starting the timer, read from the text fields
        if (TextUtils.equals(b.getText(), getString(R.string.start))) {
            totalPeriods = getNumber(periodsDisplay);
            periodLength = getNumber(periodTimeDisplay);
        } // else use the previously read values to restart the timer

        countDownTimer = new PechaKuchaTimer(totalPeriods, periodLength);
        countDownTimer.start();

        setupRunningTimerInterface();
    }

    @OnClick(R.id.pause_button)
    public void onPauseButtonClick() {
        countDownTimer.cancel();
        timeLeftOnPause = countDownTimer.getSecondsLeft();

        setupStoppedTimerInterface();
        resumeButton.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.resume_button)
    public void onResumeButtonClick() {
        countDownTimer = new PechaKuchaTimer(timeLeftOnPause + 1); // add a buffer second
        countDownTimer.start();
        setupRunningTimerInterface();
    }

    @OnClick(R.id.info_button)
    public void onInfoButtonClick() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle(R.string.info_dialog_title)
              .setIcon(R.drawable.info)
              .setMessage(getString(R.string.app_name) + "\n" +
                          getString(R.string.version_label) + " " + BuildConfig.VERSION_NAME + "\n" +
                          getString(R.string.created_by) + " " + BuildConfig.AUTHOR_NAME + " (" + getString(R.string.website) + ")")
              .setPositiveButton(getString(R.string.done), null)
              .create().show();
    }

    private void setupInitialInterface() {
        periodsDisplay.setTextColor(Color.WHITE);
        periodTimeDisplay.setTextColor(Color.WHITE);

        startButton.setText(R.string.start);
        startButton.setVisibility(View.VISIBLE);
        resumeButton.setVisibility(View.GONE);
    }

    private void setupRunningTimerInterface() {
        periodsDisplay.setEnabled(false);
        periodsDisplay.removeTextChangedListener(textWatcher);
        periodsDisplay.setTextColor(Color.WHITE);
        periodTimeDisplay.setEnabled(false);
        periodTimeDisplay.removeTextChangedListener(textWatcher);
        periodTimeDisplay.setTextColor(Color.WHITE);

        startButton.setVisibility(View.GONE);
        resumeButton.setVisibility(View.GONE);
        pauseButton.setVisibility(View.VISIBLE);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private void setupStoppedTimerInterface() {
        periodsDisplay.setEnabled(true);
        periodsDisplay.addTextChangedListener(textWatcher);
        periodTimeDisplay.setEnabled(true);
        periodTimeDisplay.addTextChangedListener(textWatcher);

        startButton.setText(R.string.restart);
        startButton.setVisibility(View.VISIBLE);
        pauseButton.setVisibility(View.GONE);

        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }


    private int getNumber(EditText editText) {
        String text = editText.getText().toString();
        if (TextUtils.isEmpty(text)) {
            text = editText.getHint().toString();
        }
        return Integer.parseInt(text);
    }


    private class PechaKuchaTimer extends CountDownTimer {

        private int secondsLeft;
        private boolean timerRunning;

        PechaKuchaTimer(int seconds) {
            super(seconds * 1000, 1000);
            timerRunning = true; // it'll be running very soon so just set to true
            secondsLeft = seconds * 1000;
        }

        PechaKuchaTimer(int periodLength, int numPeriods) {
            super(periodLength * numPeriods * 1000, 1000);
            timerRunning = true; // it'll be running very soon so just set to true
            secondsLeft = periodLength * numPeriods * 1000;
        }

        @Override
        public void onTick(long millisUntilFinished) {
            secondsLeft = (int) (millisUntilFinished / 1000);
            periodTimeDisplay.setText(String.valueOf(secondsLeft % periodLength));
            periodsDisplay.setText(String.valueOf((secondsLeft / periodLength) + 1));
        }

        @Override
        public void onFinish() {
            timerRunning = false;
            periodTimeDisplay.setText("0");
            periodTimeDisplay.setTextColor(Color.RED);

            periodsDisplay.setText("0");
            periodsDisplay.setTextColor(Color.RED);

            setupStoppedTimerInterface();
        }

        int getSecondsLeft() {
            return secondsLeft;
        }

        boolean isTimerRunning() {
            return timerRunning;
        }
    }
}
