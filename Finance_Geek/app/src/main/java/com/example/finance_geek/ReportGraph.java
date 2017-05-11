package com.example.finance_geek;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

public class ReportGraph extends Fragment {
    private PieChart chartView;
    private ArrayList<Float> totalPriceData = new ArrayList<>();
    private ArrayList<Date> totalDateData = new ArrayList<>();
    private ArrayList<String> weekly = new ArrayList<>();
    private ArrayList<Float> weekly_price = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1_report, container, false);
        chartView = (PieChart) rootView.findViewById(R.id.chart);
        try {
            setupPieCharts();
        }
        catch (ParseException e) {
            e.printStackTrace();
        }
        return rootView;
    }

    private void setupPieCharts() throws ParseException {
        HomePage activity = new HomePage();
        Map<Date, Double> data_price_date = activity.getPriceDateData();
        DateFormat df = new SimpleDateFormat("MMM dd, yyyy");
        for(Map.Entry<Date, Double> entry : data_price_date.entrySet())
        {
            double value = entry.getValue();
            totalDateData.add(entry.getKey());
            totalPriceData.add((float) value);
        }

        List<PieEntry> pieEntry = new ArrayList<>();
        int j = 0;
        for(int i = 0; i < totalPriceData.size(); i++)
        {
            Date dateobj = new Date();
            Calendar cal = new GregorianCalendar();
            cal.add(Calendar.DAY_OF_MONTH, -7);
            Date sevenDaysAgo = cal.getTime();
            String today = df.format(dateobj);
            String week = df.format(sevenDaysAgo);
            if(df.format(totalDateData.get(i)).equals(week))
            {
                j = i;
            }
        }

        for(int a = j ; a<totalPriceData.size();a++)
        {
            weekly.add(df.format(totalDateData.get(a)));
            weekly_price.add(totalPriceData.get(a));
        }

        for(int a = 0;a<weekly_price.size();a++)
        {
            pieEntry.add(new PieEntry(weekly_price.get(a), weekly.get(a)));
        }

        if(!(pieEntry.isEmpty()))
        {
            PieDataSet dataSet = new PieDataSet(pieEntry, "");
            PieData data = new PieData(dataSet);
            chartView.setData(data);
            chartView.invalidate();
            chartView.setDrawEntryLabels(true);
            chartView.animateY(5000);
            data.setValueTextSize(12f);

            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.COLORFUL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());
            dataSet.setColors(colors);
        }
        else
        {
            chartView.setNoDataText("No purchases this week!");
            Paint p = chartView.getPaint(PieChart.PAINT_INFO);
            p.setTextSize(60);
        }
    }
}
