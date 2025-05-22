package com.sena.qfinder.models;

import com.google.gson.annotations.SerializedName;

public class NotaEpisodio {

    @SerializedName("id_paciente")
    private int idPaciente;

    @SerializedName("fecha_hora_inicio")
    private String fechaHoraInicio;

    @SerializedName("fecha_hora_fin")
    private String fechaHoraFin;

    private String severidad;
    private String descripcion;
    private String intervenciones;

    @SerializedName("registrado_por")
    private String registradoPor;

    public NotaEpisodio(int idPaciente, String fechaHoraInicio, String fechaHoraFin,
                        String severidad, String descripcion, String intervenciones,
                        String registradoPor) {
        this.idPaciente = idPaciente;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.severidad = severidad;
        this.descripcion = descripcion;
        this.intervenciones = intervenciones;
        this.registradoPor = registradoPor;
    }

    // Getters y setters

    public int getIdPaciente() {
        return idPaciente;
    }

    public void setIdPaciente(int idPaciente) {
        this.idPaciente = idPaciente;
    }

    public String getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public void setFechaHoraInicio(String fechaHoraInicio) {
        this.fechaHoraInicio = fechaHoraInicio;
    }

    public String getFechaHoraFin() {
        return fechaHoraFin;
    }

    public void setFechaHoraFin(String fechaHoraFin) {
        this.fechaHoraFin = fechaHoraFin;
    }

    public String getSeveridad() {
        return severidad;
    }

    public void setSeveridad(String severidad) {
        this.severidad = severidad;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getIntervenciones() {
        return intervenciones;
    }

    public void setIntervenciones(String intervenciones) {
        this.intervenciones = intervenciones;
    }

    public String getRegistradoPor() {
        return registradoPor;
    }

    public void setRegistradoPor(String registradoPor) {
        this.registradoPor = registradoPor;
    }
}
