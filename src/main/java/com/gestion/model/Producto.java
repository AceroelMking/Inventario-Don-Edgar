package com.gestion.model;

import java.util.Objects;

public class Producto {
    // Atributos
    private Integer id; 
    private String codigoBarras;
    private String nombre;
    private String marca;
    private Categoria categoria = new Categoria();
    private Double cantidadProducto;
    private String unidadMedida;
    //private Producto producto;
    private Integer unidadAgrupada;
    private String productoPadre;
    private Double precio;
    private Double existencias;
    private Double minimoExistencias;
    private Boolean activo;

    // Constructor 
    public Producto() {
      // Vacio
    }

    public Producto(
      String codigoBarras,
      String nombre,
      String marca,
      //Integer categoria,
      Double cantidadProducto,
      String unidadMedida,
      Integer unidadAgrupada,
      Double precio,
      Double existencias,
      Double minimoExistencias,
      Boolean activo
    ) {
        this.codigoBarras = codigoBarras;
        this.nombre = nombre;
        this.marca = marca;
        //this.categoria = categoria;
        this.cantidadProducto = cantidadProducto;
        this.unidadMedida = unidadMedida;
        this.unidadAgrupada = unidadAgrupada;
        this.precio = precio;
        this.existencias = existencias;
        this.minimoExistencias = minimoExistencias;
        this.activo = activo;
    }

    // Getters
    public Integer conseguirId() {
      return id; 
    }

    public String conseguirCodigoBarras() {
      return codigoBarras; 
    }

    public String conseguirNombre() {
      return nombre; 
    }

    public String conseguirMarca() {
      return marca; 
    }

    public Categoria conseguirCategoria() {
      return categoria; 
    }

    public Double conseguirCantidadProducto() {
      return cantidadProducto; 
    }

    public String conseguirUnidadMedida() {
      return unidadMedida; 
    }

    public Integer conseguirUnidadAgrupada() {
      return unidadAgrupada; 
    }

    public String conseguirProductoPadre(){
      return  productoPadre;
    }

    public Double conseguirPrecio() {
      return precio; 
    }

    public Double conseguirExistencias() {
      return existencias; 
    }

    public Double conseguirMinimoExistencias() {
      return minimoExistencias; 
    }

    public Boolean conseguirActivo() {
      return activo; 
    }

    // Setters
    public void colocarId(Integer id) { 
      this.id = id; 
    }

    public void colocarCodigoBarras(String codigoBarras) { 
      this.codigoBarras = codigoBarras; 
    }

    public void colocarNombre(String nombre) { 
      this.nombre = nombre; 
    }

    public void colocarMarca(String marca) { 
      this.marca = marca; 
    }

    public void colocarCategoria(Categoria categoria) { 
      this.categoria = categoria; 
    }

    public void colocarCantidadProducto(Double cantidadProducto) { 
      this.cantidadProducto = cantidadProducto; 
    }

    public void colocarUnidadMedida(String unidadMedida) { 
      this.unidadMedida = unidadMedida; 
    }

    public void colocarUnidadAgrupada(Integer unidadAgrupada) { 
      this.unidadAgrupada = unidadAgrupada; 
    }

    public void colocarProductoPadre(String productoPadre) { 
      this.productoPadre = productoPadre; 
    }

    public void colocarPrecio(Double precio) { 
      this.precio = precio; 
    }

    public void colocarExistencias(Double existencias) { 
      this.existencias = existencias; 
    }

    public void colocarMinimoExistencias(Double minimoExistencias) { 
      this.minimoExistencias = minimoExistencias; 
    }

    public void colocarActivo(Boolean activo) { 
      this.activo = activo; 
    }

    // Metodos
    @Override
    public String toString() {
      return id
        + " - " + codigoBarras
        + " - " + nombre
        + " - " + marca
      ;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Producto)) return false;
        Producto otra = (Producto) o;
        return Objects.equals(id, otra.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    /*
    @Override
    public String toString() {
      return "Código: " 
        + codigoBarras 
        + " | Nombre: " 
        + nombre 
        + " | Marca: " 
        + marca 
        + " | Cantidad: " 
        + cantidadProducto 
        + " " 
        + unidadMedida 
        + " | U.A: "
        + unidadAgrupada
        + " | Precio: " 
        + precio 
        + " | Existencias: " 
        + existencias 
        + " | Mínimo: " 
        + minimoExistencias;
    }
    */
}
