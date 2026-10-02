package com.gestion.controller;

import javafx.fxml.FXML;

public class InicioControlador {

    private MenuLateralControlador menuControlador;


    /*
     * Recibe el controlador principal.
     */
    public void setMenuControlador(
        MenuLateralControlador menuControlador
    ) {

        this.menuControlador =
            menuControlador;
    }


    @FXML
    private void abrirProductos() {

        if (menuControlador != null) {
            menuControlador.abrirProductos();
        }
    }


    @FXML
    private void abrirInventario() {

        if (menuControlador != null) {
            menuControlador.abrirLotes();
        }
    }


    @FXML
    private void abrirClientes() {

        if (menuControlador != null) {
            menuControlador.abrirClientes();
        }
    }


    @FXML
    private void abrirVentas() {

        if (menuControlador != null) {
            menuControlador.abrirVentas();
        }
    }


    @FXML
    private void abrirReportes() {

        if (menuControlador != null) {
            menuControlador.abrirReportes();
        }
    }
}