package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.content.SharedPreferences;
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
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.utils.StringToHash;


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

    public static String profilePictureBytes;
    public static String email;
    public static String password;

    public static String username = "";
    public static Integer politicalLeaning = -1;
    public static Integer leaderboardPosition = -1;

    SharedPreferences signInPrefs;
    private boolean mSignedIn;
    private static final String SIGNED_IN_KEY = "signed_in";
    public static Boolean loginSuccessful;

    public static FirebaseAuth mAuth = FirebaseAuth.getInstance();

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        signInPrefs = getSharedPreferences(SIGNED_IN_KEY, MODE_PRIVATE);

        mLoginButton = findViewById(R.id.login_button);
        mRegisterTextView = findViewById(R.id.register_text_view);

        mEmail = findViewById(R.id.login_email_edit_text);
        mPassword = findViewById(R.id.login_password_edit_text);

        checkSignedIn();

        //If Sign in button is pressed + Entered email&password are valid ---> opens up Main Activity Page
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (confirmValidation()) {
                    signIn();
                }
            }
        });

        mRegisterTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                startActivityForResult(intent, REQUEST_CREDENTIALS);
                finish();

            }
        });

    }

    // Check if the user has already signed in.
    // If already signed in, open MainActivity.
    private void checkSignedIn() {
        mSignedIn = signInPrefs.getBoolean("signed_in", false);

        if (mSignedIn) {
            signIn();
            getProfileData();
            //launchMainActivity();
        }
    }

    // Attempt sign in using specified data.
    // If email and password are a valid set of Firebase Auth credentials, load the data for the account associated with that user's email address.
    // Otherwise, tell the user what type of error is encountered.
    private void signIn() {

        if (mSignedIn) {
            email = signInPrefs.getString("email", "");
            password = signInPrefs.getString("password", "");
        }
        else {
            email = mEmail.getText().toString();
            password = mPassword.getText().toString();
        }

        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {
                    loginSuccessful = true;

                    updateSignInSharedPreferences();

                    if (!mSignedIn) {
                        getProfileData();
                    }

                    Log.d("TEST", "task successful");
                }
                else {

                    loginSuccessful = false;

                    try {
                        Log.d("TEST", "task unsuccessful");
                        throw task.getException();
                    }

                    catch (FirebaseAuthInvalidUserException user)
                    {
                        Toast.makeText(getApplicationContext(), "Account does not exist.", Toast.LENGTH_SHORT).show();
                    }
                    catch (FirebaseAuthInvalidCredentialsException credentials)
                    {
                        Toast.makeText(getApplicationContext(), "Email and password do not match.", Toast.LENGTH_SHORT).show();
                    }
                    catch (Exception e)
                    {
                        Log.d("TEST", "onComplete: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Could not log in. Check your Internet connection.", Toast.LENGTH_SHORT).show();
                    }

                }

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
            else mEmail.setError("This email address is invalid.");
        }
        else mEmail.setError("This field is required.");

        if(mPassword.getText().length() > 0){
            if(mPassword.getText().length() > 5){
                mValidPassword = true;
            }
            else mPassword.setError("Password must be at least 6 characters.");
        }
        else mPassword.setError("This field is required.");

        if(mValidEmail && mValidPassword){
            return true;
        }
        else return false;

    }

    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    // Update SharedPreferences with saved sign in data.
    private void updateSignInSharedPreferences() {

        SharedPreferences.Editor editor = signInPrefs.edit();
        editor.putBoolean("signed_in", true);
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();

    }

    private void getProfileData() {

        DatabaseReference usersRef = ref.child(ProfileActivity.FIREBASE_USERS_PATH);

        usersRef.child("user_" + StringToHash.getHex(email)).child("profile_data").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                profilePictureBytes = dataSnapshot.child("profilePicture").getValue(String.class);
                username = dataSnapshot.child("username").getValue(String.class);
                politicalLeaning = dataSnapshot.child("sliderPosition").getValue(Integer.class);

                // Launch MainActivity after all profile data has been loaded.
                launchMainActivity();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TEST", "Could not get data from Firebase");
            }
        });

    }

    // Launch MainActivity from SignInActivity.
    private void launchMainActivity() {
        Intent myIntent = new Intent(LoginActivity.this, MainActivity.class);
        LoginActivity.this.startActivity(myIntent);
    }

    // Prevent being able to back into MainActivity after logging out.
    // Instead, exit the app.
    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

}
