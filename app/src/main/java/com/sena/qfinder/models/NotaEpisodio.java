package com.sena.qfinder.models;

import com.google.gson.annotations.SerializedName;

public class NotaEpisodio {

    @SerializedName("id_episodio")
    private int id;

    @SerializedName("id_paciente")
    private int idPaciente;

    @SerializedName("fecha_hora_inicio")
    private String fechaHoraInicio;

    @SerializedName("fecha_hora_fin")
    private String fechaHoraFin;

    @SerializedName("severidad")
    private String severidad;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("intervenciones")
    private String intervenciones;

    @SerializedName("registrado_por")
    private String registradoPor;

    public NotaEpisodio(int id, int idPaciente, String fechaHoraInicio, String fechaHoraFin, String severidad, String descripcion, String intervenciones, String registradoPor) {
        this.id = id;
        this.idPaciente = idPaciente;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.intervenciones = intervenciones;
        this.registradoPor = registradoPor;
    }

    public NotaEpisodio(int idPaciente, String fechaHoraInicio, String fechaHoraFin, String severidad, String descripcion, String intervenciones, String registradoPor) {
        this.idPaciente = idPaciente;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.intervenciones = intervenciones;
        this.registradoPor = registradoPor;
    }

    // Getters y Setters (sin cambios)
    // ...
}
