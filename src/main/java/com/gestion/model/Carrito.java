
package com.gestion.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public final class Carrito {

    private static final Carrito INSTANCIA = new Carrito();

    private final ObservableList<DetalleVenta> detalles =
            FXCollections.observableArrayList();

    private Integer idCliente;
    private String metodoPago;
    private String montoPagado = "";

    private Carrito() {
    }

    public static Carrito obtenerInstancia() {
        return INSTANCIA;
    }

    public ObservableList<DetalleVenta> conseguirDetalles() {
        return detalles;
    }

    public Integer conseguirIdCliente() {
        return idCliente;
    }

    public void colocarIdCliente(Integer idCliente) {
        this.idCliente = idCliente;
    }

    public String conseguirMetodoPago() {
        return metodoPago;
    }

    public void colocarMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public String conseguirMontoPagado() {
        return montoPagado;
    }

    public void colocarMontoPagado(String montoPagado) {
        this.montoPagado = montoPagado == null ? "" : montoPagado;
    }

    public boolean estaVacio() {
        return detalles.isEmpty();
    }

    public void limpiar() {
        detalles.clear();
        idCliente = null;
        metodoPago = null;
        montoPagado = "";
    }
}
