package com.gestion.controller.producto;

import java.io.IOException;

import com.gestion.model.Producto;
import com.gestion.repository.CategoriaRepositorio;
import com.gestion.repository.ProductoRepositorio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ProductoControlador {

    private final CategoriaRepositorio categoriaRepositorio =
        new CategoriaRepositorio();

    private final ProductoRepositorio productoRepositorio =
        new ProductoRepositorio();

    private final ObservableList<Producto> productosObservables =
        FXCollections.observableArrayList();

    private final FilteredList<String> listaFiltroCodigoBarras =
        new FilteredList<>(
            productoRepositorio.consultarColumnaCodigoBarras()
        );

    private final FilteredList<String> listaFiltroNombre =
        new FilteredList<>(
            productoRepositorio.consultarColumnaNombreProducto()
        );

    private final FilteredList<String> listaFiltroMarca =
        new FilteredList<>(
            productoRepositorio.consultarColumnaMarca()
        );

    private final FilteredList<String> listaFiltroNombreCategoria =
        new FilteredList<>(
            categoriaRepositorio.consultarColumnaNombreCategoria()
        );

    private final FilteredList<Producto> listaFiltroTabla =
        new FilteredList<>(
            productosObservables,
            producto -> true
        );


    @FXML
    private ComboBox<String> filtroCodigoBarras;

    @FXML
    private ComboBox<String> filtroNombre;

    @FXML
    private ComboBox<String> filtroMarca;

    @FXML
    private ComboBox<String> filtroNombreCategoria;

    @FXML
    private TableView<Producto> tablaProductos;

    @FXML
    private TableColumn<Producto, String> columnaCodigo;

    @FXML
    private TableColumn<Producto, String> columnaNombre;

    @FXML
    private TableColumn<Producto, String> columnaMarca;

    @FXML
    private TableColumn<Producto, String> columnaCategoria;

    @FXML
    private TableColumn<Producto, String> columnaUnidadMedida;

    @FXML
    private TableColumn<Producto, String> columnaProductoPadre;

    @FXML
    private TableColumn<Producto, Double> columnaPrecio;

    @FXML
    private TableColumn<Producto, Double> columnaExistencias;

    @FXML
    private TableColumn<Producto, Double> columnaMinimoExistencias;

    @FXML
    private TableColumn<Producto, Void> columnaEditar;


    private void cargarListaFiltros() {

        configurarFiltro(
            filtroCodigoBarras,
            listaFiltroCodigoBarras
        );

        configurarFiltro(
            filtroNombre,
            listaFiltroNombre
        );

        configurarFiltro(
            filtroMarca,
            listaFiltroMarca
        );

        configurarFiltro(
            filtroNombreCategoria,
            listaFiltroNombreCategoria
        );
    }


    private void inicializarProductosTablaVista() {

        columnaCodigo.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirCodigoBarras()
                    )
                )
        );

        columnaNombre.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirNombre()
                    )
                )
        );

        columnaMarca.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirMarca()
                    )
                )
        );

        columnaCategoria.setCellValueFactory(
            celda -> {

                if (
                    celda.getValue().conseguirCategoria()
                        == null
                ) {

                    return new SimpleStringProperty(
                        ""
                    );
                }

                return new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirCategoria()
                            .conseguirNombre()
                    )
                );
            }
        );

        columnaUnidadMedida.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirUnidadMedida()
                    )
                )
        );

        columnaProductoPadre.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    valorSeguro(
                        celda
                            .getValue()
                            .conseguirProductoPadre()
                    )
                )
        );

        columnaPrecio.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    celda
                        .getValue()
                        .conseguirPrecio()
                )
        );

        columnaExistencias.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    celda
                        .getValue()
                        .conseguirExistencias()
                )
        );

        columnaMinimoExistencias.setCellValueFactory(
            celda ->
                new SimpleObjectProperty<>(
                    celda
                        .getValue()
                        .conseguirMinimoExistencias()
                )
        );
    }


    private String valorSeguro(
        String valor
    ) {

        return valor == null
            ? ""
            : valor;
    }


    private void cargarProductos() {

        productosObservables.setAll(
            productoRepositorio.consultarTodo()
        );
    }


    private void inicializarColumnaEdicion() {

        columnaEditar.setCellFactory(
            columna ->
                new TableCell<>() {

                    private final Button botonEditar =
                        new Button("Editar");

                    {
                        botonEditar.setOnAction(
                            event -> {

                                Producto producto =
                                    getTableRow()
                                        .getItem();

                                if (producto != null) {

                                    editarProducto(
                                        producto
                                    );
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

                        if (empty) {

                            setGraphic(
                                null
                            );

                        } else {

                            setGraphic(
                                botonEditar
                            );
                        }
                    }
                }
        );
    }


    private void editarProducto(
        Producto producto
    ) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/producto/ProductoEditar.fxml"
                    )
                );

            Parent root =
                loader.load();

            ProductoFormularioControlador controlador =
                loader.getController();

            controlador.cargarProductoCampoTexto(
                producto
            );

            Stage ventana =
                new Stage();

            ventana.setTitle(
                "Editar producto"
            );

            ventana.setScene(
                new Scene(
                    root
                )
            );

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarProductos();

            tablaProductos.refresh();

        } catch (
            IOException e
        ) {

            e.printStackTrace();

            Alert alerta =
                new Alert(
                    Alert.AlertType.ERROR
                );

            alerta.setTitle(
                "Error"
            );

            alerta.setHeaderText(
                "No se pudo abrir el formulario de edición."
            );

            alerta.setContentText(
                e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    private void aplicarFiltros() {

        String codigo =
            obtenerTextoFiltro(
                filtroCodigoBarras
            );

        String nombre =
            obtenerTextoFiltro(
                filtroNombre
            );

        String marca =
            obtenerTextoFiltro(
                filtroMarca
            );

        String categoria =
            obtenerTextoFiltro(
                filtroNombreCategoria
            );

        listaFiltroTabla.setPredicate(
            producto -> {

                String codigoProducto =
                    valorSeguro(
                        producto
                            .conseguirCodigoBarras()
                    ).toLowerCase();

                String nombreProducto =
                    valorSeguro(
                        producto
                            .conseguirNombre()
                    ).toLowerCase();

                String marcaProducto =
                    valorSeguro(
                        producto
                            .conseguirMarca()
                    ).toLowerCase();

                String categoriaProducto =
                    "";

                if (
                    producto.conseguirCategoria()
                        != null
                ) {

                    categoriaProducto =
                        valorSeguro(
                            producto
                                .conseguirCategoria()
                                .conseguirNombre()
                        ).toLowerCase();
                }

                boolean coincideCodigo =
                    codigo.isEmpty()
                    || codigoProducto.contains(
                        codigo
                    );

                boolean coincideNombre =
                    nombre.isEmpty()
                    || nombreProducto.contains(
                        nombre
                    );

                boolean coincideMarca =
                    marca.isEmpty()
                    || marcaProducto.contains(
                        marca
                    );

                boolean coincideCategoria =
                    categoria.isEmpty()
                    || categoriaProducto.contains(
                        categoria
                    );

                return coincideCodigo
                    && coincideNombre
                    && coincideMarca
                    && coincideCategoria;
            }
        );
    }


    private String obtenerTextoFiltro(
        ComboBox<String> comboBox
    ) {

        if (
            comboBox == null
            || comboBox.getEditor() == null
            || comboBox
                .getEditor()
                .getText() == null
        ) {

            return "";
        }

        return comboBox
            .getEditor()
            .getText()
            .trim()
            .toLowerCase();
    }


    private void configurarFiltro(
        ComboBox<String> comboBox,
        FilteredList<String> lista
    ) {

        comboBox.setItems(
            lista
        );

        comboBox
            .getEditor()
            .textProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> {

                    lista.setPredicate(
                        texto -> {

                            if (
                                nuevo == null
                                || nuevo.isEmpty()
                            ) {

                                return true;
                            }

                            if (texto == null) {

                                return false;
                            }

                            return texto
                                .toLowerCase()
                                .contains(
                                    nuevo.toLowerCase()
                                );
                        }
                    );

                    aplicarFiltros();
                }
            );
    }


    @FXML
    public void initialize() {

        cargarListaFiltros();

        inicializarProductosTablaVista();

        inicializarColumnaEdicion();

        tablaProductos.setItems(
            listaFiltroTabla
        );

        cargarProductos();
    }


    @FXML
    private void abrirFormularioAgregar() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/producto/ProductoAgregar.fxml"
                    )
                );

            Parent root =
                loader.load();

            Stage ventana =
                new Stage();

            ventana.setTitle(
                "Agregar producto"
            );

            ventana.setScene(
                new Scene(
                    root
                )
            );

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarProductos();

            tablaProductos.refresh();

        } catch (
            IOException e
        ) {

            e.printStackTrace();

            Alert alerta =
                new Alert(
                    Alert.AlertType.ERROR
                );

            alerta.setTitle(
                "Error"
            );

            alerta.setHeaderText(
                "No se pudo abrir el formulario."
            );

            alerta.setContentText(
                e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    @FXML
    private void abrirCategorias() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/producto/Categorias.fxml"
                    )
                );

            Parent root =
                loader.load();

            Stage ventana =
                new Stage();

            ventana.setTitle(
                "Categorías"
            );

            ventana.setScene(
                new Scene(
                    root
                )
            );

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarProductos();

            tablaProductos.refresh();

        } catch (
            IOException e
        ) {

            e.printStackTrace();

            Alert alerta =
                new Alert(
                    Alert.AlertType.ERROR
                );

            alerta.setTitle(
                "Error"
            );

            alerta.setHeaderText(
                "No se pudo abrir Categorías."
            );

            alerta.setContentText(
                e.getMessage()
            );

            alerta.showAndWait();
        }
    }


    @FXML
    private void actualizarProductos() {

        cargarProductos();

        tablaProductos.refresh();
    }
}