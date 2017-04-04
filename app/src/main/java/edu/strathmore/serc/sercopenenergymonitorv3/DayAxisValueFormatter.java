package edu.strathmore.serc.sercopenenergymonitorv3;

import com.github.mikephil.charting.charts.BarLineChartBase;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.formatter.IAxisValueFormatter;
import com.github.mikephil.charting.formatter.IValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Bob on 24/03/2017.
 */

public class DayAxisValueFormatter implements IValueFormatter, IAxisValueFormatter {

    BarLineChartBase<?> chart;

    public DayAxisValueFormatter(BarLineChartBase<?> chart) {
        this.chart = chart;
    }

    @Override
    public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler) {
        return null;
    }


    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        try{
            SimpleDateFormat sdf = new SimpleDateFormat("h:mm a \n dd/MM/yyyy");
            Date date = new Date((long) value);
            return sdf.format(date);
        }
        catch(Exception ex){
            return "No time";
        }
    }
}