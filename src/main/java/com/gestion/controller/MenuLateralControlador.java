package com.gestion.controller;

import java.io.IOException;
import java.net.URL;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class MenuLateralControlador {

    @FXML
    private BorderPane panelBordes;


    @FXML
    public void initialize() {
        abrirInicio();
    }


    private void cargarVista(
        String archivo
    ) throws IOException {

        URL recurso =
            getClass().getResource(
                "/com/gestion/view/"
                + archivo
            );

        if (recurso == null) {
            throw new IOException(
                "No se encontró la vista: "
                + archivo
            );
        }

        FXMLLoader loader =
            new FXMLLoader(recurso);

        Parent vista =
            loader.load();

        panelBordes.setCenter(vista);
    }


    @FXML
    public void abrirInicio() {

        try {

            URL recurso =
                getClass().getResource(
                    "/com/gestion/view/Inicio.fxml"
                );

            if (recurso == null) {
                throw new IOException(
                    "No se encontró Inicio.fxml."
                );
            }

            FXMLLoader loader =
                new FXMLLoader(recurso);

            Parent vista =
                loader.load();

            /*
             * Obtenemos el controlador del dashboard.
             */
            InicioControlador controlador =
                loader.getController();

            /*
             * Le entregamos una referencia al
             * controlador principal para que los
             * accesos rápidos puedan navegar.
             */
            controlador.setMenuControlador(this);

            panelBordes.setCenter(vista);

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Inicio.",
                e
            );
        }
    }


    @FXML
    public void abrirProductos() {

        try {

            cargarVista(
                "producto/Producto.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Productos.",
                e
            );
        }
    }


    @FXML
    public void abrirLotes() {

        try {

            cargarVista(
                "lote/Lote.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Inventario / Lotes.",
                e
            );
        }
    }


    @FXML
    public void abrirClientes() {

        try {

            cargarVista(
                "cliente/Cliente.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Clientes.",
                e
            );
        }
    }


    @FXML
    public void abrirVentas() {

        try {

            cargarVista(
                "venta/Venta.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Ventas.",
                e
            );
        }
    }


    @FXML
    public void abrirReportes() {

        try {

            cargarVista(
                "reporte/ReporteVentas.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Reportes.",
                e
            );
        }
    }

    @FXML
    public void abrirAlertas() {

        try {

            cargarVista(
                "alerta/Alertas.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir Alertas.",
                e
            );
        }
    }


    @FXML
    public void abrirConfiguracionUsuario() {

        try {

            cargarVista(
                "ConfiguracionUsuario.fxml"
            );

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir la configuración.",
                e
            );
        }
    }


    @FXML
    private void cerrarSesion(
        ActionEvent evento
    ) {

        Alert confirmacion =
            new Alert(
                Alert.AlertType.CONFIRMATION
            );

        confirmacion.setTitle(
            "Cerrar sesión"
        );

        confirmacion.setHeaderText(
            "¿Desea cerrar la sesión?"
        );

        confirmacion.setContentText(
            "Regresará a la pantalla de inicio de sesión."
        );

        ButtonType respuesta =
            confirmacion
                .showAndWait()
                .orElse(
                    ButtonType.CANCEL
                );

        if (respuesta != ButtonType.OK) {
            return;
        }

        try {

            URL recurso =
                getClass().getResource(
                    "/com/gestion/view/Acceso.fxml"
                );

            if (recurso == null) {

                throw new IOException(
                    "No se encontró Acceso.fxml."
                );
            }

            FXMLLoader loader =
                new FXMLLoader(recurso);

            Parent acceso =
                loader.load();

            Node origen =
                (Node) evento.getSource();

            Stage ventana =
                (Stage) origen
                    .getScene()
                    .getWindow();

            Scene escenaAcceso =
                new Scene(acceso);

            ventana.setScene(
                escenaAcceso
            );

            ventana.setTitle(
                "Víveres y Cárnicos Don Edgar - Acceso"
            );

            ventana.centerOnScreen();
            ventana.show();

        } catch (IOException e) {

            mostrarError(
                "No se pudo cerrar la sesión.",
                e
            );
        }
    }


    private void mostrarError(
        String mensaje,
        Exception excepcion
    ) {

        excepcion.printStackTrace();

        Alert alerta =
            new Alert(
                Alert.AlertType.ERROR
            );

        alerta.setTitle(
            "Error"
        );

        alerta.setHeaderText(
            mensaje
        );

        alerta.setContentText(
            excepcion.getMessage()
        );

        alerta.showAndWait();
    }
}