package com.gestion.model;

public class Lote {

    private Integer id;
    private Producto producto;
    private String lote;
    private String fechaRegistro;
    private String fechaExpedicion;
    private String fechaVencimiento;
    private Double cantidadMovimiento;
    private Double valorMovimiento;
    private String motivo;

    // Constructor
    public Lote() {
    }

    // Getters / Conseguir

    public Integer conseguirId() {
        return id;
    }

    public Producto conseguirProducto() {
        return producto;
    }

    public String conseguirLote() {
        return lote;
    }

    public String conseguirFechaRegistro() {
        return fechaRegistro;
    }

    public String conseguirFechaExpedicion() {
        return fechaExpedicion;
    }

    public String conseguirFechaVencimiento() {
        return fechaVencimiento;
    }

    public Double conseguirCantidadMovimiento() {
        return cantidadMovimiento;
    }

    public Double conseguirValorMovimiento() {
        return valorMovimiento;
    }

    public String conseguirMotivo() {
        return motivo;
    }

    // Setters / Colocar

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarProducto(Producto producto) {
        this.producto = producto;
    }

    public void colocarLote(String lote) {
        this.lote = lote;
    }

    public void colocarFechaRegistro(String fechaRegistro) {
        this.fechaRegistro = fechaRegistro;
    }

    public void colocarFechaExpedicion(String fechaExpedicion) {
        this.fechaExpedicion = fechaExpedicion;
    }

    public void colocarFechaVencimiento(String fechaVencimiento) {
        this.fechaVencimiento = fechaVencimiento;
    }

    public void colocarCantidadMovimiento(Double cantidadMovimiento) {
        this.cantidadMovimiento = cantidadMovimiento;
    }

    public void colocarValorMovimiento(Double valorMovimiento) {
        this.valorMovimiento = valorMovimiento;
    }

    public void colocarMotivo(String motivo) {
        this.motivo = motivo;
    }
}