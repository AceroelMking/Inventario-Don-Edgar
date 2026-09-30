package com.gestion.controller.cliente;

import java.io.IOException;

import com.gestion.model.Cliente;
import com.gestion.service.ClienteServicio;

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
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClienteControlador {

    private final ClienteServicio clienteServicio =
        new ClienteServicio();

    private final ObservableList<Cliente> clientesObservables =
        FXCollections.observableArrayList();

    private final FilteredList<Cliente> clientesFiltrados =
        new FilteredList<>(clientesObservables, cliente -> true);

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Cliente> tablaClientes;

    @FXML
    private TableColumn<Cliente, String> columnaIdentificacion;

    @FXML
    private TableColumn<Cliente, String> columnaNombre;

    @FXML
    private TableColumn<Cliente, String> columnaTelefono;

    @FXML
    private TableColumn<Cliente, String> columnaCorreo;

    @FXML
    private TableColumn<Cliente, Double> columnaSaldo;

    @FXML
    private TableColumn<Cliente, Void> columnaEditar;

    @FXML
    public void initialize() {

        configurarTabla();
        configurarBusqueda();
        configurarColumnaEditar();

        tablaClientes.setItems(clientesFiltrados);

        cargarClientes();
    }

    private void configurarTabla() {

        columnaIdentificacion.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirPersona()
                    .conseguirNumeroIdentidad()
            )
        );

        columnaNombre.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue()
                    .conseguirPersona()
                    .conseguirNombre()
            )
        );

        columnaTelefono.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirTelefono()
            )
        );

        columnaCorreo.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirCorreoElectronico()
            )
        );

        columnaSaldo.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda.getValue().conseguirSaldo()
            )
        );
    }

    private void configurarBusqueda() {

        txtBuscar.textProperty().addListener(
            (observable, anterior, nuevo) -> {

                String busqueda =
                    nuevo == null
                        ? ""
                        : nuevo.trim().toLowerCase();

                clientesFiltrados.setPredicate(cliente -> {

                    if (busqueda.isEmpty()) {
                        return true;
                    }

                    String nombre =
                        cliente.conseguirPersona().conseguirNombre() == null
                            ? ""
                            : cliente.conseguirPersona()
                                .conseguirNombre()
                                .toLowerCase();

                    String identificacion =
                        cliente.conseguirPersona()
                            .conseguirNumeroIdentidad() == null
                            ? ""
                            : cliente.conseguirPersona()
                                .conseguirNumeroIdentidad()
                                .toLowerCase();

                    String telefono =
                        cliente.conseguirTelefono() == null
                            ? ""
                            : cliente.conseguirTelefono()
                                .toLowerCase();

                    String correo =
                        cliente.conseguirCorreoElectronico() == null
                            ? ""
                            : cliente.conseguirCorreoElectronico()
                                .toLowerCase();

                    return nombre.contains(busqueda)
                        || identificacion.contains(busqueda)
                        || telefono.contains(busqueda)
                        || correo.contains(busqueda);
                });
            }
        );
    }

    private void cargarClientes() {

        clientesObservables.setAll(
            clienteServicio.consultarTodo()
        );
    }

    private void configurarColumnaEditar() {

        columnaEditar.setCellFactory(
            columna -> new TableCell<>() {

                private final Button botonEditar =
                    new Button("Editar");

                {
                    botonEditar
                        .getStyleClass()
                        .add("boton-editar");

                    botonEditar.setOnAction(event -> {

                        Cliente cliente =
                            getTableRow().getItem();

                        if (cliente != null) {
                            abrirFormularioEditar(cliente);
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

    @FXML
    private void abrirFormularioAgregar() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/cliente/ClienteAgregar.fxml"
                    )
                );

            Parent root = loader.load();

            Stage ventana = new Stage();

            ventana.setTitle("Nuevo cliente");
            ventana.setScene(new Scene(root));

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarClientes();

        } catch (IOException e) {

            e.printStackTrace();

            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo abrir el formulario de cliente."
            ).showAndWait();
        }
    }

    private void abrirFormularioEditar(
        Cliente cliente
    ) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/cliente/ClienteEditar.fxml"
                    )
                );

            Parent root = loader.load();

            ClienteFormularioControlador controlador =
                loader.getController();

            controlador.cargarCliente(cliente);

            Stage ventana = new Stage();

            ventana.setTitle("Editar cliente");
            ventana.setScene(new Scene(root));

            ventana.initModality(
                Modality.APPLICATION_MODAL
            );

            ventana.showAndWait();

            cargarClientes();

        } catch (IOException e) {

            e.printStackTrace();

            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo abrir el formulario de edición."
            ).showAndWait();
        }
    }
}