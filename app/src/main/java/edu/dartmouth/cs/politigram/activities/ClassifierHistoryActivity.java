package edu.dartmouth.cs.politigram.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MenuItem;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.adapters.ClassifierHistoryRecyclerViewAdapter;
import edu.dartmouth.cs.politigram.models.ClassifierHistoryComponent;
import edu.dartmouth.cs.politigram.utils.StringToHash;

public class ClassifierHistoryActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;
    private List<ClassifierHistoryComponent> mClassifierHistoryComponentList;

    private ClassifierHistoryRecyclerViewAdapter mRecyclerViewAdapter;

    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    DatabaseReference ref = database.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classifier_history);
        setTitle(getString(R.string.classifier_history_activity_title));

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = findViewById(R.id.classifier_history_recycler_view);
        mClassifierHistoryComponentList = new ArrayList<>();

        setupRecyclerView();
        getClassifierResults();

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

    // Set up the RecyclerView with a view adapter.
    public void setupRecyclerView() {

        // Set up the RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        mRecyclerView.setLayoutManager(layoutManager);

        // Creates a new adapter to handle data entry into and clicks within RecyclerView.
        mRecyclerViewAdapter = new ClassifierHistoryRecyclerViewAdapter(mClassifierHistoryComponentList, new ClassifierHistoryRecyclerViewAdapter.OnItemClickListener() {
            @Override public void onItemClick(ClassifierHistoryComponent item) {

            }
        });

        mRecyclerView.setAdapter(mRecyclerViewAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(mRecyclerView.getContext(), layoutManager.getOrientation());
        mRecyclerView.addItemDecoration(dividerItemDecoration);

    }

    // Add data from each Firebase DataSnapshot of a past classification to the RecyclerView.
    public void addToRecyclerView(DataSnapshot dataSnapshot) {

        String imageBytes = dataSnapshot.child("imageBytes").getValue(String.class);
        String label = dataSnapshot.child("label").getValue(String.class);
        String score = dataSnapshot.child("score").getValue(String.class);

        //BoardEntry boardEntry = new BoardEntry(metricUnits, Double.valueOf(distance), inputType, activityDate, Double.valueOf(duration), email, activityType);
        //BoardRecyclerDataAdapter dataAdapter = new BoardRecyclerDataAdapter(boardEntry, getUnitPreference());
        mClassifierHistoryComponentList.add(new ClassifierHistoryComponent(imageBytes, label, "Confidence: " + score));

        mRecyclerViewAdapter.notifyDataSetChanged();

    }

    // Get past classifier results from Firebase.
    private void getClassifierResults() {

        DatabaseReference usersRef = ref.child(ProfileActivity.FIREBASE_USERS_PATH);

        usersRef.child("user_" + StringToHash.getHex(LoginActivity.email)).child("classifier_results").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot classifierResult : dataSnapshot.getChildren()) {
                    addToRecyclerView(classifierResult);
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("TEST", "Could not get data from Firebase");
            }
        });

    }

}
