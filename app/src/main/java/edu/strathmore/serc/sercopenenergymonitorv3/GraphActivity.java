package edu.strathmore.serc.sercopenenergymonitorv3;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
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


/**
 * Note that this activity uses:
 * MPAndroidChart to graph. (https://github.com/PhilJay/MPAndroidChart)
 * Custom buttons called FancyButtons from https://github.com/medyo/fancybuttons as the buttons
 */


public class GraphActivity extends AppCompatActivity {

    // To be used in the link to be sent and are not meant to be changed currently
    // Possible to change in the future to be input by the user
    final static String ROOT_LINK = "https://serc.strathmore.edu/emoncms/feed/data.json?id=";
    final static String API_KEY= "36ec19e2a135f22b50883d555eea2114";

    // Placeholder. Not used currently
    final static int INTERVAL = 900;

    // Needed in for the onCreateDialog method to decide what dialog to show
    static final int CALENDAR_DIALOG_ID_START = 0;
    static final int CALENDAR_DIALOG_ID_END = 1;
    static final int TIME_DIALOG_ID_START = 2;
    static final int TIME_DIALOG_ID_END = 3;


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
    private TextView startDateTextView, endDateTextView;
    private int year_start, year_end, month_start, month_end, day_start, day_end;


    // Needed for time dialog
    private TextView startTimeTextView, endTimeTextView;
    private int hour_start, hour_end, minute_start, minute_end;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_graph);

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

        // Sets the graph title (TextView at the top of the activity) to be have the title and tag from the intent
        TextView graphHeading = (TextView) findViewById(R.id.graph_title);
        graphHeading.setText(stationTag + " - " + stationName);

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
        // To create the date picker dialog this custom method needs to be called
        showCalendarDialog();

        // Sets the current time of the device as the start and end time variables
        hour_start = cal.get(Calendar.HOUR_OF_DAY);
        hour_end = cal.get(Calendar.HOUR_OF_DAY);
        minute_start = cal.get(Calendar.MINUTE);
        minute_end = cal.get(Calendar.MINUTE);
        // To create the time picker dialog this custom method needs to be called
        showTimeDialog();

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

        // Draw graph using the updated info in the link
        Log.i("Drawing initial graph:", "Station ID: " + stationID + " Start time UNIX: " + String.valueOf(startTime)+ ", End time UNIX: " + String.valueOf(endTime));
        drawGraph(link);

        /*
         * OnClick Listener for the button that will be used to draw the graph
         * Before the graph is drawn, the TextViews next to the date/time buttons are set to the
         * values that the user selected (especially useful if the user has pressed the "Draw Graph"
         * button without setting a custom start/end date and/or time)
         */
        FancyButton drawGraphFancyButton = (FancyButton) findViewById(R.id.btn_draw_graph);
        drawGraphFancyButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Update all the text views with the correct data
                startDateTextView.setText(day_start+"/"+(month_start+1)+"/"+year_start);
                endDateTextView.setText(day_end+"/"+(month_end+1)+"/"+year_end);
                startTimeTextView.setText(hour_start+":"+minute_start+"hrs");
                endTimeTextView.setText(hour_end+":"+minute_end+"hrs");

                Log.i("SERC Log:Start/End Date", "Start: " + day_start+"/"+(month_start+1)+"/"+year_start + " End:" + day_end+"/"+(month_end+1)+"/"+year_end);
                Log.i("SERC Log:Start/End Time", "Start: " + hour_start+":"+minute_start+"hrs" + " End:" + hour_end+":"+minute_end+"hrs");

                // Checks if the End date/time is after Start date/time
                if (Long.parseLong(endTime) < Long.parseLong(startTime)){
                    Toast.makeText(GraphActivity.this, "The Start Time or Date is after the End Time or Date", Toast.LENGTH_LONG).show();
                }
                else{
                    // Updates the link before using it in the drawGraph method
                    setLink();
                    drawGraph(link);
                }
            }
        });

    }

    // Method for drawing the graph. Requires the HTTP link to the JSON file
    private void drawGraph(String graphLink){
        LineChart lineChart = (LineChart) findViewById(R.id.graph);

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
                // Since the xAxis and yAxis ArrayList are the same length either xAxis.size() or yAxis.size()
                // could have been used
                for (int i = 0; i < xAxis.size(); i++) {
                    entries.add(new Entry((float) xAxis.get(i), yAxis.get(i).floatValue()));
                }

                // The following steps are done to prepare for the new data on the graph
                Log.i("SERC Log", "Clearing previous graph");
                lineChart.clear();
                lineChart.invalidate(); //refresh the data
                lineChart.fitScreen();  // set the zoom level back to the default

                Log.i("SERC Log", "Styling xAxis");
                // Gets the x axis
                XAxis styledXAxis = lineChart.getXAxis();
                // Sets the x axis labels to appear in the bottom
                styledXAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                // Sets the rotation angle of the x axis labels
                styledXAxis.setLabelRotationAngle(45f);

                /* DataSet objects hold data which belongs together, and allow individual styling
                 * of that data. For example, below the color of the line set to RED and the drawing
                 * of individual circles for each data point is turned off.
                 */
                Log.i("SERC Log", "Configuring the Data Set");
                LineDataSet dataSet = new LineDataSet(entries, "Power");
                dataSet.setColor(Color.RED);
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

    public void showCalendarDialog(){
        // Gets the FancyButtons and TextViews relating to the calendar
        FancyButton btnStartDate = (FancyButton) findViewById(R.id.btn_set_start_date);
        FancyButton btnEndDate = (FancyButton) findViewById(R.id.btn_set_end_date);
        startDateTextView = (TextView) findViewById(R.id.textview_set_start_date);
        endDateTextView = (TextView) findViewById(R.id.textview_set_end_date);


        btnStartDate.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showDialog(CALENDAR_DIALOG_ID_START);
                    }
                }
        );

        btnEndDate.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View v){
                        showDialog(CALENDAR_DIALOG_ID_END);
                    }
                }
        );
    }

    public void showTimeDialog(){
        // Gets the FancyButtons and TextViews relating to time
        FancyButton btnStartTime = (FancyButton) findViewById(R.id.btn_set_start_time);
        FancyButton btnEndTime = (FancyButton) findViewById(R.id.btn_set_end_time);
        startTimeTextView = (TextView) findViewById(R.id.textview_set_start_time);
        endTimeTextView = (TextView) findViewById(R.id.textview_set_end_time);


        btnStartTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID_START);
            }
        });

        btnEndTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(TIME_DIALOG_ID_END);
            }
        });
    }

    // Chooses what dialog to display depending on the arguments
    @Override
    protected Dialog onCreateDialog(int id) {
        if (id == CALENDAR_DIALOG_ID_START) {
            return new DatePickerDialog(this, datePickerListenerStart, year_start, month_start, day_start);
        } else if (id == CALENDAR_DIALOG_ID_END) {
            return new DatePickerDialog(this, datePickerListenerEnd, year_end, month_end, day_end);
        } else if (id == TIME_DIALOG_ID_START) {
            return new TimePickerDialog(this, timePickerListenerStart, hour_start, minute_start, false);
        } else if (id == TIME_DIALOG_ID_END) {
            return new TimePickerDialog(this, timePickerListenerEnd, hour_end, minute_end, false);
        }
        return null;
    }

    protected DatePickerDialog.OnDateSetListener datePickerListenerStart = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Sets the value chosen to the global variable (so as to update the link to be sent)
            year_start = year;
            month_start = month;
            day_start = dayOfMonth;

            // Set TextView textview_set_start_date to show the current chosen start date
            startDateTextView.setText(day_start+"/"+(month_start+1)+"/"+year_start);

            // Setting the UNIX timestamp that will be sent in the link for startTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year_start, month_start, day_start, hour_start, minute_start);
            startTime = String.valueOf(chosenStart.getTimeInMillis());
            setLink();
            Log.i("Chosen UNIX Start Date", startTime);
            Log.i("Start and End Date", "Start: " + day_start+"/"+(month_start+1)+"/"+year_start + " End:" + day_end+"/"+(month_end+1)+"/"+year_end);

        }
    };

    protected DatePickerDialog.OnDateSetListener datePickerListenerEnd = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            // Sets the value chosen to the global variable (so as to update the link to be sent)
            year_end = year;
            month_end = month;
            day_end = dayOfMonth;

            // Set TextView textview_set_end_date to show the current chosen end date
            endDateTextView.setText(day_end+"/"+(month_end+1)+"/"+year_end);

            // Setting the UNIX timestamp that will be sent in the link for endTime
            Calendar chosenEnd = Calendar.getInstance();
            chosenEnd.set(year_end, month_end, day_end, hour_end, minute_end);
            endTime = String.valueOf(chosenEnd.getTimeInMillis());
            setLink();
            Log.i("Chosen UNIX End Date", endTime);
            Log.i("Start and End Date", "Start: " + day_start+"/"+(month_start+1)+"/"+year_start + " End:" + day_end+"/"+(month_end+1)+"/"+year_end);
        }
    };

    protected TimePickerDialog.OnTimeSetListener timePickerListenerStart = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Sets the value chosen to the global variable (so as to update the link to be sent)
            hour_start = hourOfDay;
            minute_start = minute;

            // Set TextView textview_set_start_time to show the current chosen start time
            startTimeTextView.setText(hour_start+":"+minute_start+"hrs");

            // Setting the UNIX timestamp that will be sent in the link for startTime
            Calendar chosenStart = Calendar.getInstance();
            chosenStart.set(year_start, month_start, day_start, hour_start, minute_start);
            startTime = String.valueOf(chosenStart.getTimeInMillis());
            setLink();
            Log.i("Chosen Start Time", startTime);
            Log.i("Start and End Time", "Start: " + hour_start+":"+minute_start+"hrs" + " End:" + hour_end+":"+minute_end+"hrs");
        }
    };

    protected TimePickerDialog.OnTimeSetListener timePickerListenerEnd = new TimePickerDialog.OnTimeSetListener() {
        @Override
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            // Sets the value chosen to the global variable (so as to update the link to be sent)
            hour_end = hourOfDay;
            minute_end = minute;

            // Set TextView textview_set_end_time to show the current chosen end time
            endTimeTextView.setText(hour_end+":"+minute_end+"hrs");

            // Setting the UNIX timestamp that will be sent in the link for endTime
            Calendar chosenEnd = Calendar.getInstance();
            chosenEnd.set(year_end, month_end, day_end, hour_end, minute_end);
            endTime = String.valueOf(chosenEnd.getTimeInMillis());
            setLink();
            Log.i("Chosen End Time", endTime);
            Log.i("Start and End Time", "Start: " + hour_start+":"+minute_start+"hrs" + " End:" + hour_end+":"+minute_end+"hrs");
        }
    };

    // Method that updates the link with the latest variables set by its global variables
    private void setLink(){
        link = ROOT_LINK + String.valueOf(stationID) + "&start=" + startTime + "&end=" + endTime
                + "&interval=" + INTERVAL + "&skipmissing=1&limitinterval=1&apikey=" + API_KEY;

    }


    // AsyncTask class that modifys the global variable result
    private class CmsApi extends AsyncTask<String, Void, String> {


        private ProgressDialog dialog;


        @Override
        protected void onPreExecute() {
            dialog = new ProgressDialog(GraphActivity.this);
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
