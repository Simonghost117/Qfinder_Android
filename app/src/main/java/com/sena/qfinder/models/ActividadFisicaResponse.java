package com.sena.qfinder.models;

public class ActividadFisicaResponse {
    private int id_actividad;
    private int id_paciente;
    private String fecha_actividad;
    private int duracion;
    private String tipo_actividad;
    private String intensidad;
    private String descripcion;
    private String estado;
    private String observaciones;

    // Getters y setters
    public int getId_actividad() { return id_actividad; }
    public int getId_paciente() { return id_paciente; }
    public String getFecha_actividad() { return fecha_actividad; }
    public int getDuracion() { return duracion; }
    public String getTipo_actividad() { return tipo_actividad; }
    public String getIntensidad() { return intensidad; }
    public String getDescripcion() { return descripcion; }
    public String getEstado() { return estado; }
    public String getObservaciones() { return observaciones; }

    public void setId_actividad(int id) { this.id_actividad = id; }
    public void setId_paciente(int id) { this.id_paciente = id; }
    public void setFecha_actividad(String fecha) { this.fecha_actividad = fecha; }
    public void setDuracion(int duracion) { this.duracion = duracion; }
    public void setTipo_actividad(String tipo) { this.tipo_actividad = tipo; }
    public void setIntensidad(String intensidad) { this.intensidad = intensidad; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setEstado(String estado) { this.estado = estado; }
    public void setObservaciones(String observaciones) { this.observaciones = observaciones; }
}

