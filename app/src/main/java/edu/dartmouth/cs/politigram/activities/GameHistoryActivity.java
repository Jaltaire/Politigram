package edu.dartmouth.cs.politigram.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.ListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;

import edu.dartmouth.cs.politigram.models.GameObject;
import edu.dartmouth.cs.politigram.adapters.GameHistoryAdapter;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.utils.StringToHash;

public class GameHistoryActivity extends AppCompatActivity {

    ListView listView;
    ArrayList<GameObject> mGameObjects = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        //Set up actionbar
        setTitle("Game History");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mGameObjects = createListOfGameObjects();
        Log.d(Integer.toString(mGameObjects.size()), "GameObject size");
        listView = findViewById(R.id.game_history_listView);
        final GameHistoryAdapter adapter = new GameHistoryAdapter(this, mGameObjects);
        listView.setAdapter(adapter);

    }

    //Implements back button ---> returns to Sign in Page
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish(); //
        }
        return super.onOptionsItemSelected(item);
    }

    public ArrayList<GameObject> createListOfGameObjects() {
        Log.d("CreateList","Passing through method");
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        String Email = mAuth.getCurrentUser().getEmail();
        DataSnapshot dataSnapshot = MainActivity.dataSnap.child("user_" + StringToHash.getHex(Email)).child("game_results");
        ArrayList<GameObject> list = new ArrayList<>();

        for (DataSnapshot dataSnap : dataSnapshot.getChildren()) {
            String score = "Score: " + dataSnap.child("score").getValue().toString();
            String dateTime = dataSnap.child("dateTime").getValue().toString();
            GameObject gameObject = new GameObject(score, dateTime);
            list.add(gameObject);
        }

        return list;
    }


}
