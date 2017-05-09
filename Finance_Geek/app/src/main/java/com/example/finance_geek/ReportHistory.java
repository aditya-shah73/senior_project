package com.example.finance_geek;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ReportHistory extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.tab2_report_history, container, false);
        chartView = (PieChart) rootView.findViewById(R.id.chart);
        setupPieCharts();
        return rootView;
    }

    PieChart chartView;
    ArrayList<Float> totalPriceData = new ArrayList<>();
    ArrayList<Date> totalDateData = new ArrayList<>();

    private void setupPieCharts() {
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

        for(int i = 0; i < totalPriceData.size(); i++)
        {
            pieEntry.add(new PieEntry(totalPriceData.get(i), df.format(totalDateData.get(i))));
        }

        if(!(pieEntry.isEmpty()))
        {
            PieDataSet dataSet = new PieDataSet(pieEntry, "");
            PieData data = new PieData(dataSet);
            chartView.setData(data);
            chartView.invalidate();
            chartView.setDrawEntryLabels(true);
            chartView.animateY(5000);
            data.setValueTextSize(8f);

            ArrayList<Integer> colors = new ArrayList<>();

            for (int c : ColorTemplate.PASTEL_COLORS)
                colors.add(c);

            colors.add(ColorTemplate.getHoloBlue());
            dataSet.setColors(colors);
        }
        else
        {
            chartView.setNoDataText("No purchase history!");
            Paint p = chartView.getPaint(PieChart.PAINT_INFO);
            p.setTextSize(60);
        }
    }
}
