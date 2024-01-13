package com.example.projeto.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.projeto.R;


public class Landing extends Fragment {


    public Landing() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_landing, container, false);
        Button start = view.findViewById(R.id.ButtonS);
        start.setOnClickListener(v -> getActivity().getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.frame, Menu.class, null)
                .addToBackStack(null)
                .commit()
        );
        // Inflate the layout for this fragment
        return view;
    }
}