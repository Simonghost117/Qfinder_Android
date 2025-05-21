package com.sena.qfinder.models;

public class CitaMedica {
    private int id_cita;
    private int id_paciente;
    private String fecha_cita;
    private String motivo_cita;
    private String resultado_consulta;
    private String estado_cita;

    // Constructor vacío
    public CitaMedica() {}

    // Constructor con todos los campos
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
}
