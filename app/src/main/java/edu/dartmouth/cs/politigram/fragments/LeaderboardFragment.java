package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import edu.dartmouth.cs.politigram.adapters.BoardAdapter;
import edu.dartmouth.cs.politigram.models.BoardObject;
import edu.dartmouth.cs.politigram.R;
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
