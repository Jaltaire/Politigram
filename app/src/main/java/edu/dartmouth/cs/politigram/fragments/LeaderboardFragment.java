package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import edu.dartmouth.cs.politigram.adapters.BoardAdapter;
import edu.dartmouth.cs.politigram.models.BoardObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.MainActivity;

// Fragment within MainActivity to control leaderboard aspects of Politigram.
public class LeaderboardFragment extends Fragment {

    ArrayList<BoardObject> list;

    public LeaderboardFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_leaderboard, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onViewCreated(final View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ListView listView = view.findViewById(R.id.board_listview);

        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        final String Email = mAuth.getCurrentUser().getEmail();
        database1.child("politigram_users")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        list = new ArrayList<>();
                        list = createSortedListOfBoardObjects(dataSnapshot);
                        final BoardAdapter adapter = new BoardAdapter(getActivity(), list);
                        listView.setAdapter(adapter);
                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


    }

    // Sort users without private mode enabled by their high scores.
    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<BoardObject> createSortedListOfBoardObjects(DataSnapshot dataSnapshot) {
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

    // If privacy preference is changed, update the fragment accordingly when it re-shown after exiting settings.
    @Override
    public void onResume() {
        super.onResume();
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        if(pref.getBoolean("isFromSettings",false)){
            final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new LeaderboardFragment());
            fragmentTransaction.commit();
            pref.edit().putBoolean("isFromSettings",false).apply();
        }
    }

}
