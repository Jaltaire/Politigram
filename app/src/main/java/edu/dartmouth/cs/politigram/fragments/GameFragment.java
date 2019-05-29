package edu.dartmouth.cs.politigram.fragments;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.util.SparseArray;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.vision.Frame;
import com.google.android.gms.vision.face.Face;
import com.google.android.gms.vision.face.FaceDetector;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import edu.dartmouth.cs.politigram.models.GameObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.GameHistoryActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;
import edu.dartmouth.cs.politigram.utils.StringToHash;


// Fragment within MainActivity to control gaming aspects of Politigram.
public class GameFragment extends Fragment {

    private View mFragmentView;
    private ProgressBar mProgressBar;

    Bitmap bitmap;

    SetupGame setupGame;

    ArrayList<Map<String,String>> mapList;
    int indexInList = 0;
    ImageView imageView;
    TextView scoreTextView;
    int score = 0;

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        Log.d("onCreateView","Called");
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mFragmentView = view.findViewById(R.id.game_fragment_constraint_layout);
        mProgressBar = view.findViewById(R.id.game_fragment_progress);

        ImageButton goToGameHistory = view.findViewById(R.id.game_history_button);
        imageView = view.findViewById(R.id.imageView3);
        Button leansLeftBtn = view.findViewById(R.id.game_leans_left_button);
        Button leansRightBtn = view.findViewById(R.id.game_leans_right_button);
        scoreTextView = view.findViewById(R.id.pagetitle2);
        Log.d("onViewCreated","called");


        goToGameHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GameHistoryActivity.class);
                startActivity(intent);
            }
        });
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String Email = mAuth.getCurrentUser().getEmail();
        database1.child("politigram_users")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        setupGame = new SetupGame();
                        setupGame.execute(dataSnapshot);
                        showProgress(true);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        showProgress(true);

        leansLeftBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Clicked(0);

            }
        });

        leansRightBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Clicked(1);
            }
        });

    }

    // Create a hashmap of all Firebase users without privacy mode enabled.
    // Will use these photos in the game.
    public Map<String,String> createMapForUserPhotos(DataSnapshot dataSnapshot){
        Map<String,String> map = new HashMap<>();

        for(DataSnapshot dataSnap : dataSnapshot.getChildren()){
            if(!dataSnap.child("profile_data").child("privacy").exists()|| !dataSnap.child("profile_data").child("privacy").getValue(Boolean.class)) {

                String image = dataSnap.child("profile_data").child("profilePicture").getValue().toString();
                byte[] decodedString = Base64.decode(image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                FaceDetector detector;
                detector = new FaceDetector.Builder(getContext())
                        .setTrackingEnabled(false)
                        .setLandmarkType(FaceDetector.ALL_LANDMARKS)
                        .build();

                Frame frame = new Frame.Builder().setBitmap(decodedByte).build();

                SparseArray<Face> faces = detector.detect(frame);

                if (faces.size() == 1) {

                    String politicalScore = dataSnap.child("profile_data").child("sliderPosition").getValue().toString();
                    map.put(image, politicalScore);

                }

            }
        }

        return map;

    }

    // AsyncTask to set up the game by checking that one face is present in each profile picture.
    // If a face cannot be found (or multiple faces are found), the photo is ignored, since a user therefore cannot guess his/her political affiliation.
    private class SetupGame extends AsyncTask<DataSnapshot, String, String> {

        @Override
        protected String doInBackground(DataSnapshot... dataSnapshots) {

            Map<String,String> map = createMapForUserPhotos(dataSnapshots[0]);
            mapList = new ArrayList<>();

            for(String image : map.keySet()) {
                Map<String,String> tempMap = new HashMap<>();
                tempMap.put("image", image);
                tempMap.put("political_bent_score",map.get(image));
                mapList.add(tempMap);
            }
            Collections.shuffle(mapList);

            Log.d("Refreshing image","0");
            byte[] byteArray = Base64.decode(mapList.get(0).get("image"), Base64.NO_WRAP);
            bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            showProgress(false);
            imageView.setImageBitmap(bitmap);
        }

    }

        public int getPoliticalBent(int sliderPosition){
        if(sliderPosition <= 50){
            return 0;
        }
        return 1;
    }

    // Handle winning and losing the game.
    // Upload the game score to Firebase once the game has ended.
    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Clicked(int politicalBent){
        if(politicalBent == getPoliticalBent(Integer.parseInt(mapList.get(indexInList).get("political_bent_score")))) {
            score +=1;
            indexInList += 1;
            if(indexInList == mapList.size()){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                TextView msg = new TextView(getActivity());
                msg.setText("CONGRATULATIONS!" + "\n" + "YOU'VE COMPLETED THE GAME!");
                msg.setGravity(Gravity.CENTER_HORIZONTAL);
                msg.setTextSize(30);
                msg.setTypeface(null, Typeface.BOLD);
                msg.setTextColor(getResources().getColor(R.color.teal));
                builder.setView(msg);;
                builder.setPositiveButton("Start New Game", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    public void onClick(DialogInterface dialog, int id) {
                    }
                });
                builder.show();
                //Create Game Object
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
                String currentTime = sdf.format(new Date());
                SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
                String currentDate = sdf1.format(new Date());
                String dateTime = currentDate + "  " + currentTime;
                GameObject gameObject = new GameObject(String.valueOf(score), dateTime);

                //When Game ends and we want to save the score in Firebase
                DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
                FirebaseAuth mAuth = FirebaseAuth.getInstance();
                String Email = mAuth.getCurrentUser().getEmail();
                database1.child("politigram_users").child("user_" + StringToHash.getHex(Email)).child("game_results").push().setValue(gameObject);

                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new GameFragment());
                fragmentTransaction.commit();

            }else {
                byte[] byteArray = Base64.decode(mapList.get(indexInList).get("image"), Base64.NO_WRAP);
                Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                imageView.setImageBitmap(bitmap);
                scoreTextView.setText("SCORE: " + (score));
            }

        }else{
            android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(getActivity());
            TextView msg = new TextView(getActivity());
            msg.setText("GAME OVER!" + "\n" + "Your Score: " + score);
            msg.setGravity(Gravity.CENTER_HORIZONTAL);
            msg.setTextSize(30);
            msg.setTypeface(null, Typeface.BOLD);
            msg.setTextColor(getResources().getColor(R.color.teal));
            builder.setView(msg);
            builder.setPositiveButton("Start New Game", new DialogInterface.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                public void onClick(DialogInterface dialog, int id) {
                }
            });
            builder.show();
            //Create Game Object
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
            String currentTime = sdf.format(new Date());
            SimpleDateFormat sdf1 = new SimpleDateFormat("MM/dd/yyyy");
            String currentDate = sdf1.format(new Date());
            String dateTime = currentDate + "  " + currentTime;
            GameObject gameObject = new GameObject(String.valueOf(score), dateTime);

            //When Game ends and we want to save the score in Firebase
            DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            String Email = mAuth.getCurrentUser().getEmail();
            database1.child("politigram_users").child("user_" + StringToHash.getHex(Email)).child("game_results").push().setValue(gameObject);
            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new GameFragment());
            fragmentTransaction.commit();
        }

    }

    // Show progress bar UI when game is being loaded.
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

    // Stop AsyncTask if fragment changes, since original fragment will no longer be attached to the activity.
    @Override
    public void onPause() {
        super.onPause();

        if (setupGame != null && setupGame.getStatus().equals(AsyncTask.Status.RUNNING)) {
            setupGame.cancel(true);
        }
    }
}
