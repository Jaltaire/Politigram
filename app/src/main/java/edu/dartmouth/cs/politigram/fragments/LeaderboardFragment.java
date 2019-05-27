package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import edu.dartmouth.cs.politigram.BoardAdapter;
import edu.dartmouth.cs.politigram.BoardObject;
import edu.dartmouth.cs.politigram.GameHistoryAdapter;
import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.GameHistoryActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        list = new ArrayList<>();
        list = createSortedListOfBoardObjects();
        ListView listView = view.findViewById(R.id.board_listview);
        final BoardAdapter adapter = new BoardAdapter(getActivity(), list);
        listView.setAdapter(adapter);

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public ArrayList<BoardObject> createSortedListOfBoardObjects() {
        DataSnapshot dataSnapshot = MainActivity.dataSnap;
        ArrayList<BoardObject> listOfBoardObjects = new ArrayList<>();

        for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
            if (dataSnap.child("game_results") != null) {
                String user = dataSnap.child("profile_data").child("username").getValue().toString();
                DataSnapshot dataSnap1 = dataSnap.child("game_results");
                for (DataSnapshot dataSnap2 : dataSnap1.getChildren()) {
                    String score = dataSnap2.child("score").getValue().toString();
                    String dateTime = dataSnap2.child("dateTime").getValue().toString();
                    BoardObject boardObject = new BoardObject(user, score, dateTime);
                    listOfBoardObjects.add(boardObject);
                }
            }
        }
        listOfBoardObjects.sort(BoardObject.BoardObjectComparator);

        return listOfBoardObjects;

    }


}
