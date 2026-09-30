package com.gestion.model;

public class Usuario {
    private Persona persona = new Persona();
    private String nombreUsuario;
    private String correoElectronico;
    // private String rol;
    private int telefono;
    private String contrasena;

    // Getters
    public Persona conseguirPersona() { 
      return persona; 
    }

    public String conseguirNombreUsuario() { 
      return nombreUsuario; 
    }

    public String conseguirCorreoElectronico() { 
      return correoElectronico; 
    }

    /*public String conseguirRol() { 
      return rol; 
    }*/

    public int conseguirTelefono() {
      return telefono; 
    }

    public String conseguirContrasena() {
      return contrasena; 
    }

    // Setters
    public void colocarNombre(Persona persona) { 
      this.persona = persona; 
    }

    public void colocarNombreUsuario(String nombreUsuario) { 
      this.nombreUsuario = nombreUsuario; 
    }

    public void colocarCorreoElectronico(String correoElectronico) { 
      this.correoElectronico = correoElectronico; 
    }

    /*public void colocarRol(String rol) { 
      this.rol = rol; 
    }*/

    public void colocarTelefono(int telefono) { 
      this.telefono = telefono; 
    }
    
    public void colocarContrasena(String contrasena) { 
      this.contrasena = contrasena; 
    }

    // Metodos

}
