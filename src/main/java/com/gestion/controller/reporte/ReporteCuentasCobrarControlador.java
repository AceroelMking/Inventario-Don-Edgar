package com.gestion.controller.reporte;

import java.io.File;
import java.io.IOException;

import com.gestion.model.Cliente;
import com.gestion.repository.ClienteRepositorio;
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

public class ReporteCuentasCobrarControlador {

    private final ClienteRepositorio clienteRepositorio =
        new ClienteRepositorio();

    private final ReportePdfServicio reportePdfServicio =
        new ReportePdfServicio();

    private final ObservableList<Cliente> clientes =
        FXCollections.observableArrayList();

    private final FilteredList<Cliente> clientesFiltrados =
        new FilteredList<>(clientes, cliente -> true);

    @FXML
    private TextField txtBuscar;

    @FXML
    private TableView<Cliente> tablaCuentas;

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
    private TableColumn<Cliente, String> columnaEstado;

    @FXML
    private Label lblCantidadClientes;

    @FXML
    private Label lblTotalCobrar;

    @FXML
    public void initialize() {

        configurarTabla();
        configurarBusqueda();

        tablaCuentas.setItems(clientesFiltrados);

        cargarClientes();
    }

    private void configurarTabla() {

        columnaIdentificacion.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirPersona() != null
                    ? textoSeguro(
                        celda.getValue()
                            .conseguirPersona()
                            .conseguirNumeroIdentidad()
                    )
                    : ""
            )
        );

        columnaNombre.setCellValueFactory(
            celda -> new SimpleStringProperty(
                celda.getValue().conseguirPersona() != null
                    ? textoSeguro(
                        celda.getValue()
                            .conseguirPersona()
                            .conseguirNombre()
                    )
                    : ""
            )
        );

        columnaTelefono.setCellValueFactory(
            celda -> new SimpleStringProperty(
                textoSeguro(
                    celda.getValue().conseguirTelefono()
                )
            )
        );

        columnaCorreo.setCellValueFactory(
            celda -> new SimpleStringProperty(
                textoSeguro(
                    celda.getValue()
                        .conseguirCorreoElectronico()
                )
            )
        );

        columnaSaldo.setCellValueFactory(
            celda -> new SimpleObjectProperty<>(
                valorSeguro(
                    celda.getValue().conseguirSaldo()
                )
            )
        );

        columnaEstado.setCellValueFactory(
            celda -> new SimpleStringProperty(
                conseguirEstado(celda.getValue())
            )
        );
    }

    private void configurarBusqueda() {

        txtBuscar.textProperty().addListener(
            (observable, anterior, nuevo) ->
                aplicarFiltro()
        );
    }

    private void cargarClientes() {

        try {

            clientes.clear();

            for (Cliente cliente :
                    clienteRepositorio.consultarTodo()) {

                double saldo =
                    valorSeguro(
                        cliente.conseguirSaldo()
                    );

                if (saldo > 0) {
                    clientes.add(cliente);
                }
            }

            aplicarFiltro();

        } catch (Exception e) {

            mostrarError(
                "No se pudieron cargar las cuentas por cobrar.",
                e
            );
        }
    }

    private void aplicarFiltro() {

        String busqueda =
            txtBuscar.getText() == null
                ? ""
                : txtBuscar
                    .getText()
                    .trim()
                    .toLowerCase();

        clientesFiltrados.setPredicate(cliente -> {

            if (busqueda.isEmpty()) {
                return true;
            }

            String identificacion = "";
            String nombre = "";

            if (cliente.conseguirPersona() != null) {

                identificacion =
                    textoSeguro(
                        cliente
                            .conseguirPersona()
                            .conseguirNumeroIdentidad()
                    ).toLowerCase();

                nombre =
                    textoSeguro(
                        cliente
                            .conseguirPersona()
                            .conseguirNombre()
                    ).toLowerCase();
            }

            String telefono =
                textoSeguro(
                    cliente.conseguirTelefono()
                ).toLowerCase();

            String correo =
                textoSeguro(
                    cliente.conseguirCorreoElectronico()
                ).toLowerCase();

            return identificacion.contains(busqueda)
                || nombre.contains(busqueda)
                || telefono.contains(busqueda)
                || correo.contains(busqueda);
        });

        actualizarResumen();
    }

    private void actualizarResumen() {

        int cantidad =
            clientesFiltrados.size();

        double total = 0;

        for (Cliente cliente : clientesFiltrados) {

            total +=
                valorSeguro(
                    cliente.conseguirSaldo()
                );
        }

        lblCantidadClientes.setText(
            String.valueOf(cantidad)
        );

        lblTotalCobrar.setText(
            formatoDinero(total)
        );
    }

    private String conseguirEstado(Cliente cliente) {

        double saldo =
            valorSeguro(
                cliente.conseguirSaldo()
            );

        if (saldo > 0) {
            return "Pendiente";
        }

        return "Al día";
    }

    @FXML
    private void limpiarBusqueda() {

        txtBuscar.clear();
    }

    @FXML
    private void actualizar() {

        cargarClientes();
    }

    @FXML
    private void abrirVentas() {

        cargarReporte(
            "/com/gestion/view/reporte/ReporteVentas.fxml"
        );
    }

    @FXML
    private void abrirInventario() {

        cargarReporte(
            "/com/gestion/view/reporte/ReporteInventario.fxml"
        );
    }

    private void cargarReporte(String ruta) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(ruta)
                );

            Parent vista =
                loader.load();

            BorderPane panelPrincipal =
                (BorderPane)
                    tablaCuentas
                        .getScene()
                        .getRoot();

            panelPrincipal.setCenter(vista);

        } catch (IOException e) {

            mostrarError(
                "No se pudo abrir el reporte.",
                e
            );
        }
    }

    @FXML
    private void exportarPdf() {

        if (clientesFiltrados.isEmpty()) {

            mostrarAdvertencia(
                "No hay cuentas por cobrar para exportar."
            );

            return;
        }

        FileChooser selector =
            new FileChooser();

        selector.setTitle(
            "Guardar reporte de cuentas por cobrar"
        );

        selector.setInitialFileName(
            "reporte_cuentas_por_cobrar.pdf"
        );

        selector.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Archivo PDF",
                "*.pdf"
            )
        );

        Window ventana =
            tablaCuentas
                .getScene()
                .getWindow();

        File archivo =
            selector.showSaveDialog(ventana);

        if (archivo == null) {
            return;
        }

        try {

            reportePdfServicio
                .generarReporteCuentasCobrar(
                    archivo,
                    FXCollections.observableArrayList(
                        clientesFiltrados
                    )
                );

            Alert alerta =
                new Alert(
                    Alert.AlertType.INFORMATION
                );

            alerta.setTitle("Reporte generado");
            alerta.setHeaderText(null);

            alerta.setContentText(
                "El reporte PDF se generó correctamente."
            );

            alerta.showAndWait();

        } catch (Exception e) {

            mostrarError(
                "No se pudo generar el reporte PDF.",
                e
            );
        }
    }

    private double valorSeguro(Double valor) {

        return valor == null
            ? 0.0
            : valor;
    }

    private String textoSeguro(String texto) {

        return texto == null
            ? ""
            : texto;
    }

    private String formatoDinero(double valor) {

        return String.format(
            "$%,.0f",
            valor
        );
    }

    private void mostrarAdvertencia(String mensaje) {

        Alert alerta =
            new Alert(
                Alert.AlertType.WARNING
            );

        alerta.setTitle("Advertencia");
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }

    private void mostrarError(
            String mensaje,
            Exception excepcion) {

        excepcion.printStackTrace();

        Alert alerta =
            new Alert(
                Alert.AlertType.ERROR
            );

        alerta.setTitle("Error");
        alerta.setHeaderText(null);

        alerta.setContentText(
            mensaje
            + "\n"
            + excepcion.getMessage()
        );

        alerta.showAndWait();
    }
}