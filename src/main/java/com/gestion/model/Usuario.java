package com.gestion.model;

public class Usuario {

    private Integer id;
    private Persona persona = new Persona();
    private String nombreUsuario;
    private String correoElectronico;
    private String telefono;
    private String contrasena;

    public Integer conseguirId() {
        return id;
    }

    public Persona conseguirPersona() {
        return persona;
    }

    public String conseguirNombreUsuario() {
        return nombreUsuario;
    }

    public String conseguirCorreoElectronico() {
        return correoElectronico;
    }

    public String conseguirTelefono() {
        return telefono;
    }

    public String conseguirContrasena() {
        return contrasena;
    }

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarNombre(Persona persona) {
        this.persona = persona;
    }

    public void colocarNombreUsuario(
        String nombreUsuario
    ) {
        this.nombreUsuario = nombreUsuario;
    }

    public void colocarCorreoElectronico(
        String correoElectronico
    ) {
        this.correoElectronico = correoElectronico;
    }

    public void colocarTelefono(
        String telefono
    ) {
        this.telefono = telefono;
    }

    public void colocarContrasena(
        String contrasena
    ) {
        this.contrasena = contrasena;
    }
}