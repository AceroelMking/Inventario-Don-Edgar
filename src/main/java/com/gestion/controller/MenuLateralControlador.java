package com.gestion.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class MenuLateralControlador {

    @FXML
    private BorderPane panelBordes;


    private void cargarVista(
        String archivo
    ) throws IOException {

        FXMLLoader loader =
            new FXMLLoader(
                getClass().getResource(
                    "/com/gestion/view/"
                    + archivo
                )
            );

        Parent vista =
            loader.load();

        panelBordes.setCenter(
            vista
        );
    }


    @FXML
    private void abrirProductos() {

        try {

            cargarVista(
                "producto/Producto.fxml"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    @FXML
    private void abrirLotes() {

        try {

            cargarVista(
                "lote/Lote.fxml"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    @FXML
    private void abrirClientes() {

        try {

            cargarVista(
                "cliente/Cliente.fxml"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    @FXML
    private void abrirVentas() {

        try {

            cargarVista(
                "venta/Venta.fxml"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }


    @FXML
    private void abrirReportes() {

        try {

            cargarVista(
                "reporte/ReporteVentas.fxml"
            );

        } catch (IOException e) {

            e.printStackTrace();
        }
    }
}