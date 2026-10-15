
package com.gestion.controller.venta;

import com.gestion.model.Cliente;
import com.gestion.model.DetalleVenta;
import com.gestion.model.Producto;
import com.gestion.model.Venta;
import com.gestion.service.ClienteServicio;
import com.gestion.service.ProductoServicio;
import com.gestion.service.VentaServicio;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.util.StringConverter;

import java.util.Locale;

public class VentaControlador {

    private final VentaServicio ventaServicio =
            new VentaServicio();

    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    private final ProductoServicio productoServicio =
            new ProductoServicio();

    private final ObservableList<DetalleVenta> detalles =
            FXCollections.observableArrayList();

    private final ObservableList<Producto> productosDisponibles =
            FXCollections.observableArrayList();

    // Las sugerencias son independientes del ComboBox.
    // Así evitamos modificar su lista durante una selección.
    private final ContextMenu sugerenciasProductos =
            new ContextMenu();

    private Producto productoSeleccionado;

    private boolean actualizandoComboProducto = false;

    private static final int MAX_SUGERENCIAS = 12;

    @FXML
    private ComboBox<Cliente> comboCliente;

    @FXML
    private ComboBox<String> comboMetodoPago;

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private ComboBox<Producto> comboProducto;

    @FXML
    private RadioButton radioCodigoBarras;

    @FXML
    private RadioButton radioNombreMarca;

    @FXML
    private ToggleGroup grupoBusqueda;

    @FXML
    private TextField txtCantidad;

    @FXML
    private TableView<DetalleVenta> tablaDetalles;

    @FXML
    private TableColumn<DetalleVenta, String> columnaProducto;

    @FXML
    private TableColumn<DetalleVenta, Double> columnaCantidad;

    @FXML
    private TableColumn<DetalleVenta, Double> columnaPrecio;

    @FXML
    private TableColumn<DetalleVenta, Double> columnaSubtotal;

    @FXML
    private TableColumn<DetalleVenta, Void> columnaEliminar;

    @FXML
    private Label lblTotal;

    @FXML
    private Label lblSaldoPendiente;

    @FXML
    private TextField txtMontoPagado;

    @FXML
    public void initialize() {

        configurarClientes();
        configurarProductos();
        configurarBusquedaProductos();
        configurarMetodosPago();
        configurarTabla();
        configurarMontoPagado();
        configurarCodigoBarras();

        txtCantidad.setOnAction(event -> agregarProducto());

        tablaDetalles.setItems(detalles);

        actualizarTotales();

        Platform.runLater(() -> txtCodigoBarras.requestFocus());
    }

    // =====================================================
    // CLIENTES
    // =====================================================

    private void configurarClientes() {

        comboCliente.setItems(
                clienteServicio.consultarTodo()
        );

        comboCliente.setConverter(new StringConverter<Cliente>() {

            @Override
            public String toString(Cliente cliente) {

                if (cliente == null
                        || cliente.conseguirPersona() == null) {
                    return "";
                }

                return cliente.conseguirPersona().conseguirNombre();
            }

            @Override
            public Cliente fromString(String texto) {
                return null;
            }
        });
    }

    // =====================================================
    // PRODUCTOS Y BÚSQUEDA POR NOMBRE / MARCA
    // =====================================================

    private void configurarProductos() {

        productosDisponibles.setAll(
                productoServicio.consultarTodo()
        );

        // La lista del ComboBox permanece estable.
        comboProducto.setItems(productosDisponibles);

        comboProducto.setEditable(true);

        comboProducto.setConverter(new StringConverter<Producto>() {

            @Override
            public String toString(Producto producto) {
                return nombreProducto(producto);
            }

            @Override
            public Producto fromString(String texto) {
                return buscarProductoPorNombreExacto(texto);
            }
        });

        comboProducto.getEditor()
                .textProperty()
                .addListener((observable, anterior, nuevo) -> {

                    if (actualizandoComboProducto
                            || !radioNombreMarca.isSelected()) {
                        return;
                    }

                    // Si el usuario modifica el texto, la selección
                    // anterior deja de ser válida.
                    productoSeleccionado = null;

                    String texto = nuevo == null ? "" : nuevo;

                    if (texto.isBlank()) {
                        sugerenciasProductos.hide();
                        return;
                    }

                    mostrarSugerencias(texto);
                });

        // Selección desde la lista normal del ComboBox.
        comboProducto.setOnAction(event -> {

            if (actualizandoComboProducto
                    || !radioNombreMarca.isSelected()) {
                return;
            }

            Producto producto = comboProducto.getValue();

            if (producto == null) {
                return;
            }

            seleccionarProducto(producto);
        });

        // Enter en el campo editable.
        comboProducto.getEditor().setOnAction(event -> {

            if (!radioNombreMarca.isSelected()) {
                return;
            }

            if (productoSeleccionado == null) {

                Producto encontrado =
                        buscarProductoPorNombreExacto(
                                comboProducto.getEditor().getText()
                        );

                if (encontrado != null) {
                    seleccionarProducto(encontrado);
                }
            }

            if (productoSeleccionado != null) {
                sugerenciasProductos.hide();
                txtCantidad.requestFocus();
                txtCantidad.selectAll();
            }
        });
    }

    private String nombreProducto(Producto producto) {

        if (producto == null) {
            return "";
        }

        String nombre = producto.conseguirNombre() == null
                ? ""
                : producto.conseguirNombre();

        String marca = producto.conseguirMarca() == null
                ? ""
                : producto.conseguirMarca();

        return marca.isBlank()
                ? nombre
                : nombre + " - " + marca;
    }

    private void mostrarSugerencias(String texto) {

        if (!radioNombreMarca.isSelected()
                || comboProducto.isDisabled()) {
            sugerenciasProductos.hide();
            return;
        }

        String busqueda =
                texto.trim().toLowerCase(Locale.ROOT);

        sugerenciasProductos.getItems().clear();

        if (busqueda.isEmpty()) {
            sugerenciasProductos.hide();
            return;
        }

        int encontrados = 0;

        for (Producto producto : productosDisponibles) {

            String nombre = producto.conseguirNombre() == null
                    ? ""
                    : producto.conseguirNombre()
                            .toLowerCase(Locale.ROOT);

            String marca = producto.conseguirMarca() == null
                    ? ""
                    : producto.conseguirMarca()
                            .toLowerCase(Locale.ROOT);

            String descripcion =
                    nombreProducto(producto).toLowerCase(Locale.ROOT);

            boolean coincide =
                    nombre.contains(busqueda)
                    || marca.contains(busqueda)
                    || descripcion.contains(busqueda);

            if (!coincide) {
                continue;
            }

            MenuItem opcion =
                    new MenuItem(nombreProducto(producto));

            opcion.setOnAction(event -> {
                seleccionarProducto(producto);
            });

            sugerenciasProductos.getItems().add(opcion);

            encontrados++;

            if (encontrados >= MAX_SUGERENCIAS) {
                break;
            }
        }

        if (sugerenciasProductos.getItems().isEmpty()) {
            sugerenciasProductos.hide();
            return;
        }

        // Mostrar las sugerencias debajo del editor.
        Platform.runLater(() -> {

            if (!radioNombreMarca.isSelected()
                    || comboProducto.isDisabled()
                    || !comboProducto.getEditor().getText()
                            .equals(texto)) {
                return;
            }

            if (comboProducto.getEditor().getScene() == null) {
                return;
            }

            if (sugerenciasProductos.isShowing()) {
                sugerenciasProductos.hide();
            }

            sugerenciasProductos.show(
                    comboProducto.getEditor(),
                    javafx.geometry.Side.BOTTOM,
                    0,
                    0
            );
        });
    }

    private void seleccionarProducto(Producto producto) {

        if (producto == null) {
            return;
        }

        sugerenciasProductos.hide();

        actualizandoComboProducto = true;

        try {

            productoSeleccionado = producto;

            comboProducto.setValue(producto);

            comboProducto.getEditor().setText(
                    nombreProducto(producto)
            );

            comboProducto.getEditor().positionCaret(
                    comboProducto.getEditor().getText().length()
            );

        } finally {
            actualizandoComboProducto = false;
        }

        if (txtCantidad.getText() == null
                || txtCantidad.getText().isBlank()) {
            txtCantidad.setText("1");
        }

        Platform.runLater(() -> {

            txtCantidad.requestFocus();
            txtCantidad.selectAll();
        });
    }

    private Producto buscarProductoPorNombreExacto(String texto) {

        if (texto == null || texto.isBlank()) {
            return null;
        }

        String buscado = texto.trim();

        for (Producto producto : productosDisponibles) {

            if (nombreProducto(producto)
                    .equalsIgnoreCase(buscado)) {
                return producto;
            }
        }

        return null;
    }

    // =====================================================
    // CAMBIAR TIPO DE BÚSQUEDA
    // =====================================================

    private void configurarBusquedaProductos() {

        radioCodigoBarras.setSelected(true);

        cambiarTipoBusqueda();
    }

    @FXML
    private void cambiarTipoBusqueda() {

        boolean buscarPorCodigo =
                radioCodigoBarras.isSelected();

        sugerenciasProductos.hide();

        txtCodigoBarras.setDisable(!buscarPorCodigo);

        comboProducto.setDisable(buscarPorCodigo);

        limpiarSeleccionProducto();

        if (buscarPorCodigo) {

            Platform.runLater(
                    () -> txtCodigoBarras.requestFocus()
            );

        } else {

            Platform.runLater(() -> {
                comboProducto.getEditor().requestFocus();
            });
        }
    }

    private void limpiarSeleccionProducto() {

        sugerenciasProductos.hide();

        actualizandoComboProducto = true;

        try {

            productoSeleccionado = null;

            comboProducto.hide();

            comboProducto.getSelectionModel().clearSelection();

            comboProducto.setValue(null);

            comboProducto.getEditor().clear();

            txtCodigoBarras.clear();

        } finally {
            actualizandoComboProducto = false;
        }
    }

    // =====================================================
    // MÉTODOS DE PAGO
    // =====================================================

    private void configurarMetodosPago() {

        comboMetodoPago.setItems(
                FXCollections.observableArrayList(
                        "Efectivo",
                        "Transferencia",
                        "Crédito"
                )
        );
    }

    // =====================================================
    // TABLA DE DETALLES
    // =====================================================

    private void configurarTabla() {

        columnaProducto.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue()
                                .conseguirProducto()
                                .conseguirNombre()
                )
        );

        columnaCantidad.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue().conseguirCantidad()
                )
        );

        columnaPrecio.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue().conseguirPrecioUnitario()
                )
        );

        columnaSubtotal.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue().conseguirSubtotal()
                )
        );

        configurarColumnaEliminar();
    }

    private void configurarColumnaEliminar() {

        columnaEliminar.setCellFactory(columna -> new TableCell<>() {

            private final Button boton = new Button("Quitar");

            {
                boton.getStyleClass().add("boton-secundario");

                boton.setOnAction(event -> {

                    DetalleVenta detalle =
                            getTableRow().getItem();

                    if (detalle != null) {

                        detalles.remove(detalle);

                        actualizarTotales();

                        if (radioCodigoBarras.isSelected()) {
                            txtCodigoBarras.requestFocus();
                        } else {
                            comboProducto.getEditor().requestFocus();
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {

                super.updateItem(item, empty);

                setGraphic(empty ? null : boton);
            }
        });
    }

    // =====================================================
    // MONTO PAGADO
    // =====================================================

    private void configurarMontoPagado() {

        txtMontoPagado.textProperty().addListener(
                (observable, anterior, nuevo) ->
                        actualizarSaldoPendiente()
        );
    }

    // =====================================================
    // CÓDIGO DE BARRAS
    // =====================================================

    private void configurarCodigoBarras() {

        txtCodigoBarras.setOnAction(
                event -> buscarCodigoBarras()
        );
    }

    private Producto buscarProductoPorCodigo(String codigo) {

        if (codigo == null || codigo.isBlank()) {
            return null;
        }

        return productoServicio.consultarCodigoBarras(
                codigo.trim()
        );
    }

    private void buscarCodigoBarras() {

        if (!radioCodigoBarras.isSelected()) {
            return;
        }

        String codigo = txtCodigoBarras.getText() == null
                ? ""
                : txtCodigoBarras.getText().trim();

        if (codigo.isEmpty()) {
            return;
        }

        try {

            Producto producto =
                    buscarProductoPorCodigo(codigo);

            if (producto == null
                    || producto.conseguirId() == null) {

                mostrarError(
                        "No existe un producto con el código de barras "
                        + codigo + "."
                );

                txtCodigoBarras.selectAll();
                return;
            }

            if (txtCantidad.getText() == null
                    || txtCantidad.getText().isBlank()) {
                txtCantidad.setText("1");
            }

            // El escáner agrega automáticamente con Enter.
            agregarProductoSeleccionado(producto);

        } catch (RuntimeException e) {

            mostrarError(
                    "No se pudo buscar el producto.\n"
                    + e.getMessage()
            );

            txtCodigoBarras.selectAll();
        }
    }

    // =====================================================
    // AGREGAR PRODUCTOS
    // =====================================================

    @FXML
    private void agregarProducto() {

        Producto producto = null;

        if (radioCodigoBarras.isSelected()) {

            String codigo =
                    txtCodigoBarras.getText() == null
                    ? ""
                    : txtCodigoBarras.getText().trim();

            if (codigo.isEmpty()) {

                mostrarError(
                        "Debe ingresar un código de barras."
                );

                return;
            }

            try {

                producto = buscarProductoPorCodigo(codigo);

            } catch (RuntimeException e) {

                mostrarError(
                        "No se pudo consultar el producto.\n"
                        + e.getMessage()
                );

                return;
            }

        } else {

            producto = productoSeleccionado;

            if (producto == null) {

                producto = buscarProductoPorNombreExacto(
                        comboProducto.getEditor().getText()
                );
            }
        }

        if (producto == null) {

            mostrarError(
                    "Debe seleccionar un producto válido."
            );

            return;
        }

        agregarProductoSeleccionado(producto);
    }

    private void agregarProductoSeleccionado(Producto producto) {

        if (producto == null
                || producto.conseguirId() == null) {

            mostrarError(
                    "Debe seleccionar un producto válido."
            );

            return;
        }

        double cantidad;

        try {

            cantidad = Double.parseDouble(
                    txtCantidad.getText().trim()
            );

        } catch (NumberFormatException | NullPointerException e) {

            mostrarError(
                    "La cantidad debe ser un número válido."
            );

            return;
        }

        if (!Double.isFinite(cantidad) || cantidad <= 0) {

            mostrarError(
                    "La cantidad debe ser mayor que cero."
            );

            return;
        }

        if (producto.conseguirPrecio() == null) {

            mostrarError(
                    "El producto no tiene un precio registrado."
            );

            return;
        }

        double precio = producto.conseguirPrecio();

        if (!Double.isFinite(precio) || precio < 0) {

            mostrarError(
                    "El precio del producto no es válido."
            );

            return;
        }

        double existencias =
                producto.conseguirExistencias() == null
                ? 0.0
                : producto.conseguirExistencias();

        DetalleVenta existente =
                buscarDetalleProducto(
                        producto.conseguirId()
                );

        double cantidadActual =
                existente == null
                ? 0.0
                : existente.conseguirCantidad();

        double cantidadFinal =
                cantidadActual + cantidad;

        if (!Double.isFinite(cantidadFinal)
                || cantidadFinal > existencias) {

            mostrarError(
                    "No hay existencias suficientes de "
                    + producto.conseguirNombre()
                    + ".\nDisponible: "
                    + existencias
            );

            return;
        }

        if (existente != null) {

            existente.colocarCantidad(cantidadFinal);

            existente.colocarSubtotal(
                    cantidadFinal
                    * existente.conseguirPrecioUnitario()
            );

            tablaDetalles.refresh();

        } else {

            DetalleVenta detalle =
                    new DetalleVenta();

            detalle.colocarProducto(producto);

            detalle.colocarCantidad(cantidad);

            detalle.colocarPrecioUnitario(precio);

            detalle.colocarSubtotal(
                    cantidad * precio
            );

            detalles.add(detalle);
        }

        limpiarProducto();

        actualizarTotales();
    }

    private DetalleVenta buscarDetalleProducto(Integer idProducto) {

        if (idProducto == null) {
            return null;
        }

        for (DetalleVenta detalle : detalles) {

            if (detalle.conseguirProducto() != null
                    && detalle.conseguirProducto()
                            .conseguirId() != null
                    && detalle.conseguirProducto()
                            .conseguirId()
                            .equals(idProducto)) {

                return detalle;
            }
        }

        return null;
    }

    private void limpiarProducto() {

        limpiarSeleccionProducto();

        txtCantidad.clear();

        if (radioCodigoBarras.isSelected()) {

            Platform.runLater(
                    () -> txtCodigoBarras.requestFocus()
            );

        } else {

            Platform.runLater(() -> {
                comboProducto.getEditor().requestFocus();
            });
        }
    }

    // =====================================================
    // TOTALES Y SALDOS
    // =====================================================

    private void actualizarTotales() {

        double total =
                ventaServicio.calcularTotal(detalles);

        lblTotal.setText(
                String.format(
                        "$ %,.0f",
                        total
                )
        );

        actualizarSaldoPendiente();
    }

    private void actualizarSaldoPendiente() {

        double total =
                ventaServicio.calcularTotal(detalles);

        double pagado = 0.0;

        try {

            if (txtMontoPagado.getText() != null
                    && !txtMontoPagado.getText().isBlank()) {

                pagado = Double.parseDouble(
                        txtMontoPagado.getText().trim()
                );
            }

        } catch (NumberFormatException e) {

            pagado = 0.0;
        }

        double saldo =
                Math.max(0, total - pagado);

        lblSaldoPendiente.setText(
                String.format(
                        "$ %,.0f",
                        saldo
                )
        );
    }

    // =====================================================
    // PAGAR TODO
    // =====================================================

    @FXML
    private void pagarTodo() {

        double total =
                ventaServicio.calcularTotal(detalles);

        if (total <= 0) {

            mostrarError(
                    "Debe agregar productos antes de realizar el pago."
            );

            return;
        }

        txtMontoPagado.setText(
                String.valueOf(total)
        );
    }

    // =====================================================
    // FINALIZAR VENTA
    // =====================================================

    @FXML
    private void finalizarVenta() {

        if (detalles.isEmpty()) {

            mostrarError(
                    "Debe agregar al menos un producto a la venta."
            );

            return;
        }

        String metodoPago =
                comboMetodoPago.getValue();

        if (metodoPago == null
                || metodoPago.isBlank()) {

            mostrarError(
                    "Debe seleccionar un método de pago."
            );

            return;
        }

        double montoPagado;

        try {

            if (txtMontoPagado.getText() == null
                    || txtMontoPagado.getText().isBlank()) {

                montoPagado = 0.0;

            } else {

                montoPagado = Double.parseDouble(
                        txtMontoPagado.getText().trim()
                );
            }

        } catch (NumberFormatException e) {

            mostrarError(
                    "El monto pagado debe ser un número válido."
            );

            return;
        }

        if (!Double.isFinite(montoPagado)
                || montoPagado < 0) {

            mostrarError(
                    "El monto pagado no es válido."
            );

            return;
        }

        double total =
                ventaServicio.calcularTotal(detalles);

        if (montoPagado > total) {

            mostrarError(
                    "El monto pagado no puede ser mayor "
                    + "al total de la venta."
            );

            return;
        }

        double saldoPendiente =
                total - montoPagado;

        Cliente cliente =
                comboCliente.getValue();

        if (saldoPendiente > 0
                && cliente == null) {

            mostrarError(
                    "Debe seleccionar un cliente "
                    + "cuando existe saldo pendiente."
            );

            return;
        }

        try {

            Venta venta = new Venta();

            venta.colocarCliente(cliente);

            venta.colocarMetodoPago(metodoPago);

            venta.colocarMontoPagado(montoPagado);

            int idVenta =
                    ventaServicio.registrarVenta(
                            venta,
                            detalles
                    );

            mostrarInformacion(
                    "Venta registrada correctamente.\n"
                    + "Número de venta: "
                    + idVenta
            );

            // Limpiar los datos después del registro.
            detalles.clear();

            comboCliente.setValue(null);

            comboMetodoPago.setValue(null);

            limpiarProducto();

            txtMontoPagado.clear();

            // Actualizar las existencias.
            productosDisponibles.setAll(
                    productoServicio.consultarTodo()
            );

            // Actualizar los saldos de clientes.
            comboCliente.setItems(
                    clienteServicio.consultarTodo()
            );

            actualizarTotales();

            tablaDetalles.refresh();

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (RuntimeException e) {

            mostrarError(
                    "No se pudo registrar la venta.\n"
                    + e.getMessage()
            );
        }
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
