package com.example.projeto.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.MainActivity;
import com.example.projeto.R;
import com.example.projeto.viewmodels.SharedViewModel;


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
        db = dbHelper.getWritableDatabase();
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        MainActivity main = (MainActivity) getActivity();
        sharedViewModel = main.sharedViewModel;// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_humido, container, false);

        ////////////////////////////////////////////////////////////////////////////////
        // ir buscar o tipo da planta
        String tipoPlantaColumn = FeedReaderDbHelper.COLUMN_NAME_ATUAL;
        String[] tipoPlantaProjection = {tipoPlantaColumn};
        int tipoPlanta = getValueFromDatabase(db, tipoPlantaProjection, tipoPlantaColumn);

        ////////////////////////////////////////////////////////////////////////////////
        // imagem da planta em causa (consoante o tipo)
        ImageView imagemPlanta = view.findViewById(R.id.imagemPlanta);
        imagemPlanta.setImageResource(
                (tipoPlanta == 0) ?
                R.drawable.img_seco :
                        (tipoPlanta == 1) ?
                                R.drawable.img_neutro :
                                R.drawable.img_humido
        );

        ////////////////////////////////////////////////////////////////////////////////
        // titulo do fragmento (informar tipo da planta)
        TextView tituloFrag = view.findViewById(R.id.tituloFrag);
        tituloFrag.setText(getResources().getString(
                (tipoPlanta == 0) ?
                        R.string.moist_soil_plant:
                        (tipoPlanta == 1) ?
                                R.string.neutral_soil_plant :
                                R.string.humid_soil_plant
        ));


        ////////////////////////////////////////////////////////////////////////////////
        // settar as horas
        TextView tHoras=view.findViewById(R.id.Horas);
        String[] horasProjection = {FeedReaderDbHelper.COLUMN_NAME_HORAS};
        int hora = getValueFromDatabase(db, horasProjection, FeedReaderDbHelper.COLUMN_NAME_HORAS);
        String[] minutosProjection = {FeedReaderDbHelper.COLUMN_NAME_MINUTOS};
        int minutos = getValueFromDatabase(db, minutosProjection, FeedReaderDbHelper.COLUMN_NAME_MINUTOS);

        tHoras.setText(hora+":"+minutos+"h");
        Log.w("H",hora+":"+minutos+"h");


        ////////////////////////////////////////////////////////////////////////////////
        // ir buscar os textos presentes nos retangulos que informam sobre o ambiente
        TextView textHumidade = view.findViewById(R.id.humi);
        TextView textLuz = view.findViewById(R.id.luz);
        TextView textTemperatura = view.findViewById(R.id.temp);


                ////////////////////
                // observer relativo aos textos presentes nos retangulos que informam sobre o ambiente
                sharedViewModel.getHumidade().observe(getViewLifecycleOwner(), new Observer<Double>() {
                    @Override
                    public void onChanged(Double humidadeValue) {
                        textHumidade.setText(String.valueOf(humidadeValue));
                    }
                });

                sharedViewModel.getLuz().observe(getViewLifecycleOwner(), new Observer<Double>() {
                    @Override
                    public void onChanged(Double luzValue) {
                        // Update the TextView with the new luzValue
                        textLuz.setText(String.valueOf(luzValue));
                    }
                });

                sharedViewModel.getTemperatura().observe(getViewLifecycleOwner(), new Observer<Double>() {
                    @Override
                    public void onChanged(Double temperaturaValue) {
                        // Update the TextView with the new temperaturaValue
                        textTemperatura.setText(String.valueOf(temperaturaValue));
                    }
                });


        ////////////////////
        // botão para escolher a hora

        ImageButton edit = view.findViewById(R.id.imageButton);
        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, SetTime.class,null)
                        .addToBackStack(null)
                        .commit();
            }
        });


        ////////////////////
        // botão para ir ao gráfico de info

        ImageButton hist=view.findViewById(R.id.imageButtonHistorico);
        hist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, Historico.class,null)
                        .addToBackStack(null)
                        .commit();
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