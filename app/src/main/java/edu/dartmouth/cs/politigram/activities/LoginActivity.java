package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.dartmouth.cs.politigram.R;


// Handles credentials validation with Firebase Authentication for Politigram user accounts.
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CREDENTIALS = 0;

    private Button mLoginButton;
    private TextView mRegisterTextView;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginButton = findViewById(R.id.login_button);
        mRegisterTextView = findViewById(R.id.register_text_view);

        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivityForResult(intent, REQUEST_CREDENTIALS);
                finish();

            }
        });

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivityForResult(intent, REQUEST_CREDENTIALS);
                finish();

            }
        });

    }


}
