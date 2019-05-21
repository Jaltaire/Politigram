package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;

import edu.dartmouth.cs.politigram.R;


// Handles credentials validation with Firebase Authentication for Politigram user accounts.
public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";
    public static final int REQUEST_CREDENTIALS = 0;

    private Button mLoginButton;
    private TextView mRegisterTextView;
    private EditText mEmail;
    private EditText mPassword;
    private boolean mValidUsername;
    private boolean mValidEmail;
    private boolean mValidPassword;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mLoginButton = findViewById(R.id.login_button);
        mRegisterTextView = findViewById(R.id.register_text_view);

        mEmail = findViewById(R.id.login_email_edit_text);
        mPassword = findViewById(R.id.login_password_edit_text);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        //If Sign in button is pressed + Entered email&password are valid ---> opens up Main Activity Page
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmValidation()) {
                    mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString()).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                        startActivityForResult(intent, REQUEST_CREDENTIALS);
                                        finish();
                                        Log.d("0", "SignIn Authorized");

                                    } else {
                                        Toast.makeText(LoginActivity.this, "Email or Password is incorrect", Toast.LENGTH_LONG).show();
                                        Log.d("0", "SignIn not Authorized");
                                    }
                                }
                            }
                    );
                }

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

    //Method called to check validity of Email + Password entered
    //Compared strings entered to stored strings in Shared Preference (Profile database)
    private boolean confirmValidation() {

        if(mEmail.getText().length() > 0){
            if(isValidEmail(mEmail.getText().toString())){
                mValidEmail = true;
            }
            else mEmail.setError("This email address is invalid");
        }
        else mEmail.setError("This field is required");

        if(mPassword.getText().length() > 0){
            if(mPassword.getText().length() > 5){
                mValidPassword = true;
            }
            else mPassword.setError("Password must be at least 6 characters");
        }
        else mPassword.setError("This field is required");

        if(mValidEmail && mValidPassword){
            return true;
        }
        else return false;

    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }


}
