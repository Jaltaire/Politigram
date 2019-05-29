package edu.dartmouth.cs.politigram.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.fragments.ClassifierFragment;
import edu.dartmouth.cs.politigram.fragments.GameFragment;
import edu.dartmouth.cs.politigram.fragments.LeaderboardFragment;
import edu.dartmouth.cs.politigram.fragments.MainFragment;
import edu.dartmouth.cs.politigram.models.BoardObject;
import edu.dartmouth.cs.politigram.utils.PoliticalLeaningConversion;
import edu.dartmouth.cs.politigram.utils.StringToHash;

//test
public class MainActivity extends AppCompatActivity {

    ImageView mProfilePictureImageView;
    public static DataSnapshot dataSnap;

    TextView mUsername;
    TextView mPoliticalLeaning;
    TextView mLeaderBoardScore;

    Animation atg, atgtwo, atgthree;

    private LinearLayout mClassifierLinearLayout;
    private LinearLayout mGameLinearLayout;
    private LinearLayout mLeaderboardLinearLayout;
    private LinearLayout mSettingsLinearLayout;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new MainFragment());
        fragmentTransaction.commit();

        mLeaderBoardScore = findViewById(R.id.walletuser3);

        //Add onDataChange Listener here, so that constantly updating Classifier and Game Class
        //Retrieve data from RealTimeDatabase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        final String Email = mAuth.getCurrentUser().getEmail();
        database1.child("politigram_users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnap = dataSnapshot;
                        String currentUser = dataSnap.child("user_"+ StringToHash.getHex(Email)).child("profile_data").child("username").getValue().toString();
                        Log.d("MainActivity","onDataChange");
                        ArrayList<BoardObject> listOfSortedBoardObjects = createSortedListOfBoardObjects();
                        if (dataSnap.child("user_" + StringToHash.getHex(Email)).child("profile_data").child("privacy").exists()
                        && dataSnap.child("user_" + StringToHash.getHex(Email)).child("profile_data").child("privacy").getValue(Boolean.class)) {
                                mLeaderBoardScore.setText("N/A (privacy is on)");
                                Log.d("onDataChange","privacy is on");
                        }else {
                            if (dataSnap.child("user_" + StringToHash.getHex(Email)).child("game_results").exists()) {
                                int rank = 1;
                                for (BoardObject boardObject : listOfSortedBoardObjects) {
                                    if (boardObject.getUser().equals(currentUser)) {
                                        break;
                                    }
                                    rank += 1;
                                }
                                Log.d("onDataChange", "setting score to " + rank);
                                mLeaderBoardScore.setText("#" + rank);
                            } else {
                                mLeaderBoardScore.setText("N/A");
                                Log.d("onDataChange","never played");
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        atgtwo = AnimationUtils.loadAnimation(this, R.anim.atgtwo);
        atgthree = AnimationUtils.loadAnimation(this, R.anim.atgthree);

        mProfilePictureImageView = findViewById(R.id.main_profile_picture_image_view);
        mUsername = findViewById(R.id.main_username);
        mPoliticalLeaning = findViewById(R.id.main_political_affiliation);

        mClassifierLinearLayout = findViewById(R.id.classifier_linear_layout);
        mClassifierLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ClassifierFragment());
                fragmentTransaction.commit();
            }
        });

        mGameLinearLayout = findViewById(R.id.game_linear_layout);
        mGameLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new GameFragment());
                fragmentTransaction.commit();
            }
        });

        mLeaderboardLinearLayout = findViewById(R.id.leaderboard_linear_layout);
        mLeaderboardLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new LeaderboardFragment());
                fragmentTransaction.commit();
            }
        });

        mSettingsLinearLayout = findViewById(R.id.settings_linear_layout);
        mSettingsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void configureUI() {

        mUsername.setText(LoginActivity.username);
        mPoliticalLeaning.setText(PoliticalLeaningConversion.handlePoliticalLeaningValue(LoginActivity.politicalLeaning));

        Bitmap decodedByte;
        String profilePictureBytesString = LoginActivity.profilePictureBytes;

        if (profilePictureBytesString != null) {
            byte[] decodedString = Base64.decode(profilePictureBytesString, Base64.DEFAULT);
            decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            mProfilePictureImageView.setImageBitmap(decodedByte);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Called in onResume() such that header UI can be updated if changes are made to profile.
        configureUI();
    }

    // When back button is pressed, return to home screen on Android rather than signing out of Politigram account.
    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<BoardObject> createSortedListOfBoardObjects() {
        DataSnapshot dataSnapshot = MainActivity.dataSnap;
        ArrayList<BoardObject> listOfBoardObjects = new ArrayList<>();

        for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
            if (!dataSnap.child("profile_data").child("privacy").exists() || !dataSnap.child("profile_data").child("privacy").getValue(Boolean.class)) {
                if (dataSnap.child("game_results").getChildrenCount() != 0) {
                    String user = dataSnap.child("profile_data").child("username").getValue().toString();
                    Log.d("userForGameResults", user);
                    String image = dataSnap.child("profile_data").child("profilePicture").getValue().toString();
                    DataSnapshot dataSnap1 = dataSnap.child("game_results");
                    int maxScore = 0;
                    String score = "";
                    String dateTime = "";
                    for (DataSnapshot dataSnap2 : dataSnap1.getChildren()) {
                        if (Integer.parseInt(dataSnap2.child("score").getValue().toString()) >= maxScore) {
                            score = dataSnap2.child("score").getValue().toString();
                            dateTime = dataSnap2.child("dateTime").getValue().toString();
                            maxScore = Integer.parseInt(score);
                        }
                    }
                    BoardObject boardObject = new BoardObject(user, score, dateTime, image);
                    listOfBoardObjects.add(boardObject);
                }
            }
        }
        listOfBoardObjects.sort(BoardObject.BoardObjectComparator);

        return listOfBoardObjects;

    }
}
