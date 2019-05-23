package edu.dartmouth.cs.politigram.activities;

import android.app.FragmentTransaction;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.fragments.ClassifierFragment;
import edu.dartmouth.cs.politigram.fragments.GameFragment;
import edu.dartmouth.cs.politigram.fragments.MainFragment;
import edu.dartmouth.cs.politigram.utils.PoliticalLeaningConversion;

//test
public class MainActivity extends AppCompatActivity {

    TextView mUsername;
    TextView mPoliticalLeaning;

    Animation atg, atgtwo, atgthree;

    private LinearLayout mClassifierLinearLayout;
    private LinearLayout mGameLinearLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.add(R.id.fragment_container, new MainFragment());
        fragmentTransaction.commit();

        atg = AnimationUtils.loadAnimation(this, R.anim.atg);
        atgtwo = AnimationUtils.loadAnimation(this, R.anim.atgtwo);
        atgthree = AnimationUtils.loadAnimation(this, R.anim.atgthree);

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

        configureUI();

        //Add onDataChange Listener here, so that constantly updating Classifier and Game Class
        //Retrieve data from RealTimeDatabase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        database1.child("user_" + mUserId).child("classifier_results")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });
    }

    public void configureUI() {

        mUsername.setText(LoginActivity.username);
        mPoliticalLeaning.setText(PoliticalLeaningConversion.handlePoliticalLeaningValue(LoginActivity.politicalLeaning));

    }

}
