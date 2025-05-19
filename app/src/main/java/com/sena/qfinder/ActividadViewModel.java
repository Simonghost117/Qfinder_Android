package com.sena.qfinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ActividadViewModel extends ViewModel {
    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    public void agregarActividad(Actividad actividad) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null) {
            actual.add(actividad);
            actividades.setValue(actual);
        }
    }

    public void actualizarActividad(int index, Actividad actividad) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.set(index, actividad);
            actividades.setValue(actual);
        }
    }

    public void marcarActividadComoRealizada(int index) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index);
            actividades.setValue(actual);
        }
    }

    public void eliminarActividad(int index) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index);
            actividades.setValue(actual);
        }
    }
}