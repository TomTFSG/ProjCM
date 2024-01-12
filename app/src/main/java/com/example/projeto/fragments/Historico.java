package com.example.projeto.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Cartesian;
import com.anychart.core.cartesian.series.Line;
import com.anychart.data.Mapping;
import com.anychart.data.Set;
import com.anychart.enums.Anchor;
import com.anychart.enums.MarkerType;
import com.anychart.enums.TooltipPositionMode;
import com.anychart.graphics.vector.Stroke;
import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.R;

import java.util.ArrayList;
import java.util.List;

public class Historico extends Fragment {

    List<DataEntry> humidityData;
    List<DataEntry> temperatureData;  // Initialize temperatureData
    Cartesian line;
    AnyChartView graph;
    Line series1;
    Line series2;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    View view;

    public Historico() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        temperatureData = new ArrayList<>();
        humidityData = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_historico, container, false);

        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getReadableDatabase();

        String[] projection = {
                FeedReaderDbHelper.COLUMN_NAME_TIME,
                FeedReaderDbHelper.COLUMN_NAME_TEMP,
                FeedReaderDbHelper.COLUMN_NAME_HUMI
        };

        String sortOrder = FeedReaderDbHelper.COLUMN_NAME_TIME + " DESC";

        Cursor cursor = db.query(
                FeedReaderDbHelper.TABLE_NAME,
                projection,
                null, // Remove the selection and selectionArgs
                null,
                null,
                null,
                sortOrder
        );

        while (cursor.moveToNext()) {
            String time = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderDbHelper.COLUMN_NAME_TIME));
            String temp = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderDbHelper.COLUMN_NAME_TEMP));
            String humi = cursor.getString(cursor.getColumnIndexOrThrow(FeedReaderDbHelper.COLUMN_NAME_HUMI));

            if (temp != null) {
                temperatureData.add(new ValueDataEntry(time, Double.parseDouble(temp)));
            }
            if (humi != null) {
                humidityData.add(new ValueDataEntry(time, Double.parseDouble(humi)));
            }
        }
        cursor.close();
        setupChart();

        return view;
    }
    private void setupChart(){

        graph = view.findViewById(R.id.temp);
        graph.setBackgroundColor("black");


        line = AnyChart.line();
        line.background().fill("#2B2B2B");
        line.animation(false);

        line.padding(10d, 20d, 5d, 20d);

        line.crosshair().enabled(true);
        line.crosshair()
                .yLabel(true)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);

        line.tooltip().positionMode(TooltipPositionMode.POINT);
        line.title("Temperatura e Humidade");
        line.title().fontColor("white");
        line.yAxis(0).title("Valor");
        line.yAxis(0).title().fontColor("white");
        line.xAxis(0).labels().padding(5d, 5d, 5d, 5d);
        line.xAxis(0).labels().fontColor("white");

        Set set = Set.instantiate();
        set.data(temperatureData);
        Set setH = Set.instantiate();
        setH.data(humidityData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'temperature' }");
        Mapping series2Mapping = setH.mapAs("{ x: 'x', value: 'humidade' }");
        series1 = line.line(series1Mapping);
        series1.name("Temperatura (ºC)");
        series1.stroke("aquamarine");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        series2 = line.line(series2Mapping);
        series2.name("Humidade (%)");
        series2.stroke("magenta");
        series2.hovered().markers().enabled(true);
        series2.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series2.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);
        line.legend().enabled(true);
        line.legend().fontSize(13d);
        line.legend().fontColor("white");
        line.legend().padding(0d, 0d, 10d, 0d);


        graph.setChart(line);
    }
}