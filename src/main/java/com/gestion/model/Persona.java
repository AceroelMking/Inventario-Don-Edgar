package com.gestion.model;

public class Persona {
	private String nombre;
	private String numeroIdentidad;

	// Getters
    public String conseguirNombre() { 
      return nombre; 
    }
    public String conseguirNumeroIdentidad() {
      return numeroIdentidad; 
    }

    // Setters
    public void colocarNombre(String nombre) { 
      this.nombre = nombre; 
    }
    public void colocarNumeroIdentidad(String numeroIdentidad) { 
        this.numeroIdentidad = numeroIdentidad; 
    }

	// Metodos
}
