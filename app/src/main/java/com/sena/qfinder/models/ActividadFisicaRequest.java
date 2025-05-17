package com.sena.qfinder.models;

public class ActividadFisicaRequest {
    private String fecha_actividad;
    private int duracion;
    private String tipo_actividad;
    private String intensidad;
    private String descripcion;
    private String estado;
    private String observaciones;

    public ActividadFisicaRequest(String fecha_actividad, int duracion, String tipo_actividad,
                                  String intensidad, String descripcion, String estado, String observaciones) {
        this.fecha_actividad = fecha_actividad;
        this.duracion = duracion;
        this.tipo_actividad = tipo_actividad;
        this.intensidad = intensidad;
        this.descripcion = descripcion;
        this.estado = estado;
        this.observaciones = observaciones;
    }

}
