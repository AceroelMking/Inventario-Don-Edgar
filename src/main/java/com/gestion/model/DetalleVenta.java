package com.gestion.model;

public class DetalleVenta {

    private Integer id;
    private Venta venta;
    private Producto producto;
    private Double cantidad;
    private Double precioUnitario;
    private Double subtotal;

    public DetalleVenta() {
    }

    public Integer conseguirId() {
        return id;
    }

    public Venta conseguirVenta() {
        return venta;
    }

    public Producto conseguirProducto() {
        return producto;
    }

    public Double conseguirCantidad() {
        return cantidad;
    }

    public Double conseguirPrecioUnitario() {
        return precioUnitario;
    }

    public Double conseguirSubtotal() {
        return subtotal;
    }

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarVenta(Venta venta) {
        this.venta = venta;
    }

    public void colocarProducto(Producto producto) {
        this.producto = producto;
    }

    public void colocarCantidad(Double cantidad) {
        this.cantidad = cantidad;
    }

    public void colocarPrecioUnitario(Double precioUnitario) {
        this.precioUnitario = precioUnitario;
    }

    public void colocarSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}