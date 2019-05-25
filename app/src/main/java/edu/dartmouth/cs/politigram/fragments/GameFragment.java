package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.GameHistoryActivity;

public class GameFragment extends Fragment {

    public GameFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_game, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton goToGameHistory = view.findViewById(R.id.game_history_button);

        goToGameHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GameHistoryActivity.class);
                startActivity(intent);
            }
        });

        String score = "0";
        String dateTime = "0";
        //Create GameObject once we have the score
        GameObject gameObject = new GameObject(score, dateTime);

        //When Game ends and we want to save the score in Firebase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        database1.child("user_" + mUserId).child("game_results").push().setValue(gameObject);


    }

    //setValue(score and date/time) for firebase realtimedatabase when user ends game

}
