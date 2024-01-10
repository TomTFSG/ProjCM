package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;


public class Humido extends Fragment {
    SharedViewModel sharedViewModel;

    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;

    public Humido() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getWritableDatabase(); // Use getWritableDatabase() instead of getReadableDatabase()
        ContentValues values = new ContentValues();
        values.put(FeedReaderDbHelper.COLUMN_NAME_ATUAL, 2); // Use an appropriate integer value
        long newRowId = db.replace(FeedReaderDbHelper.TABLE_NAME, null, values);
        if (newRowId != -1) {
            Log.i(TAG, "Value inserted successfully with ID: " + newRowId);
        } else {
            Log.e(TAG, "Error inserting value into the database");
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity main=(MainActivity) getActivity();
        sharedViewModel = main.sharedViewModel;// Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_humido, container, false);

        TextView tHumi=view.findViewById(R.id.humi);
        TextView tLuz=view.findViewById(R.id.luz);
        TextView tTemp=view.findViewById(R.id.temp);


        TextView tHoras=view.findViewById(R.id.Horas);
        String[] horasProjection = {FeedReaderDbHelper.COLUMN_NAME_HORAS};
        int hora = getValueFromDatabase(db, horasProjection, FeedReaderDbHelper.COLUMN_NAME_HORAS);
        String[] minutosProjection = {FeedReaderDbHelper.COLUMN_NAME_MINUTOS};
        int minutos = getValueFromDatabase(db, minutosProjection, FeedReaderDbHelper.COLUMN_NAME_MINUTOS);

        tHoras.setText(hora+":"+minutos+"h");
        Log.w("H",hora+":"+minutos+"h");
        sharedViewModel.getHumidade().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double humidadeValue) {
                tHumi.setText(String.valueOf(humidadeValue));
            }
        });

        sharedViewModel.getLuz().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double luzValue) {
                // Update the TextView with the new luzValue
                tLuz.setText(String.valueOf(luzValue));
            }
        });

        sharedViewModel.getTemperatura().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double temperaturaValue) {
                // Update the TextView with the new temperaturaValue
                tTemp.setText(String.valueOf(temperaturaValue));
            }
        });

        ImageButton edit =view.findViewById(R.id.imageButton);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, SetTime.class,null)
                        .addToBackStack(null)
                        .commit();
                    /*
                    JSONObject payload = new JSONObject();
                    payload.put("type", 2);
                    payload.put("temp", tTemp.getText().toString());
                    payload.put("humi", tHumi.getText().toString());
                    payload.put("light", tLuz.getText().toString());
                    String jsonS = payload.toString();
                    msgRec.helper.publish("water", jsonS);*/
            }
        });

        return view;
    }

    private int getValueFromDatabase(SQLiteDatabase db, String[] projection, String columnName) {
        Cursor cursor = db.query(
                FeedReaderDbHelper.TABLE_NAME,
                projection,
                null,
                null,
                null,
                null,
                null
        );

        int value = 0;

        if (cursor != null && cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(columnName);
            value = cursor.getInt(columnIndex);
            cursor.close();
        } else {
            Log.e(TAG, "Error reading value from the database");
        }
        String a=Integer.toString(value);
        Log.e(columnName,a);
        return value;
    }

}