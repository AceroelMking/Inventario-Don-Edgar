package com.gestion.controller;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;
import javafx.scene.Scene;
import javafx.stage.Stage;

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

    @FXML
    private void abrirConfiguracionUsuario() {

        try {
            cargarVista(
                "ConfiguracionUsuario.fxml"
            );

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void cerrarSesion() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/Acceso.fxml"
                    )
                );

            Parent acceso =
                loader.load();

            Stage stage =
                (Stage) panelBordes
                    .getScene()
                    .getWindow();

            stage.setScene(
                new Scene(acceso)
            );

            stage.setTitle(
                "Víveres y Cárnicos Don Edgar - Acceso"
            );

            stage.centerOnScreen();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}