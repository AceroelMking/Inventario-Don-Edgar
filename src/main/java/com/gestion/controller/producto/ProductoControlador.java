
package com.gestion.controller.producto;

import java.io.IOException;
import java.util.Locale;
import java.util.TreeSet;

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

    // ==========================================
    // LISTA DE PRODUCTOS
    // ==========================================

    private final ObservableList<Producto> productosObservables =
            FXCollections.observableArrayList();

    private final FilteredList<Producto> listaFiltroTabla =
            new FilteredList<>(
                    productosObservables,
                    producto -> true
            );

    // ==========================================
    // LISTAS DE LOS BUSCADORES
    // ==========================================

    private final ObservableList<String> codigosObservables =
            FXCollections.observableArrayList();

    private final ObservableList<String> nombresObservables =
            FXCollections.observableArrayList();

    private final ObservableList<String> marcasObservables =
            FXCollections.observableArrayList();

    private final ObservableList<String> categoriasObservables =
            FXCollections.observableArrayList();

    // ==========================================
    // COMPONENTES DE BÚSQUEDA
    // ==========================================

    @FXML
    private ComboBox<String> filtroCodigoBarras;

    @FXML
    private ComboBox<String> filtroNombre;

    @FXML
    private ComboBox<String> filtroMarca;

    @FXML
    private ComboBox<String> filtroNombreCategoria;

    // ==========================================
    // TABLA DE PRODUCTOS
    // ==========================================

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
    private TableColumn<Producto, Double> columnaCantidad;

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

    // ==========================================
    // INICIALIZACIÓN
    // ==========================================

    @FXML
    public void initialize() {

        inicializarProductosTablaVista();

        inicializarColumnaEdicion();

        tablaProductos.setItems(listaFiltroTabla);

        cargarListaFiltros();

        recargarDatos();
    }

    // ==========================================
    // CONFIGURACIÓN DE LOS BUSCADORES
    // ==========================================

    private void cargarListaFiltros() {

        configurarFiltro(
                filtroCodigoBarras,
                codigosObservables
        );

        configurarFiltro(
                filtroNombre,
                nombresObservables
        );

        configurarFiltro(
                filtroMarca,
                marcasObservables
        );

        configurarFiltro(
                filtroNombreCategoria,
                categoriasObservables
        );
    }

    private void configurarFiltro(
            ComboBox<String> comboBox,
            ObservableList<String> lista
    ) {

        /*
         * Cada ComboBox recibe su lista completa.
         *
         * No utilizamos una FilteredList para las
         * opciones del desplegable porque puede
         * interferir con la selección del ComboBox.
         */

        comboBox.setEditable(true);

        comboBox.setItems(lista);

        /*
         * Permite escribir libremente y actualizar
         * la tabla en tiempo real.
         */

        comboBox.getEditor()
                .textProperty()
                .addListener((observable, anterior, nuevo) -> {

                    aplicarFiltros();
                });

        /*
         * También actualiza la tabla cuando el
         * usuario selecciona una opción.
         */

        comboBox.valueProperty()
                .addListener((observable, anterior, nuevo) -> {

                    aplicarFiltros();
                });

        /*
         * Cuando se abre el ComboBox, se mantiene
         * la lista completa de opciones disponibles.
         */

        comboBox.setOnShowing(event -> {

            if (comboBox.getItems() != lista) {
                comboBox.setItems(lista);
            }
        });
    }

    // ==========================================
    // ACTUALIZACIÓN DE LAS LISTAS
    // ==========================================

    private void actualizarListasFiltros() {

        /*
         * Consultamos nuevamente SQLite para que
         * los cuatro buscadores tengan información
         * actualizada.
         */

        actualizarOpciones(
                codigosObservables,
                productoRepositorio
                        .consultarColumnaCodigoBarras()
        );

        actualizarOpciones(
                nombresObservables,
                productoRepositorio
                        .consultarColumnaNombreProducto()
        );

        actualizarOpciones(
                marcasObservables,
                productoRepositorio
                        .consultarColumnaMarca()
        );

        actualizarOpciones(
                categoriasObservables,
                categoriaRepositorio
                        .consultarColumnaNombreCategoria()
        );
    }

    private void actualizarOpciones(
            ObservableList<String> destino,
            ObservableList<String> origen
    ) {

        /*
         * TreeSet permite ordenar y eliminar
         * opciones duplicadas.
         */

        TreeSet<String> opciones = new TreeSet<>(
                String.CASE_INSENSITIVE_ORDER
        );

        if (origen != null) {

            for (String opcion : origen) {

                if (opcion != null
                        && !opcion.trim().isEmpty()) {

                    opciones.add(opcion.trim());
                }
            }
        }

        /*
         * Se actualiza la misma lista observable
         * utilizada por el ComboBox.
         */

        destino.setAll(opciones);
    }

    // ==========================================
    // CARGAR PRODUCTOS
    // ==========================================

    private void cargarProductos() {

        productosObservables.setAll(
                productoRepositorio.consultarTodo()
        );
    }

    private void recargarDatos() {

        cargarProductos();

        actualizarListasFiltros();

        aplicarFiltros();

        tablaProductos.refresh();
    }

    // ==========================================
    // CONFIGURAR COLUMNAS DE LA TABLA
    // ==========================================

    private void inicializarProductosTablaVista() {

        columnaCodigo.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue()
                                        .conseguirCodigoBarras()
                        )
                )
        );

        columnaNombre.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue()
                                        .conseguirNombre()
                        )
                )
        );

        columnaMarca.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue()
                                        .conseguirMarca()
                        )
                )
        );

        columnaCategoria.setCellValueFactory(
                celda -> {

                    Producto producto = celda.getValue();

                    if (producto.conseguirCategoria() == null) {

                        return new SimpleStringProperty("");
                    }

                    return new SimpleStringProperty(
                            valorSeguro(
                                    producto.conseguirCategoria()
                                            .conseguirNombre()
                            )
                    );
                }
        );

        columnaCantidad.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue()
                                .conseguirCantidadProducto()
                )
        );

        columnaUnidadMedida.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue()
                                        .conseguirUnidadMedida()
                        )
                )
        );

        columnaProductoPadre.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue()
                                        .conseguirProductoPadre()
                        )
                )
        );

        columnaPrecio.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue()
                                .conseguirPrecio()
                )
        );

        columnaExistencias.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue()
                                .conseguirExistencias()
                )
        );

        columnaMinimoExistencias.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue()
                                .conseguirMinimoExistencias()
                )
        );
    }

    // ==========================================
    // COLUMNA EDITAR
    // ==========================================

    private void inicializarColumnaEdicion() {

        columnaEditar.setCellFactory(
                columna -> new TableCell<Producto, Void>() {

                    private final Button botonEditar =
                            new Button("Editar");

                    {
                        botonEditar.setOnAction(event -> {

                            Producto producto =
                                    getTableRow().getItem();

                            if (producto != null) {

                                editarProducto(producto);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean empty
                    ) {

                        super.updateItem(item, empty);

                        if (empty) {

                            setGraphic(null);

                        } else {

                            setGraphic(botonEditar);
                        }
                    }
                }
        );
    }

    // ==========================================
    // MÉTODOS AUXILIARES
    // ==========================================

    private String valorSeguro(String valor) {

        return valor == null ? "" : valor;
    }

    private String normalizarTexto(String texto) {

        if (texto == null) {
            return "";
        }

        return texto.trim()
                .toLowerCase(Locale.ROOT);
    }

    private String obtenerTextoFiltro(
            ComboBox<String> comboBox
    ) {

        if (comboBox == null) {
            return "";
        }

        /*
         * Para los ComboBox editables usamos
         * el texto del editor.
         *
         * Esto permite escribir solo una parte
         * del código, nombre, marca o categoría.
         */

        if (comboBox.isEditable()
                && comboBox.getEditor() != null) {

            return normalizarTexto(
                    comboBox.getEditor().getText()
            );
        }

        return normalizarTexto(
                comboBox.getValue()
        );
    }

    // ==========================================
    // FILTRAR PRODUCTOS
    // ==========================================

    private void aplicarFiltros() {

        String codigo =
                obtenerTextoFiltro(filtroCodigoBarras);

        String nombre =
                obtenerTextoFiltro(filtroNombre);

        String marca =
                obtenerTextoFiltro(filtroMarca);

        String categoria =
                obtenerTextoFiltro(filtroNombreCategoria);

        listaFiltroTabla.setPredicate(producto -> {

            if (producto == null) {
                return false;
            }

            // Código de barras del producto.

            String codigoProducto =
                    normalizarTexto(
                            producto.conseguirCodigoBarras()
                    );

            // Nombre del producto.

            String nombreProducto =
                    normalizarTexto(
                            producto.conseguirNombre()
                    );

            // Marca del producto.

            String marcaProducto =
                    normalizarTexto(
                            producto.conseguirMarca()
                    );

            // Categoría del producto.

            String categoriaProducto = "";

            if (producto.conseguirCategoria() != null) {

                categoriaProducto =
                        normalizarTexto(
                                producto.conseguirCategoria()
                                        .conseguirNombre()
                        );
            }

            // Validación de cada filtro.

            boolean coincideCodigo =
                    codigo.isEmpty()
                            || codigoProducto.contains(codigo);

            boolean coincideNombre =
                    nombre.isEmpty()
                            || nombreProducto.contains(nombre);

            boolean coincideMarca =
                    marca.isEmpty()
                            || marcaProducto.contains(marca);

            boolean coincideCategoria =
                    categoria.isEmpty()
                            || categoriaProducto.contains(categoria);

            /*
             * Todos los filtros se combinan.
             *
             * Si un campo está vacío, no limita
             * los resultados de la tabla.
             */

            return coincideCodigo
                    && coincideNombre
                    && coincideMarca
                    && coincideCategoria;
        });
    }

    // ==========================================
    // AGREGAR PRODUCTO
    // ==========================================

    @FXML
    private void abrirFormularioAgregar() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/gestion/view/producto/ProductoAgregar.fxml"
                    )
            );

            Parent root = loader.load();

            Stage ventana = new Stage();

            ventana.setTitle("Agregar producto");

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            /*
             * Cuando se cierra el formulario,
             * se actualizan los productos y
             * los cuatro buscadores.
             */

            recargarDatos();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el formulario de producto.",
                    e
            );
        }
    }

    // ==========================================
    // EDITAR PRODUCTO
    // ==========================================

    private void editarProducto(Producto producto) {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/gestion/view/producto/ProductoEditar.fxml"
                    )
            );

            Parent root = loader.load();

            ProductoFormularioControlador controlador =
                    loader.getController();

            controlador.cargarProductoCampoTexto(producto);

            Stage ventana = new Stage();

            ventana.setTitle("Editar producto");

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            /*
             * Actualiza la tabla y las opciones
             * después de editar el producto.
             */

            recargarDatos();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el formulario de edición.",
                    e
            );
        }
    }

    // ==========================================
    // ADMINISTRAR CATEGORÍAS
    // ==========================================

    @FXML
    private void abrirCategorias() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/gestion/view/producto/Categorias.fxml"
                    )
            );

            Parent root = loader.load();

            Stage ventana = new Stage();

            ventana.setTitle("Categorías");

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            /*
             * Al cerrar la ventana, se consultan
             * nuevamente las categorías de SQLite.
             *
             * Así aparecen las nuevas categorías
             * en el filtro correspondiente.
             */

            recargarDatos();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir Categorías.",
                    e
            );
        }
    }

    // ==========================================
    // BOTÓN ACTUALIZAR
    // ==========================================

    @FXML
    private void actualizarProductos() {

        recargarDatos();
    }

    // ==========================================
    // MENSAJES DE ERROR
    // ==========================================

    private void mostrarError(
            String mensaje,
            Exception e
    ) {

        Alert alerta = new Alert(
                Alert.AlertType.ERROR
        );

        alerta.setTitle("Error");

        alerta.setHeaderText(mensaje);

        alerta.setContentText(
                e.getMessage() == null
                        ? "Se produjo un error inesperado."
                        : e.getMessage()
        );

        alerta.showAndWait();
    }
}
