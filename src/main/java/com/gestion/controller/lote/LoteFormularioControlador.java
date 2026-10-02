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

    private Lote loteActual;

    private boolean modoEdicion = false;


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

                    return nombre
                        + " - "
                        + marca;
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


    public void cargarLote(
        int idLote
    ) {

        Lote lote =
            loteServicio.consultarLote(
                idLote
            );

        if (
            lote == null
            || lote.conseguirId() == null
        ) {

            throw new IllegalArgumentException(
                "No se encontró la entrada de inventario."
            );
        }

        this.loteActual = lote;

        this.modoEdicion = true;

        cargarDatosFormulario(
            lote
        );
    }


    private void cargarDatosFormulario(
        Lote lote
    ) {

        seleccionarProducto(
            lote.conseguirProducto()
        );

        comboProducto.setDisable(
            true
        );

        txtCantidad.setDisable(
            true
        );

        txtLote.setText(
            lote.conseguirLote()
        );

        fechaRegistro.setValue(
            convertirFecha(
                lote.conseguirFechaRegistro()
            )
        );

        fechaExpedicion.setValue(
            convertirFecha(
                lote.conseguirFechaExpedicion()
            )
        );

        fechaVencimiento.setValue(
            convertirFecha(
                lote.conseguirFechaVencimiento()
            )
        );

        if (
            lote.conseguirCantidadMovimiento()
            != null
        ) {

            txtCantidad.setText(
                String.valueOf(
                    lote.conseguirCantidadMovimiento()
                )
            );

        } else {

            txtCantidad.clear();
        }

        if (
            lote.conseguirValorMovimiento()
            != null
        ) {

            txtValor.setText(
                String.valueOf(
                    lote.conseguirValorMovimiento()
                )
            );

        } else {

            txtValor.clear();
        }

        txtMotivo.setText(
            lote.conseguirMotivo() == null
                ? ""
                : lote.conseguirMotivo()
        );
    }


    private void seleccionarProducto(
        Producto productoLote
    ) {

        if (
            productoLote == null
            || productoLote.conseguirId() == null
        ) {

            return;
        }

        for (
            Producto producto :
            comboProducto.getItems()
        ) {

            if (
                producto.conseguirId() != null
                && producto
                    .conseguirId()
                    .equals(
                        productoLote
                            .conseguirId()
                    )
            ) {

                comboProducto.setValue(
                    producto
                );

                return;
            }
        }

        comboProducto.setValue(
            productoLote
        );
    }


    private LocalDate convertirFecha(
        String fecha
    ) {

        if (
            fecha == null
            || fecha.isBlank()
        ) {

            return null;
        }

        try {

            return LocalDate.parse(
                fecha
            );

        } catch (Exception e) {

            return null;
        }
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
                || txtLote
                    .getText()
                    .isBlank()
            ) {

                throw new IllegalArgumentException(
                    "Debe ingresar el código del lote."
                );
            }


            if (
                fechaRegistro.getValue() == null
            ) {

                throw new IllegalArgumentException(
                    "Debe seleccionar la fecha de registro."
                );
            }


            if (
                fechaVencimiento.getValue() == null
            ) {

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


            Double precioCompraUnitario =
                null;

            if (
                txtValor.getText() != null
                && !txtValor
                    .getText()
                    .isBlank()
            ) {

                try {

                    precioCompraUnitario =
                        Double.parseDouble(
                            txtValor
                                .getText()
                                .trim()
                        );

                } catch (
                    NumberFormatException e
                ) {

                    throw new IllegalArgumentException(
                        "El precio de compra unitario debe ser un número válido."
                    );
                }

                if (
                    precioCompraUnitario < 0
                ) {

                    throw new IllegalArgumentException(
                        "El precio de compra unitario no puede ser negativo."
                    );
                }
            }


            if (modoEdicion) {

                guardarEdicion(
                    producto,
                    cantidad,
                    precioCompraUnitario
                );

            } else {

                guardarNuevo(
                    producto,
                    cantidad,
                    precioCompraUnitario
                );
            }


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
                "No se pudo guardar la entrada.\n"
                + e.getMessage()
            );
        }
    }


    private void guardarNuevo(
        Producto producto,
        double cantidad,
        Double precioCompraUnitario
    ) {

        Lote lote =
            new Lote();

        completarDatosLote(
            lote,
            producto,
            cantidad,
            precioCompraUnitario
        );

        loteServicio.agregarLote(
            lote
        );

        new Alert(
            Alert.AlertType.INFORMATION,
            "Entrada de inventario registrada correctamente."
        ).showAndWait();
    }


    private void guardarEdicion(
        Producto producto,
        double cantidad,
        Double precioCompraUnitario
    ) {

        if (
            loteActual == null
            || loteActual.conseguirId() == null
        ) {

            throw new IllegalArgumentException(
                "No existe una entrada válida para editar."
            );
        }

        /*
         * El producto y la cantidad originales
         * se conservan porque ya afectaron
         * las existencias del inventario.
         */
        Producto productoOriginal =
            loteActual.conseguirProducto();

        Double cantidadOriginal =
            loteActual
                .conseguirCantidadMovimiento();

        completarDatosLote(
            loteActual,
            productoOriginal,
            cantidadOriginal,
            precioCompraUnitario
        );

        loteServicio.editarLote(
            loteActual
        );

        new Alert(
            Alert.AlertType.INFORMATION,
            "Entrada de inventario actualizada correctamente."
        ).showAndWait();
    }


    private void completarDatosLote(
        Lote lote,
        Producto producto,
        double cantidad,
        Double precioCompraUnitario
    ) {

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
            fechaExpedicion.getValue()
            != null
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
            precioCompraUnitario
        );

        lote.colocarMotivo(
            txtMotivo.getText() == null
                ? ""
                : txtMotivo
                    .getText()
                    .trim()
        );
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