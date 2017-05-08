package edu.strathmore.serc.sercopenenergymonitorv3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import mehdi.sakout.fancybuttons.FancyButton;

import static edu.strathmore.serc.sercopenenergymonitorv3.R.id.container;

/**
 * Note that this activity uses:
 * MPAndroidChart to graph. (https://github.com/PhilJay/MPAndroidChart)
 * Custom buttons, FancyButtons from https://github.com/medyo/fancybuttons as the buttons
 */

public class GraphTabbed extends AppCompatActivity {


    // To be used in the link to be sent and are not meant to be changed currently
    // Possible to change in the future to be input by the user
    String ROOT_LINK = "";
    String API_KEY = "";
    /* For serc website
    ROOT_LINK = "https://serc.strathmore.edu/emoncms/feed/data.json?id=";
    API_KEY= "36ec19e2a135f22b50883d555eea2114";
    */

    // Placeholder. Not used currently
    final static int INTERVAL = 900;

    // Needed for link and is meant to be changed depending on the values input by the user
    private String startTime = "";
    private String endTime = "";
    private int stationID = 0;

    private String stationName = "";
    private String stationTag = "";
    private String link;

    // Global String variable that holds the JSON array when the user requests a time range
    private String result = "[]";


    // Needed for calendar dialog
    private int year_start, year_end, month_start, month_end, day_start, day_end;


    // Needed for time dialog
    private int hour_start, hour_end, minute_start, minute_end;


    private LineChart lineChart;




    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    View graphParametersPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph_tabbed);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);


        // Get API Key and Root Link from settings
        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        ROOT_LINK = appSettings.getString("root_link_editpref", "");
        ROOT_LINK = ROOT_LINK + "/feed/data.json?id=";
        API_KEY = appSettings.getString("api_key_edit","");


        //Getting data from received intent to start GraphActivity
        Bundle extras = getIntent().getExtras();
        // Checks that there are extras in the intent
        if (extras != null) {
            stationID = extras.getInt("Station_ID");
            stationTag = extras.getString("Station_tag");
            stationName = extras.getString("Station_name");
            Log.i("StationExtras in intent", "Station_ID" + String.valueOf(stationID) +
                    "Station_Tag" + String.valueOf(stationTag)+"Station_Name" + String.valueOf(stationName));
        }



        /* Setting today's date and time as default values when the calendar dialog first shows up
         * Otherwise this will default to 01 Jan 1970 (UNIX = 0) and result in a lot of swiping for
         * the user to get to today's date
         */
        Calendar cal;
        // Setting the date for the calendar dialog to be today
        cal = Calendar.getInstance(); // Get current time on the device
        // Sets the end date variables to be current time on the device
        year_end = cal.get(Calendar.YEAR);
        month_end = cal.get(Calendar.MONTH);
        day_end = cal.get(Calendar.DAY_OF_MONTH);



        // Sets the current time of the device as the start and end time variables
        hour_start = cal.get(Calendar.HOUR_OF_DAY);
        hour_end = cal.get(Calendar.HOUR_OF_DAY);
        minute_start = cal.get(Calendar.MINUTE);
        minute_end = cal.get(Calendar.MINUTE);


        /**
         * When the user presses the "Draw Graph" button without setting a custom start/end date and/or
         * time, the app should draw the graph for the past week. Because of this, the default values
         * for the UNIX start time should be a week from the device's current time. As exactly one week
         * from the device's current time will have the same time variables, only the date variables
         * need to be changed
         */
        // Getting the current time from the system clock in milliseconds
        Long tsLong = System.currentTimeMillis();
        // Setting the current time as now and the start time as one week from that date
        endTime = tsLong.toString();
        startTime = String.valueOf(Long.parseLong(endTime) - 604800000L); //604,800,000 is one week in milliseconds


        // Setting the date for the calendar dialog to be a week from today
        cal.setTimeInMillis(Long.parseLong(startTime));
        day_start = cal.get(Calendar.DAY_OF_MONTH);
        month_start = cal.get(Calendar.MONTH);
        year_start = cal.get(Calendar.YEAR);


        // Updating the link to include the change in new UNIX start time and end time
        setLink();



    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_graph_tabbed, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        //noinspection SimplifiableIfStatement
        if (id == android.R.id.home){
            onBackPressed();
            return true;
        }
        if (id == R.id.action_settings) {
            Intent openSettings = new Intent(this, SettingsActivity.class);
            startActivity(openSettings);
            return true;
        }

        if (id == R.id.action_reset_zoom) {
            lineChart.fitScreen();
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */


    public static class GraphParametersFragment extends Fragment{

        // Empty constructor
        public GraphParametersFragment(){}

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            //return super.onCreateView(inflater, container, savedInstanceState);
            final View parametersView = inflater.inflate(R.layout.graph_parameters,container, false);

            // Sets the graph title (TextView at the top of the activity) to be have the title and tag from the intent
            TextView graphHeading = (TextView) parametersView.findViewById(R.id.graph_title);
            graphHeading.setText(((GraphTabbed)getActivity()).stationTag + " - " + ((GraphTabbed)getActivity()).stationName);

            // OnClickListener for "Set Start Date" button to show DatePicker Dialog
            FancyButton calStart = (FancyButton) parametersView.findViewById(R.id.btn_set_start_date);
            calStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerStartFragment();
                    newFragment.show(getFragmentManager(), "datePickerStart");
                }
            });

            // OnClickListener for "Set End Date" button to show DatePicker Dialog
            FancyButton calEnd = (FancyButton) parametersView.findViewById(R.id.btn_set_end_date);
            calEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new DatePickerEndFragment();
                    newFragment.show(getFragmentManager(), "datePickerEnd");
                }
            });

            // OnClickListener for "Set Start Time" button to show TimePicker Dialog
            FancyButton timeStart = (FancyButton) parametersView.findViewById(R.id.btn_set_start_time);
            timeStart.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new TimePickerStartFragment();
                    newFragment.show(getFragmentManager(), "timePickerStart");
                }
            });

            // OnClickListener for "Set End Time" button to show TimePicker Dialog
            FancyButton timeEnd = (FancyButton) parametersView.findViewById(R.id.btn_set_end_time);
            timeEnd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogFragment newFragment = new TimePickerEndFragment();
                    newFragment.show(getFragmentManager(), "timePickerEnd");
                }
            });



            // OnClickListener for the "Draw Graph" button
            FancyButton drawGraphBtn = (FancyButton) parametersView.findViewById(R.id.btn_draw_graph_tabbed);
            drawGraphBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Draw Graph
                    ((GraphTabbed)getActivity()).drawGraph(((GraphTabbed)getActivity()).link);

                    // Set TextView
                    TextView startTimeText = (TextView) parametersView.findViewById(R.id.textview_set_start_time);
                    TextView endTimeText = (TextView) parametersView.findViewById(R.id.textview_set_end_time);
                    TextView startDateText = (TextView) parametersView.findViewById(R.id.textview_set_start_date);
                    TextView endDateText = (TextView) parametersView.findViewById(R.id.textview_set_end_date);

                    startDateText.setText(((GraphTabbed)getActivity()).day_start + "/" +(((GraphTabbed)getActivity()).month_start+1) + "/" + ((GraphTabbed)getActivity()).year_start);
                    endDateText.setText(((GraphTabbed)getActivity()).day_end + "/" + (((GraphTabbed)getActivity()).month_end+1) + "/" +((GraphTabbed)getActivity()).year_end);
                    startTimeText.setText(((GraphTabbed)getActivity()).hour_start+":"+((GraphTabbed)getActivity()).minute_start+"hrs");
                    endTimeText.setText(((GraphTabbed)getActivity()).hour_end+":"+((GraphTabbed)getActivity()).minute_end+"hrs");


                    // Move tab graph automatically
                    ((GraphTabbed)getActivity()).mViewPager.setCurrentItem(1, true);

                }
            });


            return parametersView;
        }


    }


    // Handles the behaviour for the DatePicker Dialog window for Start Date
    public static class DatePickerStartFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            /* Use the last date chosen as the date that is selected when the dialog pops up. This is
             * stored in the global variables year_start, month_start and dat_start. If it is the first
             * time the dialog is being launched, it will default the day a week from now
             */
            int year = ((GraphTabbed)getActivity()).year_start;
            int month = ((GraphTabbed)getActivity()).month_start;
            int day = ((GraphTabbed)getActivity()).day_start;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        // Excuted if user clicks 'Ok' on the dialog
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            // Show chosen date in TextView
            String dateText = String.valueOf(dayOfMonth) + "/" + String.valueOf(month+1) + "/"
                    + String.valueOf(year);
            TextView startDateText = (TextView) getActivity().findViewById(R.id.textview_set_start_date);
            startDateText.setText(dateText);



            // Get start time from activity
            int hourStart = ((GraphTabbed)getActivity()).hour_start;
            int minuteStart = ((GraphTabbed)getActivity()).minute_start;
            // Setting the UNIX timestamp that will be sent in the link for startTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year, month, dayOfMonth, hourStart, minuteStart);
            ((GraphTabbed)getActivity()).startTime = String.valueOf(chosenStart.getTimeInMillis());
            //setLink();

            //Set the global variables to current date chosen
            ((GraphTabbed)getActivity()).day_start = dayOfMonth;
            ((GraphTabbed)getActivity()).month_start = month;
            ((GraphTabbed)getActivity()).year_start = year;

        }
    }

    // Handles the behaviour for the DatePicker Dialog window for End Date
    public static class DatePickerEndFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            /* Use the last date chosen as the date that is selected when the dialog pops up. This is
             * stored in the global variables year_end, month_end and day_end. If it is the first
             * time the dialog is being launched, it will default to the current date.
             */
            int year = ((GraphTabbed)getActivity()).year_end;
            int month = ((GraphTabbed)getActivity()).month_end;
            int day = ((GraphTabbed)getActivity()).day_end;

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        // Executed if user clicks 'Ok' on the dialog
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {

            // Show chosen date in TextView
            String dateText = String.valueOf(dayOfMonth) + "/" + String.valueOf(month+1) + "/"
                    + String.valueOf(year);
            TextView endDateText = (TextView) getActivity().findViewById(R.id.textview_set_end_date);
            endDateText.setText(dateText);


            // Get start time from activity
            int hourEnd = ((GraphTabbed)getActivity()).hour_end;
            int minuteEnd = ((GraphTabbed)getActivity()).minute_end;
            // Setting the UNIX timestamp that will be sent in the link for startTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year, month, dayOfMonth, hourEnd, minuteEnd);
            ((GraphTabbed)getActivity()).endTime = String.valueOf(chosenStart.getTimeInMillis());
            //setLink();

            //Set the global variables to current date chosen
            ((GraphTabbed)getActivity()).day_end = dayOfMonth;
            ((GraphTabbed)getActivity()).month_end = month;
            ((GraphTabbed)getActivity()).year_end = year;

        }
    }

    // Handles the behaviour for the TimePicker Dialog for Start Time
    public static class TimePickerStartFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            /* Use the last time chosen as the time that is selected when the dialog pops up. This is
             * stored in the global variables hour_start and minute_start. If it is the first time
             * the dialog is being launched, it will default to the current time.
             */
            int hour = ((GraphTabbed)getActivity()).hour_start;
            int minute = ((GraphTabbed)getActivity()).minute_start;


            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Show Chosen Time in TextView
            String timeText = String.valueOf(hourOfDay) + ":" + String.valueOf(minute) + " hrs";
            TextView startTimeText = (TextView) getActivity().findViewById(R.id.textview_set_start_time);
            startTimeText.setText(timeText);

            // Get Date from Activity
            int day = ((GraphTabbed)getActivity()).day_start;
            int month = ((GraphTabbed)getActivity()).month_start;
            int year = ((GraphTabbed)getActivity()).year_start;
            // Setting the UNIX timestamp that will be sent in the link for startTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year, month, day, hourOfDay, minute);
            ((GraphTabbed)getActivity()).startTime = String.valueOf(chosenStart.getTimeInMillis());
            //setLink();

            // Set the global variable to time chosen
            ((GraphTabbed)getActivity()).hour_start = hourOfDay;
            ((GraphTabbed)getActivity()).minute_start = minute;

        }
    }

    // Handles the behaviour for the TimePicker Dialog for End Time
    public static class TimePickerEndFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            /* Use the last time chosen as the time that is selected when the dialog pops up. This is
             * stored in the global variables hour_end and minute_end. If it is the first time
             * the dialog is being launched, it will default to the current time.
             */
            int hour = ((GraphTabbed)getActivity()).hour_end;
            int minute = ((GraphTabbed)getActivity()).minute_end;


            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute, DateFormat.is24HourFormat(getActivity()));
        }

        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Show Chosen Time in TextView
            String timeText = String.valueOf(hourOfDay) + ":" + String.valueOf(minute) + " hrs";
            TextView endTimeText = (TextView) getActivity().findViewById(R.id.textview_set_end_time);
            endTimeText.setText(timeText);

            // Get Date from Activity
            int day = ((GraphTabbed)getActivity()).day_end;
            int month = ((GraphTabbed)getActivity()).month_end;
            int year = ((GraphTabbed)getActivity()).year_end;
            // Setting the UNIX timestamp that will be sent in the link for endTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year, month, day, hourOfDay, minute);
            ((GraphTabbed)getActivity()).endTime = String.valueOf(chosenStart.getTimeInMillis());
            //setLink();

            // Set the global variable to time chosen
            ((GraphTabbed)getActivity()).hour_end = hourOfDay;
            ((GraphTabbed)getActivity()).minute_end = minute;

        }
    }


    public static class GraphFragment extends Fragment{

        // Empty constructor
        public GraphFragment(){}

        @Nullable
        @Override
        public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            // Get the xml layout file for the fragment and store it as a view
            View graphOnlyView = inflater.inflate(R.layout.graph_only,container, false);
            // Set the global varible to the graph as the fragment is created
            ((GraphTabbed)getActivity()).lineChart = (LineChart) graphOnlyView.findViewById(R.id.graph_full_page);
            ((GraphTabbed)getActivity()).drawGraph(((GraphTabbed)getActivity()).link);
            // Return the View
            return graphOnlyView;
        }
    }








    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position){
                case 0:
                    return new GraphParametersFragment();
                case 1:
                    return new GraphFragment();

            }
            return null;
        }

        @Override
        public int getCount() {
            // Show 2 total pages.
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Parameters";
                case 1:
                    return "Graph";
            }
            return null;
        }
    }


    private void setLink(){
        link = ROOT_LINK + String.valueOf(stationID) + "&start=" + startTime + "&end=" + endTime
                + "&interval=" + INTERVAL + "&skipmissing=1&limitinterval=1&apikey=" + API_KEY;

    }

    // Method for drawing the graph. Requires the HTTP link to the JSON file
    private void drawGraph(String graphLink){
        // First update the link
        setLink();


        /*View graphOnlyView = getLayoutInflater().inflate(R.layout.graph_only, null, false);
        lineChart = (LineChart) graphOnlyView.findViewById(R.id.graph);*/


        // Gets the amount of time before Graph is zeroed from settings
        SharedPreferences appSettings = PreferenceManager.getDefaultSharedPreferences(this);
        float minutesInactivity = Float.valueOf(appSettings.getString("graph_zero_listpref", "-1"));


        try {

            // CmsApiCall returns the JSON in form of a continuous string
            AsyncTask localCmiCall = new CmsApi().execute(graphLink);



            /* For this call, the JSON consists of one large parent JSON Array with multiple children
             * arrays each containing 2 data points. The time at position 0 and the power reading at
             * position 1 in the child arrays.
             */
            Log.i("SERC Log", "Result in JSON: " + result);
            JSONArray parentJSON = new JSONArray(result);
            JSONArray childJSONArray;

            // Array list of entry objects needed that will be used by the LineDataSet object
            List<Entry> entries = new ArrayList<>();
            // Array list for the values of x (timestamp) and y (power values) coordinates of the graph
            ArrayList<Long> xAxis = new ArrayList<>();
            ArrayList<Double> yAxis = new ArrayList<>();

            /**
             * This cycles through each JSON array in the main array and adds the element in the first
             * position (the timestamp in milliseconds) to xAxis array list and the second element in
             * the array (the power reading ) to the yAxis array list. These 2 ArrayLists are then used
             * to create an ArrayList of Entry objects stored in the variable entries (i.e. each Entry
             * object contains the xAxis and yAxis values from one JSON array)
             */
            // First checks if any data is being sent (i.e. it is not an empty array)

            if(!parentJSON.isNull(0)) {
                Log.i("SERC Log", "Not null array: Response from API Call not null");
                for (int i = 0; i < parentJSON.length(); i++) {
                    childJSONArray = parentJSON.getJSONArray(i);

                    for (int j = 0; j < childJSONArray.length(); j++) {
                        // Check if value is null and adds 0 if so to avoid NullException error
                        if (childJSONArray.get(1) == null) {
                            yAxis.add(0d);

                        } else {

                            xAxis.add(childJSONArray.getLong(0));
                            yAxis.add(childJSONArray.getDouble(1));

                        }
                    }
                }

                Log.i("SERC Log", "Adding to entries: Changing the elements of the array into Entry objects");
                /**
                 * This is used to add the x and y axis values to as Entry objects to an ArrayList.
                 * It also 'zeros' the graph by adding 0 as y axis reading just before and after the
                 * the 2 x axis values the are further apart than the threshold value
                 */
                Float threshold = minutesInactivity * 60000f; //Converts minutes to milliseconds
                long previousX = Long.valueOf(startTime);
                Long absTimeDiff;
                Long zeroOffset = Long.valueOf(1000);
                if (threshold > 0f) {
                    // Since the xAxis and yAxis ArrayList are the same length either xAxis.size() or yAxis.size()
                    // could have been used
                    for (int i = 0; i < xAxis.size(); i++) {
                        Long currentX = xAxis.get(i);
                        absTimeDiff = Math.abs(currentX - previousX);


                        if(absTimeDiff>threshold){

                            entries.add(new Entry((float) (previousX+zeroOffset), 0f));
                            entries.add(new Entry((float) (currentX-zeroOffset), 0f));
                            entries.add(new Entry((float) xAxis.get(i), yAxis.get(i).floatValue()));

                        } else{
                            entries.add(new Entry((float) xAxis.get(i), yAxis.get(i).floatValue()));
                        }


                        previousX = currentX;
                    }
                } else{
                    for (int i = 0; i < xAxis.size(); i++) {
                        entries.add(new Entry((float) xAxis.get(i), yAxis.get(i).floatValue()));
                    }
                }




                // The following steps are done to prepare for the new data on the graph
                Log.i("SERC Log", "Clearing previous graph");
                lineChart.clear();
                lineChart.invalidate(); //refresh the data
                lineChart.fitScreen();  // set the zoom level back to the default

                // Gets the preference for whether or not the grid will be drawn
                boolean toDrawGrid = appSettings.getBoolean("graph_draw_grid_pref", true);
                lineChart.getXAxis().setDrawGridLines(toDrawGrid);
                lineChart.getAxisLeft().setDrawGridLines(toDrawGrid);
                lineChart.getAxisRight().setDrawGridLines(toDrawGrid);

                Log.i("SERC Log", "Styling xAxis");
                // Gets the x axis
                XAxis styledXAxis = lineChart.getXAxis();
                // Sets the x axis labels to appear in the according to settings
                int xAxisLabelPosition = Integer.valueOf(appSettings.getString("graph_x_axis_position_listpref","1"));
                switch (xAxisLabelPosition){
                    case 1:
                        styledXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        break;
                    case 2:
                        styledXAxis.setPosition(XAxis.XAxisPosition.TOP);
                        break;
                    case 3:
                        styledXAxis.setPosition(XAxis.XAxisPosition.BOTH_SIDED);
                        break;

                }


                // Sets the rotation angle of the x axis labels
                styledXAxis.setLabelRotationAngle(45f);

                // Removing right Y Axis labels
                YAxis rightYAxis = lineChart.getAxisRight();
                // Get from settings
                boolean allowRightYAxisLabel = appSettings.getBoolean("graph_y_axis_both_sides", false);
                rightYAxis.setDrawLabels(allowRightYAxisLabel);

                /* DataSet objects hold data which belongs together, and allow individual styling
                 * of that data. For example, below the color of the line set to RED (by default) and
                 * the drawing of individual circles for each data point is turned off.
                 */
                Log.i("SERC Log", "Configuring the Data Set");
                LineDataSet dataSet = new LineDataSet(entries, "Power");

                // Getting the color of the line and setting it
                int graphColor = Integer.valueOf(appSettings.getString("graph_line_color_listpref", "1"));
                switch (graphColor){
                    case 1:
                        dataSet.setColor(Color.RED);
                        break;
                    case 2:
                        dataSet.setColor(Color.CYAN);
                        break;
                    case 3:
                        dataSet.setColor(Color.BLACK);
                        break;
                    case 4:
                        dataSet.setColor(Color.BLUE);
                        break;
                    case 5:
                        dataSet.setColor(Color.GREEN);
                        break;
                    case 6:
                        dataSet.setColor(Color.MAGENTA);
                        break;
                    case 7:
                        dataSet.setColor(Color.YELLOW);
                        break;

                }

                dataSet.setDrawCircles(false);

                /* As a last step, one needs to add the LineDataSet object (or objects) that were created
                 * to a LineData object. This object holds all data that is represented by a Chart
                 * instance and allows further styling.
                 */
                LineData lineData = new LineData(dataSet);

                /**
                 * This sets the styling of the x axis according to how it has been defined in the
                 * DayAxisValueFormatter class. In this instance, the UNIX timestamp is converted to
                 * human readable time in the DayAxisValueFormatter class
                 */
                styledXAxis.setValueFormatter(new DayAxisValueFormatter(lineChart));

                // Sets the size and position of the graph's legend
                Legend legend = lineChart.getLegend();
                legend.setXEntrySpace(5f);
                legend.setFormSize(5f);
                legend.setPosition(Legend.LegendPosition.RIGHT_OF_CHART_INSIDE);

                // Helps to clear the extra whitespace in the graph
                lineChart.getDescription().setText("");

                // Sets the LineData object to the LineChart object lineChart that is part of the view
                lineChart.setData(lineData);
                lineChart.notifyDataSetChanged();

            }
            lineChart.invalidate(); //refresh


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // AsyncTask class that modifys the global variable result
    private class CmsApi extends AsyncTask<String, Void, String> {


        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(GraphTabbed.this);
            super.onPreExecute();
            Log.i("SERC Log:", "Starting onPreExecute");
            dialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            dialog.setMessage("Loading. Please wait...");
            dialog.setIndeterminate(true);
            dialog.setCanceledOnTouchOutside(false);
            Log.i("SERC Log:", "Showing ProgressBar");
            dialog.show();
        }


        @Override
        protected String doInBackground(String... params) {
            //result = "";
            try {
                String urlstring = params[0];
                Log.i("SERC Log:", "HTTP Connecting: " + urlstring);

                // Recommended way of making http requests is HttpURLConnection
                URL url = new URL(urlstring);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

                try {
                    InputStream reader = new BufferedInputStream(urlConnection.getInputStream());
                    Log.i("SERC Log:", "Starting to read text");
                    String text = "";
                    int i = 0;
                    while ((i = reader.read()) != -1) {
                        text += (char) i;
                    }
                    Log.i("SERC Log:", "HTTP Response: " + text);
                    result = text;

                } catch (Exception e) {
                    Log.i("SERC Log:", "HTTP Exception: " + e);
                } finally {
                    Log.i("SERC Log:", "HTTP Disconnecting");
                    urlConnection.disconnect();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.i("SERC Log:", "HTTP Exception: " + e);
            }
            Log.i("Result from CMSApi", result);
            return result;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            Log.i("SERC Log", "Loading Dialog onPostExecute exists: "+String.valueOf(dialog.isShowing()));
            if(dialog.isShowing()) {
                dialog.dismiss();
            }
        }

    }
}
