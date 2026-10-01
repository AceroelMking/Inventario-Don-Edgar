package com.gestion.model;

public class Venta {

    private Integer id;
    private Cliente cliente;
    private String fecha;
    private Double subtotal;
    private Double total;
    private String metodoPago;
    private Double montoPagado;
    private Double saldoPendiente;

    public Venta() {
    }

    public Integer conseguirId() {
        return id;
    }

    public Cliente conseguirCliente() {
        return cliente;
    }

    public String conseguirFecha() {
        return fecha;
    }

    public Double conseguirSubtotal() {
        return subtotal;
    }

    public Double conseguirTotal() {
        return total;
    }

    public String conseguirMetodoPago() {
        return metodoPago;
    }

    public Double conseguirMontoPagado() {
        return montoPagado;
    }

    public Double conseguirSaldoPendiente() {
        return saldoPendiente;
    }

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public void colocarFecha(String fecha) {
        this.fecha = fecha;
    }

    public void colocarSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }

    public void colocarTotal(Double total) {
        this.total = total;
    }

    public void colocarMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public void colocarMontoPagado(Double montoPagado) {
        this.montoPagado = montoPagado;
    }

    public void colocarSaldoPendiente(Double saldoPendiente) {
        this.saldoPendiente = saldoPendiente;
    }
}