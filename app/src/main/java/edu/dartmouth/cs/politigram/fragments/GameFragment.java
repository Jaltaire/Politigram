package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.GameHistoryActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;

public class GameFragment extends Fragment {
    boolean isLeft;
    boolean isCorrect;
    ArrayList<Map<String,String>> mapList;
    int indexInList = 0;
    ImageView imageView;

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
        imageView = view.findViewById(R.id.imageView3);
        Button leansLeftBtn = view.findViewById(R.id.game_leans_left_button);
        Button leansRightBtn = view.findViewById(R.id.game_leans_right_button);


        goToGameHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), GameHistoryActivity.class);
                startActivity(intent);
            }
        });

        Map<String,String> map = createMapForUserPhotos();
        mapList = new ArrayList<>();

        for(String image : map.keySet()) {
            Map<String,String> tempMap = new HashMap<>();
            tempMap.put("image", image);
            tempMap.put("political_bent_score",map.get(image));
            mapList.add(tempMap);
        }

        byte[] byteArray = Base64.decode(mapList.get(0).get("image"), Base64.NO_WRAP);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        imageView.setImageBitmap(bitmap);


        leansLeftBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Clicked(0);

            }
        });

        leansRightBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Clicked(1);
            }
        });


//        String score = "0";
//        String dateTime = "0";
//        //Create GameObject once we have the score
//        GameObject gameObject = new GameObject(score, dateTime);
//
//        //When Game ends and we want to save the score in Firebase
//        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
//        FirebaseAuth mAuth = FirebaseAuth.getInstance();
//        FirebaseUser User = mAuth.getCurrentUser();
//        String mUserId = User.getUid();
//        database1.child("user_" + mUserId).child("game_results").push().setValue(gameObject);



    }

    public Map<String,String> createMapForUserPhotos(){
        DataSnapshot dataSnapshot = MainActivity.dataSnap;
        Map<String,String> map = new HashMap<>();

        for(DataSnapshot dataSnap : dataSnapshot.getChildren()){
            String image = dataSnap.child("profile_data").child("profilePicture").getValue().toString();
            String politicalScore = dataSnap.child("profile_data").child("sliderPosition").getValue().toString();
            map.put(image,politicalScore);
        }

        return map;

    }

    public int getPoliticalBent(int sliderPosition){
        if(sliderPosition <= 50){
            return 0;
        }
        return 1;
    }

    public void Clicked(int politicalBent){
        if(politicalBent == getPoliticalBent(Integer.parseInt(mapList.get(indexInList).get("political_bent_score")))) {
            byte[] byteArray = Base64.decode(mapList.get(indexInList).get("image"), Base64.NO_WRAP);
            Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            imageView.setImageBitmap(bitmap);
            indexInList += 1;
        }else{
            Toast.makeText(getActivity(),"Oops! That's incorrect!",Toast.LENGTH_LONG).show();
        }

    }

    //setValue(score and date/time) for firebase realtimedatabase when user ends game

}
