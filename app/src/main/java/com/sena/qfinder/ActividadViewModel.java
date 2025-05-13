package com.sena.qfinder;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.ArrayList;
import java.util.List;

public class ActividadViewModel extends ViewModel {
    // MutableLiveData para mantener la lista de actividades
    private final MutableLiveData<List<Actividad>> actividades = new MutableLiveData<>(new ArrayList<>());

    // Getter para obtener las actividades
    public LiveData<List<Actividad>> getActividades() {
        return actividades;
    }

    // Método para agregar una nueva actividad a la lista
    public void agregarActividad(Actividad actividad) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null) {
            actual.add(actividad);
            actividades.setValue(actual);  // Actualiza el LiveData
        }
    }

    // Método para actualizar una actividad en un índice específico
    public void marcarActividadComoRealizada(int index) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index); // Elimina la actividad
            actividades.setValue(actual); // Actualiza LiveData
        }
    }

    // Método para eliminar una actividad de la lista en un índice específico
    public void eliminarActividad(int index) {
        List<Actividad> actual = actividades.getValue();
        if (actual != null && index >= 0 && index < actual.size()) {
            actual.remove(index);
            actividades.setValue(actual);  // Actualiza el LiveData
        }
    }

    // Método para verificar si la lista de actividades está vacía
    public boolean tieneActividades() {
        List<Actividad> actual = actividades.getValue();
        return actual != null && !actual.isEmpty();
    }
}
