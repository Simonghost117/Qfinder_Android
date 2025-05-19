package com.sena.qfinder.model;

public class Registro {
    private String titulo;
    private String fecha;
    private long timestamp;

    public Registro(String titulo, String fecha, long timestamp) {
        this.titulo = titulo;
        this.fecha = fecha;
        this.timestamp = timestamp;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getFecha() {
        return fecha;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
