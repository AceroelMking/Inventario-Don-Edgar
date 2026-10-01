package com.gestion.controller.reporte;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

import com.gestion.model.Venta;
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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ReporteVentasControlador {

    private final VentaServicio ventaServicio =
        new VentaServicio();

    private final ReportePdfServicio reportePdfServicio =
        new ReportePdfServicio();

    private final ObservableList<Venta> ventas =
        FXCollections.observableArrayList();

    private final FilteredList<Venta> ventasFiltradas =
        new FilteredList<>(
            ventas,
            venta -> true
        );


    @FXML
    private DatePicker fechaDesde;

    @FXML
    private DatePicker fechaHasta;

    @FXML
    private ComboBox<String> cmbMetodoPago;

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

    @FXML
    private Label lblCantidadVentas;

    @FXML
    private Label lblTotalVendido;

    @FXML
    private Label lblTotalPagado;

    @FXML
    private Label lblTotalPendiente;


    @FXML
    public void initialize() {

        configurarTabla();

        configurarFiltros();

        tablaVentas.setItems(
            ventasFiltradas
        );

        cargarVentas();
    }


    /*
     * =====================================================
     * CONFIGURACIÓN DE LA TABLA
     * =====================================================
     */

    private void configurarTabla() {

        columnaId.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    celda
                        .getValue()
                        .conseguirId()
                )
        );


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


        columnaCliente.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    conseguirNombreCliente(
                        celda.getValue()
                    )
                )
        );


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
    }


    /*
     * =====================================================
     * FILTROS
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


        cmbMetodoPago.setOnAction(
            evento -> aplicarFiltros()
        );


        fechaDesde
            .valueProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> aplicarFiltros()
            );


        fechaHasta
            .valueProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> aplicarFiltros()
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


        } catch (RuntimeException e) {

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


        if (
            desde != null
            && hasta != null
            && desde.isAfter(hasta)
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
                 * FILTRO POR MÉTODO DE PAGO
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
                 * CONVERTIR FECHA
                 */

                LocalDate fechaVenta =
                    convertirFecha(
                        venta.conseguirFecha()
                    );


                /*
                 * SI EXISTE FILTRO DE FECHA
                 * PERO LA FECHA NO ES VÁLIDA
                 */

                if (
                    (
                        desde != null
                        || hasta != null
                    )
                    && fechaVenta == null
                ) {

                    return false;
                }


                /*
                 * FECHA DESDE
                 */

                if (
                    desde != null
                    && fechaVenta.isBefore(
                        desde
                    )
                ) {

                    return false;
                }


                /*
                 * FECHA HASTA
                 */

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
     * ACTUALIZAR TOTALES
     * =====================================================
     */

    private void actualizarTotales() {

        double totalVendido =
            0.0;

        double totalPagado =
            0.0;

        double totalPendiente =
            0.0;


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


            totalPendiente +=
                valorSeguro(
                    venta
                        .conseguirSaldoPendiente()
                );
        }


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


        lblTotalPendiente.setText(
            formatoDinero(
                totalPendiente
            )
        );
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


        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir el reporte de inventario.",
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
         * NO LO ESCRIBIÓ
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
                "El reporte PDF fue generado "
                + "correctamente."
            );


            alerta.showAndWait();


        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo generar el PDF.",
                e
            );
        }
    }


    /*
     * =====================================================
     * OBTENER VENTAS FILTRADAS
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
     * OBTENER NOMBRE DEL CLIENTE
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