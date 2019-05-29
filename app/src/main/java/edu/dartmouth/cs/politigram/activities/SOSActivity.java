package edu.dartmouth.cs.politigram.activities;

import android.content.Context;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import edu.dartmouth.cs.politigram.R;

// Similates reception of a phone call in order to get out of a politically-dangerous situation (i.e. Politifucked).
public class SOSActivity extends AppCompatActivity {

    Vibrator v;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sos);

        runVibrationPattern();
    }

    // Creates an SOS vibration pattern for the end user; simulates vibrations during incoming call.
    private void runVibrationPattern() {
        v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

        int dot = 200;
        int dash = 500;
        int short_gap = 200;
        int medium_gap = 500;
        int long_gap = 1000;
        long[] pattern = {
                0,
                dot, short_gap, dot, short_gap, dot,    // s
                medium_gap,
                dash, short_gap, dash, short_gap, dash, // o
                medium_gap,
                dot, short_gap, dot, short_gap, dot,    // s
                long_gap
        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            v.vibrate(VibrationEffect.createWaveform(pattern, 1));
        } else {
            //deprecated in API 26
            v.vibrate(pattern, 1);
        }
    }

    // When activity is paused, stop vibrations.
    @Override
    protected void onPause() {
        super.onPause();

        v.cancel();
    }
}
