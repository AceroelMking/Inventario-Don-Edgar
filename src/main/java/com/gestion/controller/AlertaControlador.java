package com.gestion.controller.alerta;

import com.gestion.model.Alerta;
import com.gestion.service.AlertaServicio;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

public class AlertaControlador {

    private final AlertaServicio alertaServicio =
        new AlertaServicio();


    private final ObservableList<Alerta> alertas =
        FXCollections.observableArrayList();


    private final FilteredList<Alerta> alertasFiltradas =
        new FilteredList<>(
            alertas,
            alerta -> true
        );


    @FXML
    private TextField txtBuscar;

    @FXML
    private ComboBox<String> cmbTipo;


    @FXML
    private TableView<Alerta> tablaAlertas;

    @FXML
    private TableColumn<Alerta, String> columnaTipo;

    @FXML
    private TableColumn<Alerta, String> columnaProducto;

    @FXML
    private TableColumn<Alerta, String> columnaCodigo;

    @FXML
    private TableColumn<Alerta, String> columnaLote;

    @FXML
    private TableColumn<Alerta, String> columnaVencimiento;

    @FXML
    private TableColumn<Alerta, String> columnaDetalle;

    @FXML
    private TableColumn<Alerta, String> columnaEstado;


    @FXML
    private Label lblTotalAlertas;

    @FXML
    private Label lblStock;

    @FXML
    private Label lblProximosVencer;

    @FXML
    private Label lblVencidos;


    @FXML
    public void initialize() {

        configurarTabla();

        configurarFiltros();

        configurarAnchoTabla();

        tablaAlertas.setItems(
            alertasFiltradas
        );

        cargarAlertas();
    }


    private void configurarTabla() {

        columnaTipo.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirTipo()
                    )
                )
        );


        columnaProducto.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirProducto()
                    )
                )
        );


        columnaCodigo.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirCodigoBarras()
                    )
                )
        );


        columnaLote.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirLote()
                    )
                )
        );


        columnaVencimiento.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirFechaVencimiento()
                    )
                )
        );


        columnaDetalle.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirDetalle()
                    )
                )
        );


        columnaEstado.setCellValueFactory(
            celda ->
                new SimpleStringProperty(
                    textoSeguro(
                        celda
                            .getValue()
                            .conseguirEstado()
                    )
                )
        );
    }


    private void configurarFiltros() {

        cmbTipo.getItems().setAll(
            "Todas",
            "Stock",
            "Próximo a vencer",
            "Vencido"
        );

        cmbTipo.setValue(
            "Todas"
        );


        txtBuscar
            .textProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> aplicarFiltro()
            );


        cmbTipo
            .valueProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) -> aplicarFiltro()
            );
    }


    private void cargarAlertas() {

        try {

            alertas.setAll(
                alertaServicio
                    .consultarAlertas()
            );

            aplicarFiltro();

        } catch (RuntimeException e) {

            e.printStackTrace();

            javafx.scene.control.Alert alerta =
                new javafx.scene.control.Alert(
                    javafx.scene.control.Alert.AlertType.ERROR
                );

            alerta.setTitle(
                "Error"
            );

            alerta.setHeaderText(
                "No se pudieron cargar las alertas."
            );

            alerta.setContentText(
                e.getMessage()
            );

            alerta.showAndWait();
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


        String tipoSeleccionado =
            cmbTipo.getValue() == null
                ? "Todas"
                : cmbTipo.getValue();


        alertasFiltradas.setPredicate(
            alerta -> {

                boolean coincideBusqueda =
                    busqueda.isEmpty()
                    || textoSeguro(
                        alerta.conseguirProducto()
                    )
                        .toLowerCase()
                        .contains(busqueda)

                    || textoSeguro(
                        alerta.conseguirCodigoBarras()
                    )
                        .toLowerCase()
                        .contains(busqueda)

                    || textoSeguro(
                        alerta.conseguirLote()
                    )
                        .toLowerCase()
                        .contains(busqueda)

                    || textoSeguro(
                        alerta.conseguirEstado()
                    )
                        .toLowerCase()
                        .contains(busqueda);


                boolean coincideTipo =
                    switch (tipoSeleccionado) {

                        case "Stock" ->
                            "Stock".equals(
                                alerta.conseguirTipo()
                            );

                        case "Próximo a vencer" ->
                            "Próximo a vencer".equals(
                                alerta.conseguirEstado()
                            );

                        case "Vencido" ->
                            "Vencido".equals(
                                alerta.conseguirEstado()
                            );

                        default ->
                            true;
                    };


                return coincideBusqueda
                    && coincideTipo;
            }
        );


        actualizarResumen();

        tablaAlertas.refresh();
    }


    private void actualizarResumen() {

        int stock = 0;

        int proximos = 0;

        int vencidos = 0;


        for (
            Alerta alerta
            : alertasFiltradas
        ) {

            if (
                "Stock".equals(
                    alerta.conseguirTipo()
                )
            ) {

                stock++;
            }


            if (
                "Próximo a vencer".equals(
                    alerta.conseguirEstado()
                )
            ) {

                proximos++;
            }


            if (
                "Vencido".equals(
                    alerta.conseguirEstado()
                )
            ) {

                vencidos++;
            }
        }


        lblTotalAlertas.setText(
            String.valueOf(
                alertasFiltradas.size()
            )
        );


        lblStock.setText(
            String.valueOf(stock)
        );


        lblProximosVencer.setText(
            String.valueOf(proximos)
        );


        lblVencidos.setText(
            String.valueOf(vencidos)
        );
    }


    private void configurarAnchoTabla() {

        tablaAlertas
            .widthProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nuevo
                ) ->
                    ajustarAnchoColumnas(
                        nuevo.doubleValue()
                    )
            );


        tablaAlertas
            .sceneProperty()
            .addListener(
                (
                    observable,
                    anterior,
                    nueva
                ) -> {

                    if (nueva != null) {

                        tablaAlertas.applyCss();

                        tablaAlertas.layout();

                        ajustarAnchoColumnas(
                            tablaAlertas.getWidth()
                        );
                    }
                }
            );
    }


    private void ajustarAnchoColumnas(
        double anchoTabla
    ) {

        if (anchoTabla <= 0) {
            return;
        }


        double disponible =
            Math.max(
                0,
                anchoTabla - 30
            );


        columnaTipo.setPrefWidth(
            disponible * 0.09
        );

        columnaProducto.setPrefWidth(
            disponible * 0.17
        );

        columnaCodigo.setPrefWidth(
            disponible * 0.12
        );

        columnaLote.setPrefWidth(
            disponible * 0.10
        );

        columnaVencimiento.setPrefWidth(
            disponible * 0.12
        );

        columnaDetalle.setPrefWidth(
            disponible * 0.25
        );

        columnaEstado.setPrefWidth(
            disponible * 0.15
        );
    }


    @FXML
    private void limpiarFiltros() {

        txtBuscar.clear();

        cmbTipo.setValue(
            "Todas"
        );
    }


    @FXML
    private void actualizar() {

        cargarAlertas();
    }


    private String textoSeguro(
        String texto
    ) {

        return texto == null
            ? ""
            : texto;
    }
}