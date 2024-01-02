package com.example.projeto;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Seco extends Fragment {
    SharedViewModel sharedViewModel;


    public Seco() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        MainActivity main=(MainActivity) getActivity();
        View view=inflater.inflate(R.layout.fragment_humido, container, false);

        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);


        TextView tHumi=view.findViewById(R.id.humi);
        TextView tLuz=view.findViewById(R.id.luz);
        TextView tTemp=view.findViewById(R.id.temp);
        // Observe changes and update UI
        sharedViewModel.getHumidade().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double newHumidade) {
                tHumi.setText(Double.toString(newHumidade));
            }
        });

        sharedViewModel.getLuz().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double newLuz) {
                tLuz.setText(Double.toString(newLuz));
            }
        });

        sharedViewModel.getTemperatura().observe(getViewLifecycleOwner(), new Observer<Double>() {
            @Override
            public void onChanged(Double newTemperatura) {
                tTemp.setText(Double.toString(newTemperatura));
            }
        });
        return view;
    }
}