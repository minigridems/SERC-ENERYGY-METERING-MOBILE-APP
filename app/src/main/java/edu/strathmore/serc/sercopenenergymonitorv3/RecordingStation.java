package edu.strathmore.serc.sercopenenergymonitorv3;

/**
 * This class is used to define the different recording stations from the JSON received from
 * the CMS API
 *
 * Created by Bob on 19/03/2017.
 */

public class RecordingStation {
    // Attributes for the recording station
    private int mStationID;
    private String mStationName;
    private String mStationTag;
    private long mStationTime;
    private int mStationValueReading;

    // Public constructor for the Recording Station class
    public RecordingStation(int stationID, String stationName, String stationTag, int stationTime, int stationValueReading){
        mStationID = stationID;
        mStationName = stationName;
        mStationTag = stationTag;
        mStationTime = stationTime;
        mStationValueReading = stationValueReading;

    }

    // Getter methods for the various attributes

    public int getStationID(){
        return mStationID;
    }

    public String getStationName(){
        return mStationName;
    }

    public String getStationTag(){
        return mStationTag;
    }

    public long getStationTime(){
        return mStationTime;
    }

    public int getStationValueReading(){
        return mStationValueReading;
    }

}
