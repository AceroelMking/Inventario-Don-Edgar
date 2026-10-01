package com.gestion.controller.lote;

import java.time.LocalDate;

import com.gestion.model.Lote;
import com.gestion.model.Producto;
import com.gestion.service.LoteServicio;
import com.gestion.service.ProductoServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class LoteFormularioControlador {

    private final LoteServicio loteServicio =
        new LoteServicio();

    private final ProductoServicio productoServicio =
        new ProductoServicio();

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private TextField txtLote;

    @FXML
    private DatePicker fechaRegistro;

    @FXML
    private DatePicker fechaExpedicion;

    @FXML
    private DatePicker fechaVencimiento;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TextField txtValor;

    @FXML
    private TextField txtMotivo;


    @FXML
    public void initialize() {

        cargarProductos();

        fechaRegistro.setValue(
            LocalDate.now()
        );
    }


    private void cargarProductos() {

        comboProducto.setItems(
            productoServicio.consultarTodo()
        );

        comboProducto.setConverter(
            new StringConverter<>() {

                @Override
                public String toString(
                    Producto producto
                ) {

                    if (producto == null) {
                        return "";
                    }

                    String nombre =
                        producto.conseguirNombre() == null
                            ? ""
                            : producto.conseguirNombre();

                    String marca =
                        producto.conseguirMarca() == null
                            ? ""
                            : producto.conseguirMarca();

                    if (marca.isBlank()) {
                        return nombre;
                    }

                    return nombre + " - " + marca;
                }


                @Override
                public Producto fromString(
                    String texto
                ) {
                    return null;
                }
            }
        );
    }


    @FXML
    private void guardar() {

        try {

            Producto producto =
                comboProducto.getValue();

            if (producto == null) {
                throw new IllegalArgumentException(
                    "Debe seleccionar un producto."
                );
            }


            if (
                txtLote.getText() == null
                || txtLote.getText().isBlank()
            ) {

                throw new IllegalArgumentException(
                    "Debe ingresar el código del lote."
                );
            }


            if (fechaRegistro.getValue() == null) {

                throw new IllegalArgumentException(
                    "Debe seleccionar la fecha de registro."
                );
            }


            if (fechaVencimiento.getValue() == null) {

                throw new IllegalArgumentException(
                    "Debe seleccionar la fecha de vencimiento."
                );
            }


            double cantidad;

            try {

                cantidad =
                    Double.parseDouble(
                        txtCantidad
                            .getText()
                            .trim()
                    );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                    "La cantidad debe ser un número válido."
                );
            }


            if (cantidad <= 0) {

                throw new IllegalArgumentException(
                    "La cantidad debe ser mayor que cero."
                );
            }


            Double valor = null;

            if (
                txtValor.getText() != null
                && !txtValor.getText().isBlank()
            ) {

                try {

                    valor =
                        Double.parseDouble(
                            txtValor
                                .getText()
                                .trim()
                        );

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                        "El valor debe ser un número válido."
                    );
                }
            }


            Lote lote =
                new Lote();

            lote.colocarProducto(
                producto
            );

            lote.colocarLote(
                txtLote
                    .getText()
                    .trim()
            );

            lote.colocarFechaRegistro(
                fechaRegistro
                    .getValue()
                    .toString()
            );


            if (
                fechaExpedicion.getValue() != null
            ) {

                lote.colocarFechaExpedicion(
                    fechaExpedicion
                        .getValue()
                        .toString()
                );

            } else {

                lote.colocarFechaExpedicion(
                    null
                );
            }


            lote.colocarFechaVencimiento(
                fechaVencimiento
                    .getValue()
                    .toString()
            );

            lote.colocarCantidadMovimiento(
                cantidad
            );

            lote.colocarValorMovimiento(
                valor
            );

            lote.colocarMotivo(
                txtMotivo.getText() == null
                    ? ""
                    : txtMotivo
                        .getText()
                        .trim()
            );


            loteServicio.agregarLote(
                lote
            );


            new Alert(
                Alert.AlertType.INFORMATION,
                "Entrada de inventario registrada correctamente."
            ).showAndWait();


            cerrarVentana();


        } catch (
            IllegalArgumentException e
        ) {

            mostrarError(
                e.getMessage()
            );

        } catch (
            RuntimeException e
        ) {

            e.printStackTrace();

            mostrarError(
                "No se pudo registrar la entrada.\n"
                + e.getMessage()
            );
        }
    }


    @FXML
    private void cancelar() {

        cerrarVentana();
    }


    private void cerrarVentana() {

        Stage ventana =
            (Stage) txtLote
                .getScene()
                .getWindow();

        ventana.close();
    }


    private void mostrarError(
        String mensaje
    ) {

        new Alert(
            Alert.AlertType.ERROR,
            mensaje
        ).showAndWait();
    }
}