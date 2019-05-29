package edu.dartmouth.cs.politigram.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import edu.dartmouth.cs.politigram.models.GameObject;
import edu.dartmouth.cs.politigram.R;

//Adapter for ListView in GameHistory
public class GameHistoryAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<GameObject> mGameObjects;


    public GameHistoryAdapter(Context mContext, ArrayList<GameObject> mGameObjects){
        this.mContext = mContext;
        this.mGameObjects = mGameObjects;
    }


    @Override
    public int getCount() {
        return mGameObjects.size();
    }

    @Override
    public GameObject getItem(int position) {
        return mGameObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.game_history_list_layout, parent, false);
        TextView firstText = view.findViewById(R.id.text1);
        TextView secondText = view.findViewById(R.id.text2);
        String score = getItem(position).getScore();
        String dateTime = getItem(position).getDateTime();
        firstText.setText(score);
        secondText.setText(dateTime);

        return view;
    }




}