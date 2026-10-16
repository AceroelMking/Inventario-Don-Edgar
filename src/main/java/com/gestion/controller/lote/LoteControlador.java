
package com.gestion.controller.lote;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

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
import javafx.scene.control.Button;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class LoteControlador {

    private final LoteServicio loteServicio =
            new LoteServicio();

    private final DateTimeFormatter formatoFecha =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private TableView<Lote> tablaLotes;

    @FXML
    private TableColumn<Lote, String> columnaProducto;

    @FXML
    private TableColumn<Lote, String> columnaLote;

    @FXML
    private TableColumn<Lote, String> columnaFechaRegistro;

    // Nueva columna: fecha de expedición
    @FXML
    private TableColumn<Lote, String> columnaFechaExpedicion;

    @FXML
    private TableColumn<Lote, String> columnaFechaVencimiento;

    @FXML
    private TableColumn<Lote, Double> columnaCantidad;

    @FXML
    private TableColumn<Lote, Double> columnaValor;

    @FXML
    private TableColumn<Lote, String> columnaMotivo;

    @FXML
    private TableColumn<Lote, Void> columnaEditar;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        configurarTabla();

        cargarLotes();
    }

    // =====================================================
    // CONFIGURACIÓN DE LA TABLA
    // =====================================================

    private void configurarTabla() {

        columnaProducto.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        celda.getValue().conseguirProducto() == null
                                ? ""
                                : celda.getValue()
                                        .conseguirProducto()
                                        .conseguirNombre()
                )
        );

        columnaLote.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue().conseguirLote()
                        )
                )
        );

        columnaFechaRegistro.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        formatearFecha(
                                celda.getValue().conseguirFechaRegistro()
                        )
                )
        );

        // NUEVA COLUMNA
        columnaFechaExpedicion.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        formatearFecha(
                                celda.getValue().conseguirFechaExpedicion()
                        )
                )
        );

        columnaFechaVencimiento.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        formatearFecha(
                                celda.getValue().conseguirFechaVencimiento()
                        )
                )
        );

        columnaCantidad.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue().conseguirCantidadMovimiento()
                )
        );

        columnaValor.setCellValueFactory(
                celda -> new SimpleObjectProperty<>(
                        celda.getValue().conseguirValorMovimiento()
                )
        );

        columnaMotivo.setCellValueFactory(
                celda -> new SimpleStringProperty(
                        valorSeguro(
                                celda.getValue().conseguirMotivo()
                        )
                )
        );

        configurarColumnaEditar();
    }

    // =====================================================
    // FORMATO DE FECHAS
    // =====================================================

    private String formatearFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            return "";
        }

        try {

            LocalDate fechaConvertida =
                    LocalDate.parse(fecha);

            return fechaConvertida.format(formatoFecha);

        } catch (DateTimeParseException e) {

            // Si la fecha tiene un formato inesperado,
            // mostramos su valor original.
            return fecha;
        }
    }

    private String valorSeguro(String valor) {
        return valor == null ? "" : valor;
    }

    // =====================================================
    // BOTÓN EDITAR
    // =====================================================

    private void configurarColumnaEditar() {

        columnaEditar.setCellFactory(
                columna -> new TableCell<>() {

                    private final Button botonEditar =
                            new Button("Editar");

                    {
                        botonEditar.getStyleClass().add(
                                "boton-secundario"
                        );

                        botonEditar.setOnAction(evento -> {

                            Lote lote = getTableRow().getItem();

                            if (lote != null) {
                                abrirFormularioEditar(lote);
                            }
                        });
                    }

                    @Override
                    protected void updateItem(
                            Void item,
                            boolean vacio
                    ) {

                        super.updateItem(item, vacio);

                        if (vacio) {

                            setGraphic(null);

                        } else {

                            setGraphic(botonEditar);
                        }
                    }
                }
        );
    }

    // =====================================================
    // CARGAR LOTES
    // =====================================================

    private void cargarLotes() {

        ObservableList<Lote> lotes =
                loteServicio.consultarTodo();

        tablaLotes.setItems(lotes);

        tablaLotes.refresh();
    }

    // =====================================================
    // ABRIR FORMULARIO PARA AGREGAR
    // =====================================================

    @FXML
    private void abrirFormularioAgregar() {

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/gestion/view/lote/LoteAgregar.fxml"
                    )
            );

            Parent root = loader.load();

            Stage ventana = new Stage();

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

            // Actualizar tabla al cerrar el formulario.
            cargarLotes();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el formulario de entrada."
            );
        }
    }

    // =====================================================
    // ABRIR FORMULARIO PARA EDITAR
    // =====================================================

    private void abrirFormularioEditar(Lote lote) {

        if (lote == null || lote.conseguirId() == null) {

            mostrarError(
                    "No se pudo identificar la entrada seleccionada."
            );

            return;
        }

        try {

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(
                            "/com/gestion/view/lote/LoteEditar.fxml"
                    )
            );

            Parent root = loader.load();

            LoteFormularioControlador controlador =
                    loader.getController();

            controlador.cargarLote(
                    lote.conseguirId()
            );

            Stage ventana = new Stage();

            ventana.setTitle(
                    "Editar entrada de inventario"
            );

            ventana.setScene(
                    new Scene(root)
            );

            ventana.initModality(
                    Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            // Recargar los lotes después de editar.
            cargarLotes();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo abrir el formulario de edición."
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                    "No se pudo cargar la entrada seleccionada.\n"
                    + e.getMessage()
            );
        }
    }

    // =====================================================
    // MENSAJES
    // =====================================================

    private void mostrarError(String mensaje) {

        new Alert(
                Alert.AlertType.ERROR,
                mensaje
        ).showAndWait();
    }
}
