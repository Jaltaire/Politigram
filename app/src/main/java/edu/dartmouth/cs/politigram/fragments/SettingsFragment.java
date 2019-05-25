package edu.dartmouth.cs.politigram.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.LoginActivity;
import edu.dartmouth.cs.politigram.activities.ProfileActivity;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends PreferenceFragment {

    Preference mEditProfile;
    Preference mSignOut;

    private static final String SIGNED_IN_KEY = "signed_in";
    private static final String PRIVACY_KEY  = "privacy";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        mEditProfile = findPreference(getString(R.string.edit_profile_key));
        mEditProfile.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {

                Intent myIntent = new Intent(getActivity(), ProfileActivity.class);
                getActivity().startActivityForResult(myIntent, 0);

                return true;
            }
        });

        mSignOut = findPreference(getString(R.string.sign_out_key));
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

        // Store value of privacy switch. If on, do not share profile info with Politigram or participate in leaderboard.
        SharedPreferences.Editor editor = getActivity().getSharedPreferences(PRIVACY_KEY, MODE_PRIVATE).edit();
        PreferenceManager preferenceManager = getPreferenceManager();
        if (preferenceManager.getSharedPreferences().getBoolean(getString(R.string.privacy_setting_key), false)){
            editor.putBoolean("privacy_on", true);
        } else {
            editor.putBoolean("privacy_on", false);
        }
        editor.apply();

    }
}
