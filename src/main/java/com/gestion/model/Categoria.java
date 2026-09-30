package com.gestion.model;

import java.util.Objects;

public class Categoria {

    private Integer id;
    private String nombre;
    private Integer activo;

    public Categoria() {
        //Vacio
    }

    public Categoria(Integer id, String nombre, Integer activo) {
        this.id = id;
        this.nombre = nombre;
        this.activo = activo;
    }

    public Integer conseguirId() {
        return id;
    }

    public String conseguirNombre() {
        return nombre;
    }

    public Integer conseguirActivo() {
        return activo;
    }

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarNombre(String nombre) {
        this.nombre = nombre;
    }

    public void colocarActivo(Integer activo) {
        this.activo = activo;
    }

      // Metodos
    @Override
    public String toString() {
      return id + " - " + nombre;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Categoria)) return false;
        Categoria otra = (Categoria) o;
        return Objects.equals(id, otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}