
package com.gestion.controller.lote;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;

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

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/uuuu")
                    .withResolverStyle(ResolverStyle.STRICT);

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

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        cargarProductos();

        configurarFecha(fechaRegistro);
        configurarFecha(fechaExpedicion);
        configurarFecha(fechaVencimiento);

        fechaRegistro.setValue(LocalDate.now());

        // La fecha de registro es automática.
        fechaRegistro.setEditable(false);
        fechaRegistro.setDisable(true);
    }

    // =====================================================
    // CONFIGURACIÓN DE FECHAS
    // =====================================================

    private void configurarFecha(DatePicker campoFecha) {

        campoFecha.setPromptText("dd/MM/aaaa");

        campoFecha.setConverter(
                new StringConverter<LocalDate>() {

                    @Override
                    public String toString(LocalDate fecha) {

                        if (fecha == null) {
                            return "";
                        }

                        return fecha.format(formatoFecha);
                    }

                    @Override
                    public LocalDate fromString(String texto) {

                        if (texto == null || texto.isBlank()) {
                            return null;
                        }

                        try {

                            return LocalDate.parse(
                                    texto.trim(),
                                    formatoFecha
                            );

                        } catch (DateTimeParseException e) {

                            // Una fecha inválida se convierte en null
                            // en lugar de lanzar una excepción.
                            return null;
                        }
                    }
                }
        );

        // Normalizar el texto al salir del campo.
        campoFecha.focusedProperty().addListener(
                (observable, anterior, tieneFoco) -> {

                    if (!tieneFoco && !campoFecha.isDisabled()) {

                        campoFecha.commitValue();

                        LocalDate fecha = campoFecha.getValue();

                        campoFecha.getEditor().setText(
                                fecha == null
                                        ? ""
                                        : fecha.format(formatoFecha)
                        );
                    }
                }
        );
    }

    // =====================================================
    // PRODUCTOS
    // =====================================================

    private void cargarProductos() {

        comboProducto.setItems(
                productoServicio.consultarTodo()
        );

        comboProducto.setConverter(
                new StringConverter<Producto>() {

                    @Override
                    public String toString(Producto producto) {

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
                    public Producto fromString(String texto) {
                        return null;
                    }
                }
        );
    }

    // =====================================================
    // CARGAR LOTE PARA EDICIÓN
    // =====================================================

    public void cargarLote(int idLote) {

        Lote lote = loteServicio.consultarLote(idLote);

        if (lote == null || lote.conseguirId() == null) {

            throw new IllegalArgumentException(
                    "No se encontró la entrada de inventario."
            );
        }

        this.loteActual = lote;
        this.modoEdicion = true;

        cargarDatosFormulario(lote);
    }

    private void cargarDatosFormulario(Lote lote) {

        seleccionarProducto(
                lote.conseguirProducto()
        );

        // No permitir cambiar el producto ni la cantidad
        // porque ya afectaron las existencias.
        comboProducto.setDisable(true);
        txtCantidad.setDisable(true);

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

        if (lote.conseguirCantidadMovimiento() != null) {

            txtCantidad.setText(
                    String.valueOf(
                            lote.conseguirCantidadMovimiento()
                    )
            );

        } else {

            txtCantidad.clear();
        }

        if (lote.conseguirValorMovimiento() != null) {

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

    private void seleccionarProducto(Producto productoLote) {

        if (productoLote == null
                || productoLote.conseguirId() == null) {
            return;
        }

        for (Producto producto : comboProducto.getItems()) {

            if (producto.conseguirId() != null
                    && producto.conseguirId()
                            .equals(productoLote.conseguirId())) {

                comboProducto.setValue(producto);
                return;
            }
        }

        comboProducto.setValue(productoLote);
    }

    // =====================================================
    // CONVERTIR FECHAS DE SQLITE
    // =====================================================

    private LocalDate convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            return null;
        }

        try {

            // Formato almacenado en SQLite: yyyy-MM-dd
            return LocalDate.parse(fecha);

        } catch (DateTimeParseException e) {

            return null;
        }
    }

    // =====================================================
    // GUARDAR LOTE
    // =====================================================

    @FXML
    private void guardar() {

        try {

            Producto producto = comboProducto.getValue();

            if (producto == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar un producto."
                );
            }

            if (txtLote.getText() == null
                    || txtLote.getText().isBlank()) {

                throw new IllegalArgumentException(
                        "Debe ingresar el código del lote."
                );
            }

            // Confirmar las fechas escritas manualmente.
            fechaExpedicion.commitValue();
            fechaVencimiento.commitValue();

            if (!modoEdicion) {

                // Fecha automática en el registro de un lote nuevo.
                fechaRegistro.setValue(LocalDate.now());
            }

            if (fechaRegistro.getValue() == null) {

                throw new IllegalArgumentException(
                        "Debe existir una fecha de registro."
                );
            }

            if (fechaVencimiento.getValue() == null) {

                throw new IllegalArgumentException(
                        "Debe seleccionar una fecha de vencimiento válida."
                );
            }

            LocalDate expedicion = fechaExpedicion.getValue();
            LocalDate vencimiento = fechaVencimiento.getValue();

            if (expedicion != null
                    && expedicion.isAfter(vencimiento)) {

                throw new IllegalArgumentException(
                        "La fecha de expedición no puede ser "
                        + "posterior a la fecha de vencimiento."
                );
            }

            // =================================================
            // VALIDACIÓN DE CANTIDAD
            // =================================================

            double cantidad;

            try {

                cantidad = Double.parseDouble(
                        txtCantidad.getText().trim()
                );

            } catch (Exception e) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser un número válido."
                );
            }

            if (!Double.isFinite(cantidad) || cantidad <= 0) {

                throw new IllegalArgumentException(
                        "La cantidad debe ser mayor que cero."
                );
            }

            // =================================================
            // VALIDACIÓN DE PRECIO
            // =================================================

            Double precioCompraUnitario = null;

            if (txtValor.getText() != null
                    && !txtValor.getText().isBlank()) {

                try {

                    precioCompraUnitario = Double.parseDouble(
                            txtValor.getText().trim()
                    );

                } catch (NumberFormatException e) {

                    throw new IllegalArgumentException(
                            "El precio de compra unitario "
                            + "debe ser un número válido."
                    );
                }

                if (!Double.isFinite(precioCompraUnitario)
                        || precioCompraUnitario < 0) {

                    throw new IllegalArgumentException(
                            "El precio de compra unitario "
                            + "no puede ser negativo."
                    );
                }
            }

            // =================================================
            // AGREGAR O EDITAR
            // =================================================

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

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo guardar la entrada.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // AGREGAR LOTE
    // =====================================================

    private void guardarNuevo(
            Producto producto,
            double cantidad,
            Double precioCompraUnitario
    ) {

        Lote lote = new Lote();

        completarDatosLote(
                lote,
                producto,
                cantidad,
                precioCompraUnitario
        );

        loteServicio.agregarLote(lote);

        mostrarInformacion(
                "Entrada de inventario registrada correctamente."
        );
    }

    // =====================================================
    // EDITAR LOTE
    // =====================================================

    private void guardarEdicion(
            Producto producto,
            double cantidad,
            Double precioCompraUnitario
    ) {

        if (loteActual == null
                || loteActual.conseguirId() == null) {

            throw new IllegalArgumentException(
                    "No existe una entrada válida para editar."
            );
        }

        // Conservar producto y cantidad originales.
        Producto productoOriginal =
                loteActual.conseguirProducto();

        Double cantidadOriginal =
                loteActual.conseguirCantidadMovimiento();

        completarDatosLote(
                loteActual,
                productoOriginal,
                cantidadOriginal,
                precioCompraUnitario
        );

        loteServicio.editarLote(loteActual);

        mostrarInformacion(
                "Entrada de inventario actualizada correctamente."
        );
    }

    // =====================================================
    // COMPLETAR DATOS DEL LOTE
    // =====================================================

    private void completarDatosLote(
            Lote lote,
            Producto producto,
            double cantidad,
            Double precioCompraUnitario
    ) {

        lote.colocarProducto(producto);

        lote.colocarLote(
                txtLote.getText().trim()
        );

        // Solo establecer la fecha de registro al crear.
        // Al editar, se conserva la fecha original.
        if (!modoEdicion) {

            lote.colocarFechaRegistro(
                    LocalDate.now().toString()
            );
        }

        if (fechaExpedicion.getValue() != null) {

            lote.colocarFechaExpedicion(
                    fechaExpedicion.getValue().toString()
            );

        } else {

            lote.colocarFechaExpedicion(null);
        }

        lote.colocarFechaVencimiento(
                fechaVencimiento.getValue().toString()
        );

        lote.colocarCantidadMovimiento(cantidad);

        lote.colocarValorMovimiento(
                precioCompraUnitario
        );

        lote.colocarMotivo(
                txtMotivo.getText() == null
                        ? ""
                        : txtMotivo.getText().trim()
        );
    }

    // =====================================================
    // CANCELAR Y CERRAR
    // =====================================================

    @FXML
    private void cancelar() {

        cerrarVentana();
    }

    private void cerrarVentana() {

        Stage ventana =
                (Stage) txtLote.getScene().getWindow();

        ventana.close();
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarError(String mensaje) {

        new Alert(
                Alert.AlertType.ERROR,
                mensaje
        ).showAndWait();
    }

    private void mostrarInformacion(String mensaje) {

        new Alert(
                Alert.AlertType.INFORMATION,
                mensaje
        ).showAndWait();
    }
}
