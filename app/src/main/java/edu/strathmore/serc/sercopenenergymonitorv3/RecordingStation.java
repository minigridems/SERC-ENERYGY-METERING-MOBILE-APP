package edu.strathmore.serc.sercopenenergymonitorv3;

/**
 * This class is used to define the different recording stations from the JSON received from
 * the CMS API
 *
 * Created by Bob on 19/03/2017.
 */

public class RecordingStation {
    private int mStationID;
    private String mStationName;
    private String mStationTag;
    private int mStationTime;
    private int mStationValueReading;


    public RecordingStation(int stationID, String stationName, String stationTag, int stationTime, int stationValueReading){
        mStationID = stationID;
        mStationName = stationName;
        mStationTag = stationTag;
        mStationTime = stationTime;
        mStationValueReading = stationValueReading;

    }

    public int getStationID(){
        return mStationID;
    }

    public String getStationName(){
        return mStationName;
    }

    public String getStationTag(){
        return mStationTag;
    }

    public int getStationTime(){
        return mStationTime;
    }

    public int getStationValueReading(){
        return mStationValueReading;
    }

}
