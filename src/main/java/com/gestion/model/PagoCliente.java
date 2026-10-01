package com.gestion.model;

public class PagoCliente {

    private Integer id;
    private Cliente cliente;
    private String fecha;
    private Double monto;
    private String observacion;

    public PagoCliente() {
    }

    public Integer conseguirId() {
        return id;
    }

    public void colocarId(Integer id) {
        this.id = id;
    }

    public Cliente conseguirCliente() {
        return cliente;
    }

    public void colocarCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public String conseguirFecha() {
        return fecha;
    }

    public void colocarFecha(String fecha) {
        this.fecha = fecha;
    }

    public Double conseguirMonto() {
        return monto;
    }

    public void colocarMonto(Double monto) {
        this.monto = monto;
    }

    public String conseguirObservacion() {
        return observacion;
    }

    public void colocarObservacion(String observacion) {
        this.observacion = observacion;
    }
}