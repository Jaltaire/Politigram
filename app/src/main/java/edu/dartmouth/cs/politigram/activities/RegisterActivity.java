package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import edu.dartmouth.cs.politigram.R;


// Handles new user registration.
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private SeekBar mPoliticalLeaningSeekBar;
    private TextView mPoliticalLeaningTextView;

    private TextView mLoginTextView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPoliticalLeaningSeekBar = findViewById(R.id.political_leaning_seek_bar);
        mPoliticalLeaningTextView = findViewById(R.id.political_leaning_text_view);

        mLoginTextView = findViewById(R.id.login_text_view);

        Point maxSizePoint = new Point();
        getWindowManager().getDefaultDisplay().getSize(maxSizePoint);
        final int maxX = maxSizePoint.x;
        mPoliticalLeaningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    double thumbX = mPoliticalLeaningSeekBar.getThumb().getBounds().exactCenterX();
                    handlePoliticalLeanText(progressValue, thumbX);

                    int backgroundColor = interpolateColor(Color.rgb(63,63,228), Color.rgb(228,63,63), (0.5f * progressValue/100f));
                    setActivityBackgroundColor(backgroundColor);
                }
                //int middle = this.getHeight()/2;
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, LoginActivity.REQUEST_CREDENTIALS);
                finish();

            }
        });

    }

    private void handlePoliticalLeanText(int progress, double thumbX) {

        String leaningLabel;

        if (progress >= 0 && progress < 14) leaningLabel = "FAR LEFT";
        else if (progress >= 14 && progress < 28) leaningLabel = "LEFT";
        else if (progress >= 28 && progress < 42) leaningLabel = "CENTER-LEFT";
        else if (progress >= 42 && progress <= 58) leaningLabel = "CENTER";
        else if (progress > 58 && progress <= 72) leaningLabel = "CENTER-RIGHT";
        else if (progress > 72 && progress <= 86) leaningLabel = "RIGHT";
        else leaningLabel = "FAR RIGHT";

        mPoliticalLeaningTextView.setText(leaningLabel);

        mPoliticalLeaningTextView.setX((float) thumbX);

    }

    private float interpolate(float a, float b, float proportion) {
        return (a + ((a - b) * proportion));
    }

    private int interpolateColor(int a, int b, float proportion) {
        float[] hsva = new float[3];
        float[] hsvb = new float[3];
        Color.colorToHSV(a, hsva);
        Color.colorToHSV(b, hsvb);
        for (int i = 0; i < 3; i++) {
            hsvb[i] = interpolate(hsva[i], hsvb[i], proportion);
        }
        return Color.HSVToColor(hsvb);
    }

    private void setActivityBackgroundColor(int color) {

        // Tint the color to make it darker.

        Color myColor = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {

            View view = this.getWindow().getDecorView();

            myColor = Color.valueOf(color);
            float r = myColor.red() * (2f/3f);
            float g = myColor.green() * (2f/3f);
            float b = myColor.blue() * (2f/3f);

            int newColor = Color.rgb(r, g, b);

            //view.setBackgroundColor(newColor);

            //Window window = this.getWindow();
            //window.setNavigationBarColor(newColor);
        }

    }

}