package com.gestion.model;

public class Cliente {
    private int id;
    private Persona persona = new Persona();
    private String correoElectronico;
    private int telefono;
    private Double saldo;

    // Getters
    public int conseguirId() {
      return id; 
    }

    public Persona conseguirPersona() { 
      return persona; 
    }

    public String conseguirCorreoElectronico() { 
      return correoElectronico; 
    }

    public int conseguirTelefono() {
      return telefono; 
    }

    public Double conseguirSaldo() {
      return saldo; 
    }

    // Setters
    public void colocarId(int id) { 
      this.id = id; 
    }

    public void colocarPersona(Persona persona) { 
      this.persona = persona; 
    }

    public void colocarCorreoElectronico(String correoElectronico) { 
      this.correoElectronico = correoElectronico; 
    }

    public void colocarTelefono(int telefono) { 
      this.telefono = telefono; 
    }

    public void colocarSaldo(Double saldo) { 
      this.saldo = saldo; 
    }
    
    // Metodos

}
