package edu.dartmouth.cs.politigram.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.fragments.ClassifierFragment;

public class ClassifierResultActivity extends AppCompatActivity {

    private ImageView mImageView;
    private TextView mLabelTextView;
    private TextView mConfidenceTextView;
    private TextView mStateTextView;

    private Bitmap mBitmap;
    private String mLabel;
    private String mConfidence;
    private String mState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.classification_result_activity_title));
        setContentView(R.layout.activity_classifier_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mImageView = findViewById(R.id.classification_result_image_view);
        mLabelTextView = findViewById(R.id.classification_result_label);
        mConfidenceTextView = findViewById(R.id.classification_result_confidence);
        mStateTextView = findViewById(R.id.classification_state);

        Intent intent = getIntent();
        byte[] byteArray = intent.getByteArrayExtra(ClassifierFragment.INTENT_IMAGE_KEY);
        mBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        mLabel = intent.getStringExtra(ClassifierFragment.INTENT_LABEL_KEY);
        mConfidence = intent.getStringExtra(ClassifierFragment.INTENT_CONFIDENCE_KEY);

        mState = intent.getStringExtra(ClassifierFragment.INTENT_STATE_KEY);

        configureUI();

    }

    // Allows interaction with ActionBar back button.
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish(); // Close the activity
        }

        return super.onOptionsItemSelected(item);
    }

    // Show classifier results computed in ClassifierFragment.
    private void configureUI() {

        mImageView.setImageBitmap(mBitmap);
        mLabelTextView.setText(mLabel);
        mConfidenceTextView.setText("CONFIDENCE: " + mConfidence);

        if (mState != null) {
            mStateTextView.setText("Classifier prediction scaled according to voter data from " + mState + ".");
        }

    }
}
