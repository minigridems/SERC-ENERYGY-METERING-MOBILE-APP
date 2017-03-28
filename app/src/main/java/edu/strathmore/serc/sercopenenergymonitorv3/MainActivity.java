package edu.strathmore.serc.sercopenenergymonitorv3;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
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

import java.util.ArrayList;
import java.util.Collections;
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


    private String selectedStationsInSettings = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Log start of onCreate method
        Log.i("SERC Log", "OnCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        // Create an ArrayList of RecordingStation Objects with the variable name recordingStations
        Log.i("SERC Log:", "Calling CMSApi");
        ArrayList<RecordingStation> recordingStations = getRecordingStationsList();

        /**
         * Here the shared preferences are pulled and refreshed to include all the stations that have been pulled
         * from calling CMSApi. This stored in a string set
         */
        Log.i("SERC Log:", "Pulling preferences");
        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> recordingStationsInSettings = appSettings.getStringSet("stations_multi_list", Collections.<String>emptySet());
        Set<String> chosenRecordingStations = new HashSet<>();
        /**
         * Check for the first time the app is run.
         * Checks that there is something in the list and displays it in the logs
         */
        if (!recordingStationsInSettings.isEmpty()) {
            // Logging entries in the list
            Log.i("SERC Log:", "stations_multi_list size: "+ String.valueOf(recordingStationsInSettings.size()));
            for (int i=0; i<recordingStationsInSettings.size(); i++){
                Log.i("SERC Log:", "stations_multi_list " + String.valueOf(i) + ": "+ String.valueOf(recordingStationsInSettings.toArray()[i]));
            }
        }

        // Creates an array list of names of the stations in the form "TAG - NAME"
        Log.i("SERC Log:", "Building List of Recording Station Names");
        ArrayList<String> recordingStationNames = new ArrayList<>();
        for (int i=0; i<recordingStations.size(); i++){
            recordingStationNames.add(recordingStations.get(i).getStationTag() + " - " + recordingStations.get(i).getStationName());
        }
        // Adds these names to a new Set chosenRecordingStations
        Log.i("SERC Log:", "Adding the names to settings");
        for (int i=0; i<recordingStationNames.size(); i++) {
            String name = recordingStationNames.get(i);
            chosenRecordingStations.add(name);
        }
        Log.i("SERC Log:", "Saving settings");
        SharedPreferences.Editor editor = appSettings.edit();
        editor.putStringSet("stations_multi_list", chosenRecordingStations);
        editor.apply();



        // Setting the RecordingStationAdapter<RecordingStation> to the ListView
        adapter = new RecordingStationAdapter(this, recordingStations);
        ListView listView = (ListView) findViewById(R.id.polling_results_list_view);
        listView.setAdapter(adapter);

        // OnItemClickLister for each item in the ListView. On Click this sends an intent to open
        // GraphActivity while passing some information about the object to GraphActivity in the intent
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent graphIntent = new Intent(MainActivity.this, GraphActivity.class);
                // Getting the station ID of the Clicked item to be sent with the intent
                graphIntent.putExtra("Station_ID", adapter.getItem(position).getStationID());
                graphIntent.putExtra("Station_name", adapter.getItem(position).getStationName());
                graphIntent.putExtra("Station_tag", adapter.getItem(position).getStationTag());
                startActivity(graphIntent);

            }
        });

        // onClick Listener for Swiping up to refresh feed. Calls the refreshContent method
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener(){
            @Override
            public void onRefresh(){
                refreshContent();
            }
        });

        //Define onClick action for the Floating action button. Sends intent to open GraphActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent openGraphIntent = new Intent(MainActivity.this, GraphActivity.class);
                startActivity(openGraphIntent);

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
        ArrayList<RecordingStation> recordingStationsList = getRecordingStationsList();

        /*0 = Wasini
        * 2 = Talek
        * 3 = Other
        * *//*
        ArrayList<RecordingStation> selectedRecordingStationsList = new ArrayList<>();
        if (selectedStationsInSettings.contains("0") && selectedStationsInSettings.contains("1")){
            selectedRecordingStationsList = recordingStationsList;
        }
        else if(selectedStationsInSettings.contains("0")){
            for (int i =0; i<recordingStationsList.size(); i++){
                if (recordingStationsList.get(i).getStationTag().contains("Wasini")){
                    selectedRecordingStationsList.add(recordingStationsList.get(i));
                }
            }
        } else if(selectedStationsInSettings.contains("2")){
            for (int i =0; i<recordingStationsList.size(); i++){
                if (recordingStationsList.get(i).getStationTag().contains("Talek")){
                    selectedRecordingStationsList.add(recordingStationsList.get(i));
                }
            }
        }
*/
        // Clear the adapter and load up new content to adapter
        adapter.clear();
        adapter.addAll(recordingStationsList);
        swipeRefreshLayout.setRefreshing(false); //stop the refresh dialog once finished
    }

    // Method that calls CmsApiCall class and returns an ArrayList of Recording Station objects
    private ArrayList<RecordingStation> getRecordingStationsList(){

        // Create an ArrayList of RecordingStation Objects with the variable name recordingStations
        ArrayList<RecordingStation> recordingStations = new ArrayList<RecordingStation>();

        String result = "";
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

}
