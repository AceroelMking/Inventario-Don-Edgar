package com.gestion.controller.producto;

import java.util.Optional;

import com.gestion.model.Categoria;
import com.gestion.model.Producto;
import com.gestion.model.UnidadesMedida;
import com.gestion.repository.CategoriaRepositorio;
import com.gestion.repository.ProductoRepositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ProductoFormularioControlador {
    private Producto producto = new Producto();
    //private Categoria categoria = new Categoria();

    private ProductoRepositorio productoRepositorio = new ProductoRepositorio();
    private CategoriaRepositorio categoriaRepositorio = new CategoriaRepositorio();
    
    private ObservableList<Producto> unidadesAgrupadas = productoRepositorio.consultarTodo();
    private ObservableList<Categoria> categorias = categoriaRepositorio.consultarTodo();
    private ObservableList<String> UNIDADES_ORIGINALES = UnidadesMedida.UNIDADES_ORIGINALES;
    private ObservableList<String> unidadesProdRepo = productosACadena();

    private FilteredList<String> unidadesFiltradasProdRepo = new FilteredList<>(unidadesProdRepo);
    //private FilteredList<String> categoriasFiltradasCatRepo = new FilteredList<>(categoriasACadena());
    //private FilteredList<String> unidadesFiltradas  = new FilteredList<>(UNIDADES_ORIGINALES);
    @FXML private TextField txtCodigoBarras;
    @FXML private TextField txtNombreProducto;
    @FXML private TextField txtMarca;
    @FXML private ChoiceBox<Categoria> seleccionCategoria;
    @FXML private TextField txtCantidadProducto;
    @FXML private RadioButton unidadMedida;
    @FXML private RadioButton unidadAgrupada;
    @FXML private ToggleGroup grupoTipoUnidad;
    @FXML private ChoiceBox<String> seleccionUnidadMedida;
    @FXML private ComboBox<String> txtUnidadAgrupada;
    @FXML private TextField txtPrecio;
    @FXML private TextField txtExistencias;
    @FXML private TextField txtMinimoExistencias;
    @FXML private Button botonCancelar;

    // Setters
    public void colocarProducto(Producto producto) {
		this.producto = producto;
	}

    // Metodos
    @FXML
    public void initialize() {

        seleccionCategoria.setItems(categorias);
        // Configuración inicial
        configurarTipoUnidad();
        // Detectar cuando cambia el RadioButton
        grupoTipoUnidad.selectedToggleProperty().addListener((obs, anterior, nuevo) -> {
            configurarTipoUnidad();
        });
        // ComboBox unidad de medida
        seleccionUnidadMedida.setItems(UnidadesMedida.UNIDADES_ORIGINALES);
        // ComboBox unidad agrupada
        txtUnidadAgrupada.setItems(unidadesFiltradasProdRepo);
        txtUnidadAgrupada.getEditor().textProperty().addListener((obs, old, nuevo) -> {
            if (nuevo == null || nuevo.isEmpty()) {
                unidadesFiltradasProdRepo.setPredicate(p -> true);
            } else {
                unidadesFiltradasProdRepo.setPredicate(s ->
                    s.toLowerCase().contains(nuevo.toLowerCase())
                );
            }
        });
    }

    public void cargarProductoCampoTexto (Producto producto) {
        colocarProducto(producto);
        txtCodigoBarras.setText(producto.conseguirCodigoBarras());
        txtNombreProducto.setText(producto.conseguirNombre());
        txtMarca.setText(producto.conseguirMarca());
        categorias.add(producto.conseguirCategoria());
        seleccionCategoria.setValue(producto.conseguirCategoria());
        txtCantidadProducto.setText(String.valueOf(producto.conseguirCantidadProducto()));
        seleccionUnidadMedida.setValue(producto.conseguirUnidadMedida());

        //RGLA DE NEGOCIO:
        if (producto.conseguirUnidadAgrupada() == null) {
            unidadMedida.setSelected(true);
            unidadAgrupada.setSelected(false);
            txtUnidadAgrupada.setValue("");
        } else {
            unidadMedida.setSelected(false);
            unidadAgrupada.setSelected(true);
            Producto productoPadre = productoRepositorio.consultarProductoDesactivado(
                producto.conseguirUnidadAgrupada()
            );
            String productoPadreCadena = productoACadena(productoPadre);
                System.out.println(productoPadreCadena + " <- productoPadreCadena");
                System.out.println(producto.conseguirProductoPadre() + " <- nombre pAgrupada");
            if (!productoRepositorio.estaActivo(producto.conseguirUnidadAgrupada())) {
                unidadesProdRepo.add(productoPadreCadena);
            }
            txtUnidadAgrupada.setValue(productoPadreCadena);
        }
        txtPrecio.setText(String.valueOf(producto.conseguirPrecio()));
        txtExistencias.setText(String.valueOf(producto.conseguirExistencias()));
        txtMinimoExistencias.setText(String.valueOf(producto.conseguirMinimoExistencias()));
        verifiarHijoPadre();
    }

    private void verifiarHijoPadre(){
        unidadesProdRepo.remove(productoACadena(producto));
    }

    private String productoACadena(Producto prod){
        return prod.conseguirId() + " - "
            + prod.conseguirCodigoBarras() + " - "
            + prod.conseguirNombre() + " - "
            + prod.conseguirMarca();
    }

    private ObservableList<String> productosACadena() {
        ObservableList<String> cadenas = FXCollections.observableArrayList();
        for(Producto prod: unidadesAgrupadas){
            if (prod.conseguirUnidadAgrupada() == null) {
                cadenas.add(productoACadena(prod));
            }
        }
        return cadenas;
    }

    private void configurarTipoUnidad() {
        if (unidadMedida.isSelected()) {
            seleccionUnidadMedida.setDisable(false);
            txtUnidadAgrupada.setDisable(true);
        } else if (unidadAgrupada.isSelected()) {
            seleccionUnidadMedida.setDisable(true);
            txtUnidadAgrupada.setDisable(false);
        }
    }

    private boolean mostrarAlertaAdvertencia(String titulo, String cabecera, String cuerpo) {
        boolean bandera = false;
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle(titulo);
        alerta.setHeaderText(cabecera);
        alerta.setContentText(cuerpo);
        // Definimos botones personalizados
        ButtonType botonContinuar = new ButtonType("Si", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonRevisar   = new ButtonType("No", ButtonBar.ButtonData.CANCEL_CLOSE);
        alerta.getButtonTypes().setAll(botonContinuar, botonRevisar);
        // Capturamos la respuesta
        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == botonContinuar) {
            bandera = true;
        } else if (resultado.isPresent() && resultado.get() == botonRevisar) {
            bandera = false;
        }
        return bandera;
    }

    private Integer productoCadenaId(String cadena) {
        return Integer.parseInt(
            cadena.split("-")[0].trim()
        );
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage ventana = (Stage) botonCancelar.getScene().getWindow();
        ventana.close();
    }

    @FXML
    private void borrarProducto() {  
        if(mostrarAlertaAdvertencia("AVISO",
            "Se eliminara el producto.", 
            "¿Esta seguro de esta accion?"
        )){
            //////// ¡CUANDO LOTE ESTE LISTO DEVE INTENTARSE BORRADO FISICO DE LA BD!
            productoRepositorio.borrarProducto(producto.conseguirId());
            cerrarVentana();
        }
    }

    @FXML
    private void aceptar() {
///////////// Validacion Codigo de barras //////////////////////////////////////////////////////////
        if (txtCodigoBarras.getText() == null || txtCodigoBarras.getText().isBlank()) {
            if (!
                mostrarAlertaAdvertencia("Alerta",
                    "Codigo de barras vacio", 
                    "¿Esta seguro de esta accion?"
                )
            ) {
                return;
            } else {
                producto.colocarCodigoBarras(null);
            }
        } else {
            producto.colocarCodigoBarras(txtCodigoBarras.getText().trim());
        }
///////////// Validacion Nobre de producto //////////////////////////////////////////////////////////
        if (txtNombreProducto.getText() == null || txtNombreProducto.getText().isBlank()) {
            new Alert(
                AlertType.ERROR,
                "Nombre de producto vacio, por favor corrijalo para continuar"
            ).showAndWait();
            return;
        }
        producto.colocarNombre(txtNombreProducto.getText().trim());
///////////// Validacion Marca //////////////////////////////////////////////////////////
        if (txtMarca.getText().isBlank()) {
            new Alert(
                AlertType.ERROR,
                "Marca o distribuidor vacia, por favor corrijala para continuar"
            ).showAndWait();
            return;
        }
        producto.colocarMarca(txtMarca.getText().trim());
///////////// Validacion Categoria //////////////////////////////////////////////////////////
        if (seleccionCategoria.getSelectionModel().getSelectedItem() == null) {
            new Alert(
                AlertType.ERROR,
                "Categoria vacia, por favor corrijala para continuar"
            ).showAndWait();
            return;
        }
        producto.colocarCategoria(seleccionCategoria.getSelectionModel().getSelectedItem());
///////////// Validacion Cantidad de producto //////////////////////////////////////////////////////////
        if (txtCantidadProducto.getText() == null || txtCantidadProducto.getText().isBlank()) {
             new Alert(
                    AlertType.ERROR,
                    "Cantidad producto vacio, por favor indique \"0\" o corríjala para continuar"
            ).showAndWait();
            return;
        }
        try {
            if (Double.parseDouble(txtCantidadProducto.getText().trim()) <= 0) {
                new Alert(
                    AlertType.ERROR,
                    "Cantidad producto es inferior o igual a 0, por favor corríjala para continuar"
                ).showAndWait();
                return;
            }
            producto.colocarCantidadProducto(
                Double.parseDouble(txtCantidadProducto.getText().trim())
            );
        } catch (NumberFormatException e) {
            new Alert(
                AlertType.ERROR,
                "Cantidad producto no es numérica, por favor corríjala para continuar"
            ).showAndWait();
            return;
        }
        if (unidadMedida.isSelected() 
            && seleccionUnidadMedida.getValue() != null 
            && seleccionUnidadMedida.getValue().equals(UnidadesMedida.UNIDAD)
        ) {
            double numero =  Double.parseDouble(txtCantidadProducto.getText().trim());
            if (!(numero % 1 == 0)) {
                 new Alert(
                    AlertType.ERROR,
                    "Campo \"Unidad\" seleccionado pero Cantidad de producto no es un entero, por favor corríjala para continuar"
                ).showAndWait();
                return;
            }
        }
///////////// Validacion Unidad medida //////////////////////////////////////////////////////////
        if (unidadMedida.isSelected()) {
            if (seleccionUnidadMedida.getSelectionModel().getSelectedItem() == null ||
                !UNIDADES_ORIGINALES.contains(
                    seleccionUnidadMedida.getSelectionModel().getSelectedItem()
                )
            ) {
                new Alert(
                    AlertType.ERROR,
                    "Debe seleccionar una unidad de medida válida."
                ).showAndWait();
                return;
            }
            producto.colocarUnidadMedida(
                seleccionUnidadMedida.getSelectionModel().getSelectedItem()
            );
            producto.colocarUnidadAgrupada(null);
        } else if (unidadAgrupada.isSelected()) {
            if (txtUnidadAgrupada.getSelectionModel().getSelectedItem() == null ||
                !unidadesProdRepo.contains(
                    txtUnidadAgrupada.getSelectionModel().getSelectedItem()
                )
            ) {
                txtUnidadAgrupada.getEditor().clear();
                new Alert(
                    AlertType.ERROR,
                    "Debe seleccionar un producto padre válido."
                ).showAndWait();
                return;
            }
            if (productoRepositorio.consultarColumnaUnidadAgrupada()
                .contains(
                    String.valueOf(producto.conseguirId())
                )
            ) {
                new Alert(
                    AlertType.ERROR,
                    "Este producto cuenta con unidades hijas, no puede ser una unidad agrupada."
                ).showAndWait();
                return;
            }
            producto.colocarUnidadMedida(null);
            producto.colocarUnidadAgrupada(
                productoCadenaId(
                    txtUnidadAgrupada.getSelectionModel().getSelectedItem()
                )
            );
        }
///////////// validacion Precio //////////////////////////////////////////////////////////
        if (txtPrecio.getText() == null || txtPrecio.getText().isBlank()) {
            new Alert(
                AlertType.ERROR,
                "Precio vacio, por favor corrijalo para continuar"
            ).showAndWait();
            return;
        }
        try {
            if (Double.parseDouble(txtPrecio.getText().trim()) <= 0 ) {
                new Alert(
                    AlertType.ERROR,
                    "Precio es inferior o igual a 0, por favor corrijalo para continuar"
                ).showAndWait();
                return;
            }
            producto.colocarPrecio(Double.parseDouble(txtPrecio.getText().trim()));
        } catch (NumberFormatException e) {
            new Alert(
                AlertType.ERROR,
                "Precio no es numerico, por favor corrijalo para continuar"
            ).showAndWait();
            return;
        }
///////////// Validacion Exixtencias //////////////////////////////////////////////////////////
        producto.colocarExistencias(0.0); // ¡NO SE PUEDE MODIFICAR ESTE CAMPO DESDE ESTA TABLA, SOLO DESDE LOTES!
///////////// Validacion Minimo de existencias //////////////////////////////////////////////////////////
        if (txtMinimoExistencias.getText() == null || txtMinimoExistencias.getText().isBlank()) {
            new Alert(
                AlertType.ERROR,
                "Minimo existencias esta vacio, por favor indique \"0\" o corrijalo para continuar"
            ).showAndWait();
            return;
        }
        try {
            if (Double.parseDouble(txtMinimoExistencias.getText().trim()) < 0 ) {
                new Alert(
                    AlertType.ERROR,
                    "Minimo de existencias es inferior a 0, por favor corrijalo para continuar"
                ).showAndWait();
                return;
            }
            producto.colocarMinimoExistencias(Double.parseDouble(txtMinimoExistencias.getText().trim()));
        } catch (NumberFormatException e) {
            new Alert(
                AlertType.ERROR,
                "Minimo de existencias no es un numerico, por favor indique \"0\" o corrijalo para continuar"
            ).showAndWait();
            return;
        }
        System.out.println(producto.toString());
////////////// Guardar o editar //////////////////////////////////////////////////////////
        try {
            if (producto.conseguirId() == null) { // AGREGAR
                productoRepositorio.agregarProducto(producto);
                new Alert(
                    AlertType.INFORMATION,
                    "Producto guardado correctamente"
                ).showAndWait();
            } else { // EDITAR
                productoRepositorio.editarProducto(producto);
                new Alert(
                    AlertType.INFORMATION,
                    "Producto editado correctamente"
                ).showAndWait();
            }
        } catch (RuntimeException e) {
            System.out.print("ERROR: " + e);
            new Alert(
                AlertType.ERROR,
                "No se pudo guardar el producto: " + e.getMessage()
            ).showAndWait();
            return;
        }
        cerrarVentana();
    }
}
