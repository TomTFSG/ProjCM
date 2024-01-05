package com.example.projeto;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.Observer;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.json.JSONException;
import org.json.JSONObject;

public class Humido extends Fragment {
    SharedViewModel sharedViewModel;
    MqttHelper helper;

    public Humido() {
        // Required empty public constructor
    }
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String brookerUri;
        String clientId;
        brookerUri="tcp://broker.hivemq.com:1883";
        clientId="userAndroid";
        helper =new MqttHelper(getContext(),brookerUri,clientId);
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

        ImageButton edit =view.findViewById(R.id.imageButton);

        edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)  {
                try {
                    JSONObject payload = new JSONObject();
                    payload.put("type", 2);
                    payload.put("temp", tTemp.getText().toString());
                    payload.put("humi", tHumi.getText().toString());
                    payload.put("light", tLuz.getText().toString());
                    String jsonS = payload.toString();
                    main.helper.publish("water", jsonS);
                }
                catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });




        return view;
    }
}