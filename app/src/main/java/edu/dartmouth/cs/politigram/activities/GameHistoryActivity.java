package edu.dartmouth.cs.politigram.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import edu.dartmouth.cs.politigram.GameObject;
import edu.dartmouth.cs.politigram.GameHistoryAdapter;
import edu.dartmouth.cs.politigram.R;

public class GameHistoryActivity extends AppCompatActivity {

    ListView listView;
    final static ArrayList<GameObject> mGameObjects = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_history);

        mGameObjects.add(new GameObject("4","05/23/19 6:16"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));
        mGameObjects.add(new GameObject("4","05/23/19 6:16"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));
        mGameObjects.add(new GameObject("4","05/23/19 6:16"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));
        mGameObjects.add(new GameObject("2","05/23/19 6:19"));

        //Set up toolbar
        Toolbar toolbar = findViewById(R.id.game_history_toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Game History");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

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


}
