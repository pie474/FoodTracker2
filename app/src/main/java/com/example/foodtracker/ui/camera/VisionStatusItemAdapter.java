
package com.example.foodtracker.ui.camera;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.foodtracker.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class VisionStatusItemAdapter extends RecyclerView.Adapter<VisionStatusItemAdapter.ViewHolder> {
    private ArrayList<VisionStatusItem> dataSet;
    private CameraActivity activity;
    Context context;


/**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView nameView;
        private ImageView imageView;
        private int pos;

        public void setPosition(int p) {
            pos = p;
        }

        public ViewHolder(View view, CameraActivity mainActivity) {
            super(view);
            // Define click listener for the ViewHolder's View

            nameView = (TextView) itemView.findViewById(R.id.statusItemName);
            imageView = itemView.findViewById(R.id.statusImage);

            //imageView.setImageResource(R.drawable.ic_empty_circle_outline_24);

            /*final ImageButton cameraButton = itemView.findViewById(R.id.imageButton);
            cameraButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainActivity.removeFood(pos);
                }
            });*/
        }

        public TextView getNameView() {
            return nameView;
        }

        public ImageView getStatusIcon() {
            return imageView;
        }
    }


/**
     * Initialize the dataset of the Adapter.
     *
     * @param dataSet String[] containing the data to populate views to be used
     * by RecyclerView.
     */


    public VisionStatusItemAdapter(ArrayList<VisionStatusItem> dataSet, Context context, CameraActivity activity) {
        this.dataSet = dataSet;
        this.context = context;
        this.activity = activity;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.vision_status_item, viewGroup, false);

        return new ViewHolder(view, activity);
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, int position) {
        /*
        ViewHolder viewHolder1 = (ViewHolder)viewHolder;
        viewHolder1.textView.setText(localDataSet.toString());
        viewHolder1.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(context, "Item Selected", Toast.LENGTH_LONG).show();
            }
        });
        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        */

        // viewHolder.getTextView().setText(localDataSet[position]);
        viewHolder.setPosition(position);
        viewHolder.getNameView().setText(dataSet.get(position).getName());
        viewHolder.getStatusIcon().setImageResource(dataSet.get(position).getIcon());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        // return localDataSet.length
        return dataSet.size();
    }
}


