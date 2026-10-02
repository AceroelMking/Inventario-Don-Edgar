package com.gestion.controller.reporte;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.gestion.model.PagoCliente;
import com.gestion.model.Venta;
import com.gestion.repository.PagoClienteRepositorio;
import com.gestion.service.ReportePdfServicio;
import com.gestion.service.VentaServicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ReporteVentasControlador {

    /*
     * =====================================================
     * SERVICIOS Y REPOSITORIOS
     * =====================================================
     */

    private final VentaServicio ventaServicio =
        new VentaServicio();

    private final ReportePdfServicio reportePdfServicio =
        new ReportePdfServicio();

    private final PagoClienteRepositorio pagoClienteRepositorio =
        new PagoClienteRepositorio();


    /*
     * =====================================================
     * LISTAS
     * =====================================================
     */

    private final ObservableList<Venta> ventas =
        FXCollections.observableArrayList();

    private final FilteredList<Venta> ventasFiltradas =
        new FilteredList<>(
            ventas,
            venta -> true
        );


    /*
     * =====================================================
     * FILTROS
     * =====================================================
     */

    @FXML
    private DatePicker fechaDesde;

    @FXML
    private DatePicker fechaHasta;

    @FXML
    private ComboBox<String> cmbMetodoPago;


    /*
     * =====================================================
     * TABLA
     * =====================================================
     */

    @FXML
    private TableView<Venta> tablaVentas;

    @FXML
    private TableColumn<Venta, Integer> columnaId;

    @FXML
    private TableColumn<Venta, String> columnaFecha;

    @FXML
    private TableColumn<Venta, String> columnaCliente;

    @FXML
    private TableColumn<Venta, String> columnaMetodo;

    @FXML
    private TableColumn<Venta, Double> columnaTotal;

    @FXML
    private TableColumn<Venta, Double> columnaPagado;

    @FXML
    private TableColumn<Venta, Double> columnaPendiente;


    /*
     * =====================================================
     * RESUMEN DE VENTAS
     * =====================================================
     */

    @FXML
    private Label lblCantidadVentas;

    @FXML
    private Label lblTotalVendido;

    @FXML
    private Label lblTotalPagado;


    /*
     * =====================================================
     * ESTADO DEL CRÉDITO
     * =====================================================
     */

    @FXML
    private Label lblTotalPendiente;

    @FXML
    private Label lblCreditoRecuperado;

    @FXML
    private Label lblCreditoPendiente;

    @FXML
    private Label lblPorcentajeRecuperado;

    @FXML
    private ProgressBar barraRecuperacion;


    /*
     * =====================================================
     * INICIALIZAR
     * =====================================================
     */

    @FXML
    public void initialize() {

        configurarTabla();

        configurarFiltros();

        /*
         * Ajusta automáticamente las columnas
         * al ancho disponible de la tabla.
         */
        configurarAnchoTabla();

        tablaVentas.setItems(
            ventasFiltradas
        );

        cargarVentas();
    }


    /*
     * =====================================================
     * CONFIGURAR TABLA
     * =====================================================
     */

    private void configurarTabla() {

        /*
         * ID
         */

        columnaId.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    celda
                        .getValue()
                        .conseguirId()
                )
        );


        /*
         * FECHA
         */

        columnaFecha.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirFecha()
                    )
                )
        );


        /*
         * CLIENTE
         */

        columnaCliente.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    conseguirNombreCliente(
                        celda.getValue()
                    )
                )
        );


        /*
         * MÉTODO DE PAGO
         */

        columnaMetodo.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirMetodoPago()
                    )
                )
        );


        /*
         * TOTAL
         */

        columnaTotal.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirTotal()
                    )
                )
        );


        /*
         * PAGADO AL VENDER
         */

        columnaPagado.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirMontoPagado()
                    )
                )
        );


        /*
         * CRÉDITO GENERADO
         */

        columnaPendiente.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirSaldoPendiente()
                    )
                )
        );


        /*
         * FORMATO DE DINERO
         */

        configurarColumnaDinero(
            columnaTotal
        );

        configurarColumnaDinero(
            columnaPagado
        );

        configurarColumnaDinero(
            columnaPendiente
        );
    }


    /*
     * =====================================================
     * AJUSTAR COLUMNAS AL ANCHO DE LA TABLA
     * =====================================================
     */

    private void configurarAnchoTabla() {

        tablaVentas
            .widthProperty()
            .addListener(
                (
                    observable,
                    anchoAnterior,
                    anchoNuevo
                ) -> {

                    ajustarAnchoColumnas(
                        anchoNuevo.doubleValue()
                    );
                }
            );


        /*
         * También intentamos realizar el ajuste
         * cuando JavaFX termine de mostrar la tabla.
         */

        tablaVentas
            .sceneProperty()
            .addListener(
                (
                    observable,
                    escenaAnterior,
                    escenaNueva
                ) -> {

                    if (
                        escenaNueva != null
                    ) {

                        tablaVentas.applyCss();
                        tablaVentas.layout();

                        ajustarAnchoColumnas(
                            tablaVentas.getWidth()
                        );
                    }
                }
            );
    }


    /*
     * =====================================================
     * DISTRIBUIR ANCHO DE COLUMNAS
     * =====================================================
     */

    private void ajustarAnchoColumnas(
        double anchoTabla
    ) {

        if (
            anchoTabla <= 0
        ) {

            return;
        }


        /*
         * Dejamos un pequeño margen para la barra
         * vertical, bordes y espacio interno de la tabla.
         */

        double disponible =
            Math.max(
                0,
                anchoTabla - 30
            );


        /*
         * DISTRIBUCIÓN:
         *
         * ID                 5 %
         * Fecha             16 %
         * Cliente           21 %
         * Método            13 %
         * Total             14 %
         * Pagado            16 %
         * Crédito           15 %
         *
         * TOTAL            100 %
         */

        columnaId.setPrefWidth(
            disponible * 0.05
        );

        columnaFecha.setPrefWidth(
            disponible * 0.16
        );

        columnaCliente.setPrefWidth(
            disponible * 0.21
        );

        columnaMetodo.setPrefWidth(
            disponible * 0.13
        );

        columnaTotal.setPrefWidth(
            disponible * 0.14
        );

        columnaPagado.setPrefWidth(
            disponible * 0.16
        );

        columnaPendiente.setPrefWidth(
            disponible * 0.15
        );
    }


    /*
     * =====================================================
     * FORMATEAR COLUMNAS DE DINERO
     * =====================================================
     */

    private void configurarColumnaDinero(
        TableColumn<Venta, Double> columna
    ) {

        columna.setCellFactory(
            tabla ->
                new TableCell<Venta, Double>() {

                    @Override
                    protected void updateItem(
                        Double valor,
                        boolean vacia
                    ) {

                        super.updateItem(
                            valor,
                            vacia
                        );


                        if (
                            vacia
                            || valor == null
                        ) {

                            setText(
                                null
                            );

                        } else {

                            setText(
                                formatoDinero(
                                    valor
                                )
                            );
                        }
                    }
                }
        );
    }


    /*
     * =====================================================
     * CONFIGURAR FILTROS
     * =====================================================
     */

    private void configurarFiltros() {

        cmbMetodoPago
            .getItems()
            .setAll(
                "Todos",
                "Efectivo",
                "Transferencia",
                "Crédito"
            );


        cmbMetodoPago.setValue(
            "Todos"
        );


        /*
         * FILTRO POR MÉTODO
         */

        cmbMetodoPago.setOnAction(
            evento ->
                aplicarFiltros()
        );


        /*
         * FILTRO DESDE
         */

        fechaDesde
            .valueProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) ->
                    aplicarFiltros()
            );


        /*
         * FILTRO HASTA
         */

        fechaHasta
            .valueProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) ->
                    aplicarFiltros()
            );
    }


    /*
     * =====================================================
     * CARGAR VENTAS
     * =====================================================
     */

    private void cargarVentas() {

        try {

            ventas.setAll(
                ventaServicio
                    .consultarVentas()
            );


            aplicarFiltros();


        } catch (
            RuntimeException e
        ) {

            mostrarError(
                "No se pudieron cargar las ventas.",
                e
            );
        }
    }


    /*
     * =====================================================
     * APLICAR FILTROS
     * =====================================================
     */

    @FXML
    private void aplicarFiltros() {

        LocalDate desde =
            fechaDesde.getValue();

        LocalDate hasta =
            fechaHasta.getValue();

        String metodo =
            cmbMetodoPago.getValue();


        /*
         * VALIDAR RANGO
         */

        if (
            desde != null
            && hasta != null
            && desde.isAfter(
                hasta
            )
        ) {

            Alert alerta =
                new Alert(
                    Alert.AlertType.WARNING
                );


            alerta.setTitle(
                "Fechas incorrectas"
            );

            alerta.setHeaderText(
                null
            );

            alerta.setContentText(
                "La fecha inicial no puede ser "
                + "posterior a la fecha final."
            );

            alerta.showAndWait();

            return;
        }


        ventasFiltradas.setPredicate(
            venta -> {

                /*
                 * ===============================
                 * MÉTODO DE PAGO
                 * ===============================
                 */

                if (
                    metodo != null
                    && !metodo.equalsIgnoreCase(
                        "Todos"
                    )
                ) {

                    if (
                        venta.conseguirMetodoPago()
                        == null
                    ) {

                        return false;
                    }


                    if (
                        !venta
                            .conseguirMetodoPago()
                            .equalsIgnoreCase(
                                metodo
                            )
                    ) {

                        return false;
                    }
                }


                /*
                 * ===============================
                 * FECHA
                 * ===============================
                 */

                LocalDate fechaVenta =
                    convertirFecha(
                        venta.conseguirFecha()
                    );


                if (
                    (
                        desde != null
                        || hasta != null
                    )
                    && fechaVenta == null
                ) {

                    return false;
                }


                if (
                    desde != null
                    && fechaVenta.isBefore(
                        desde
                    )
                ) {

                    return false;
                }


                if (
                    hasta != null
                    && fechaVenta.isAfter(
                        hasta
                    )
                ) {

                    return false;
                }


                return true;
            }
        );


        actualizarTotales();

        tablaVentas.refresh();
    }


    /*
     * =====================================================
     * CONVERTIR FECHA
     * =====================================================
     */

    private LocalDate convertirFecha(
        String fecha
    ) {

        if (
            fecha == null
            || fecha.isBlank()
        ) {

            return null;
        }


        DateTimeFormatter formatoFechaHora =
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
            );


        try {

            return LocalDateTime
                .parse(
                    fecha,
                    formatoFechaHora
                )
                .toLocalDate();


        } catch (
            DateTimeParseException e
        ) {

            try {

                return LocalDate.parse(
                    fecha,
                    DateTimeFormatter
                        .ISO_LOCAL_DATE
                );


            } catch (
                DateTimeParseException ex
            ) {

                return null;
            }
        }
    }


    /*
     * =====================================================
     * ACTUALIZAR INDICADORES
     * =====================================================
     */

    private void actualizarTotales() {

        double totalVendido =
            0.0;

        double totalPagado =
            0.0;

        double creditoGenerado =
            0.0;


        /*
         * ===============================
         * CALCULAR VENTAS
         * ===============================
         */

        for (
            Venta venta
            : ventasFiltradas
        ) {

            totalVendido +=
                valorSeguro(
                    venta
                        .conseguirTotal()
                );


            totalPagado +=
                valorSeguro(
                    venta
                        .conseguirMontoPagado()
                );


            creditoGenerado +=
                valorSeguro(
                    venta
                        .conseguirSaldoPendiente()
                );
        }


        /*
         * ===============================
         * CRÉDITO RECUPERADO
         * ===============================
         */

        double creditoRecuperado =
            calcularCreditoRecuperado(
                fechaDesde.getValue(),
                fechaHasta.getValue()
            );


        /*
         * Para el indicador visual no permitimos
         * recuperar más de lo generado.
         */

        double recuperadoAplicable =
            Math.min(
                creditoRecuperado,
                creditoGenerado
            );


        /*
         * ===============================
         * CRÉDITO PENDIENTE
         * ===============================
         */

        double creditoPendiente =
            Math.max(
                0.0,
                creditoGenerado
                    - recuperadoAplicable
            );


        /*
         * ===============================
         * PORCENTAJE DE RECUPERACIÓN
         * ===============================
         */

        double porcentajeRecuperado =
            0.0;


        if (
            creditoGenerado > 0
        ) {

            porcentajeRecuperado =
                (
                    recuperadoAplicable
                    / creditoGenerado
                )
                * 100.0;
        }


        double progreso =
            porcentajeRecuperado
                / 100.0;


        /*
         * ===============================
         * RESUMEN DE VENTAS
         * ===============================
         */

        lblCantidadVentas.setText(
            String.valueOf(
                ventasFiltradas.size()
            )
        );


        lblTotalVendido.setText(
            formatoDinero(
                totalVendido
            )
        );


        lblTotalPagado.setText(
            formatoDinero(
                totalPagado
            )
        );


        /*
         * ===============================
         * ESTADO DEL CRÉDITO
         * ===============================
         */

        lblTotalPendiente.setText(
            formatoDinero(
                creditoGenerado
            )
        );


        lblCreditoRecuperado.setText(
            formatoDinero(
                creditoRecuperado
            )
        );


        lblCreditoPendiente.setText(
            formatoDinero(
                creditoPendiente
            )
        );


       lblPorcentajeRecuperado.setText(
            String.format(
                "%.0f %%",
                porcentajeRecuperado
            )
        );


        barraRecuperacion.setProgress(
            progreso
        );
    }


    /*
     * =====================================================
     * CALCULAR CRÉDITO RECUPERADO
     * =====================================================
     */

    private double calcularCreditoRecuperado(
        LocalDate desde,
        LocalDate hasta
    ) {

        double total =
            0.0;


        ObservableList<PagoCliente> pagos =
            pagoClienteRepositorio
                .consultarTodos();


        for (
            PagoCliente pago
            : pagos
        ) {

            LocalDate fechaPago =
                convertirFecha(
                    pago.conseguirFecha()
                );


            /*
             * Si existen filtros de fecha y
             * no podemos interpretar la fecha,
             * ignoramos ese pago.
             */

            if (
                (
                    desde != null
                    || hasta != null
                )
                && fechaPago == null
            ) {

                continue;
            }


            /*
             * DESDE
             */

            if (
                desde != null
                && fechaPago.isBefore(
                    desde
                )
            ) {

                continue;
            }


            /*
             * HASTA
             */

            if (
                hasta != null
                && fechaPago.isAfter(
                    hasta
                )
            ) {

                continue;
            }


            /*
             * SUMAR ABONO
             */

            total +=
                valorSeguro(
                    pago.conseguirMonto()
                );
        }


        return total;
    }


    /*
     * =====================================================
     * LIMPIAR FILTROS
     * =====================================================
     */

    @FXML
    private void limpiarFiltros() {

        fechaDesde.setValue(
            null
        );


        fechaHasta.setValue(
            null
        );


        cmbMetodoPago.setValue(
            "Todos"
        );


        aplicarFiltros();
    }


    /*
     * =====================================================
     * ACTUALIZAR
     * =====================================================
     */

    @FXML
    private void actualizar() {

        cargarVentas();
    }


    /*
     * =====================================================
     * ABRIR REPORTE DE INVENTARIO
     * =====================================================
     */

    @FXML
    private void abrirInventario() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/reporte/ReporteInventario.fxml"
                    )
                );


            Parent vista =
                loader.load();


            BorderPane panelPrincipal =
                (BorderPane) tablaVentas
                    .getScene()
                    .getRoot();


            panelPrincipal.setCenter(
                vista
            );


        } catch (
            IOException e
        ) {

            mostrarError(
                "No se pudo abrir el reporte de inventario.",
                e
            );
        }
    }


    /*
     * =====================================================
     * ABRIR CUENTAS POR COBRAR
     * =====================================================
     */

    @FXML
    private void abrirCuentasCobrar() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/reporte/ReporteCuentasCobrar.fxml"
                    )
                );


            Parent vista =
                loader.load();


            BorderPane panelPrincipal =
                (BorderPane) tablaVentas
                    .getScene()
                    .getRoot();


            panelPrincipal.setCenter(
                vista
            );


        } catch (
            IOException e
        ) {

            mostrarError(
                "No se pudo abrir el reporte de cuentas por cobrar.",
                e
            );
        }
    }


    /*
     * =====================================================
     * EXPORTAR PDF
     * =====================================================
     */

    @FXML
    private void exportarPdf() {

        if (
            ventasFiltradas.isEmpty()
        ) {

            Alert alerta =
                new Alert(
                    Alert.AlertType.WARNING
                );


            alerta.setTitle(
                "Reporte vacío"
            );

            alerta.setHeaderText(
                null
            );

            alerta.setContentText(
                "No existen ventas para exportar."
            );

            alerta.showAndWait();

            return;
        }


        FileChooser selector =
            new FileChooser();


        selector.setTitle(
            "Guardar reporte de ventas"
        );


        selector.setInitialFileName(
            "reporte_ventas.pdf"
        );


        selector
            .getExtensionFilters()
            .add(
                new FileChooser.ExtensionFilter(
                    "Documento PDF",
                    "*.pdf"
                )
            );


        Window ventana =
            tablaVentas
                .getScene()
                .getWindow();


        File archivo =
            selector.showSaveDialog(
                ventana
            );


        if (
            archivo == null
        ) {

            return;
        }


        /*
         * AGREGAR .PDF SI EL USUARIO
         * NO LO ESCRIBIÓ.
         */

        if (
            !archivo
                .getName()
                .toLowerCase()
                .endsWith(".pdf")
        ) {

            archivo =
                new File(
                    archivo
                        .getAbsolutePath()
                    + ".pdf"
                );
        }


        try {

            reportePdfServicio
                .generarReporteVentas(
                    archivo,
                    conseguirVentasFiltradas(),
                    fechaDesde.getValue(),
                    fechaHasta.getValue(),
                    cmbMetodoPago.getValue()
                );


            Alert alerta =
                new Alert(
                    Alert.AlertType.INFORMATION
                );


            alerta.setTitle(
                "Reporte generado"
            );

            alerta.setHeaderText(
                null
            );

            alerta.setContentText(
                "El reporte PDF fue generado correctamente."
            );

            alerta.showAndWait();


        } catch (
            RuntimeException e
        ) {

            mostrarError(
                "No se pudo generar el PDF.",
                e
            );
        }
    }


    /*
     * =====================================================
     * CONSEGUIR VENTAS FILTRADAS
     * =====================================================
     */

    private ObservableList<Venta>
        conseguirVentasFiltradas() {

        return FXCollections
            .observableArrayList(
                ventasFiltradas
            );
    }


    /*
     * =====================================================
     * CONSEGUIR NOMBRE DEL CLIENTE
     * =====================================================
     */

    private String conseguirNombreCliente(
        Venta venta
    ) {

        if (
            venta == null
            || venta.conseguirCliente() == null
            || venta
                .conseguirCliente()
                .conseguirPersona() == null
            || venta
                .conseguirCliente()
                .conseguirPersona()
                .conseguirNombre() == null
            || venta
                .conseguirCliente()
                .conseguirPersona()
                .conseguirNombre()
                .isBlank()
        ) {

            return "Venta de contado";
        }


        return venta
            .conseguirCliente()
            .conseguirPersona()
            .conseguirNombre();
    }


    /*
     * =====================================================
     * VALOR SEGURO
     * =====================================================
     */

    private double valorSeguro(
        Double valor
    ) {

        if (
            valor == null
        ) {

            return 0.0;
        }


        return valor;
    }


    /*
     * =====================================================
     * TEXTO SEGURO
     * =====================================================
     */

    private String textoSeguro(
        String texto
    ) {

        if (
            texto == null
        ) {

            return "";
        }


        return texto;
    }


    /*
     * =====================================================
     * FORMATO DE DINERO
     * =====================================================
     */

    private String formatoDinero(
        double valor
    ) {

        return "$"
            + String.format(
                "%,.0f",
                valor
            )
            .replace(
                ',',
                '.'
            );
    }


    /*
     * =====================================================
     * MOSTRAR ERROR
     * =====================================================
     */

    private void mostrarError(
        String mensaje,
        Exception e
    ) {

        String detalle =
            e.getMessage();


        if (
            detalle == null
            || detalle.isBlank()
        ) {

            detalle =
                e.getClass()
                    .getSimpleName();
        }


        Alert alerta =
            new Alert(
                Alert.AlertType.ERROR
            );


        alerta.setTitle(
            "Error"
        );

        alerta.setHeaderText(
            null
        );

        alerta.setContentText(
            mensaje
            + "\n"
            + detalle
        );

        alerta.showAndWait();


        e.printStackTrace();
    }
}