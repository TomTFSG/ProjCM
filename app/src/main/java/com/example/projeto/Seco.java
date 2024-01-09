package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

public class Seco extends Fragment {
    SharedViewModel sharedViewModel;

    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;

    public Seco() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = new Intent(MessageReceiver.ACTION_UPDATE_TYPE);
        intent.putExtra("newTemperatureValue", 0);
        getActivity().sendBroadcast(intent);
        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getWritableDatabase(); // Use getWritableDatabase() instead of getReadableDatabase()
        ContentValues values = new ContentValues();
        values.put(FeedReaderDbHelper.COLUMN_NAME_ATUAL, 0); // Use an appropriate integer value
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
        View view=inflater.inflate(R.layout.fragment_seco, container, false);

        TextView tHumi=view.findViewById(R.id.humi);
        TextView tLuz=view.findViewById(R.id.luz);
        TextView tTemp=view.findViewById(R.id.temp);

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

}