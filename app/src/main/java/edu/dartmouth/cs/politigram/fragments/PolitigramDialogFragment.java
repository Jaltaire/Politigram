package edu.dartmouth.cs.politigram.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;

import java.util.Calendar;
import java.util.List;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.activities.MainActivity;
import edu.dartmouth.cs.politigram.activities.ProfileActivity;

public class PolitigramDialogFragment extends DialogFragment {

    // Different dialog IDs
    public static final int DIALOG_ID_PHOTO_SELECTOR = 0;
    public static final int DIALOG_ID_CLASSIFIER_PHOTO_SELECTOR = 1;

    // For photo picker selection
    public static final int ID_PHOTO_SELECTOR_FROM_CAMERA = 0;
    public static final int ID_PHOTO_SELECTOR_FROM_GALLERY = 1;

    private static final String DIALOG_ID_KEY = "dialog_id";

    public static PolitigramDialogFragment newInstance(int dialog_id) {
        PolitigramDialogFragment fragments = new PolitigramDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(DIALOG_ID_KEY, dialog_id);
        fragments.setArguments(bundle);
        return fragments;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int dialog_id = getArguments().getInt(DIALOG_ID_KEY);

        final Activity parent = getActivity();

        final Calendar cal;
        final EditText textEntryEditText;

        // Handle each dialog fragment case depending upon what type of dialog is requested.
        switch (dialog_id) {
            case DIALOG_ID_PHOTO_SELECTOR:
                // Build picture picker dialog for choosing to take a picture with camera app.
                AlertDialog.Builder builder = new AlertDialog.Builder(parent);
                builder.setTitle(R.string.set_profile_picture_dialog_title);
                // Listener checks for user to press button.
                DialogInterface.OnClickListener dialogListener = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        ((ProfileActivity) parent).onPhotoPickerItemSelected(item);
                    }
                };
                // Create and display the user dialog.
                builder.setItems(R.array.set_profile_picture_options, dialogListener);
                return builder.create();

            case DIALOG_ID_CLASSIFIER_PHOTO_SELECTOR:
                // Build picture picker dialog for choosing to take a picture with camera app.
                AlertDialog.Builder builder2 = new AlertDialog.Builder(parent);
                builder2.setTitle(R.string.set_profile_picture_dialog_title);
                // Listener checks for user to press button.
                DialogInterface.OnClickListener dialogListener2 = new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            final Fragment currentFragment = getFragmentManager().findFragmentById(R.id.fragment_container);
                            if (currentFragment instanceof ClassifierFragment) {
                                ((ClassifierFragment) currentFragment).onPhotoPickerItemSelected(item);
                            }
                        }
                    }
                };
                // Create and display the user dialog.
                builder2.setItems(R.array.set_profile_picture_options, dialogListener2);
                return builder2.create();

            default: // If case is different from those specified, return null.
                return null;

        }

    }
}
