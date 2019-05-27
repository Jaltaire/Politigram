package edu.dartmouth.cs.politigram;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

//Adapter for ListView in GameHistory
public class BoardAdapter extends BaseAdapter {

    private Context mContext;
    private ArrayList<BoardObject> mBoardObjects;


    public BoardAdapter(Context mContext, ArrayList<BoardObject> mBoardObjects){
        this.mContext = mContext;
        this.mBoardObjects =  mBoardObjects;
    }


    @Override
    public int getCount() {
        return mBoardObjects.size();
    }

    @Override
    public BoardObject getItem(int position) {
        return mBoardObjects.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View view;
        LayoutInflater inflater = LayoutInflater.from(mContext);
        view = inflater.inflate(R.layout.board_layout, parent, false);
        TextView userEmail_textview = view.findViewById(R.id.user_textview);
        TextView score_textview = view.findViewById(R.id.score_textview);
        TextView dateTime_textview = view.findViewById(R.id.dateTime_textview);
        String score = getItem(position).getScore();
        String dateTime = getItem(position).getDateTime();
        String user = getItem(position).getUser();
        userEmail_textview.setText(user);
        dateTime_textview.setText(dateTime);
        score_textview.setText("Score: " + score);

        return view;
    }




}