package com.gestion.model;

public class Alerta {

    private String tipo;
    private String producto;
    private String codigoBarras;
    private String lote;
    private String fechaVencimiento;
    private String detalle;
    private String estado;

    public Alerta() {
    }

    public Alerta(
        String tipo,
        String producto,
        String codigoBarras,
        String lote,
        String fechaVencimiento,
        String detalle,
        String estado
    ) {

        this.tipo = tipo;
        this.producto = producto;
        this.codigoBarras = codigoBarras;
        this.lote = lote;
        this.fechaVencimiento = fechaVencimiento;
        this.detalle = detalle;
        this.estado = estado;
    }

    public String conseguirTipo() {
        return tipo;
    }

    public String conseguirProducto() {
        return producto;
    }

    public String conseguirCodigoBarras() {
        return codigoBarras;
    }

    public String conseguirLote() {
        return lote;
    }

    public String conseguirFechaVencimiento() {
        return fechaVencimiento;
    }

    public String conseguirDetalle() {
        return detalle;
    }

    public String conseguirEstado() {
        return estado;
    }

    public void colocarTipo(
        String tipo
    ) {
        this.tipo = tipo;
    }

    public void colocarProducto(
        String producto
    ) {
        this.producto = producto;
    }

    public void colocarCodigoBarras(
        String codigoBarras
    ) {
        this.codigoBarras = codigoBarras;
    }

    public void colocarLote(
        String lote
    ) {
        this.lote = lote;
    }

    public void colocarFechaVencimiento(
        String fechaVencimiento
    ) {
        this.fechaVencimiento =
            fechaVencimiento;
    }

    public void colocarDetalle(
        String detalle
    ) {
        this.detalle = detalle;
    }

    public void colocarEstado(
        String estado
    ) {
        this.estado = estado;
    }
}