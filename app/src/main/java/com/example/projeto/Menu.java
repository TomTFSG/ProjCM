package com.example.projeto;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;


public class Menu extends Fragment {

    public Menu() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_menu, container, false);
        ImageButton humido=view.findViewById(R.id.humido);
        Button neutro=view.findViewById(R.id.neutro);
        Button seco=view.findViewById(R.id.seco);

        humido.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, Humido.class,null)
                        .addToBackStack(null)
                        .commit();
            }
        });
        neutro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i(TAG,"CLICKED");
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, Neutro.class,null)
                        .addToBackStack(null)
                        .commit();
            }
        });
        seco.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frame, Seco.class,null)
                        .addToBackStack(null)
                        .commit();
            }
        });

        return view;
    }
}