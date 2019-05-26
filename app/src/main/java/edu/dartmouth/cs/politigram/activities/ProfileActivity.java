package edu.dartmouth.cs.politigram.activities;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soundcloud.android.crop.Crop;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.HashSet;
import java.util.Set;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.fragments.PolitigramDialogFragment;
import edu.dartmouth.cs.politigram.models.ProfileEntry;
import edu.dartmouth.cs.politigram.utils.PoliticalLeaningConversion;
import edu.dartmouth.cs.politigram.utils.StringToHash;


// Handles new user registration.
public class ProfileActivity extends AppCompatActivity {
    private static final String TAG = "ProfileActivity";

    private TextView mProfileTitleTextView;

    private SeekBar mPoliticalLeaningSeekBar;
    private TextView mPoliticalLeaningTextView;
    private EditText mEmail;
    private EditText mPassword;
    private EditText mUsername;
    private TextView mUsernameTextView;
    private boolean mValidUsername;
    private boolean mValidEmail;
    private boolean mValidPassword;
    private TextView mLoginTextView;
    private Button mRegisterBtn;

    private static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    private static final int REQUEST_CODE_TAKE_FROM_GALLERY = 1;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final String PROFILE_PICTURE_INSTANCE_STATE_KEY = "saved_profile_picture";

    private static final String SAVED_PROFILE_KEY = "saved_profile";

    public static final String FIREBASE_USERS_PATH = "politigram_users";

    // mTempImageCaptureURI used to prevent setting a null mImageCaptureURI if a photo is taken and not cropped.
    // As such, device can be rotated after this action and most recent profile picture will be retained.
    private Uri mTempImageCaptureURI;
    private Uri mImageCaptureURI;

    private ImageView mImageView;
    private boolean mSavedProfilePicture = true;

    private FloatingActionButton mSetProfilePictureButton;

    final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();

    private int mCallerFlag;
    private boolean mUsernameAvailable;

    private Set<String> takenUsernames = new HashSet<>();

    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        if (getCallingActivity() != null) {
            String callingClass = getCallingActivity().getClassName();

            Log.d("TEST", "calling class: " + callingClass);

            if (callingClass.equals(LoginActivity.class.getName())) mCallerFlag = 0;
            else if (callingClass.equals(SettingsActivity.class.getName())) mCallerFlag = 1;

        }

        mProfileTitleTextView = findViewById(R.id.profile_title_text_view);

        mImageView = findViewById(R.id.profile_picture_image_view);
        mSetProfilePictureButton = findViewById(R.id.set_profile_picture_button);

        mPoliticalLeaningSeekBar = findViewById(R.id.political_leaning_seek_bar);
        mPoliticalLeaningTextView = findViewById(R.id.political_leaning_text_view);

        mLoginTextView = findViewById(R.id.login_text_view);
        mRegisterBtn = findViewById(R.id.profile_save_button);

        mUsername = findViewById(R.id.register_username_edit_text);
        mUsernameTextView = findViewById(R.id.register_username_available_text_view);
        mEmail = findViewById(R.id.register_email_edit_text);
        mPassword = findViewById(R.id.register_password_edit_text);

        setupUI();

        checkDevicePermissions();
        getTakenUsernames();

        mSetProfilePictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSetProfilePicturePressed(v);
            }
        });

        mUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TEST", "beforeTextChanged");
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Log.d("TEST", "onTextChanged");
                handleEnteredUsername();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                Log.d("TEST", "afterTextChanged");
            }
        });

        Point maxSizePoint = new Point();
        getWindowManager().getDefaultDisplay().getSize(maxSizePoint);
        final int maxX = maxSizePoint.x;
        mPoliticalLeaningSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    double thumbX = mPoliticalLeaningSeekBar.getThumb().getBounds().exactCenterX();
                    handlePoliticalLeanText(progressValue, thumbX);

                    int backgroundColor = interpolateColor(Color.rgb(63, 63, 228), Color.rgb(228, 63, 63), (0.5f * progressValue / 100f));
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

        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (confirmValidation()) {
                    if (mCallerFlag == 0) registerUserOnFirebase();
                    else if (mCallerFlag == 1) saveProfileToFirebase();
                }
            }
        });


        mLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mCallerFlag == 0) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivityForResult(intent, LoginActivity.REQUEST_CREDENTIALS);
                    finish();
                }

                else if (mCallerFlag == 1) {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    //Check if camera and storage permissions have been granted.
    private void checkDevicePermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }
    }

    // Prompt user to provide permission to access camera and external storage.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){ // If storage or camera permissions have not been granted...
            if (Build.VERSION.SDK_INT >= 23) { // If Android system is running Marshmallow or higher...
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                    alertDialog.setMessage("Permission is required for this app to work properly.").setTitle("Permission Required");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) { // Anonymous function to check if external storage can be used.
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0); }
                            }
                    );
                    // Continue trying to request permissions unless user specifies "don't ask me again".
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
                }
                else { // If a user refuses to accept the requested permissions, the user cannot take a photo or save app data.

                    if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        mSetProfilePictureButton.setVisibility(View.INVISIBLE);
                    }

                    if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        mSetProfilePictureButton.setVisibility(View.INVISIBLE);
                    }

                }
            }
        }
    }

    public void onSetProfilePicturePressed(View v) {
        displayDialog(PolitigramDialogFragment.DIALOG_ID_PHOTO_SELECTOR);
    }

    public void displayDialog(int id) {
        DialogFragment fragment = PolitigramDialogFragment.newInstance(id);
        fragment.show(getFragmentManager(),
                getString(R.string.dialog_fragment_tag_photo_picker));
    }

    public void onPhotoPickerItemSelected(int item) {

        Intent intent;

        switch (item) {

            case PolitigramDialogFragment.ID_PHOTO_SELECTOR_FROM_CAMERA:
                // Take photo from the camera.
                intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                // Construct temporary image path and name to save the photo.
                ContentValues values = new ContentValues(1);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
                mTempImageCaptureURI = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                // Save the temporary URI as a value for the image's location.
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mTempImageCaptureURI);
                intent.putExtra("return-data", true);
                try {
                    // Try to take a photo using the camera app.
                    startActivityForResult(intent, REQUEST_CODE_TAKE_FROM_CAMERA);
                } catch (ActivityNotFoundException e) {
                    e.printStackTrace();
                }
                break;

            case PolitigramDialogFragment.ID_PHOTO_SELECTOR_FROM_GALLERY:
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(i, REQUEST_CODE_TAKE_FROM_GALLERY);

            default: // For any other case, end the method.
                return;
        }

    }

    // Handle data after activity returns from implicit intents.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA:
                beginCrop(mTempImageCaptureURI);
                break;

            case REQUEST_CODE_TAKE_FROM_GALLERY:
                mTempImageCaptureURI = data.getData();
                beginCrop(mTempImageCaptureURI);
                break;

            case Crop.REQUEST_CROP:
                handleCrop(resultCode, data);
                break;
        }
    }

    private void beginCrop(Uri source) {

        // Get cached image taken by camera or selected from gallery.
        // Prevents an additional photo from being saved to storage during crop.
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped_photo"));

        // Crop the specified photo.
        Crop.of(source, destination).asSquare().start(this);

    }

    private void handleCrop(int resultCode, Intent result) {

        // If the crop was successful, keep the URI of the cropped image so it can be displayed, even if the main activity is refreshed.
        if (resultCode == RESULT_OK) {

            mImageCaptureURI = Crop.getOutput(result);
            mImageView.setImageResource(0);
            mImageView.setImageURI(mImageCaptureURI);

            mSavedProfilePicture = false;

        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }

    }

    private void handlePoliticalLeanText(int progress, double thumbX) {

        String label = PoliticalLeaningConversion.handlePoliticalLeaningValue(progress);

        mPoliticalLeaningTextView.setText(label.toUpperCase());
        //mPoliticalLeaningTextView.setX((float) thumbX);

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

        if (!mUsernameAvailable && mUsername.getText().length() > 1) mUsername.setError("Username unavailable. Select a different username.");

        if (mUsername.getText().length() > 1) {
            mValidUsername = true;
        }
        else if (mUsername.getText().length() == 0) mUsername.setError("This field is required.");

        if (mEmail.getText().length() > 0) {
            if (isValidEmail(mEmail.getText().toString())) {
                mValidEmail = true;
            } else mEmail.setError("This email address is invalid.");
        } else mEmail.setError("This field is required.");

        if (mPassword.getText().length() > 0) {
            if (mPassword.getText().length() > 5) {
                mValidPassword = true;
            } else mPassword.setError("Password must be at least 6 characters.");
        } else mPassword.setError("This field is required.");

        if (mValidEmail && mValidPassword && mValidUsername && mUsernameAvailable) {
            return true;
        } else return false;
    }

    //Standard method used to check validity of email
    public static boolean isValidEmail(CharSequence target) {
        return (!TextUtils.isEmpty(target) && Patterns.EMAIL_ADDRESS.matcher(target).matches());
    }

    public void onSaveFinished() {
        if (mCallerFlag == 0) {
            Toast.makeText(getApplicationContext(), getString(R.string.registered_toast), Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
            startActivityForResult(intent, LoginActivity.REQUEST_CREDENTIALS);
        }
        else if (mCallerFlag == 1) {
            Toast.makeText(getApplicationContext(),
                    getString(R.string.profile_saved_toast),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    // Add new user on Firebase through FirebaseAuth.
    private void registerUserOnFirebase() {

        String email = mEmail.getText().toString();
        String password = mPassword.getText().toString();

        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    onSaveFinished();
                    saveProfileToFirebase();
                }
                else {

                    try
                    {
                        throw task.getException();
                    }

                    catch (FirebaseAuthWeakPasswordException weakPassword)
                    {
                        mPassword.setError(getString(R.string.edit_text_password_length_invalid));
                        mPassword.requestFocus();
                    }
                    catch (FirebaseAuthInvalidCredentialsException malformedEmail)
                    {
                        mEmail.setError(getString(R.string.edit_text_email_invalid));
                        mEmail.requestFocus();
                    }
                    catch (FirebaseAuthUserCollisionException existEmail)
                    {
                        mEmail.setError(getString(R.string.edit_text_email_taken));
                        mEmail.requestFocus();
                    }
                    catch (Exception e)
                    {
                        Log.d("TEST", "onComplete: " + e.getMessage());
                        Toast.makeText(getApplicationContext(), "Could not complete registration at this time. Check your Internet connection.", Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });

    }

    // Save profile data to Firebase Realtime Database.
    private void saveProfileToFirebase() {

        mImageView.buildDrawingCache();
        Bitmap bitmap = mImageView.getDrawingCache();
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,bos);
        byte[] bb = bos.toByteArray();
        String image = Base64.encodeToString(bb, Base64.NO_WRAP);


        String usernameText = mUsername.getText().toString();
        String emailText = mEmail.getText().toString();
        String passwordText = mPassword.getText().toString();

        Integer politicalBarPosition = mPoliticalLeaningSeekBar.getProgress();

        ProfileEntry mProfileEntry = new ProfileEntry(image, usernameText, politicalBarPosition);

        DatabaseReference usersRef = ref.child(FIREBASE_USERS_PATH);

        usersRef.child("user_" + StringToHash.getHex(emailText)).child("profile_data").setValue(mProfileEntry)
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()){
                            Log.d("TEST", "Successfully added/updated entry in Firebase");
                            onSaveFinished();
                        }
                        else {
                            if (task.getException() != null) Log.d("TEST", "Firebase insertion failed");
                        }
                    }
                });

        Log.d("TEST", "profile saved/updated on firebase");

        if (mCallerFlag == 1) {

            FirebaseUser user = LoginActivity.mAuth.getCurrentUser();
            user.updatePassword(passwordText)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Log.d("TEST", "User password updated.");
                            }
                        }
                    });

            LoginActivity.profilePictureBytes = image;
            LoginActivity.username = mUsername.getText().toString();
            LoginActivity.password = mPassword.getText().toString();
            LoginActivity.politicalLeaning = mPoliticalLeaningSeekBar.getProgress();

        }

    }

    private void getTakenUsernames() {

        DatabaseReference usersRef = ref.child(ProfileActivity.FIREBASE_USERS_PATH);

        usersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot entry : dataSnapshot.getChildren()) {

                    String username = entry.child("profile_data").child("username").getValue(String.class);
                    takenUsernames.add(username);

                }

                // Ignore current user's username if one exists.
                String currentUsername = LoginActivity.username;
                Log.d("TEST", "current username: " + currentUsername);
                if (currentUsername != null) takenUsernames.remove(LoginActivity.username);

                Log.d("TEST", takenUsernames.toString());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TEST", "Could not get data from Firebase");
            }
        });

    }

    private void handleEnteredUsername() {

        String usernameText = mUsername.getText().toString();

        if (usernameText.length() > 0) {
            if (takenUsernames.contains(usernameText)) {
                mUsernameTextView.setText(usernameText + " is unavailable. Choose a different username.");
                mUsernameAvailable = false;
            }
            else {
                mUsernameTextView.setText(usernameText + " is available!");
                mUsernameAvailable = true;
            }
        }
        else mUsernameTextView.setText("");

    }

    private void setupUI() {

        if (mCallerFlag == 1) {
            mProfileTitleTextView.setText("UPDATE PROFILE");
            mEmail.setEnabled(false);

            Bitmap decodedByte;
            String profilePictureBytesString = LoginActivity.profilePictureBytes;

            if (profilePictureBytesString != null) {
                byte[] decodedString = Base64.decode(profilePictureBytesString, Base64.DEFAULT);
                decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                mImageView.setImageBitmap(decodedByte);
            }

            mUsername.setText(LoginActivity.username);
            mEmail.setText(LoginActivity.email);
            mPassword.setText(LoginActivity.password);

            mPoliticalLeaningSeekBar.setProgress(LoginActivity.politicalLeaning);
            handlePoliticalLeanText(mPoliticalLeaningSeekBar.getProgress(), mPoliticalLeaningSeekBar.getThumb().getBounds().exactCenterX());

            mRegisterBtn.setText("SAVE PROFILE");

            mLoginTextView.setText(getText(R.string.cancel_profile_edits_text));

            handleEnteredUsername();
        }

    }

}