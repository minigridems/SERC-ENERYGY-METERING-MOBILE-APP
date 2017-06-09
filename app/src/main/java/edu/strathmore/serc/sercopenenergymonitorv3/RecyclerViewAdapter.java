package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bob on 08/06/2017.
 * Custom adapter needed for the RecyclerView in MainActivityRecyclerView
 */

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    // Member variable for the list of RecordingStation objects
    ArrayList<RecordingStation> mRecordingStations;
    // Storing context for easy access
    Context mContext;



    /************ Creating OnItemClickListener ************/
    // Listener member variable
    private OnItemClickListener listener;

    public interface OnItemClickListener{
        void onItemClick (View itemView, int position);
    }

    // Method that allows the parent Activity/fragment to define the listener
    public void setOnItemClickListener (OnItemClickListener clickListener){
        listener = clickListener;
    }


    public class ViewHolder extends RecyclerView.ViewHolder{

        public TextView nameTextView;
        public TextView tagTextView;
        public TextView powerReadingTextView;

        public ViewHolder(final View itemView) {
            super(itemView);

            // Get the various TextViews from list_item.xml
            nameTextView = (TextView) itemView.findViewById(R.id.station_name);
            tagTextView = (TextView) itemView.findViewById(R.id.station_tag);
            powerReadingTextView = (TextView) itemView.findViewById(R.id.station_power_reading);

            // Setup the click listener
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Triggers click upwards to the adapter on click
                    if(listener != null){
                        int position = getAdapterPosition();
                        // Make sure position exists in RecyclerView
                        if (position != RecyclerView.NO_POSITION){
                            listener.onItemClick(itemView, position);
                        }
                    }
                }
            });

        }
    }


    // Constructor for the Adapter
    public RecyclerViewAdapter(Context context, ArrayList<RecordingStation> recordingStations){
        mContext = context;
        mRecordingStations = recordingStations;
    }

    // Usually involves inflating a layout from XML and returning the holder
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout for the main activity
        View listItemView = inflater.inflate(R.layout.list_item, parent, false);

        // Return ViewHolder instance
        ViewHolder viewHolder = new ViewHolder(listItemView);
        return viewHolder;
    }

    // Involves populating data into the item through holder
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // Get the RecordingStation based on position
        RecordingStation recordingStation = mRecordingStations.get(position);

        // Set TextViews based on the attributes of the RecordingStation
        TextView nameTV = holder.nameTextView;
        nameTV.setText(recordingStation.getStationName());
        TextView tagTV = holder.tagTextView;
        tagTV.setText(recordingStation.getStationTag());
        TextView powerReadingTV = holder.powerReadingTextView;
        powerReadingTV.setText(String.valueOf(recordingStation.getStationValueReading()) + " W");

    }

    // Returns the total count of items in the list
    @Override
    public int getItemCount() {
        return mRecordingStations.size();
    }

    public RecordingStation getRecordingStation(int position){
        return mRecordingStations.get(position);
    }

    /*
    * The following 2 classes are used to help clear and load new data to the adapter
    * */
    // Clean all elements of the recycler
    public void clear(){
        mRecordingStations.clear();
        notifyDataSetChanged();
    }
    // Add list of items
    public void addAll(ArrayList<RecordingStation> recordingStations){
        mRecordingStations.addAll(recordingStations);
        notifyDataSetChanged();
    }


}

