package com.gestion.controller.cliente;

import java.io.IOException;
import java.util.Optional;

import com.gestion.model.Cliente;
import com.gestion.service.ClienteServicio;
import com.gestion.service.PagoClienteServicio;

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
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class ClienteControlador {

    private final ClienteServicio clienteServicio =
        new ClienteServicio();

    private final PagoClienteServicio pagoClienteServicio =
        new PagoClienteServicio();

    private final ObservableList<Cliente> clientesObservables =
        FXCollections.observableArrayList();

    private final FilteredList<Cliente> clientesFiltrados =
        new FilteredList<>(
            clientesObservables,
            cliente -> true
        );


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

        tablaClientes.setItems(
            clientesFiltrados
        );

        cargarClientes();
    }


    private void configurarTabla() {

        columnaIdentificacion.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda
                    .getValue()
                    .conseguirPersona()
                    .conseguirNumeroIdentidad()
            )
        );


        columnaNombre.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda
                    .getValue()
                    .conseguirPersona()
                    .conseguirNombre()
            )
        );


        columnaTelefono.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda
                    .getValue()
                    .conseguirTelefono()
            )
        );


        columnaCorreo.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda
                    .getValue()
                    .conseguirCorreoElectronico()
            )
        );


        columnaSaldo.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                celda
                    .getValue()
                    .conseguirSaldo()
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
                ) -> {

                    String busqueda =
                        nuevo == null
                            ? ""
                            : nuevo
                                .trim()
                                .toLowerCase();


                    clientesFiltrados.setPredicate(
                        cliente -> {

                            if (busqueda.isEmpty()) {
                                return true;
                            }


                            String nombre =
                                cliente
                                    .conseguirPersona()
                                    .conseguirNombre() == null
                                    ? ""
                                    : cliente
                                        .conseguirPersona()
                                        .conseguirNombre()
                                        .toLowerCase();


                            String identificacion =
                                cliente
                                    .conseguirPersona()
                                    .conseguirNumeroIdentidad() == null
                                    ? ""
                                    : cliente
                                        .conseguirPersona()
                                        .conseguirNumeroIdentidad()
                                        .toLowerCase();


                            String telefono =
                                cliente.conseguirTelefono() == null
                                    ? ""
                                    : cliente
                                        .conseguirTelefono()
                                        .toLowerCase();


                            String correo =
                                cliente
                                    .conseguirCorreoElectronico() == null
                                    ? ""
                                    : cliente
                                        .conseguirCorreoElectronico()
                                        .toLowerCase();


                            return nombre.contains(busqueda)
                                || identificacion.contains(busqueda)
                                || telefono.contains(busqueda)
                                || correo.contains(busqueda);
                        }
                    );
                }
            );
    }


    private void cargarClientes() {

        clientesObservables.setAll(
            clienteServicio.consultarTodo()
        );

        tablaClientes.refresh();
    }


    private void configurarColumnaEditar() {

        columnaEditar.setCellFactory(
            columna -> new TableCell<>() {

                private final Button botonEditar =
                    new Button("Editar");

                private final Button botonAbonar =
                    new Button("Abonar");

                private final Button botonHistorial =
                    new Button("Historial");

                private final Button botonEliminar =
                    new Button("Eliminar");

                private final HBox filaSuperior =
                    new HBox(6);

                private final HBox filaInferior =
                    new HBox(6);

                private final VBox contenedor =
                    new VBox(6);


                {

                    botonEditar
                        .getStyleClass()
                        .add("boton-editar");

                    botonAbonar
                        .getStyleClass()
                        .add("boton-secundario");

                    botonHistorial
                        .getStyleClass()
                        .add("boton-secundario");

                    botonEliminar
                        .getStyleClass()
                        .add("boton-eliminar");


                    /*
                    * Tamaño uniforme de los botones.
                    */

                    botonEditar.setPrefWidth(82);
                    botonAbonar.setPrefWidth(82);
                    botonHistorial.setPrefWidth(82);
                    botonEliminar.setPrefWidth(82);

                    botonEditar.setMinWidth(82);
                    botonAbonar.setMinWidth(82);
                    botonHistorial.setMinWidth(82);
                    botonEliminar.setMinWidth(82);


                    filaSuperior.getChildren().addAll(
                        botonEditar,
                        botonAbonar
                    );

                    filaInferior.getChildren().addAll(
                        botonHistorial,
                        botonEliminar
                    );

                    contenedor.getChildren().addAll(
                        filaSuperior,
                        filaInferior
                    );


                    botonEditar.setOnAction(
                        event -> {

                            Cliente cliente =
                                getTableRow().getItem();

                            if (cliente != null) {

                                abrirFormularioEditar(
                                    cliente
                                );
                            }
                        }
                    );


                    botonAbonar.setOnAction(
                        event -> {

                            Cliente cliente =
                                getTableRow().getItem();

                            if (cliente != null) {

                                registrarAbono(
                                    cliente
                                );
                            }
                        }
                    );


                    botonHistorial.setOnAction(
                        event -> {

                            Cliente cliente =
                                getTableRow().getItem();

                            if (cliente != null) {

                                abrirHistorialPagos(
                                    cliente
                                );
                            }
                        }
                    );


                    botonEliminar.setOnAction(
                        event -> {

                            Cliente cliente =
                                getTableRow().getItem();

                            if (cliente != null) {

                                eliminarCliente(
                                    cliente
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

                        setGraphic(null);

                    } else {

                        setGraphic(
                            contenedor
                        );
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


            Parent root =
                loader.load();


            Stage ventana =
                new Stage();


            ventana.setTitle(
                "Nuevo cliente"
            );


            ventana.setScene(
                new Scene(root)
            );


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


            Parent root =
                loader.load();


            ClienteFormularioControlador controlador =
                loader.getController();


            controlador.cargarCliente(
                cliente
            );


            Stage ventana =
                new Stage();


            ventana.setTitle(
                "Editar cliente"
            );


            ventana.setScene(
                new Scene(root)
            );


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


    private void registrarAbono(
        Cliente cliente
    ) {

        if (cliente == null) {
            return;
        }


        double saldoActual =
            cliente.conseguirSaldo() == null
                ? 0.0
                : cliente.conseguirSaldo();


        if (saldoActual <= 0) {

            new Alert(
                Alert.AlertType.INFORMATION,
                "El cliente no tiene saldo pendiente."
            ).showAndWait();

            return;
        }


        String nombre =
            cliente
                .conseguirPersona()
                .conseguirNombre();


        TextInputDialog dialogoMonto =
            new TextInputDialog();


        dialogoMonto.setTitle(
            "Registrar abono"
        );


        dialogoMonto.setHeaderText(
            "Registrar abono de "
            + nombre
        );


        dialogoMonto.setContentText(
            "Saldo pendiente: $"
            + String.format(
                "%,.0f",
                saldoActual
            )
            + "\n\nMonto del abono:"
        );


        Optional<String> resultadoMonto =
            dialogoMonto.showAndWait();


        if (resultadoMonto.isEmpty()) {
            return;
        }


        double monto;


        try {

            monto =
                Double.parseDouble(
                    resultadoMonto
                        .get()
                        .trim()
                );


        } catch (NumberFormatException e) {

            new Alert(
                Alert.AlertType.ERROR,
                "El monto debe ser un número válido."
            ).showAndWait();

            return;
        }


        if (monto <= 0) {

            new Alert(
                Alert.AlertType.ERROR,
                "El monto debe ser mayor que cero."
            ).showAndWait();

            return;
        }


        if (monto > saldoActual) {

            new Alert(
                Alert.AlertType.ERROR,
                "El abono no puede superar "
                + "el saldo pendiente."
            ).showAndWait();

            return;
        }


        TextInputDialog dialogoObservacion =
            new TextInputDialog();


        dialogoObservacion.setTitle(
            "Observación"
        );


        dialogoObservacion.setHeaderText(
            "Observación del abono"
        );


        dialogoObservacion.setContentText(
            "Observación opcional:"
        );


        Optional<String> resultadoObservacion =
            dialogoObservacion.showAndWait();


        if (resultadoObservacion.isEmpty()) {
            return;
        }


        String observacion =
            resultadoObservacion.get();


        Alert confirmacion =
            new Alert(
                Alert.AlertType.CONFIRMATION
            );


        confirmacion.setTitle(
            "Confirmar abono"
        );


        confirmacion.setHeaderText(
            "¿Registrar este abono?"
        );


        confirmacion.setContentText(
            "Cliente: "
            + nombre
            + "\n\nSaldo actual: $"
            + String.format(
                "%,.0f",
                saldoActual
            )
            + "\nAbono: $"
            + String.format(
                "%,.0f",
                monto
            )
            + "\nSaldo restante: $"
            + String.format(
                "%,.0f",
                saldoActual - monto
            )
        );


        ButtonType respuesta =
            confirmacion
                .showAndWait()
                .orElse(
                    ButtonType.CANCEL
                );


        if (respuesta != ButtonType.OK) {
            return;
        }


        try {

            int idPago =
                pagoClienteServicio.registrarPago(
                    cliente,
                    monto,
                    observacion
                );


            cargarClientes();


            new Alert(
                Alert.AlertType.INFORMATION,
                "Abono registrado correctamente."
                + "\n\nNúmero de pago: "
                + idPago
                + "\nCliente: "
                + nombre
                + "\nAbono: $"
                + String.format(
                    "%,.0f",
                    monto
                )
                + "\nSaldo restante: $"
                + String.format(
                    "%,.0f",
                    saldoActual - monto
                )
            ).showAndWait();


        } catch (RuntimeException e) {

            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo registrar el abono.\n"
                + e.getMessage()
            ).showAndWait();
        }
    }


    private void abrirHistorialPagos(
        Cliente cliente
    ) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/cliente/HistorialPagos.fxml"
                    )
                );


            Parent root =
                loader.load();


            HistorialPagosControlador controlador =
                loader.getController();


            controlador.cargarCliente(
                cliente
            );


            Stage ventana =
                new Stage();


            ventana.setTitle(
                "Historial de abonos"
            );


            ventana.setScene(
                new Scene(root)
            );


            ventana.initModality(
                Modality.APPLICATION_MODAL
            );


            ventana.showAndWait();


            cargarClientes();


        } catch (IOException e) {

            e.printStackTrace();


            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo abrir el historial de abonos."
            ).showAndWait();


        } catch (RuntimeException e) {

            e.printStackTrace();


            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo cargar el historial de abonos.\n"
                + e.getMessage()
            ).showAndWait();
        }
    }


    private void eliminarCliente(
        Cliente cliente
        ) {

        if (cliente == null) {
            return;
        }

        double saldoPendiente =
            cliente.conseguirSaldo() == null
                ? 0.0
                : cliente.conseguirSaldo();

        /*
        * No permitir eliminar clientes
        * que todavía tengan deuda.
        */
        if (saldoPendiente > 0) {

            Alert alerta =
                new Alert(
                    Alert.AlertType.WARNING
                );

            alerta.setTitle(
                "Cliente con saldo pendiente"
            );

            alerta.setHeaderText(
                "No se puede eliminar este cliente."
            );

            alerta.setContentText(
                "El cliente todavía tiene un saldo pendiente de $"
                + String.format(
                    "%,.0f",
                    saldoPendiente
                )
                + ".\n\n"
                + "Debe registrar los abonos correspondientes "
                + "hasta dejar el saldo en cero antes de eliminarlo."
            );

            alerta.showAndWait();

            return;
        }

        String nombre =
            cliente
                .conseguirPersona()
                .conseguirNombre();

        Alert confirmacion =
            new Alert(
                Alert.AlertType.CONFIRMATION
            );

        confirmacion.setTitle(
            "Eliminar cliente"
        );

        confirmacion.setHeaderText(
            "¿Desea eliminar al cliente "
            + nombre
            + "?"
        );

        confirmacion.setContentText(
            "El cliente dejará de estar disponible, "
            + "pero su historial de ventas se conservará."
        );

        ButtonType respuesta =
            confirmacion
                .showAndWait()
                .orElse(
                    ButtonType.CANCEL
                );

        if (respuesta != ButtonType.OK) {
            return;
        }

        try {

            clienteServicio.eliminarCliente(
                cliente.conseguirId()
            );

            cargarClientes();

            new Alert(
                Alert.AlertType.INFORMATION,
                "Cliente eliminado correctamente."
            ).showAndWait();

        } catch (RuntimeException e) {

            new Alert(
                Alert.AlertType.ERROR,
                "No se pudo eliminar el cliente.\n"
                + e.getMessage()
            ).showAndWait();
        }
    }
}