package com.example.finance_geek;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import java.text.*;
import java.util.*;

public class ReportHistory extends Fragment {

    private LineChart lineChart;
    private ArrayList<Float> totalPriceData = new ArrayList<>();
    private ArrayList<Date> totalDateData = new ArrayList<>();
    private ArrayList<String> dateData = new ArrayList<>();
    private ArrayList<Float> priceData = new ArrayList<>();
    private ArrayList<String> monthly = new ArrayList<>();
    private ArrayList<Float> monthly_price = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2_report_history, container, false);
        lineChart = (LineChart) rootView.findViewById(R.id.line_chart);
        setupLineCharts();
        return rootView;
    }

    private void setupLineCharts() {
        HomePage activity = new HomePage();
        Map<Date, Double> data_price_date = activity.getPriceDateData();
        DateFormat df = new SimpleDateFormat("MMddyy");
        ArrayList<Entry> graphData = new ArrayList<>();
        ArrayList<ILineDataSet> lds1 = new ArrayList<>();
        LineDataSet lds;
        LineData ld;
        float sum = 0;
        for(Map.Entry<Date, Double> entry : data_price_date.entrySet())
        {
            double value = entry.getValue();
            totalDateData.add(entry.getKey());
            totalPriceData.add((float) value);
        }

        Calendar cal = new GregorianCalendar();
        cal.add(Calendar.MONTH, -1);

        for(int a = 0 ; a<totalPriceData.size();a++)
        {
            monthly.add(df.format(totalDateData.get(a)));
            monthly_price.add(totalPriceData.get(a));
        }

        for(int a = 0 ; a<monthly.size();a++)
        {
            if(monthly.get(a).substring(1,2).equals((cal.get(Calendar.MONTH) + 1) + ""))
            {
                dateData.add(monthly.get(a));
                priceData.add(monthly_price.get(a));
                sum = sum + monthly_price.get(a);
            }
        }

        int s = (int) sum;

        String str = "Spending trends over the past month with total expenditure of $" + (s)  + "";

        for(int i=0;i<priceData.size();i++)
        {
            graphData.add(new Entry(i+1,priceData.get(i)));
            lds = new LineDataSet(graphData,str);
            lds.setDrawCircles(false);
            lds.setColor(Color.BLUE);
            lds1.add(lds);
            ld = new LineData(lds);
            lineChart.setData(ld);
        }
        lineChart.setVisibleXRangeMaximum(65f);
        lineChart.animateY(5000);
    }
}
