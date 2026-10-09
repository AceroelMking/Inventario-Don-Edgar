package com.gestion.controller.categoria;

import java.util.Optional;

import com.gestion.model.Categoria;
import com.gestion.repository.CategoriaRepositorio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.kordamp.ikonli.javafx.FontIcon;
import org.kordamp.ikonli.materialdesign.MaterialDesign;

public class CategoriaControlador {
    private CategoriaRepositorio categoriaRepositorio = new CategoriaRepositorio();
    private final ObservableList<Categoria> ListaCategorias = FXCollections.observableArrayList();
    @FXML private Button botonSalir;
    @FXML private TableView<Categoria> tablaCategorias;
    @FXML private TableColumn<Categoria, Integer> columnaId;   
    @FXML private TableColumn<Categoria, String> columnaCategoria;
    @FXML private TableColumn<Categoria, Void> columnaEditar;
    
    private void inicializarCategoriasTablaVista() {
        columnaCategoria.setCellValueFactory(
            celda -> new SimpleStringProperty(celda.getValue().conseguirNombre()));
        columnaId.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(celda.getValue().conseguirId()));
    }

    private void cargarCategorias(){
        ListaCategorias.setAll(categoriaRepositorio.consultarTodo());
    }

    private void inicializarColumnaEdicion(){
        columnaEditar.setCellFactory(columna -> new TableCell<>(){
            private final Button botonEditar = new Button();
            {
                FontIcon iconoEditar = new FontIcon(MaterialDesign.MDI_PENCIL);
                botonEditar.setGraphic(iconoEditar);
                botonEditar.setOnAction(event -> {
                    Categoria categoria = getTableRow().getItem();
                    if (categoria != null){
                        editarCategoria(categoria);
                    }
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty){
                super.updateItem(item, empty);
                if (empty){
                    setGraphic(null);
                } else {
                    setGraphic(botonEditar);
                }
            }
        });
    }

    private void editarCategoria(Categoria categoria) {
        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Editar Categoria");
        alerta.setHeaderText("Nombre de la categoria:");
        alerta.setContentText(null);
        TextField txtNombreCategoria = new TextField();
        HBox contenedor = new HBox(txtNombreCategoria);
        contenedor.setPadding(new Insets(15, 5, 5, 5));
        contenedor.setAlignment(Pos.CENTER);
        txtNombreCategoria.setText(categoria.conseguirNombre());
        alerta.getDialogPane().setContent(contenedor);
        ButtonType botonContinuar = new ButtonType("Guardar", ButtonBar.ButtonData.OK_DONE);
        ButtonType botonCancelar = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);
        ButtonType botonEliminar = new ButtonType("Eliminar", ButtonBar.ButtonData.OTHER);
        alerta.getButtonTypes().setAll(botonContinuar, botonCancelar, botonEliminar);
        Optional<ButtonType> resultado = alerta.showAndWait();
        if (resultado.isPresent() && resultado.get() == botonContinuar){
            if (txtNombreCategoria.getText().trim().isEmpty()){
                new Alert(AlertType.ERROR,"Debe ingresar un nombre para la categoria.").showAndWait();
                return;
            }
            categoria.colocarNombre(txtNombreCategoria.getText().trim());
            CategoriaRepositorio catRepo = new CategoriaRepositorio();
            catRepo.editarCategoria(categoria);
        }
        if (resultado.isPresent() && resultado.get() == botonEliminar){
            if (!categoriaRepositorio.borrarCategoriaFisicamente(categoria.conseguirId())){
                CategoriaRepositorio catRepo = new CategoriaRepositorio();
                catRepo.borrarCategoria(categoria.conseguirId());
            }
        }
        cargarCategorias();
    }

    @FXML 
    public void initialize(){
        inicializarCategoriasTablaVista();
        inicializarColumnaEdicion();
        tablaCategorias.setItems(ListaCategorias);
        cargarCategorias();
    }

    @FXML 
    public void salir(){
        Stage ventana = (Stage) botonSalir.getScene().getWindow();
        ventana.close();
    }

    @FXML
    public void agregarCategoria() {

        Alert alerta = new Alert(AlertType.CONFIRMATION);
        alerta.setTitle("Agregar Categoría");
        alerta.setHeaderText("Nombre de la nueva categoría:");
        alerta.setContentText(null);

        TextField txtNombreCategoria = new TextField();
        txtNombreCategoria.setPromptText("Nombre");

        HBox contenedor = new HBox(txtNombreCategoria);
        contenedor.setPadding(new Insets(15, 5, 5, 5));
        contenedor.setAlignment(Pos.CENTER);

        alerta.getDialogPane().setContent(contenedor);

        ButtonType botonContinuar = new ButtonType(
            "Guardar", ButtonBar.ButtonData.OK_DONE
        );

        ButtonType botonCancelar = new ButtonType(
            "Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE
        );

        alerta.getButtonTypes().setAll(
            botonContinuar,
            botonCancelar
        );

        Optional<ButtonType> resultado = alerta.showAndWait();

        if (resultado.isEmpty() || resultado.get() != botonContinuar) {
            return;
        }

        String nombre = txtNombreCategoria.getText().trim();

        if (nombre.isEmpty()) {

            new Alert(
                AlertType.ERROR,
                "Debe ingresar un nombre para la categoría."
            ).showAndWait();

            return;
        }

        try {

            // Verificar si la categoría ya existe.
            Categoria categoriaExistente =
                categoriaRepositorio.consultarCategoriaPorNombre(nombre);

            if (categoriaExistente != null) {

                // La categoría existe y está activa.
                if (categoriaExistente.conseguirActivo() == 1) {

                    new Alert(
                        AlertType.WARNING,
                        "La categoría '" + nombre
                        + "' ya se encuentra registrada y activa."
                    ).showAndWait();

                    return;
                }

                // La categoría existe, pero está desactivada.
                Alert confirmacion = new Alert(AlertType.CONFIRMATION);

                confirmacion.setTitle("Recuperar categoría");

                confirmacion.setHeaderText(
                    "La categoría ya existe, pero está desactivada."
                );

                confirmacion.setContentText(
                    "¿Deseas reactivar la categoría '"
                    + categoriaExistente.conseguirNombre() + "'?"
                );

                ButtonType botonReactivar = new ButtonType(
                    "Reactivar",
                    ButtonBar.ButtonData.OK_DONE
                );

                ButtonType botonNo = new ButtonType(
                    "Cancelar",
                    ButtonBar.ButtonData.CANCEL_CLOSE
                );

                confirmacion.getButtonTypes().setAll(
                    botonReactivar,
                    botonNo
                );

                Optional<ButtonType> respuesta = confirmacion.showAndWait();

                if (respuesta.isPresent()
                        && respuesta.get() == botonReactivar) {

                    boolean reactivada = categoriaRepositorio.activarCategoria(
                        categoriaExistente.conseguirId()
                    );

                    if (reactivada) {

                        new Alert(
                            AlertType.INFORMATION,
                            "La categoría fue reactivada correctamente."
                        ).showAndWait();

                    } else {

                        new Alert(
                            AlertType.WARNING,
                            "No se pudo reactivar la categoría."
                        ).showAndWait();
                    }
                }

            } else {

                // La categoría no existe, se registra normalmente.
                Categoria nuevaCategoria = new Categoria();
                nuevaCategoria.colocarNombre(nombre);

                categoriaRepositorio.agregarCategoria(nuevaCategoria);

                new Alert(
                    AlertType.INFORMATION,
                    "La categoría fue registrada correctamente."
                ).showAndWait();
            }

            cargarCategorias();

        } catch (RuntimeException e) {

            new Alert(
                AlertType.ERROR,
                "No se pudo guardar la categoría: " + e.getMessage()
            ).showAndWait();

            e.printStackTrace();
        }
    }
}
