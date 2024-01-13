package com.example.projeto.fragments;


import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.MainActivity;
import com.example.projeto.R;
import com.example.projeto.viewmodels.SharedViewModel;



public class PlantaFrag extends Fragment {
    SharedViewModel sharedViewModel;

    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;

    public PlantaFrag() {
        // Required empty public constructor
    }

    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getWritableDatabase();
    }

    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        MainActivity main = (MainActivity) getActivity();
        sharedViewModel = main.sharedViewModel;// Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_plantafrag, container, false);

        ////////////////////////////////////////////////////////////////////////////////
        // ir buscar o tipo da planta
        int tipoPlanta = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_ATUAL);

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
        TextView tHoras = view.findViewById(R.id.Horas);
        int hora = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_HORAS);
        int minutos = dbHelper.getValueFromColumnName(FeedReaderDbHelper.COLUMN_NAME_MINUTOS);

        String horarioString = hora + ":" + ((minutos < 10) ? "0" : "") + minutos + "h";
        tHoras.setText(horarioString);
        Log.w("H",horarioString);


        ////////////////////////////////////////////////////////////////////////////////
        // ir buscar os textos presentes nos retangulos que informam sobre o ambiente
        TextView textHumidade = view.findViewById(R.id.humi);
        TextView textLuz = view.findViewById(R.id.luz);
        TextView textTemperatura = view.findViewById(R.id.temp);


                ////////////////////
                // observer relativo aos textos presentes nos retangulos que informam sobre o ambiente
                sharedViewModel.getHumidade().observe(getViewLifecycleOwner(), humidadeValue -> {
                    textHumidade.setText(String.valueOf(humidadeValue));
                });

                sharedViewModel.getLuz().observe(getViewLifecycleOwner(), luzValue -> {
                    textLuz.setText(String.valueOf(luzValue));
                });

                sharedViewModel.getTemperatura().observe(getViewLifecycleOwner(), temperaturaValue -> {
                    textTemperatura.setText(String.valueOf(temperaturaValue));
                });


        ////////////////////
        // botão para escolher a hora

        Button gotoHorario = view.findViewById(R.id.btnGotoHorario);
        gotoHorario.setOnClickListener(v -> getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, HoraFrag.class,null)
                .addToBackStack(null)
                .commit()
        );


        ////////////////////
        // botão para ir ao gráfico de info

        ImageButton hist = view.findViewById(R.id.imageButtonHistorico);
        hist.setOnClickListener(v -> getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, Historico.class,null)
                .addToBackStack(null)
                .commit()
        );

        return view;
    }
}