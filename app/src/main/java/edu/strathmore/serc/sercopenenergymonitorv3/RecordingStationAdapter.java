package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Bob on 19/03/2017.
 */

public class RecordingStationAdapter extends ArrayAdapter<RecordingStation> {


    public RecordingStationAdapter(Context context, ArrayList<RecordingStation> recordingStations){
        // The constructor ArrayAdapter(Context context, int resource, T[] objects) has been used
        // Since no resource id is used, we pass a 0 (generic) to avoid errors
        super(context, 0, recordingStations);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        // Check if the existing View is being reused, otherwise inflate a new view
        View listItemView = convertView;
        if(listItemView == null){
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        // Get the recording station object at this position on the list
        RecordingStation currentRecordingStation = getItem(position);

        //Find the TextView in list_item.xml with the id station_tag
        TextView stationTagTextView = (TextView) listItemView.findViewById(R.id.station_tag);
        // Get the Station tag name from the current object and set this text to the text view
        stationTagTextView.setText(currentRecordingStation.getStationTag());

        //Find the TextView in list_item.xml with the id station_name
        TextView stationNameTextView = (TextView) listItemView.findViewById(R.id.station_name);
        // Get the Station name from the current object and set this text to the text view
        stationNameTextView.setText(currentRecordingStation.getStationName());

        //Find the TextView in list_item.xml with the id station_power_reading
        TextView stationPowerTextView = (TextView) listItemView.findViewById(R.id.station_power_reading);
        // Get the Station power reading from the current object and set this text to the text view
        // Because this is an int, we first have to convert it to String. If we pass an int to setText
        // it will look for a resource with that id and throw a ResourceNotFound exception
        stationPowerTextView.setText(String.valueOf(currentRecordingStation.getStationValueReading()+" W"));


        // Return the ListView containing the 3 TextViews
        return listItemView;
    }
}
