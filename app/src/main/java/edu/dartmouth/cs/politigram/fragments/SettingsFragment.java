package edu.dartmouth.cs.politigram.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.preference.SwitchPreference;
import android.support.annotation.NonNull;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

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

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.LoginActivity;
import edu.dartmouth.cs.politigram.activities.MainActivity;
import edu.dartmouth.cs.politigram.activities.ProfileActivity;
import edu.dartmouth.cs.politigram.utils.InternetConnectionTester;
import edu.dartmouth.cs.politigram.utils.StringToHash;

import static android.content.Context.MODE_PRIVATE;

// Handle app and account settings in Politigram.
public class SettingsFragment extends PreferenceFragment {

    Preference mEditProfile;
    Preference mSignOut;
    SwitchPreference privacy;

    private static final String SIGNED_IN_KEY = "signed_in";
    private static final String PRIVACY_KEY = "privacy";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mEditProfile = findPreference(getString(R.string.edit_profile_key));
        mSignOut = findPreference(getString(R.string.sign_out_key));
        privacy = (SwitchPreference) findPreference("PrivacySetting");

        mSignOut.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Set signed_in flag in SharedPreferences to false on log out.
                SharedPreferences.Editor editor = getActivity().getSharedPreferences(SIGNED_IN_KEY, MODE_PRIVATE).edit();
                editor.putBoolean("signed_in", false);
                editor.apply();

                // Exit SettingsActivity and return to SignInActivity.
                Intent myIntent = new Intent(getActivity(), LoginActivity.class);
                myIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK |
                        Intent.FLAG_ACTIVITY_NEW_TASK);
                getActivity().startActivityForResult(myIntent, 0);

                return true;
            }
        });

        if (!InternetConnectionTester.hasInternetConnection(getContext())) {
            mEditProfile.setEnabled(false);
            privacy.setEnabled(false);
            Toast.makeText(getContext(), "No Internet connection. Cannot adjust privacy settings or profile.", Toast.LENGTH_LONG).show();
        }

        else {

            mEditProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {

                    Intent myIntent = new Intent(getActivity(), ProfileActivity.class);
                    getActivity().startActivityForResult(myIntent, 0);

                    return true;
                }
            });

            // Set the privacy preference value to the current value stored in Firebase.
            DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
            FirebaseAuth mAuth = FirebaseAuth.getInstance();
            final String Email = mAuth.getCurrentUser().getEmail();
            database1.child("politigram_users")
                    .addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.child("user_" + StringToHash.getHex(Email)).child("profile_data").child("privacy").exists()) {
                                privacy.setChecked(dataSnapshot.child("user_" + StringToHash.getHex(Email)).child("profile_data").child("privacy").getValue(Boolean.class));
                            } else {
                                privacy.setChecked(false);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });

        }
    }

    // Update the privacy preference flag on Firebase according to the user's selection.
    // This way, if user logs out or logs in on another device, privacy preferences will be maintained.
    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        switch (preference.getKey()){
            case "PrivacySetting":
                SwitchPreference privacy = (SwitchPreference)findPreference("PrivacySetting");

                if (InternetConnectionTester.hasInternetConnection(getContext())) {
                    final DatabaseReference database1 = FirebaseDatabase.getInstance().getReference();
                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                    final String Email = mAuth.getCurrentUser().getEmail();
                    database1.child("politigram_users").child("user_" + StringToHash.getHex(Email)).child("profile_data").child("privacy")
                            .setValue(privacy.isChecked());
                    Log.d("privacy", Boolean.toString(privacy.isChecked()));

                }
                return true;
            default:
                return false;
        }

    }
}
