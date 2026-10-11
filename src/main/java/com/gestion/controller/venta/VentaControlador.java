package com.gestion.controller.venta;

import com.gestion.model.Cliente;
import com.gestion.model.DetalleVenta;
import com.gestion.model.Producto;
import com.gestion.model.Venta;
import com.gestion.service.ClienteServicio;
import com.gestion.service.ProductoServicio;
import com.gestion.service.VentaServicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.util.StringConverter;
import java.util.Locale;

import javafx.collections.transformation.FilteredList;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;


public class VentaControlador {

    private final VentaServicio ventaServicio =
        new VentaServicio();

    private final ClienteServicio clienteServicio =
        new ClienteServicio();

    private final ProductoServicio productoServicio =
        new ProductoServicio();

    private final ObservableList<DetalleVenta> detalles =
        FXCollections.observableArrayList();


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

    private final ObservableList<Producto> productosDisponibles =
            FXCollections.observableArrayList();

    private final FilteredList<Producto> productosFiltrados =
            new FilteredList<>(productosDisponibles);

    private boolean actualizandoComboProducto = false;

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


        /*
         * MEJORA:
         * Al presionar ENTER en el campo cantidad,
         * se agrega el producto sin necesidad
         * de utilizar el mouse.
         */
        txtCantidad.setOnAction(
            event -> agregarProducto()
        );


        tablaDetalles.setItems(detalles);

        actualizarTotales();

        txtCodigoBarras.requestFocus();
    }


    private void configurarClientes() {

        comboCliente.setItems(
            clienteServicio.consultarTodo()
        );


        comboCliente.setConverter(
            new StringConverter<>() {

                @Override
                public String toString(
                    Cliente cliente
                ) {

                    if (
                        cliente == null
                        || cliente.conseguirPersona() == null
                    ) {

                        return "";
                    }


                    return cliente
                        .conseguirPersona()
                        .conseguirNombre();
                }


                @Override
                public Cliente fromString(
                    String texto
                ) {

                    return null;
                }
            }
        );
    }

    private void configurarProductos() {

        productosDisponibles.setAll(
            productoServicio.consultarTodo()
        );

        comboProducto.setItems(productosFiltrados);
        comboProducto.setEditable(true);

        comboProducto.setConverter(new StringConverter<Producto>() {

            @Override
            public String toString(Producto producto) {

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

            @Override
            public Producto fromString(String texto) {
                return null;
            }
        });

        comboProducto.getEditor().textProperty().addListener(
            (observable, anterior, nuevo) -> {

                if (actualizandoComboProducto
                        || !radioNombreMarca.isSelected()) {
                    return;
                }

                Producto seleccionado = comboProducto.getSelectionModel()
                        .getSelectedItem();

                if (seleccionado != null
                        && comboProducto.getConverter()
                            .toString(seleccionado).equals(nuevo)) {
                    return;
                }

                String busqueda = nuevo == null
                        ? ""
                        : nuevo.trim().toLowerCase(Locale.ROOT);

                actualizandoComboProducto = true;

                try {

                    productosFiltrados.setPredicate(producto -> {

                        if (busqueda.isEmpty()) {
                            return true;
                        }

                        String nombre = producto.conseguirNombre() == null
                                ? ""
                                : producto.conseguirNombre()
                                    .toLowerCase(Locale.ROOT);

                        String marca = producto.conseguirMarca() == null
                                ? ""
                                : producto.conseguirMarca()
                                    .toLowerCase(Locale.ROOT);

                        return nombre.contains(busqueda)
                                || marca.contains(busqueda)
                                || (nombre + " " + marca).contains(busqueda);
                    });

                } finally {
                    actualizandoComboProducto = false;
                }
            }
        );

        comboProducto.setOnAction(event -> {

            if (actualizandoComboProducto) {
                return;
            }

            Producto seleccionado = comboProducto.getValue();

            if (seleccionado != null) {

                actualizandoComboProducto = true;

                try {
                    comboProducto.getEditor().setText(
                        comboProducto.getConverter().toString(seleccionado)
                    );
                } finally {
                    actualizandoComboProducto = false;
                }

                if (txtCantidad.getText().isBlank()) {
                    txtCantidad.setText("1");
                }

                txtCantidad.requestFocus();
                txtCantidad.selectAll();
            }
        });
    }

    private void configurarBusquedaProductos() {

        radioCodigoBarras.setSelected(true);

        cambiarTipoBusqueda();
    }

    @FXML
    private void cambiarTipoBusqueda() {

        boolean buscarPorCodigo = radioCodigoBarras.isSelected();

        txtCodigoBarras.setDisable(!buscarPorCodigo);
        comboProducto.setDisable(buscarPorCodigo);

        txtCodigoBarras.clear();

        actualizandoComboProducto = true;

        try {

            comboProducto.setValue(null);
            comboProducto.getEditor().clear();

            productosFiltrados.setPredicate(producto -> true);

        } finally {
            actualizandoComboProducto = false;
        }

        if (buscarPorCodigo) {

            txtCodigoBarras.requestFocus();

        } else {

            comboProducto.requestFocus();
        }
    }

    private void configurarMetodosPago() {

        comboMetodoPago.setItems(
            FXCollections.observableArrayList(
                "Efectivo",
                "Transferencia",
                "Crédito"
            )
        );
    }


    private void configurarTabla() {

        columnaProducto.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda
                    .getValue()
                    .conseguirProducto()
                    .conseguirNombre()
            )
        );


        columnaCantidad.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda
                    .getValue()
                    .conseguirCantidad()
            )
        );


        columnaPrecio.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda
                    .getValue()
                    .conseguirPrecioUnitario()
            )
        );


        columnaSubtotal.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda
                    .getValue()
                    .conseguirSubtotal()
            )
        );


        configurarColumnaEliminar();
    }


    private void configurarColumnaEliminar() {

        columnaEliminar.setCellFactory(
            columna -> new TableCell<>() {

                private final Button boton =
                    new Button("Quitar");


                {
                    boton
                        .getStyleClass()
                        .add("boton-secundario");


                    boton.setOnAction(
                        event -> {

                            DetalleVenta detalle =
                                getTableRow().getItem();


                            if (detalle != null) {

                                detalles.remove(
                                    detalle
                                );


                                actualizarTotales();


                                txtCodigoBarras.requestFocus();
                            }
                        }
                    );
                }


                @Override
                protected void updateItem(
                    Void item,
                    boolean empty
                ) {

                    super.updateItem(
                        item,
                        empty
                    );


                    setGraphic(
                        empty ? null : boton
                    );
                }
            }
        );
    }


    private void configurarMontoPagado() {

        txtMontoPagado
            .textProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> actualizarSaldoPendiente()
            );
    }


    private void configurarCodigoBarras() {

        /*
         * Los lectores USB normalmente funcionan
         * como un teclado.
         *
         * Escriben el código y posteriormente
         * envían ENTER.
         */

        txtCodigoBarras.setOnAction(
            event -> buscarCodigoBarras()
        );
    }


    private void buscarCodigoBarras() {

        String codigo =
            txtCodigoBarras
                .getText()
                .trim();


        if (codigo.isEmpty()) {
            return;
        }


        try {

            Producto producto =
                productoServicio
                    .consultarCodigoBarras(
                        codigo
                    );


            if (
                producto == null
                || producto.conseguirId() == null
            ) {

                mostrarError(
                    "No existe un producto con el código de barras "
                    + codigo
                    + "."
                );


                txtCodigoBarras.selectAll();

                return;
            }


            actualizandoComboProducto = true;

            try {
                comboProducto.setValue(producto);
            } finally {
                actualizandoComboProducto = false;
            }


            /*
             * Si el campo cantidad se encuentra vacío,
             * utilizamos una unidad por defecto.
             */
            if (txtCantidad.getText().isBlank()) {

                txtCantidad.setText("1");
            }


            /*
             * Cuando el código proviene del lector,
             * agregamos directamente el producto.
             */
            agregarProducto();


        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo buscar el producto.\n"
                + e.getMessage()
            );


            txtCodigoBarras.selectAll();
        }
    }

    @FXML
    private void pagarTodo() {

        double total = ventaServicio.calcularTotal(detalles);

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

    @FXML
    private void agregarProducto() {

        Producto producto =
            comboProducto.getValue();


        if (producto == null) {

            mostrarError(
                "Debe seleccionar un producto."
            );

            return;
        }


        double cantidad;


        try {

            cantidad =
                Double.parseDouble(
                    txtCantidad
                        .getText()
                        .trim()
                );


        } catch (NumberFormatException e) {

            mostrarError(
                "La cantidad debe ser un número válido."
            );

            return;
        }


        if (cantidad <= 0) {

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


        double existencias =
            producto.conseguirExistencias() == null
                ? 0.0
                : producto.conseguirExistencias();


        /*
         * Verificamos si el producto ya se encuentra
         * agregado en la venta.
         */
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


        /*
         * La cantidad total agregada del producto
         * no puede superar las existencias disponibles.
         */
        if (cantidadFinal > existencias) {

            mostrarError(
                "No hay existencias suficientes de "
                + producto.conseguirNombre()
                + ".\nDisponible: "
                + existencias
            );

            return;
        }


        /*
         * Si el producto ya estaba en la venta,
         * incrementamos su cantidad.
         */
        if (existente != null) {

            existente.colocarCantidad(
                cantidadFinal
            );


            existente.colocarSubtotal(
                cantidadFinal
                * existente.conseguirPrecioUnitario()
            );


            tablaDetalles.refresh();


        } else {

            /*
             * Si todavía no existe,
             * creamos un nuevo detalle.
             */
            DetalleVenta detalle =
                new DetalleVenta();


            detalle.colocarProducto(
                producto
            );


            detalle.colocarCantidad(
                cantidad
            );


            detalle.colocarPrecioUnitario(
                producto.conseguirPrecio()
            );


            detalle.colocarSubtotal(
                cantidad
                * producto.conseguirPrecio()
            );


            detalles.add(
                detalle
            );
        }


        limpiarProducto();

        actualizarTotales();
    }


    private DetalleVenta buscarDetalleProducto(
        Integer idProducto
    ) {

        if (idProducto == null) {
            return null;
        }


        for (
            DetalleVenta detalle
            : detalles
        ) {

            if (
                detalle.conseguirProducto() != null
                && detalle
                    .conseguirProducto()
                    .conseguirId() != null
                && detalle
                    .conseguirProducto()
                    .conseguirId()
                    .equals(idProducto)
            ) {

                return detalle;
            }
        }


        return null;
    }


    private void limpiarProducto() {

        actualizandoComboProducto = true;

        try {

            comboProducto.setValue(null);
            comboProducto.getEditor().clear();

            productosFiltrados.setPredicate(producto -> true);

        } finally {
            actualizandoComboProducto = false;
        }

        txtCantidad.clear();
        txtCodigoBarras.clear();

        if (radioCodigoBarras.isSelected()) {

            txtCodigoBarras.requestFocus();

        } else {

            comboProducto.requestFocus();
        }
    }


    private void actualizarTotales() {

        double total =
            ventaServicio.calcularTotal(
                detalles
            );


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
            ventaServicio.calcularTotal(
                detalles
            );


        double pagado =
            0.0;


        try {

            if (
                !txtMontoPagado
                    .getText()
                    .isBlank()
            ) {

                pagado =
                    Double.parseDouble(
                        txtMontoPagado
                            .getText()
                            .trim()
                    );
            }


        } catch (NumberFormatException e) {

            pagado = 0.0;
        }


        double saldo =
            Math.max(
                0,
                total - pagado
            );


        lblSaldoPendiente.setText(
            String.format(
                "$ %,.0f",
                saldo
            )
        );
    }


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


        if (
            metodoPago == null
            || metodoPago.isBlank()
        ) {

            mostrarError(
                "Debe seleccionar un método de pago."
            );

            return;
        }


        double montoPagado;


        try {

            if (txtMontoPagado.getText().isBlank()) {

                montoPagado = 0.0;


            } else {

                montoPagado =
                    Double.parseDouble(
                        txtMontoPagado
                            .getText()
                            .trim()
                    );
            }


        } catch (NumberFormatException e) {

            mostrarError(
                "El monto pagado debe ser un número válido."
            );

            return;
        }


        if (montoPagado < 0) {

            mostrarError(
                "El monto pagado no puede ser negativo."
            );

            return;
        }


        double total =
            ventaServicio.calcularTotal(
                detalles
            );


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


        /*
         * Si queda dinero pendiente,
         * necesitamos saber quién lo debe.
         */
        if (
            saldoPendiente > 0
            && cliente == null
        ) {

            mostrarError(
                "Debe seleccionar un cliente "
                + "cuando existe saldo pendiente."
            );

            return;
        }


        try {

            Venta venta =
                new Venta();


            venta.colocarCliente(
                cliente
            );


            venta.colocarMetodoPago(
                metodoPago
            );


            venta.colocarMontoPagado(
                montoPagado
            );


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


            /*
             * Limpiar la venta después
             * de registrarla correctamente.
             */
            detalles.clear();

            comboCliente.setValue(null);

            comboMetodoPago.setValue(null);

            comboProducto.setValue(null);

            txtCantidad.clear();

            txtCodigoBarras.clear();

            txtMontoPagado.clear();


            /*
             * Consultamos nuevamente los productos
             * para obtener las existencias actualizadas.
             */
            productosDisponibles.setAll(
                productoServicio.consultarTodo()
            );

            productosFiltrados.setPredicate(producto -> true);


            /*
             * Consultamos nuevamente los clientes
             * para obtener sus saldos actualizados.
             */
            comboCliente.setItems(
                clienteServicio.consultarTodo()
            );


            actualizarTotales();

            tablaDetalles.refresh();

            if (radioCodigoBarras.isSelected()) {
                txtCodigoBarras.requestFocus();
            } else {
                comboProducto.requestFocus();
            }


        } catch (IllegalArgumentException e) {

            mostrarError(
                e.getMessage()
            );


        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo registrar la venta.\n"
                + e.getMessage()
            );
        }
    }


    private void mostrarError(
        String mensaje
    ) {

        new Alert(
            Alert.AlertType.ERROR,
            mensaje
        ).showAndWait();
    }


    private void mostrarInformacion(
        String mensaje
    ) {

        new Alert(
            Alert.AlertType.INFORMATION,
            mensaje
        ).showAndWait();
    }
}