package com.example.projeto.viewmodels;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SharedViewModel extends ViewModel {
    private MutableLiveData<Double> humidade = new MutableLiveData<>();
    private MutableLiveData<Double> luz = new MutableLiveData<>();
    private MutableLiveData<Double> temperatura = new MutableLiveData<>();

    public LiveData<Double> getHumidade() {
        Log.d("Humido", "Humidade changed: " + humidade.getValue());
        return humidade;
    }

    public LiveData<Double> getLuz() {
        return luz;
    }

    public LiveData<Double> getTemperatura() {
        return temperatura;
    }
    public void setHumidade(double value) {
        humidade.setValue(value);
    }

    public void setLuz(double value) {
        luz.setValue(value);
    }

    public void setTemperatura(double value) {
        temperatura.setValue(value);
    }
}
