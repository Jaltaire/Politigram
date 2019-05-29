package edu.dartmouth.cs.politigram.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;
import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.models.BoardObject;

//<<<<<<< HEAD:app/src/main/java/edu/dartmouth/cs/politigram/BoardAdapter.java
//import de.hdodenhof.circleimageview.CircleImageView;
//=======
//import edu.dartmouth.cs.politigram.models.BoardObject;
//import edu.dartmouth.cs.politigram.R;
//>>>>>>> 7d544ca5ccacafb18ec105d3d8403e3988185166:app/src/main/java/edu/dartmouth/cs/politigram/adapters/BoardAdapter.java

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
        CircleImageView board_imageview = view.findViewById(R.id.board_imageview);
        String score = getItem(position).getScore();
        String dateTime = getItem(position).getDateTime();
        String image = getItem(position).getImage();
        String user = getItem(position).getUser();
        userEmail_textview.setText(user);
        dateTime_textview.setText(dateTime);
        score_textview.setText("Score: " + score);
        byte[] byteArray = Base64.decode(image, Base64.NO_WRAP);
        Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
        board_imageview.setImageBitmap(bitmap);

        return view;
    }




}