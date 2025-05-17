package com.sena.qfinder.models;

import java.util.List;

public class PacienteListResponse {
    private List<PacienteResponse> data; // o "pacientes" según lo que devuelva tu API

    // Constructor vacío
    public PacienteListResponse() {
    }

    // Getter y Setter
    public List<PacienteResponse> getData() {
        return data;
    }

    public void setData(List<PacienteResponse> data) {
        this.data = data;
    }
}