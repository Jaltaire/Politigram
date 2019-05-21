package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.dartmouth.cs.politigram.R;


// Handles new user registration.
public class RegisterActivity extends AppCompatActivity {
    private static final String TAG = "RegisterActivity";

    private SeekBar mPoliticalLeaningSeekBar;
    private TextView mPoliticalLeaningTextView;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mUsername;
    private boolean mValidUsername;
    private boolean mValidEmail;
    private boolean mValidPassword;
    private TextView mLoginTextView;

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mPoliticalLeaningSeekBar = findViewById(R.id.political_leaning_seek_bar);
        mPoliticalLeaningTextView = findViewById(R.id.political_leaning_text_view);

        mLoginTextView = findViewById(R.id.login_text_view);

        mUsername = findViewById(R.id.register_username_edit_text);
        mEmail = findViewById(R.id.register_email_edit_text);
        mPassword = findViewById(R.id.register_password_edit_text);

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
                if (confirmValidation()) {

                    final FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(),
                            mPassword.getText().toString()).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                        //Doesn't always run for some reason
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            Log.d("register", "complete");
                            if (task.isSuccessful()) {
                                //Sign in success, update UI with the signed-in user's information
                                Log.d("0", "createUserWithEmail:success");
                                Toast.makeText(RegisterActivity.this, "Authentication worked.",
                                        Toast.LENGTH_LONG).show();

                            } else {
                                // If sign in fails, display a message to the user.
                                Log.w("0", "createUserWithEmail:failure", task.getException());
                                Toast.makeText(RegisterActivity.this, "Authentication failed: "
                                                + task.getException().getMessage(),
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                    });

                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, LoginActivity.REQUEST_CREDENTIALS);
                    finish();

                }

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

    //Method called after Register button is pressed ---> checks that all required info has been inputted correctly
    //else returns false and generates error message in the respective TextInputEditTexts
    public boolean confirmValidation() {

        if (mUsername.getText().length() > 0) {
            mValidUsername = true;
        } else mUsername.setError("This field is required");

        if (mEmail.getText().length() > 0) {
            if (isValidEmail(mEmail.getText().toString())) {
                mValidEmail = true;
            } else mEmail.setError("This email address is invalid");
        } else mEmail.setError("This field is required");

        if (mPassword.getText().length() > 0) {
            if (mPassword.getText().length() > 7) {
                mValidPassword = true;
            } else mPassword.setError("Password must be at least 6 characters");
        } else mPassword.setError("This field is required");

        if (mValidEmail && mValidPassword && mValidUsername) {
            return true;
        } else return false;
    }

    //Standard method used to check validity of email
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

}