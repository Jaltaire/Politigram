package edu.dartmouth.cs.politigram.fragments;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.soundcloud.android.crop.Crop;

import java.io.File;
import java.io.IOException;

import edu.dartmouth.cs.politigram.ClassifierObject;
import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.R;

import static android.app.Activity.RESULT_OK;

public class ClassifierFragment extends android.app.Fragment {

    private static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    private static final int REQUEST_CODE_TAKE_FROM_GALLERY = 1;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final String PROFILE_PICTURE_INSTANCE_STATE_KEY = "saved_profile_picture";

    private Uri mTempImageCaptureURI;
    private Uri mImageCaptureURI;

    private ImageView mImageView;

    private Button mPredictionButton;
    private Button mHistoryButton;

    private boolean mSavedProfilePicture = true;

    private boolean mAllowClassification = false;

    public ClassifierFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_classifier, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mImageView = view.findViewById(R.id.classifier_fragment_image_view);

        mPredictionButton = view.findViewById(R.id.classifier_fragment_prediction_button);
        mHistoryButton = view.findViewById(R.id.classifier_fragment_history_button);

        mPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSetProfilePicturePressed(view);
            }
        });

        checkDevicePermissions();

        String image = "image";
        String result = "liberal";

        //Create ClassifierObject once we have the score
        ClassifierObject classifierObject = new ClassifierObject(image,result);

        //When photo is selected, add classifierObject to Firebase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        database1.child("user_" + mUserId).child("classifier_results").push().setValue(classifierObject);

        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getActivity(), ClassifierHistoryFragment.class);

            }
        });

    }

    //Check if camera and storage permissions have been granted.
    private void checkDevicePermissions() {
        if(Build.VERSION.SDK_INT < 23)
            return;

        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 0);
        }
    }

    // Prompt user to provide permission to access camera and external storage.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED){ // If storage or camera permissions have not been granted...
            if (Build.VERSION.SDK_INT >= 23) { // If Android system is running Marshmallow or higher...
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
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

                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        mPredictionButton.setEnabled(false);
                    }

                    if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        mPredictionButton.setEnabled(false);
                    }

                }
            }
        }
    }

    public void onSetProfilePicturePressed(View v) {
        displayDialog(PolitigramDialogFragment.DIALOG_ID_CLASSIFIER_PHOTO_SELECTOR);
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
                mTempImageCaptureURI = getActivity().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

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
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case REQUEST_CODE_TAKE_FROM_CAMERA:
                beginCrop(mTempImageCaptureURI);
                Log.d("TEST", "beginning crop");
                break;

            case REQUEST_CODE_TAKE_FROM_GALLERY:
                mTempImageCaptureURI = data.getData();
                beginCrop(mTempImageCaptureURI);
                Log.d("TEST", "beginning crop");
                break;

            case Crop.REQUEST_CROP:
                handleCrop(resultCode, data);
                Log.d("TEST", "now handling crop");
                break;
        }
    }

    private void beginCrop(Uri source) {

        // Get cached image taken by camera or selected from gallery.
        // Prevents an additional photo from being saved to storage during crop.
        Uri destination = Uri.fromFile(new File(getActivity().getCacheDir(), "cropped_photo"));

        // Crop the specified photo.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Crop.of(source, destination).asSquare().start(getContext(), this);
        }

    }

    private void handleCrop(int resultCode, Intent result) {

        Log.d("TEST", "crop handled");

        // If the crop was successful, keep the URI of the cropped image so it can be displayed, even if the main activity is refreshed.
        if (resultCode == RESULT_OK) {

            Log.d("TEST", "crop okay!");

            mImageCaptureURI = Crop.getOutput(result);
            checkForFace();
            //mImageView.setImageResource(0);
            //mImageView.setImageURI(mImageCaptureURI);

            mSavedProfilePicture = false;

        } else if (resultCode == Crop.RESULT_ERROR) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Toast.makeText(getContext(), Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

    }

    private void checkForFace() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Toast.makeText(getContext(), "Checking for faces in photo...", Toast.LENGTH_SHORT).show();
        }

        //mImageView.setImageResource(0);
        //mImageView.setImageURI(mImageCaptureURI);

        try {

            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageCaptureURI);

            Bitmap tempBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);

            tempCanvas.drawBitmap(bitmap, 0, 0, null);

            FaceDetector detector = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                detector = new FaceDetector.Builder(getContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();

                Frame frame = new Frame.Builder().setBitmap(bitmap).build();

                SparseArray<Face> faces = detector.detect(frame);
                Log.d("TEST", Integer.toString(faces.size()));

                Paint paint = new Paint();
                paint.setColor(Color.RED);

                int scale = 1;

                for (int i = 0; i < faces.size(); ++i) {
                    Face face = faces.valueAt(i);
                    for (Landmark landmark : face.getLandmarks()) {
                        int cx = (int) (landmark.getPosition().x * scale);
                        int cy = (int) (landmark.getPosition().y * scale);
                        tempCanvas.drawCircle(cx, cy, 10, paint);
                    }
                }

                detector.release();

                mImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

                if (faces.size() != 1) {

                    mPredictionButton.setText("SELECT NEW PHOTO");
                    mPredictionButton.setBackground(getResources().getDrawable(R.drawable.bgbtnguide_invalid));

                    if (faces.size() == 0) {
                        Toast.makeText(getContext(), "No face detected! Select a new photo.", Toast.LENGTH_SHORT).show();
                    }

                    else {
                        Toast.makeText(getContext(), "Multiple faces detected! Select a new photo.", Toast.LENGTH_SHORT).show();
                    }

                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}

//setValue(image & resultfromAPI) for firebase realtimedatabase when user clicks button(either select photo or run or something)
