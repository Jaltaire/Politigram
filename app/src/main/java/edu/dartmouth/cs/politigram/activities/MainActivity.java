package edu.dartmouth.cs.politigram.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
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
import java.util.List;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.fragments.ClassifierFragment;
import edu.dartmouth.cs.politigram.fragments.GameFragment;
import edu.dartmouth.cs.politigram.fragments.LeaderboardFragment;
import edu.dartmouth.cs.politigram.fragments.MainFragment;
import edu.dartmouth.cs.politigram.utils.PoliticalLeaningConversion;

//test
public class MainActivity extends AppCompatActivity {

    ImageView mProfilePictureImageView;
    public static DataSnapshot dataSnap;

    TextView mUsername;
    TextView mPoliticalLeaning;

    Animation atg, atgtwo, atgthree;

    private LinearLayout mClassifierLinearLayout;
    private LinearLayout mGameLinearLayout;
    private LinearLayout mLeaderboardLinearLayout;
    private LinearLayout mSettingsLinearLayout;

    private ImageView mClassifierTabImageView;
    private ImageView mGameTabImageView;
    private ImageView mLeaderboardTabImageView;
    private ImageView mSettingsTabImageView;

    private int mVolumeUpCount = 0;
    private double mVolumeUpInitialTime = System.currentTimeMillis();

    private double MAX_TIME_DIFFERENCE = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new MainFragment());
        fragmentTransaction.commit();

        //Add onDataChange Listener here, so that constantly updating Classifier and Game Class
        //Retrieve data from RealTimeDatabase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        database1.child("politigram_users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        dataSnap = dataSnapshot;
                        Log.d("MainActivity","onDataChange");

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

        mClassifierTabImageView = findViewById(R.id.classifier_tab_image_view);
        mGameTabImageView = findViewById(R.id.game_tab_image_view);
        mLeaderboardTabImageView = findViewById(R.id.leaderboard_tab_image_view);
        mSettingsTabImageView = findViewById(R.id.settings_tab_image_view);

        mClassifierLinearLayout = findViewById(R.id.classifier_linear_layout);
        mClassifierLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ClassifierFragment());
                fragmentTransaction.commit();

                adjustImageViewUI(0);
            }
        });

        mGameLinearLayout = findViewById(R.id.game_linear_layout);
        mGameLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new GameFragment());
                fragmentTransaction.commit();

                adjustImageViewUI(1);
            }
        });

        mLeaderboardLinearLayout = findViewById(R.id.leaderboard_linear_layout);
        mLeaderboardLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new LeaderboardFragment());
                fragmentTransaction.commit();

                adjustImageViewUI(2);
            }
        });

        mSettingsLinearLayout = findViewById(R.id.settings_linear_layout);
        mSettingsLinearLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), SettingsActivity.class);
                startActivity(intent);

                //adjustImageViewUI(3);
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

    private void adjustImageViewUI(int position) {

        List<ImageView> mImageViewList = new ArrayList<>();
        mImageViewList.add(mClassifierTabImageView);
        mImageViewList.add(mGameTabImageView);
        mImageViewList.add(mLeaderboardTabImageView);
        mImageViewList.add(mSettingsTabImageView);

        ImageView focus = mImageViewList.get(position);
        focus.getLayoutParams().width = 235;
        focus.getLayoutParams().height = 235;

        mImageViewList.remove(focus);
        for (ImageView imageView : mImageViewList) {
            imageView.getLayoutParams().width = 220;
            imageView.getLayoutParams().height = 220;
        }

        mClassifierTabImageView.requestLayout();

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

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP)) {
            Log.d("TEST", "volume up pressed");

            if (mVolumeUpCount == 2 && System.currentTimeMillis() - mVolumeUpInitialTime <= MAX_TIME_DIFFERENCE) {
                Log.d("TEST", "launch sos activity");
                mVolumeUpCount = 0;
            }

            else if (mVolumeUpCount != 0 && System.currentTimeMillis() - mVolumeUpInitialTime > MAX_TIME_DIFFERENCE) {
                mVolumeUpCount = 1;
                mVolumeUpInitialTime = System.currentTimeMillis();
            }

            else {
                mVolumeUpCount = mVolumeUpCount + 1;
            }


        }
        return true;

    }
}
