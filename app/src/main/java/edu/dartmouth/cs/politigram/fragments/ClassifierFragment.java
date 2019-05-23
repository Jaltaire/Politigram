package edu.dartmouth.cs.politigram.fragments;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import edu.dartmouth.cs.politigram.ClassifierObject;
import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.R;

public class ClassifierFragment extends android.app.Fragment {

    public ClassifierFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_classifier, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Button goToHistoryBtn = view.findViewById(R.id.classifier_fragment_history_button);

        String image = "image";
        String result = "liberal";

        //Create ClassifierObject once we have the score
        ClassifierObject classifierObject = new ClassifierObject(image,result);

        //When photo is selected, add classifierObject to Firebase
        DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        FirebaseUser User = mAuth.getCurrentUser();
        String mUserId = User.getUid();
        database1.child("user_" + mUserId).child("classifier_results").push().setValue(classifierObject);



        goToHistoryBtn.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onClick(View v) {
                //Intent intent = new Intent(getActivity(), ClassifierHistoryFragment.class);

            }
        });

    }

    //setValue(image & resultfromAPI) for firebase realtimedatabase when user clicks button(either select photo or run or something)
}
