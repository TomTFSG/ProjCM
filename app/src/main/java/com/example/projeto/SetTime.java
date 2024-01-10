package com.example.projeto;

import android.app.TimePickerDialog;
import android.os.Bundle;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.fragment.app.Fragment;


public class SetTime extends Fragment{
    Double numPickerMin;
    Double numPickerSec;
    public SetTime() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // setContentView(R.layout.fragment_hora)
    int hourOfDay=23;
    int minute=55;
    boolean is24HourView=true;

  _timePickerDialog=new TimePickerDialog(this,android.R.style.Theme_Holo_Dialog, new TimePickerDialog.OnTimeSetListener(){
 
      public void onTimeSet(TimePicker timePicker, int iHora , int iMinut){
          _editTextTime.setText(iHora + ":" + iMinut);
          Toast.makeText(getContext(), "i=" + iHora + "i1=" + iMinut, Toast.LENGTH_SHORT).show();
      }

  },hourOfDay , minute, is24HourView ) ;

  _timePickerDialog.show();


    }


    }


