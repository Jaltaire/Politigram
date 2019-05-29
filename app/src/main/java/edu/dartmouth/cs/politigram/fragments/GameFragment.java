package edu.dartmouth.cs.politigram.fragments;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Base64;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

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

import edu.dartmouth.cs.politigram.models.GameObject;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.GameHistoryActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;
import edu.dartmouth.cs.politigram.utils.StringToHash;

public class GameFragment extends Fragment {

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
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        Map<String,String> map = createMapForUserPhotos(dataSnapshot);
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
                        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
                        imageView.setImageBitmap(bitmap);


                    }
                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });




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


//        String score = "0";
//        String dateTime = "0";
//        //Create GameObject once we have the score




    }

    public Map<String,String> createMapForUserPhotos(DataSnapshot dataSnapshot){
        Map<String,String> map = new HashMap<>();

        for(DataSnapshot dataSnap : dataSnapshot.getChildren()){
            if(!dataSnap.child("profile_data").child("privacy").exists()|| !dataSnap.child("profile_data").child("privacy").getValue(Boolean.class)) {
                String image = dataSnap.child("profile_data").child("profilePicture").getValue().toString();
                String politicalScore = dataSnap.child("profile_data").child("sliderPosition").getValue().toString();
                map.put(image, politicalScore);
            }
        }

        return map;

    }

    public int getPoliticalBent(int sliderPosition){
        if(sliderPosition <= 50){
            return 0;
        }
        return 1;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void Clicked(int politicalBent){
        if(politicalBent == getPoliticalBent(Integer.parseInt(mapList.get(indexInList).get("political_bent_score")))) {
            score +=1;
            indexInList += 1;
            if(indexInList == mapList.size()){
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//                builder.setTitle("CONGRATULATIONS");
                TextView msg = new TextView(getActivity());
                msg.setText("CONGRATULATIONS!" + "\n" + "YOU'VE COMPLETED THE GAME!");
                msg.setGravity(Gravity.CENTER_HORIZONTAL);
                msg.setTextSize(30);
                msg.setTypeface(null, Typeface.BOLD);
                msg.setTextColor(getResources().getColor(R.color.teal));
                builder.setView(msg);;
//                builder.setMessage("YOU'VE WON THE GAME WITH A SCORE OF " + score + "!");
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
            msg.setText("SORRY GAME OVER!" + "\n" + "Your Score Is " + score);
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

    //setValue(score and date/time) for firebase realtimedatabase when user ends game

}
