package com.gestion.controller.reporte;

import java.io.File;
import java.io.IOException;

import com.gestion.model.Producto;
import com.gestion.repository.ProductoRepositorio;
import com.gestion.service.ReportePdfServicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.stage.FileChooser;
import javafx.stage.Window;

public class ReporteInventarioControlador {

    /*
     * =====================================================
     * REPOSITORIOS Y SERVICIOS
     * =====================================================
     */

    private final ProductoRepositorio productoRepositorio =
        new ProductoRepositorio();

    private final ReportePdfServicio reportePdfServicio =
        new ReportePdfServicio();


    /*
     * =====================================================
     * LISTAS
     * =====================================================
     */

    private final ObservableList<Producto> productos =
        FXCollections.observableArrayList();

    private final FilteredList<Producto> productosFiltrados =
        new FilteredList<>(
            productos,
            producto -> true
        );


    /*
     * =====================================================
     * BÚSQUEDA
     * =====================================================
     */

    @FXML
    private TextField txtBuscar;


    /*
     * =====================================================
     * TABLA
     * =====================================================
     */

    @FXML
    private TableView<Producto> tablaInventario;

    @FXML
    private TableColumn<Producto, String> columnaCodigo;

    @FXML
    private TableColumn<Producto, String> columnaNombre;

    @FXML
    private TableColumn<Producto, String> columnaMarca;

    @FXML
    private TableColumn<Producto, String> columnaCategoria;

    @FXML
    private TableColumn<Producto, Double> columnaExistencias;

    @FXML
    private TableColumn<Producto, Double> columnaMinimo;

    @FXML
    private TableColumn<Producto, String> columnaUnidad;

    @FXML
    private TableColumn<Producto, String> columnaEstado;


    /*
     * =====================================================
     * RESUMEN
     * =====================================================
     */

    @FXML
    private Label lblCantidadProductos;

    @FXML
    private Label lblStockBajo;

    @FXML
    private Label lblSinExistencias;


    /*
     * =====================================================
     * INICIALIZAR
     * =====================================================
     */

    @FXML
    public void initialize() {

        configurarTabla();

        configurarBusqueda();

        configurarAnchoTabla();

        tablaInventario.setItems(
            productosFiltrados
        );

        cargarProductos();
    }


    /*
     * =====================================================
     * CONFIGURAR TABLA
     * =====================================================
     */

    private void configurarTabla() {

        /*
         * CÓDIGO DE BARRAS
         */

        columnaCodigo.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirCodigoBarras()
                    )
                )
        );


        /*
         * NOMBRE
         */

        columnaNombre.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirNombre()
                    )
                )
        );


        /*
         * MARCA
         */

        columnaMarca.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirMarca()
                    )
                )
        );


        /*
         * CATEGORÍA
         */

        columnaCategoria.setCellValueFactory(
            celda -> {

                Producto producto =
                    celda.getValue();


                if (
                    producto.conseguirCategoria() == null
                    || producto
                        .conseguirCategoria()
                        .conseguirNombre() == null
                ) {

                    return new SimpleStringProperty(
                        ""
                    );
                }


                return new SimpleStringProperty(
                    producto
                        .conseguirCategoria()
                        .conseguirNombre()
                );
            }
        );


        /*
         * EXISTENCIAS
         */

        columnaExistencias.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirExistencias()
                    )
                )
        );


        /*
         * EXISTENCIAS MÍNIMAS
         */

        columnaMinimo.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirMinimoExistencias()
                    )
                )
        );


        /*
         * UNIDAD DE MEDIDA
         */

        columnaUnidad.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirUnidadMedida()
                    )
                )
        );


        /*
         * ESTADO
         */

        columnaEstado.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    conseguirEstado(
                        celda.getValue()
                    )
                )
        );
    }


    /*
     * =====================================================
     * AJUSTAR COLUMNAS AL ANCHO DE LA TABLA
     * =====================================================
     */

    private void configurarAnchoTabla() {

        /*
         * Cada vez que cambie el ancho de la tabla,
         * volvemos a distribuir las columnas.
         */

        tablaInventario
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
         * Cuando JavaFX agregue la tabla a la escena,
         * hacemos también un primer ajuste.
         */

        tablaInventario
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

                        tablaInventario.applyCss();

                        tablaInventario.layout();

                        ajustarAnchoColumnas(
                            tablaInventario.getWidth()
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
         * Restamos un pequeño margen para:
         *
         * - barra vertical
         * - bordes
         * - espacio interno de la tabla
         */

        double disponible =
            Math.max(
                0,
                anchoTabla - 30
            );


        /*
         * DISTRIBUCIÓN:
         *
         * Código       12 %
         * Producto     20 %
         * Marca        13 %
         * Categoría    14 %
         * Existencias  11 %
         * Mínimo        8 %
         * Unidad       10 %
         * Estado       12 %
         *
         * TOTAL       100 %
         */

        columnaCodigo.setPrefWidth(
            disponible * 0.12
        );

        columnaNombre.setPrefWidth(
            disponible * 0.20
        );

        columnaMarca.setPrefWidth(
            disponible * 0.13
        );

        columnaCategoria.setPrefWidth(
            disponible * 0.14
        );

        columnaExistencias.setPrefWidth(
            disponible * 0.11
        );

        columnaMinimo.setPrefWidth(
            disponible * 0.08
        );

        columnaUnidad.setPrefWidth(
            disponible * 0.10
        );

        columnaEstado.setPrefWidth(
            disponible * 0.12
        );
    }


    /*
     * =====================================================
     * CONFIGURAR BÚSQUEDA
     * =====================================================
     */

    private void configurarBusqueda() {

        txtBuscar
            .textProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) ->
                    aplicarFiltro()
            );
    }


    /*
     * =====================================================
     * CARGAR PRODUCTOS
     * =====================================================
     */

    private void cargarProductos() {

        try {

            productos.setAll(
                productoRepositorio
                    .consultarTodo()
            );


            aplicarFiltro();


        } catch (
            RuntimeException e
        ) {

            mostrarError(
                "No se pudo cargar el inventario.",
                e
            );
        }
    }


    /*
     * =====================================================
     * APLICAR FILTRO
     * =====================================================
     */

    private void aplicarFiltro() {

        String busqueda =
            txtBuscar.getText();


        if (
            busqueda == null
        ) {

            busqueda = "";
        }


        final String filtro =
            busqueda
                .trim()
                .toLowerCase();


        productosFiltrados.setPredicate(
            producto -> {

                /*
                 * Si no hay búsqueda,
                 * mostramos todo.
                 */

                if (
                    filtro.isEmpty()
                ) {

                    return true;
                }


                /*
                 * CÓDIGO
                 */

                String codigo =
                    textoSeguro(
                        producto
                            .conseguirCodigoBarras()
                    )
                    .toLowerCase();


                /*
                 * NOMBRE
                 */

                String nombre =
                    textoSeguro(
                        producto
                            .conseguirNombre()
                    )
                    .toLowerCase();


                /*
                 * MARCA
                 */

                String marca =
                    textoSeguro(
                        producto
                            .conseguirMarca()
                    )
                    .toLowerCase();


                /*
                 * CATEGORÍA
                 */

                String categoria =
                    "";


                if (
                    producto
                        .conseguirCategoria()
                    != null
                ) {

                    categoria =
                        textoSeguro(
                            producto
                                .conseguirCategoria()
                                .conseguirNombre()
                        )
                        .toLowerCase();
                }


                /*
                 * COINCIDENCIA
                 */

                return codigo.contains(
                    filtro
                )
                || nombre.contains(
                    filtro
                )
                || marca.contains(
                    filtro
                )
                || categoria.contains(
                    filtro
                );
            }
        );


        actualizarResumen();

        tablaInventario.refresh();
    }


    /*
     * =====================================================
     * ACTUALIZAR RESUMEN
     * =====================================================
     */

    private void actualizarResumen() {

        int stockBajo =
            0;

        int sinExistencias =
            0;


        for (
            Producto producto
            : productosFiltrados
        ) {

            double existencias =
                valorSeguro(
                    producto
                        .conseguirExistencias()
                );


            double minimo =
                valorSeguro(
                    producto
                        .conseguirMinimoExistencias()
                );


            /*
             * SIN EXISTENCIAS
             */

            if (
                existencias <= 0
            ) {

                sinExistencias++;


            /*
             * STOCK BAJO
             */

            } else if (
                minimo > 0
                && existencias <= minimo
            ) {

                stockBajo++;
            }
        }


        /*
         * PRODUCTOS MOSTRADOS
         */

        lblCantidadProductos.setText(
            String.valueOf(
                productosFiltrados.size()
            )
        );


        /*
         * STOCK BAJO
         */

        lblStockBajo.setText(
            String.valueOf(
                stockBajo
            )
        );


        /*
         * SIN EXISTENCIAS
         */

        lblSinExistencias.setText(
            String.valueOf(
                sinExistencias
            )
        );
    }


    /*
     * =====================================================
     * CONSEGUIR ESTADO DEL PRODUCTO
     * =====================================================
     */

    private String conseguirEstado(
        Producto producto
    ) {

        double existencias =
            valorSeguro(
                producto
                    .conseguirExistencias()
            );


        double minimo =
            valorSeguro(
                producto
                    .conseguirMinimoExistencias()
            );


        /*
         * SIN EXISTENCIAS
         */

        if (
            existencias <= 0
        ) {

            return "Sin existencias";
        }


        /*
         * STOCK BAJO
         */

        if (
            minimo > 0
            && existencias <= minimo
        ) {

            return "Stock bajo";
        }


        /*
         * DISPONIBLE
         */

        return "Disponible";
    }


    /*
     * =====================================================
     * LIMPIAR BÚSQUEDA
     * =====================================================
     */

    @FXML
    private void limpiarBusqueda() {

        txtBuscar.clear();
    }


    /*
     * =====================================================
     * ACTUALIZAR
     * =====================================================
     */

    @FXML
    private void actualizar() {

        cargarProductos();
    }


    /*
     * =====================================================
     * ABRIR REPORTE DE VENTAS
     * =====================================================
     */

    @FXML
    private void abrirVentas() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/reporte/ReporteVentas.fxml"
                    )
                );


            Parent vista =
                loader.load();


            BorderPane panelPrincipal =
                (BorderPane) tablaInventario
                    .getScene()
                    .getRoot();


            panelPrincipal.setCenter(
                vista
            );


        } catch (
            IOException e
        ) {

            mostrarError(
                "No se pudo abrir el reporte de ventas.",
                e
            );
        }
    }


    /*
     * =====================================================
     * ABRIR REPORTE DE CUENTAS POR COBRAR
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
                (BorderPane) tablaInventario
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
     * EXPORTAR INVENTARIO A PDF
     * =====================================================
     */

    @FXML
    private void exportarPdf() {

        /*
         * No exportamos si no existen productos
         * dentro del resultado actual.
         */

        if (
            productosFiltrados.isEmpty()
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
                "No existen productos para exportar."
            );

            alerta.showAndWait();

            return;
        }


        /*
         * SELECTOR DE ARCHIVO
         */

        FileChooser selector =
            new FileChooser();


        selector.setTitle(
            "Guardar reporte de inventario"
        );


        selector.setInitialFileName(
            "reporte_inventario.pdf"
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
            tablaInventario
                .getScene()
                .getWindow();


        File archivo =
            selector.showSaveDialog(
                ventana
            );


        /*
         * El usuario canceló.
         */

        if (
            archivo == null
        ) {

            return;
        }


        /*
         * Asegurarnos de que termine en .pdf
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


        /*
         * GENERAR PDF
         */

        try {

            reportePdfServicio
                .generarReporteInventario(
                    archivo,
                    FXCollections
                        .observableArrayList(
                            productosFiltrados
                        )
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
                "El reporte de inventario fue "
                + "generado correctamente."
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