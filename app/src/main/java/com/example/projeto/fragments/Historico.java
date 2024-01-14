package com.example.projeto.fragments;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.TextViewCompat;
import androidx.fragment.app.Fragment;

import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

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
import com.anychart.scales.Linear;
import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.R;
import com.example.projeto.misc.forHistorico.LineFromHistoryGraph;

import java.util.ArrayList;
import java.util.List;

public class Historico extends Fragment {

    List<DataEntry> DataLuz, DataTemperatura, DataHumidade;
    Cartesian line;
    AnyChartView graph;
    LineFromHistoryGraph LinhaLuz, LinhaTemperatura, LinhaHumidade;
    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    View view;

    public Historico() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DataLuz = new ArrayList<>();
        DataTemperatura = new ArrayList<>();
        DataHumidade = new ArrayList<>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_historico, container, false);

        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getReadableDatabase();

        String[] projection = {
                FeedReaderDbHelper.COLUMN_NAME_TIME,
                FeedReaderDbHelper.COLUMN_NAME_LIGHT,
                FeedReaderDbHelper.COLUMN_NAME_TEMP,
                FeedReaderDbHelper.COLUMN_NAME_HUMI,
                FeedReaderDbHelper.COLUMN_NAME_REGA
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


        int[] counters = new int[]{0, 0, 0};
        while (cursor.moveToNext()) {
            String time = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));
            for(int i = 1; i < projection.length - 1; i++){
                String coluna = projection[i];
                String valGraf = cursor.getString(cursor.getColumnIndexOrThrow(coluna));

                if(valGraf != null && time != null && counters[i - 1] < 7){
                    if(i == 1) DataLuz.add(new ValueDataEntry(time, Double.parseDouble(valGraf)));
                    else if(i == 2) DataHumidade.add(new ValueDataEntry(time, Double.parseDouble(valGraf)));
                    else if(i == 3) DataTemperatura.add(new ValueDataEntry(time, Double.parseDouble(valGraf)));

                    counters[i - 1]++;
                }
            }

            String valRega = cursor.getString(cursor.getColumnIndexOrThrow(projection[4]));
            if(valRega != null && time != null) {
                LinearLayout infoRega = view.findViewById(R.id.infoRega);
                LinearLayout regarLayout = new LinearLayout(getContext());
                regarLayout.setPadding(24, 24, 24, 24);
                regarLayout.setBackgroundColor(getResources().getColor(R.color.white));
                LinearLayout.LayoutParams linearParams = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.FILL_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                linearParams.setMargins(64, 16, 64, 16);

                regarLayout.setLayoutParams(linearParams);
                regarLayout.setOrientation(LinearLayout.HORIZONTAL);

                // horario e rega (dL)
                String[] texts = new String[]{time, valRega + "dL"};

                for(int i = 0; i < texts.length; i++) {
                    TextView textView = new TextView(getContext());
                    LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT
                    );
                    textParams.setMargins(0, 0, 32, 0);
                    textView.setLayoutParams(textParams);
                    textView.setText(texts[i]);
                    textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                    regarLayout.addView(textView);
                }

                infoRega.addView(regarLayout);
            }
        }
        cursor.close();





        String[] projectionRega= {
                FeedReaderDbHelper.COLUMN_NAME_TIME,
                FeedReaderDbHelper.COLUMN_NAME_REGA
        };
        Cursor cursorRega = db.query(
                FeedReaderDbHelper.TABLE_NAME,
                projectionRega,
                null, // Remove the selection and selectionArgs
                null,
                null,
                null,
                sortOrder
        );

        while (cursorRega.moveToNext()) {
                /*
            String val = cursor.getString(cursor.getColumnIndexOrThrow(projection[1]));

            String time = cursor.getString(cursor.getColumnIndexOrThrow(projection[0]));

            if(val != null && time != null) {
                LinearLayout infoRega = view.findViewById(R.id.infoRega);
                LinearLayout regarLayout = new LinearLayout(getContext());
                ViewGroup.LayoutParams linearParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.FILL_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                regarLayout.setLayoutParams(linearParams);
                regarLayout.setOrientation(LinearLayout.HORIZONTAL);

                TextView dataRega = new TextView(getContext());
                TextView nRega = new TextView(getContext());
                ViewGroup.LayoutParams textParams = new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                dataRega.setLayoutParams(textParams);
                nRega.setLayoutParams(textParams);


                dataRega.setText("Data Aqui");
                dataRega.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 20);
                nRega.setText("000dL");
                regarLayout.addView(dataRega);
                regarLayout.addView(nRega);
                infoRega.addView(regarLayout);
            }
             */
        }
        cursorRega.close();



        setupChart();


        //Botao p tras

        Button back=view.findViewById(R.id.buttonBack);
        back.setOnClickListener(v -> getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, PlantaFrag.class,null)
                .addToBackStack(null)
                .commit()
        );
        return view;
    }
    private void setupChart(){

        graph = view.findViewById(R.id.temp);
        graph.setBackgroundColor("#1a2b25");


        line = AnyChart.line();
        line.background().fill("#1a2b25");
        line.animation(false);

        line.crosshair()
                .enabled(true)
                .yLabel(false)
                .yStroke((Stroke) null, null, null, (String) null, (String) null);


        line.xAxis(0).labels().padding(5d, 5d, 5d, 5d);

        LinhaLuz = new LineFromHistoryGraph(
                "#D0D9AF",
                "Light (%)",
                "{ x: 'x', value: 'light' }",
                DataLuz
        );
        LinhaLuz.doLineOnCartesian(line);
        //
        LinhaTemperatura = new LineFromHistoryGraph(
                "#FAB0B9",
                "Temperature (ºC)",
                "{ x: 'x', value: 'temperature' }",
                DataTemperatura
        );
        LinhaTemperatura.doLineOnCartesian(line);
        //
        LinhaHumidade = new LineFromHistoryGraph(
                "#495B30",
                "Soil Moisture (%)",
                "{ x: 'x', value: 'soilmoist' }",
                DataHumidade
        );
        LinhaHumidade.doLineOnCartesian(line);
        /*
        Set set = Set.instantiate();
        set.data(temperatureData);
        Set setH = Set.instantiate();
        setH.data(humidityData);
        Mapping series1Mapping = set.mapAs("{ x: 'x', value: 'temperature' }");
        Mapping series2Mapping = setH.mapAs("{ x: 'x', value: 'humidade' }");
        series1 = line.line(series1Mapping);
        series1.name("Temperatura (ºC)");
        series1.stroke("#FAB0B9");
        series1.hovered().markers().enabled(true);
        series1.hovered().markers()
                .type(MarkerType.CIRCLE)
                .size(4d);
        series1.tooltip()
                .position("right")
                .anchor(Anchor.LEFT_CENTER)
                .offsetX(5d)
                .offsetY(5d);

        /////////////////////////////////
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

         */
        line.legend().enabled(true);
        line.legend().fontSize(13d);
        line.legend().fontColor("white");
        line.legend().padding(0d, 0d, 10d, 0d);


        graph.setChart(line);
    }
}