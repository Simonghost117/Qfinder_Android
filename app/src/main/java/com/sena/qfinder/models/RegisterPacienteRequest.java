package com.sena.qfinder.models;

public class RegisterPacienteRequest {
    private int id_usuario; // 🔹 campo obligatorio
    private String nombre;
    private String apellido;
    private String identificacion;
    private String fecha_nacimiento; // 🔹 debe coincidir con el backend
    private String sexo;
    private String diagnostico_principal; // 🔹 debe coincidir con el backend

    // Constructor
    public RegisterPacienteRequest(int id_usuario, String nombre, String apellido,
                                   String identificacion, String fecha_nacimiento,
                                   String sexo, String diagnostico_principal) {
        this.id_usuario = id_usuario;
        this.nombre = nombre;
        this.apellido = apellido;
        this.identificacion = identificacion;
        this.fecha_nacimiento = fecha_nacimiento;
        this.sexo = sexo;
        this.diagnostico_principal = diagnostico_principal;
    }

    // Getters
    public int getId_usuario() {
        return id_usuario;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getIdentificacion() {
        return identificacion;
    }

    public String getFecha_nacimiento() {
        return fecha_nacimiento;
    }

    public String getSexo() {
        return sexo;
    }

    public String getDiagnostico_principal() {
        return diagnostico_principal;
    }
}
