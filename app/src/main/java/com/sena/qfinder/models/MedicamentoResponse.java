package com.sena.qfinder.models;

public class MedicamentoResponse {
    private String message;
    private String nombre;
    private String tipo;
    private String descripcion;

    public String getMessage() {
        return message;
    }

    public MedicamentoResponse(String nombre, String tipo, String descripcion) {
        //this.message = message;
        this.nombre = nombre;
        this.tipo = tipo;
        this.descripcion = descripcion;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }
}
