package com.gestion.controller.cliente;

import com.gestion.model.Cliente;
import com.gestion.model.PagoCliente;
import com.gestion.service.PagoClienteServicio;

import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class HistorialPagosControlador {

    private final PagoClienteServicio pagoClienteServicio =
        new PagoClienteServicio();

    private final ObservableList<PagoCliente> pagos =
        FXCollections.observableArrayList();

    private Cliente cliente;


    @FXML
    private Label lblCliente;

    @FXML
    private Label lblSaldo;

    @FXML
    private TableView<PagoCliente> tablaPagos;

    @FXML
    private TableColumn<PagoCliente, String> columnaFecha;

    @FXML
    private TableColumn<PagoCliente, Double> columnaMonto;

    @FXML
    private TableColumn<PagoCliente, String> columnaObservacion;


    @FXML
    public void initialize() {

        columnaFecha.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirFecha()
            )
        );

        columnaMonto.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda.getValue().conseguirMonto()
            )
        );

        columnaObservacion.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirObservacion()
            )
        );

        tablaPagos.setItems(pagos);
    }


    public void cargarCliente(Cliente cliente) {

        this.cliente = cliente;

        lblCliente.setText(
            cliente
                .conseguirPersona()
                .conseguirNombre()
        );

        double saldo =
            cliente.conseguirSaldo() == null
                ? 0.0
                : cliente.conseguirSaldo();

        lblSaldo.setText(
            "$" + String.format("%,.0f", saldo)
        );

        cargarPagos();
    }


    private void cargarPagos() {

        if (cliente == null) {
            return;
        }

        pagos.setAll(
            pagoClienteServicio.consultarPagos(
                cliente
            )
        );

        tablaPagos.refresh();
    }


    @FXML
    private void cerrar() {

        Stage ventana =
            (Stage) tablaPagos
                .getScene()
                .getWindow();

        ventana.close();
    }
}