package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class MainActivity extends AppCompatActivity {

    //For the call to be made to the CMS API
    private static String rootLinkAddress = "https://serc.strathmore.edu";
    private static String apiKey = "36ec19e2a135f22b50883d555eea2114";

    // For the SwipeRefreshLayout used both in the onCreate and refresh method
    private SwipeRefreshLayout swipeRefreshLayout;

    // For the adapter used in the ListView of the Main Activity/Screen.
    // Needs to be global as it is used both in the onCreate and refresh method
    private RecordingStationAdapter adapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log start of onCreate method
        Log.i("SERC Log", "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create an ArrayList of RecordingStation Objects with the variable name recordingStations
        //Full Array list from the CMS platform
        Log.i("SERC Log:", "Calling CMSApi");
        ArrayList<RecordingStation> recordingStations = getRecordingStationsList();

        // Sublist of recording stations as chosen in settings
        ArrayList<RecordingStation> recordingStationsForAdapter = getRecordingStationInSettings(recordingStations);


        // Setting the RecordingStationAdapter<RecordingStation> to the ListView to display
        adapter = new RecordingStationAdapter(this, recordingStationsForAdapter);
        ListView listView = (ListView) findViewById(R.id.polling_results_list_view);
        listView.setAdapter(adapter);

        /*
         * OnItemClickLister for each item in the ListView. When an item in the listview is clicked,
         * this sends an intent to open GraphActivity (while passing some information about the
         * object to GraphActivity within the intent)
         */
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Intent to open GraphActivity
                Intent graphIntent = new Intent(MainActivity.this, GraphActivity.class);

                // Getting the station ID, name and tag of the Clicked item to be sent with the intent
                graphIntent.putExtra("Station_ID", adapter.getItem(position).getStationID());
                graphIntent.putExtra("Station_name", adapter.getItem(position).getStationName());
                graphIntent.putExtra("Station_tag", adapter.getItem(position).getStationTag());

                // Start GraphActivity
                startActivity(graphIntent);

            }
        });


        /**
         * This is the listener for when a user long clicks/presses on an item in the listview.
         * This opens a Dialog showing all the information stored for the that RecordingStation object
         */
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                AlertDialog.Builder  alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setTitle("Station Details");
                alertDialogBuilder.setIcon(R.mipmap.ic_launcher_serc);
                alertDialogBuilder.setPositiveButton("Ok", null);

                Date currentTime = new Date(adapter.getItem(position).getStationTime()*1000);
                SimpleDateFormat sdf = new SimpleDateFormat("EEE, d MMM yyyy h:mm a");
                String stationTime = sdf.format(currentTime);
                CharSequence[] stationDetails = {
                        "Station ID: " + String.valueOf(adapter.getItem(position).getStationID()),
                        "Station Name: " + adapter.getItem(position).getStationName(),
                        "Station Tag: " + adapter.getItem(position).getStationTag(),
                        "Current Reading: " + String.valueOf(adapter.getItem(position).getStationValueReading()),
                        "Current Time: " + stationTime};

                alertDialogBuilder.setItems(stationDetails, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        

                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

                /* This returns a boolean to indicate whether you have consumed the event and it
                 * should not be carried further. That is, return true to indicate that you have
                 * handled the event and it should stop here; return false if you have not handled
                 * it and/or the event should continue to any other on-click listeners.
                 * If false is returned, OnItemClickListener will be triggered resulting in GraphActivity
                 * being opened
                 */
                return true;
            }
        });

        // onClick Listener for Swiping up to refresh feed. Calls the refreshContent method
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                // Calls the refresh content method defined within this class
                refreshContent();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent openSettingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(openSettingsIntent);
            return true;
        }
        if (id==R.id.action_about){
            Intent openAboutIntent = new Intent(this, AboutActivity.class);
            startActivity(openAboutIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Method to refresh content. Called when user swipes up to refresh
    private void refreshContent(){
        // Full list of Stations from the platform
        ArrayList<RecordingStation> recordingStationsList = getRecordingStationsList();

        // Sublist of recording stations as chosen in settings
        ArrayList<RecordingStation> recordingStationsForAdapter = getRecordingStationInSettings(recordingStationsList);

        // Clear the adapter and load up new content to adapter
        adapter.clear();
        adapter.addAll(recordingStationsForAdapter);
        swipeRefreshLayout.setRefreshing(false); //stop the refresh dialog once finished
    }

    // Method that calls CmsApiCall class and returns an ArrayList of Recording Station objects
    private ArrayList<RecordingStation> getRecordingStationsList(){

        // Create an ArrayList of RecordingStation Objects with the variable name recordingStations
        ArrayList<RecordingStation> recordingStations = new ArrayList<>();

        String result;
        try {
            // Call CmsApiCall using the MainActivity as the context. The result is the JSON file in
            // form of a continuous String.
            result = new CmsApiCall(MainActivity.this).execute(rootLinkAddress+"/emoncms/feed/list.json&apikey="+apiKey).get();

            // This changes the JSON String into a JSON object. The response for this call consists of
            // one JSON array with individual objects for each node added to the Emon CMS platform
            JSONArray parentJSON = new JSONArray(result);
            JSONObject childJSON;


            // Cycles through all objects within the JSON array
            for (int i=0; i<parentJSON.length(); i++){

                // Initialising the variables needed for the RecordingStation constructor
                int id =0;
                String name="";
                String tag="";
                int time=0;
                int powerReading=0;

                // Setting childJSON as an object in the JSON array at position i
                childJSON = parentJSON.getJSONObject(i);

                //Checks if ID field exists and is not null
                if (childJSON.has("id") && !childJSON.isNull("id")){
                    id = childJSON.getInt("id");
                }
                //Checks if name field exists and is not null
                if (childJSON.has("name") && !childJSON.isNull("name")){
                    name = childJSON.getString("name");
                }
                //Checks if tag field exists and is not null
                if (childJSON.has("tag") && !childJSON.isNull("tag")){
                    tag = childJSON.getString("tag");
                }
                //Checks if time field exists and is not null
                if (childJSON.has("time") && !childJSON.isNull("time")){
                    time = childJSON.getInt("time");
                }
                //Checks if value field exists and is not null
                if (childJSON.has("value") && !childJSON.isNull("value")){
                    powerReading = childJSON.getInt("value");
                }

                //Creating new RecordingStation object with the values and adding it to the ArrayList for each loop
                RecordingStation recordingStation = new RecordingStation(id, name, tag, time, powerReading);
                recordingStations.add(recordingStation);

            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return recordingStations;
    }

    private ArrayList<RecordingStation> getRecordingStationInSettings(ArrayList<RecordingStation> recordingStations){
        /*
         * This function takes in an array list of the recording stations and returns an array list
         * of recording stations which is a subset of the of the input. This subset contains all the
         * recording stations contained within the setting prefferences
         */
        Log.i("SERC Log", "Pulling preferences");
        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> recordingStationsInSettings = appSettings.getStringSet("selected_station_list", Collections.<String>emptySet());
        Set<String> chosenRecordingStations = new HashSet<>();


        // Creates an array list of names of the stations in the form "TAG - NAME"
        Log.i("SERC Log:", "Building List of Recording Station Names");
        ArrayList<String> recordingStationNames = new ArrayList<>();
        for (int i=0; i<recordingStations.size(); i++){
            recordingStationNames.add(recordingStations.get(i).getStationTag() + " - " + recordingStations.get(i).getStationName());
        }

        // Sort List Alphabetically
        Collections.sort(recordingStationNames, String.CASE_INSENSITIVE_ORDER);

        // Adds these names to a new String Array List to chosenRecordingStations
        Log.i("SERC Log:", "Adding the names to settings");
        chosenRecordingStations.addAll(recordingStationNames);

        Log.i("SERC Log:", "Saving new settings");
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putStringSet("full_station_list", chosenRecordingStations);

        Log.i("SERC Log", "Checking if selected_station_list in SharedPrefs is empty: " + String.valueOf(recordingStations.isEmpty()));
        if (recordingStationsInSettings.isEmpty()) {
            // Adds full list in case selected list is empty e.g. on first launch
            editor.putStringSet("selected_station_list", chosenRecordingStations);
            editor.apply();
        }

        // Logging entries in the list
        Log.i("SERC Log:", "selected_station_list size: "+ String.valueOf(recordingStationsInSettings.size()));
        for (int i=0; i<recordingStationsInSettings.size(); i++){
            Log.i("SERC Log:", "selected_station_list " + String.valueOf(i) + ": "+ String.valueOf(recordingStationsInSettings.toArray()[i]));
        }

        ArrayList<RecordingStation> recordingStationsForAdapter = new ArrayList<>();
        for (String nameTag:recordingStationsInSettings){
            //String nameTag = recordingStationsInSettings;
            for (int j = 0; j < recordingStations.size(); j++){
                String currentStn = recordingStations.get(j).getStationTag() + " - " + recordingStations.get(j).getStationName();
                Log.i("SERC Log", "currentStn: " + currentStn);
                Log.i("SERC Log", "nameTag: " + nameTag);
                //Log.i("SERC Log", "nameTag==currentStn " + String.valueOf(nameTag==currentStn));
                Log.i("SERC Log", "nameTag.contains(currentStn): " + String.valueOf(nameTag.contains(currentStn)));

                if (nameTag.contains(currentStn)){
                    recordingStationsForAdapter.add(recordingStations.get(j));

                }
            }
        }

        return recordingStationsForAdapter;
    }

}
