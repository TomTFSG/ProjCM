package com.example.projeto.fragments;

import static androidx.constraintlayout.helper.widget.MotionEffect.TAG;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;

import com.example.projeto.MainActivity;
import com.example.projeto.misc.FeedReaderDbHelper;
import com.example.projeto.R;
import com.example.projeto.viewmodels.SharedViewModel;


public class Menu extends Fragment {

    FeedReaderDbHelper dbHelper;
    SQLiteDatabase db;
    SharedViewModel sharedViewModel;



    public Menu() {}

    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        dbHelper = new FeedReaderDbHelper(getContext());
        db = dbHelper.getWritableDatabase(); // Use getWritableDatabase() instead of getReadableDatabase()
    }

    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        MainActivity main = (MainActivity) getActivity();
        sharedViewModel = main.sharedViewModel;// Inflate the layout for this fragment

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_menu, container, false);

        ////////////////////////////////////////////////////////////////////////////////
        // array de imagebuttons do menu (quando o user escolhe uma planta)
        ImageButton[] btnImagemTipoPlanta = new ImageButton[]{
            view.findViewById(R.id.seco), // atual = 0
            view.findViewById(R.id.neutro), // atual = 1
            view.findViewById(R.id.humido) // atual = 2
        };

        for(int i = 0; i < btnImagemTipoPlanta.length; i++) {
            ImageButton btnImg = btnImagemTipoPlanta[i];
            final int k = i;

            btnImg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setTipoPlanta(k);
                    getActivity().getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frame, PlantaFrag.class, null)
                            .addToBackStack(null)
                            .commit();
                    }
                });
            }

        return view;
    }


    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    ///////////    ///////////    ///////////    ///////////    ///////////
    private void setTipoPlanta(int tipo){
        ContentValues values = new ContentValues();
        values.put(FeedReaderDbHelper.COLUMN_NAME_ATUAL, tipo); // Use an appropriate integer value
        long newRowId = db.update(FeedReaderDbHelper.TABLE_NAME, values, null, null);

        if (newRowId != -1) {
            Log.i(TAG, "Value inserted successfully with ID: " + newRowId);
        } else {
            Log.e(TAG, "Error inserting value into the database");
        }
    }
}