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
import com.github.mikephil.charting.utils.ColorTemplate;
import java.util.*;

public class ReportGraph extends Fragment {
    PieChart chartView;
    ArrayList<Float> totalPriceData = new ArrayList<>();
    ArrayList<String> totalDateData = new ArrayList<>();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab1_report, container, false);
        chartView = (PieChart) rootView.findViewById(R.id.chart);
        setupPieCharts();
        return rootView;
    }

    private void setupPieCharts() {
        HomePage activity = new HomePage();
        HashMap<String, Double> data_price_data = activity.getPriceDateData();

        for(Map.Entry<String, Double> entry : data_price_data.entrySet())
        {
            double value = entry.getValue();

            totalDateData.add(entry.getKey());
            totalPriceData.add((float) value);
        }

        List<PieEntry> pieEntry = new ArrayList<>();

        for(int i = 0; i < totalPriceData.size(); i++)
        {
            pieEntry.add(new PieEntry(totalPriceData.get(i), totalDateData.get(i)));
        }

        PieDataSet dataSet = new PieDataSet(pieEntry,"");
        PieData data = new PieData(dataSet);
        chartView.setData(data);
        chartView.invalidate();
        chartView.setDrawEntryLabels(true);
        chartView.animateY(5000);
        data.setValueTextSize(12f);

        ArrayList<Integer> colors = new ArrayList<>();

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());
        dataSet.setColors(colors);
    }
}
