package com.sena.qfinder.models;

import com.google.gson.annotations.SerializedName;

public class CitaMedica {

    @SerializedName("id_cita")
    private int id_cita;

    @SerializedName("id_paciente")
    private int id_paciente;

    @SerializedName("fecha_cita")
    private String fecha_cita;

    @SerializedName("motivo_cita")
    private String motivo_cita;

    @SerializedName("resultado_consulta")
    private String resultado_consulta;

    @SerializedName("estado_cita")
    private String estado_cita;

    // Nuevos campos agregados
    @SerializedName("titulo")
    private String titulo;

    @SerializedName("descripcion")
    private String descripcion;

    @SerializedName("tipo")
    private String tipo;

    @SerializedName("recordar_un_dia_antes")
    private boolean recordar_un_dia_antes;

    @SerializedName("recordar_mismo_dia")
    private boolean recordar_mismo_dia;

    // Constructor vacío
    public CitaMedica() {}

    // Constructor con todos los campos antiguos
    public CitaMedica(int id_paciente, String fecha_cita, String motivo_cita, String resultado_consulta, String estado_cita) {
        this.id_paciente = id_paciente;
        this.fecha_cita = fecha_cita;
        this.motivo_cita = motivo_cita;
        this.resultado_consulta = resultado_consulta;
        this.estado_cita = estado_cita;
    }

    // Getters y Setters

    public int getIdCita() {
        return id_cita;
    }

    public void setIdCita(int id_cita) {
        this.id_cita = id_cita;
    }

    public int getIdPaciente() {
        return id_paciente;
    }

    public void setIdPaciente(int id_paciente) {
        this.id_paciente = id_paciente;
    }

    public String getFechaCita() {
        return fecha_cita;
    }

    public void setFechaCita(String fecha_cita) {
        this.fecha_cita = fecha_cita;
    }

    public String getMotivoCita() {
        return motivo_cita;
    }

    public void setMotivoCita(String motivo_cita) {
        this.motivo_cita = motivo_cita;
    }

    public String getResultadoConsulta() {
        return resultado_consulta;
    }

    public void setResultadoConsulta(String resultado_consulta) {
        this.resultado_consulta = resultado_consulta;
    }

    public String getEstadoCita() {
        return estado_cita;
    }

    public void setEstadoCita(String estado_cita) {
        this.estado_cita = estado_cita;
    }

    // Nuevos Getters y Setters

    public String getTitulo() {
        return titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public boolean isRecordar_un_dia_antes() {
        return recordar_un_dia_antes;
    }

    public void setRecordar_un_dia_antes(boolean recordar_un_dia_antes) {
        this.recordar_un_dia_antes = recordar_un_dia_antes;
    }

    public boolean isRecordar_mismo_dia() {
        return recordar_mismo_dia;
    }

    public void setRecordar_mismo_dia(boolean recordar_mismo_dia) {
        this.recordar_mismo_dia = recordar_mismo_dia;
    }
}
