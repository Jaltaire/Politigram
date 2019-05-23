package edu.dartmouth.cs.politigram.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

import edu.dartmouth.cs.politigram.R;

public class ClassifierHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier_history);

        setTitle(getString(R.string.classifier_history_activity_title));
        setContentView(R.layout.activity_classifier_result);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
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

}
