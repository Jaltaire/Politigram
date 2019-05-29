package edu.dartmouth.cs.politigram.fragments;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.graphics.drawable.BitmapDrawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.android.gms.vision.face.Landmark;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.soundcloud.android.crop.Crop;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import edu.dartmouth.cs.politigram.activities.LoginActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;
import edu.dartmouth.cs.politigram.activities.ProfileActivity;
import edu.dartmouth.cs.politigram.models.ClassifierEntry;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.ClassifierHistoryActivity;
import edu.dartmouth.cs.politigram.activities.ClassifierResultActivity;
import edu.dartmouth.cs.politigram.utils.InternetConnectionTester;
import edu.dartmouth.cs.politigram.utils.ReferenceVariables;
import edu.dartmouth.cs.politigram.utils.StringToHash;

import static android.app.Activity.RESULT_OK;

// Fragment within MainActivity to control classifier aspects of Politigram.
public class ClassifierFragment extends android.app.Fragment {

    private static final int REQUEST_CODE_TAKE_FROM_CAMERA = 0;
    private static final int REQUEST_CODE_TAKE_FROM_GALLERY = 1;
    private static final String URI_INSTANCE_STATE_KEY = "saved_uri";
    private static final String PROFILE_PICTURE_INSTANCE_STATE_KEY = "saved_profile_picture";

    public static final String INTENT_IMAGE_KEY = "intent_image_key";
    public static final String INTENT_LABEL_KEY = "intent_label_key";
    public static final String INTENT_CONFIDENCE_KEY = "intent_confidence_key";
    public static final String INTENT_STATE_KEY = "intent_state_key";

    private Uri mTempImageCaptureURI;
    private Uri mImageCaptureURI;

    private Bitmap mImageCaptureBitmap;
    private byte[] mImageCaptureByteArray;

    private View mFragmentView;
    private ProgressBar mProgressBar;

    private ImageView mImageView;

    private Button mPredictionButton;
    private Button mHistoryButton;

    private boolean mSavedProfilePicture = true;

    private boolean mAllowClassification = false;

    private boolean mScaleClassificationResultToLocation = true;

    private FusedLocationProviderClient fusedLocationClient;

    private boolean mPhotoFromCamera;

    Double scaledScore = null;
    String scaledLabel = null;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();
    DatabaseReference usersRef = ref.child(ProfileActivity.FIREBASE_USERS_PATH);

    SparseArray<Face> faces;

    CheckForFacesTask checkForFacesTask;
    PerformClassificationTask performClassificationTask;

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

        mFragmentView = view.findViewById(R.id.classifier_fragment_constraint_layout);
        mProgressBar = view.findViewById(R.id.classifier_fragment_progress);

        mImageView = view.findViewById(R.id.classifier_fragment_image_view);

        mPredictionButton = view.findViewById(R.id.classifier_fragment_prediction_button);
        mHistoryButton = view.findViewById(R.id.classifier_fragment_history_button);

        mPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (InternetConnectionTester.hasInternetConnection(getContext())) {
                    if (!mAllowClassification) onSetProfilePicturePressed(view);
                    else classifyPhoto();
                }
                else {
                    Toast.makeText(getContext(), "No Internet connection. Cannot use classifier.", Toast.LENGTH_LONG).show();
                }

            }
        });

        mHistoryButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                if (InternetConnectionTester.hasInternetConnection(getContext())) {
                    Intent intent = new Intent(getContext(), ClassifierHistoryActivity.class);
                    startActivity(intent);
                }
                else {
                    Toast.makeText(getContext(), "No Internet connection. Cannot view classifier history.", Toast.LENGTH_LONG).show();
                }
            }
        });

        checkDevicePermissions();
        //classifyPhoto();

        Log.d("is Datasnapshot null",Boolean.toString(MainActivity.dataSnap==null));

    }

    //Check if camera, storage, and location permissions have been granted.
    private void checkDevicePermissions() {
        if (Build.VERSION.SDK_INT < 23)
            return;

        if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
    }

    // Prompt user to provide permission to access camera, external storage, and device location.
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_DENIED || grantResults[1] == PackageManager.PERMISSION_DENIED || grantResults[2] == PackageManager.PERMISSION_DENIED || grantResults[3] == PackageManager.PERMISSION_DENIED) { // If storage or camera permissions have not been granted...
            if (Build.VERSION.SDK_INT >= 23) { // If Android system is running Marshmallow or higher...
                if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) || shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) || shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    AlertDialog.Builder alertDialog = new AlertDialog.Builder(getContext());
                    alertDialog.setMessage("Permission is required for this app to work properly.").setTitle("Permission Required");
                    alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                                public void onClick(DialogInterface dialog, int id) { // Anonymous function to check if external storage can be used.
                                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                                }
                            }
                    );
                    // Continue trying to request permissions unless user specifies "don't ask me again".
                    requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
                } else { // If a user refuses to accept the requested permissions, the user cannot take a photo or save app data.

                    if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        mPredictionButton.setEnabled(false);
                    }

                    if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        mPredictionButton.setEnabled(false);
                    }

                    if (getActivity().checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED || getActivity().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        mScaleClassificationResultToLocation = false;
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
                mPhotoFromCamera = true;
                beginCrop(mTempImageCaptureURI);
                Log.d("TEST", "beginning crop");
                break;

            case REQUEST_CODE_TAKE_FROM_GALLERY:
                mPhotoFromCamera = false;
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

    // Check that one face is present in the photo.
    // Prediction will be inaccurate if it does not match training data of single-person images.
    private void checkForFace() {

        Toast.makeText(getContext(), "Checking for faces in photo...", Toast.LENGTH_SHORT).show();

        //mImageView.setImageResource(0);
        //mImageView.setImageURI(mImageCaptureURI);

        try {

            mImageCaptureBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mImageCaptureURI);

            checkForFacesTask = new CheckForFacesTask();
            checkForFacesTask.execute();
            showProgress(true);


        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    // AsyncTask to check for presence of face in background. Avoids blocking UI thread.
    private class CheckForFacesTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {

            FaceDetector detector = null;
            detector = new FaceDetector.Builder(getContext())
                    .setTrackingEnabled(false)
                    .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                    .build();

            Frame frame = new Frame.Builder().setBitmap(mImageCaptureBitmap).build();

            faces = detector.detect(frame);

            detector.release();

            return null;

        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            Log.d("TEST", Integer.toString(faces.size()));

            Bitmap tempBitmap = Bitmap.createBitmap(mImageCaptureBitmap.getWidth(), mImageCaptureBitmap.getHeight(), Bitmap.Config.RGB_565);
            Canvas tempCanvas = new Canvas(tempBitmap);

            tempCanvas.drawBitmap(mImageCaptureBitmap, 0, 0, null);

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

            mImageView.setImageDrawable(new BitmapDrawable(getResources(), tempBitmap));

            if (faces.size() != 1) {

                mPredictionButton.setText("SELECT NEW PHOTO");
                mPredictionButton.setBackground(getResources().getDrawable(R.drawable.bgbtnguide_invalid));

                if (faces.size() == 0) {
                    Toast.makeText(getContext(), "No face detected! Select a new photo.", Toast.LENGTH_LONG).show();
                    showProgress(false);
                } else {
                    Toast.makeText(getContext(), "Multiple faces detected! Select a new photo.", Toast.LENGTH_LONG).show();
                    showProgress(false);
                }

            } else {

                mPredictionButton.setText("CLASSIFY PHOTO");
                mPredictionButton.setBackground(getResources().getDrawable(R.drawable.bgbtnguide_valid));
                mAllowClassification = true;
                showProgress(false);

            }

        }
    }

    // Classify the photo using Google's AutoML Vision platform.
    private void classifyPhoto() {

        Log.d("TEST", "attempting to classify photo");
        performClassificationTask = new PerformClassificationTask();
        performClassificationTask.execute();
        showProgress(true);

    }

    // Perform AutoML Vision classification in the background in order to avoid blocking UI thread.
    private class PerformClassificationTask extends AsyncTask<String, String, String> {
        protected String doInBackground(String... urls) {

            try {

                List<String> scopeList = new ArrayList<String>();
                scopeList.add("https://www.googleapis.com/auth/cloud-platform");

                InputStream is = getActivity().getAssets().open("app.json");
                GoogleCredential credential = GoogleCredential.fromStream(is).createScoped(scopeList);
                credential.refreshToken();
                final String accessToken = credential.getAccessToken();

                Log.d("TEST", accessToken);

                JSONObject payloadJSON = new JSONObject();
                try {

                    // Scale size of Bitmap such that AutoML Vision API can process picture even if original is large.
                    // Experienced API connection errors when trying to upload full-size images.
                    Bitmap tempBitmap = Bitmap.createScaledBitmap(mImageCaptureBitmap, 500, 500, false);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    tempBitmap.compress(Bitmap.CompressFormat.PNG, 100, bos);
                    mImageCaptureByteArray = bos.toByteArray();
                    String image = Base64.encodeToString(mImageCaptureByteArray, Base64.NO_WRAP);

                    JSONObject imageBytesJSON = new JSONObject();
                    imageBytesJSON.put("imageBytes", image);

                    JSONObject imageJSON = new JSONObject();
                    imageJSON.put("image", imageBytesJSON);

                    payloadJSON.put("payload", imageJSON);

                    Log.d("TEST", "json object created!");
                    Log.d("TEST", payloadJSON.toString());

                } catch (JSONException e) {
                    e.printStackTrace();
                    showProgress(false);
                }

                JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                        (Request.Method.POST, ReferenceVariables.AUTO_ML_HOOK, payloadJSON, new Response.Listener<JSONObject>() {

                            @Override
                            public void onResponse(JSONObject response) {
                                Log.d("TEST", "got response!");
                                Log.d("TEST", response.toString());

                                try {

                                    if (response.length() == 0) {
                                        Toast.makeText(getContext(), "Could not get accurate reading from photo. Try again with a different image.", Toast.LENGTH_LONG).show();
                                        mPredictionButton.setText("SELECT NEW PHOTO");
                                        mPredictionButton.setBackground(getResources().getDrawable(R.drawable.bgbtnguide_invalid));
                                        mAllowClassification = false;
                                        showProgress(false);

                                    } else {

                                        JSONArray jsonArray = response.getJSONArray("payload");

                                        JSONObject jsonIntermediary = jsonArray.getJSONObject(0);

                                        String label = jsonIntermediary.getString("displayName");
                                        String score = jsonIntermediary.getJSONObject("classification").getString("score");

                                        handleCurrentLocation(label, score);

                                    }


                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    showProgress(false);
                                }

                            }
                        }, new Response.ErrorListener() {

                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: Handle error
                                Log.d("TEST", "POST request unsuccessful");
                                Toast.makeText(getContext(), "Classification unsuccessful. Try again.", Toast.LENGTH_LONG).show();
                                showProgress(false);
                                error.printStackTrace();
                            }

                        }) {

                    @Override
                    public Map<String, String> getHeaders() throws AuthFailureError {

                        Map<String, String> params = new HashMap<String, String>();
                        params.put("Authorization", "Bearer " + accessToken);

                        return params;
                    }
                };

                RequestQueue queue = null;
                queue = Volley.newRequestQueue(getContext());
                queue.add(jsonObjectRequest);

                Log.d("TEST", "end of volley api call");


            } catch (IOException e) {
                e.printStackTrace();
                showProgress(false);
            }

            return null;
        }

    }

    // If current location can be acquired, use it to improve the classifier prediction by referencing 2016 U.S. presidential election voter data by state.
    // Ignore scaling with location data if location permissions have not been granted.
    // Only perform scaling for user-snapped photos (i.e. photos not selected from gallery), since metadata is more likely to be valid, whereas gallery metadata may not be accurate (i.e. for an image downloaded from the Internet).
    private void handleCurrentLocation(final String classifierLabel, final String classifierScore) {

        Log.d("TEST", "handling current location");

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(getActivity());
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            if (mPhotoFromCamera) {

                Log.d("TEST", "permissions granted...");

                fusedLocationClient.getLastLocation()
                        .addOnSuccessListener(getActivity(), new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                Log.d("TEST", "got location");
                                if (location != null) {

                                    Geocoder gcd = new Geocoder(getContext(), Locale.getDefault());
                                    List<Address> addresses;
                                    try {
                                        addresses = gcd.getFromLocation(location.getLatitude(),
                                                location.getLongitude(), 1);
                                        if (addresses.size() > 0) {
                                            String state = addresses.get(0).getAdminArea();
                                            String country = addresses.get(0).getCountryName();

                                            String label = classifierLabel;
                                            String score = classifierScore;

                                            Log.d("TEST", "my state: " + state);

                                            // If location can be determined, scale the classifier prediction according to 2016 U.S. presidential election voter data by state.
                                            if (state != null && country.equals("United States")) {
                                                computeScaledValues(classifierLabel, Double.valueOf(classifierScore), state);

                                                if (scaledScore != null && scaledLabel != null) {
                                                    Log.d("TEST", "using scaled values");
                                                    label = scaledLabel;
                                                    score = scaledScore.toString();

                                                    Log.d("TEST", label);
                                                    Log.d("TEST", score);

                                                    processClassifierResult(label, score, state);
                                                }

                                                else {
                                                    processClassifierResult(label, score, null);
                                                }
                                            }

                                            else {
                                                processClassifierResult(label, score, null);
                                            }

                                        }

                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        Toast.makeText(getContext(), "Could not fetch data from last location.", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            }
                        });

            }

            else {
                processClassifierResult(classifierLabel, classifierScore, null);
            }

        }

        // If user has not granted locations permission, show the user the normal classifier results.
        else {
            processClassifierResult(classifierLabel, classifierScore, null);
        }

    }

    // Scale classifier values according to voting trends by state.
    private void computeScaledValues(String classifierLabel, double classifierScore, String currentState) {

        Log.d("TEST", "computing scaled values");

        Double democraticVoteCount = null;
        Double republicanVoteCount = null;

        try {
            InputStreamReader isr = new InputStreamReader(getActivity().getAssets().open("election-data.csv"));
            BufferedReader reader = new BufferedReader(isr);
            reader.readLine();
            String line;
            while ((line = reader.readLine()) != null) {
                String[] components = line.split(",");

                String state = components[1];
                String party = components[9];
                //String totalVoteCount = components[12];

                Log.d("TEST", state + " " + party + " " + components[11]);

                if (currentState.equals(state) && party.equals("democrat")) democraticVoteCount = Double.valueOf(components[11]);
                else if (currentState.equals(state) && party.equals("republican")) republicanVoteCount = Double.valueOf(components[11]);

                if (democraticVoteCount != null && republicanVoteCount != null) break;

            }

        }

        catch (IOException e) {
            e.printStackTrace();
        }

        // Normalize vote counts for each party (since some non-major-party candidates also receive votes).
        if (democraticVoteCount != null && republicanVoteCount != null) {
            Double totalVotes = democraticVoteCount + republicanVoteCount;

            Double democraticProportion = democraticVoteCount / totalVotes;
            Double republicanProportion = republicanVoteCount / totalVotes;

            Double opposingScore = 1 - classifierScore;

            if (classifierLabel.equals("Liberal")) {
                Double liberalScore = classifierScore * democraticProportion;
                Double conservativeScore = opposingScore * republicanProportion;

                Double totalScore = liberalScore + conservativeScore;

                // Normalize liberal and conservative scores.
                liberalScore = liberalScore/totalScore;
                conservativeScore = conservativeScore/totalScore;

                if (conservativeScore > liberalScore) {
                    scaledScore = conservativeScore;
                    scaledLabel = "Conservative";
                }
                else {
                    scaledScore = liberalScore;
                    scaledLabel = "Liberal";
                }

                Log.d("TEST", "updated scores");
            }

            else if (classifierLabel.equals("Conservative")) {
                Double liberalScore = opposingScore * democraticProportion;
                Double conservativeScore = classifierScore * republicanProportion;

                Double totalScore = liberalScore + conservativeScore;

                // Normalize liberal and conservative scores.
                liberalScore = liberalScore/totalScore;
                conservativeScore = conservativeScore/totalScore;

                if (liberalScore > conservativeScore) {
                    scaledScore = liberalScore;
                    scaledLabel = "Liberal";
                }
                else {
                    scaledScore = conservativeScore;
                    scaledLabel = "Conservative";
                }

                Log.d("TEST", "updated scores");
            }

        }

    }

    // Process the full classifier result (including after scaling), and then launch ClassifierResultActivity to display the results to the user.
    private void processClassifierResult(final String label, final String score, String state) {

        Bitmap bitmap = mImageCaptureBitmap;
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100, bos);
        byte[] bb = bos.toByteArray();
        final String image = Base64.encodeToString(bb, Base64.NO_WRAP);

        final Set<String> storedImageBytes = new HashSet<String>();

        // Get all classifier history image entries. If the same image has already been classified for this user, don't add the classification result to history again.
        // After a user gets the classifier results, we allow him/her to immediately reclassify the same image in case he/she wants to see the full classifier result again (i.e. with metadata stating that result was scaled with location).
        // As such, since we expect some users will re-classify the same image, we intentionally avoid adding the same image to a classifier history multiple times.
        usersRef.child("user_" + StringToHash.getHex(LoginActivity.email)).child("classifier_results").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot entry : dataSnapshot.getChildren()) {
                    String imageBytes = entry.child("imageBytes").getValue(String.class);
                    Log.d("TEST", "image: " + imageBytes);
                    storedImageBytes.add(imageBytes);
                }

                if (!storedImageBytes.contains(image)) {
                    uploadClassifierResult(image, label, score);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TEST", "Could not get data from Firebase");
            }
        });

        Intent intent = new Intent(getContext(), ClassifierResultActivity.class);
        intent.putExtra(INTENT_IMAGE_KEY, mImageCaptureByteArray);
        intent.putExtra(INTENT_LABEL_KEY, label);
        intent.putExtra(INTENT_CONFIDENCE_KEY, score);
        intent.putExtra(INTENT_STATE_KEY, state);
        startActivity(intent);

        showProgress(false);

    }

    // Add classifier result to history on Firebase.
    private void uploadClassifierResult(String image, String label, String score) {

        ClassifierEntry mClassifierEntry = new ClassifierEntry(image, label, score);

        usersRef.child("user_" + StringToHash.getHex(LoginActivity.email)).child("classifier_results").child(Long.toString(System.currentTimeMillis())).setValue(mClassifierEntry)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d("TEST", "Successfully added classifier entry in Firebase");
                        } else {
                            if (task.getException() != null)
                                Log.d("TEST", "Firebase insertion failed");
                        }
                    }
                });

    }

    private void showProgress(final boolean show) {
        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        mFragmentView.setVisibility(show ? View.GONE : View.VISIBLE);
        mFragmentView.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mFragmentView.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        mProgressBar.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                mProgressBar.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }

    // Cancel active AsyncTasks if active fragment changes since original fragment will no longer be attached to the activity.
    @Override
    public void onPause() {
        super.onPause();

        if (checkForFacesTask != null && checkForFacesTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            checkForFacesTask.cancel(true);
        }

        if (performClassificationTask != null && performClassificationTask.getStatus().equals(AsyncTask.Status.RUNNING)) {
            performClassificationTask.cancel(true);
        }
    }

}
