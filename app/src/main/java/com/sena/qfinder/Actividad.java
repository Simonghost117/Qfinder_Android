package com.sena.qfinder;

public class Actividad {
    private String paciente;
    private String fecha;
    private String hora;
    private String descripcion;
    private String recordarAntes;  // Cambiado de boolean a String
    private String repetirCada;

    public Actividad(String paciente, String fecha, String hora, String descripcion, String recordarAntes, String repetirCada) {
        this.paciente = paciente;
        this.fecha = fecha;
        this.hora = hora;
        this.descripcion = descripcion;
        this.recordarAntes = recordarAntes;  // Guardar el valor del Spinner como String
        this.repetirCada = repetirCada;
    }

    // Getters y setters si los necesitas
    public String getPaciente() { return paciente; }
    public String getFecha() { return fecha; }
    public String getHora() { return hora; }
    public String getDescripcion() { return descripcion; }
    public String getRecordarAntes() { return recordarAntes; }  // Cambiado a String
    public String getRepetirCada() { return repetirCada; }

    public void setPaciente(String paciente) { this.paciente = paciente; }
    public void setFecha(String fecha) { this.fecha = fecha; }
    public void setHora(String hora) { this.hora = hora; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
    public void setRecordarAntes(String recordarAntes) { this.recordarAntes = recordarAntes; }  // Cambiado a String
    public void setRepetirCada(String repetirCada) { this.repetirCada = repetirCada; }
}
