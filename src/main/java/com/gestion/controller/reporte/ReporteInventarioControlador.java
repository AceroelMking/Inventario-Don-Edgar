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

    private final ProductoRepositorio productoRepositorio =
        new ProductoRepositorio();

    private final ReportePdfServicio reportePdfServicio =
        new ReportePdfServicio();

    private final ObservableList<Producto> productos =
        FXCollections.observableArrayList();

    private final FilteredList<Producto> productosFiltrados =
        new FilteredList<>(
            productos,
            producto -> true
        );


    @FXML
    private TextField txtBuscar;

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

    @FXML
    private Label lblCantidadProductos;

    @FXML
    private Label lblStockBajo;

    @FXML
    private Label lblSinExistencias;


    @FXML
    public void initialize() {

        configurarTabla();

        configurarBusqueda();

        tablaInventario.setItems(
            productosFiltrados
        );

        cargarProductos();
    }


    private void configurarTabla() {

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


        columnaEstado.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    conseguirEstado(
                        celda.getValue()
                    )
                )
        );
    }


    private void configurarBusqueda() {

        txtBuscar
            .textProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> aplicarFiltro()
            );
    }


    private void cargarProductos() {

        try {

            productos.setAll(
                productoRepositorio
                    .consultarTodo()
            );


            aplicarFiltro();


        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo cargar el inventario.",
                e
            );
        }
    }


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

                if (
                    filtro.isEmpty()
                ) {

                    return true;
                }


                String codigo =
                    textoSeguro(
                        producto
                            .conseguirCodigoBarras()
                    )
                    .toLowerCase();


                String nombre =
                    textoSeguro(
                        producto
                            .conseguirNombre()
                    )
                    .toLowerCase();


                String marca =
                    textoSeguro(
                        producto
                            .conseguirMarca()
                    )
                    .toLowerCase();


                String categoria = "";


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


    private void actualizarResumen() {

        int stockBajo = 0;

        int sinExistencias = 0;


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


            if (
                existencias <= 0
            ) {

                sinExistencias++;


            } else if (
                minimo > 0
                && existencias <= minimo
            ) {

                stockBajo++;
            }
        }


        lblCantidadProductos.setText(
            String.valueOf(
                productosFiltrados.size()
            )
        );


        lblStockBajo.setText(
            String.valueOf(
                stockBajo
            )
        );


        lblSinExistencias.setText(
            String.valueOf(
                sinExistencias
            )
        );
    }


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


        if (
            existencias <= 0
        ) {

            return "Sin existencias";
        }


        if (
            minimo > 0
            && existencias <= minimo
        ) {

            return "Stock bajo";
        }


        return "Disponible";
    }


    @FXML
    private void limpiarBusqueda() {

        txtBuscar.clear();
    }


    @FXML
    private void actualizar() {

        cargarProductos();
    }


    /*
     * VOLVER AL REPORTE DE VENTAS
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


        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir el reporte de ventas.",
                e
            );
        }
    }


    /*
     * EXPORTAR INVENTARIO A PDF
     */

    @FXML
    private void exportarPdf() {

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


        if (
            archivo == null
        ) {

            return;
        }


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


        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo generar el PDF.",
                e
            );
        }
    }


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