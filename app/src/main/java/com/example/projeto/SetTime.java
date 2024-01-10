package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.NumberPicker;
import androidx.fragment.app.Fragment;

import java.util.Calendar;

public class SetTime extends Fragment {

    FeedReaderDbHelper dbHelper;
    AlarmManager alarmManager;
    SQLiteDatabase db;
    private PendingIntent pendingIntent;

    public SetTime() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_hora, container, false);
        MainActivity main=(MainActivity) getActivity();
        alarmManager=main.alarmManager;
        pendingIntent = main.pendingIntent;
        //HORAS
        NumberPicker hPicker = view.findViewById(R.id.numPickerHora);  // Replace with your NumberPicker ID
        hPicker.setMinValue(0);
        hPicker.setMaxValue(23);
        //MINUTOS
        NumberPicker mPicker = view.findViewById(R.id.numPickerMin);  // Replace with your NumberPicker ID
        mPicker.setMinValue(0);
        mPicker.setMaxValue(59);
        
        Button saveB=view.findViewById(R.id.buttonSaveTime);
        saveB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int h=hPicker.getValue();
                int m=mPicker.getValue();

                dbHelper = new FeedReaderDbHelper(getContext());
                db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(FeedReaderDbHelper.COLUMN_NAME_HORAS, h);
                values.put(FeedReaderDbHelper.COLUMN_NAME_MINUTOS, m);

                String selection = FeedReaderDbHelper.COLUMN_NAME_ATUAL + " = ?";
                String[] selectionArgs = { "1" };

                int rowsUpdated = db.update(
                        FeedReaderDbHelper.TABLE_NAME,
                        values,
                        selection,
                        selectionArgs
                );

                if (rowsUpdated > 0) {
                    Log.i(TAG, "Values updated successfully");
                } else {
                    Log.e(TAG, "Error updating values in the database");
                }


                Log.i("HORAS","HORARIO MARCADO PARA AS: "+h+":"+m);
                updateAlarmTime(h,m);
                String[] projection = {
                        FeedReaderDbHelper.COLUMN_NAME_ATUAL
                };

                Cursor cursor = db.query(
                        FeedReaderDbHelper.TABLE_NAME,
                        projection,
                        null,
                        null,
                        null,
                        null,
                        null
                );
                Double type=0.0;
                if (cursor != null && cursor.moveToFirst()) {
                    int columnIndex = cursor.getColumnIndexOrThrow(FeedReaderDbHelper.COLUMN_NAME_ATUAL);
                    type = cursor.getDouble(columnIndex);
                    cursor.close();
                } else {
                    Log.e(TAG, "Error reading value from the database");
                }
                if(type.equals(0)) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, Seco.class, null)
                            .addToBackStack(null)
                            .commit();
                }else if(type.equals(1)) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, Neutro.class, null)
                            .addToBackStack(null)
                            .commit();
                }else if(type.equals(2)) {
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, Humido.class, null)
                            .addToBackStack(null)
                            .commit();
                }
            }
        });

        return view;
    }
    private void updateAlarmTime(int h, int m) {
        // Update alarm time logic here
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, h);
        calendar.set(Calendar.MINUTE, m);
        if (calendar.getTimeInMillis() <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        long triggerTime = calendar.getTimeInMillis();
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
    }
}
