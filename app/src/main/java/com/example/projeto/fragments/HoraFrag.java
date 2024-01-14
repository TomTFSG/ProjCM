package com.example.projeto.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.MainActivity;
import com.example.projeto.R;

import java.util.Calendar;

public class HoraFrag extends Fragment {

    FeedReaderDbHelper dbHelper;
    AlarmManager alarmManager;
    SQLiteDatabase db;
    private PendingIntent pendingIntent;

    public HoraFrag() {
        // Required empty public constructor
    }

    private void changeSelected(TextView v){
        Typeface MontserratAlternates_SEMIBOLD = Typeface.createFromAsset(
                getActivity().getAssets(),
                "fonts/MontserratAlternates-SemiBold.ttf"
        );

        v.setTextColor(getResources().getColor(R.color.verdeescuro));
        v.setTypeface(MontserratAlternates_SEMIBOLD);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_horafrag, container, false);
        MainActivity main=(MainActivity) getActivity();
        alarmManager = main.alarmManager;
        pendingIntent = main.pendingIntent;

        //////////////////////////////////////////////////
        // Number Pickers
        //HORAS
        NumberPicker hPicker = view.findViewById(R.id.numPickerHora);  // Replace with your NumberPicker ID
        hPicker.setMinValue(0);
        hPicker.setMaxValue(23);
        //MINUTOS
        NumberPicker mPicker = view.findViewById(R.id.numPickerMin);  // Replace with your NumberPicker ID
        mPicker.setMinValue(0);
        mPicker.setMaxValue(59);





        //////////////////////////////////////////////////
        // Voltar para trás (botão de save)
        Button saveBtn = view.findViewById(R.id.buttonSaveTime);
        saveBtn.setOnClickListener(v -> {
            int h = hPicker.getValue();
            int m = mPicker.getValue();

            dbHelper = new FeedReaderDbHelper(getContext());
            dbHelper.setHoras(h, m);
            updateAlarmTime(h,m);
            getActivity().getSupportFragmentManager().popBackStack();
        });


        Button back=view.findViewById(R.id.buttonBack);
        back.setOnClickListener(v -> getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, PlantaFrag.class,null)
                .addToBackStack(null)
                .commit()
        );
        return view;
    }
    private void updateAlarmTime(int h, int m) {
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
