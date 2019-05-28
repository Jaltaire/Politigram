package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
