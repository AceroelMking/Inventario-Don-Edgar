package com.gestion.controller.lote;

import java.io.IOException;

import com.gestion.model.Lote;
import com.gestion.service.LoteServicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoteControlador {

    private final LoteServicio loteServicio =
        new LoteServicio();

    @FXML
    private TableView<Lote> tablaLotes;

    @FXML
    private TableColumn<Lote, String> columnaProducto;

    @FXML
    private TableColumn<Lote, String> columnaLote;

    @FXML
    private TableColumn<Lote, String> columnaFechaRegistro;

    @FXML
    private TableColumn<Lote, String> columnaFechaVencimiento;

    @FXML
    private TableColumn<Lote, Double> columnaCantidad;

    @FXML
    private TableColumn<Lote, Double> columnaValor;

    @FXML
    private TableColumn<Lote, String> columnaMotivo;


    @FXML
    public void initialize() {

        configurarTabla();

        cargarLotes();
    }


    private void configurarTabla() {

        columnaProducto.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirProducto()
                    .conseguirNombre()
            )
        );

        columnaLote.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirLote()
            )
        );

        columnaFechaRegistro.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirFechaRegistro()
            )
        );

        columnaFechaVencimiento.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirFechaVencimiento()
            )
        );

        columnaCantidad.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda.getValue()
                    .conseguirCantidadMovimiento()
            )
        );

        columnaValor.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda.getValue()
                    .conseguirValorMovimiento()
            )
        );

        columnaMotivo.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirMotivo()
            )
        );
    }


    private void cargarLotes() {

        ObservableList<Lote> lotes =
            loteServicio.consultarTodo();

        tablaLotes.setItems(lotes);
    }


    @FXML
    private void abrirFormularioAgregar() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/lote/LoteAgregar.fxml"
                    )
                );

            Parent root =
                loader.load();

            Stage ventana =
                new Stage();

            ventana.setTitle(
                "Registrar entrada de inventario"
            );

            ventana.setScene(
                new Scene(root)
            );

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarLotes();

        } catch (IOException e) {

            e.printStackTrace();

            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo abrir el formulario de entrada."
            ).showAndWait();
        }
    }
}