package edu.dartmouth.cs.politigram.adapters;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import edu.dartmouth.cs.politigram.R;
import edu.dartmouth.cs.politigram.models.ClassifierHistoryComponent;

// Adapter for RecyclerView to add BoardComponents.
public class ClassifierHistoryRecyclerViewAdapter extends RecyclerView.Adapter<ClassifierHistoryRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ClassifierHistoryComponent item);
    }

    private final List<ClassifierHistoryComponent> items;
    private final OnItemClickListener listener;

    public ClassifierHistoryRecyclerViewAdapter(List<ClassifierHistoryComponent> items, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.classifier_history_recyclerview_row, parent, false);
        return new ViewHolder(v);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        ClassifierHistoryComponent component = items.get(position);

        String imageBytes = component.getImageBytes();
        String label = component.getLabel();
        String score = component.getScore();

        Bitmap decodedByte;
        byte[] decodedString = Base64.decode(imageBytes, Base64.DEFAULT);
        decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        holder.photoImageView.setImageBitmap(decodedByte);

        holder.labelTextView.setText(label);
        holder.scoreTextView.setText(score);

        holder.bind(items.get(position), listener);
    }

    @Override public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView photoImageView;
        TextView labelTextView;
        TextView scoreTextView;

        public ViewHolder(View itemView) {
            super(itemView);
            photoImageView = itemView.findViewById(R.id.classifier_history_row_image);
            labelTextView = itemView.findViewById(R.id.classifier_history_row_label);
            scoreTextView = itemView.findViewById(R.id.classifier_history_row_score);
        }

        public void bind(final ClassifierHistoryComponent item, final OnItemClickListener listener) {

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {

                }
            });
        }
    }
}