package com.example.finance_geek;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.util.ArrayList;
import java.util.List;

public class ReportGraph extends Fragment {

    float prices[] = {10,20,30,50,60,20,45,12,28,40,18,15};
    String months[] = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    PieChart chartView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1_report, container, false);
        chartView = (PieChart) rootView.findViewById(R.id.chart);
        setupPieCharts();
        return rootView;
    }

    private void setupPieCharts(){

        List<PieEntry> pieEntry = new ArrayList<>();

        for(int i = 0; i < prices.length; i++)
        {
            pieEntry.add(new PieEntry(prices[i], months[i]));
        }

        PieDataSet dataSet = new PieDataSet(pieEntry, "Prices for food");
        PieData data = new PieData(dataSet);
        chartView.setData(data);
        chartView.invalidate();
    }
}
