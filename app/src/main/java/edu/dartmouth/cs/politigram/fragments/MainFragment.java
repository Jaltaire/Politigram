package edu.dartmouth.cs.politigram.fragments;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import edu.dartmouth.cs.politigram.R;

// Initial fragment within MainActivity to prompt user to try classifier feature of Politigram.
public class MainFragment extends Fragment {

    TextView nameuser, walletuser, review, network, plugins, myapps, mainmenus,
            pagetitle, pagesubtitle;

    private Button mPredictionButton;
    Animation atg, atgtwo, atgthree;
    ImageView imageView3;


    public MainFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            atg = AnimationUtils.loadAnimation(getContext(), R.anim.atg);
            atgtwo = AnimationUtils.loadAnimation(getContext(), R.anim.atgtwo);
            atgthree = AnimationUtils.loadAnimation(getContext(), R.anim.atgthree);
        }

        imageView3 = view.findViewById(R.id.imageView3);

        pagetitle = view.findViewById(R.id.pagetitle);
        pagesubtitle = view.findViewById(R.id.pagesubtitle);

        mPredictionButton = view.findViewById(R.id.main_fragment_prediction_button);

        // pass an animation
        imageView3.startAnimation(atg);

        pagetitle.startAnimation(atgtwo);
        pagesubtitle.startAnimation(atgtwo);

        mPredictionButton.startAnimation(atgthree);

        mPredictionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.fragment_container, new ClassifierFragment());
                fragmentTransaction.commit();
            }
        });
    }
}
