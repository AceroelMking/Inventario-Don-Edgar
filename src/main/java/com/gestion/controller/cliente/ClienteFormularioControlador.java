
package com.gestion.controller.cliente;

import com.gestion.model.Cliente;
import com.gestion.model.Persona;
import com.gestion.repository.ClienteRepositorio;
import com.gestion.service.ClienteServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Stage;

public class ClienteFormularioControlador {

    private Cliente cliente;

    private final ClienteServicio clienteServicio =
            new ClienteServicio();

    private final ClienteRepositorio clienteRepositorio =
            new ClienteRepositorio();

    @FXML
    private TextField txtNombre;

    @FXML
    private TextField txtIdentificacion;

    @FXML
    private TextField txtTelefono;

    @FXML
    private TextField txtCorreo;

    @FXML
    private Button botonCancelar;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        // Identificación: solo números, máximo 10 dígitos.
        txtIdentificacion.setTextFormatter(
            new TextFormatter<String>(cambio -> {

                String texto = cambio.getControlNewText();

                return texto.matches("[0-9]{0,10}")
                    ? cambio
                    : null;
            })
        );

        // Teléfono: solo números, máximo 10 dígitos.
        txtTelefono.setTextFormatter(
            new TextFormatter<String>(cambio -> {

                String texto = cambio.getControlNewText();

                return texto.matches("[0-9]{0,10}")
                    ? cambio
                    : null;
            })
        );
    }

    // =====================================================
    // CARGAR CLIENTE PARA EDICIÓN
    // =====================================================

    public void cargarCliente(Cliente cliente) {

        this.cliente = cliente;

        txtNombre.setText(
            cliente.conseguirPersona().conseguirNombre()
        );

        txtIdentificacion.setText(
            cliente.conseguirPersona()
                .conseguirNumeroIdentidad()
        );

        txtTelefono.setText(
            cliente.conseguirTelefono() == null
                ? ""
                : cliente.conseguirTelefono()
        );

        txtCorreo.setText(
            cliente.conseguirCorreoElectronico() == null
                ? ""
                : cliente.conseguirCorreoElectronico()
        );
    }

    // =====================================================
    // GUARDAR CLIENTE
    // =====================================================

    @FXML
    private void guardar() {

        String nombre = txtNombre.getText() == null
            ? ""
            : txtNombre.getText().trim();

        String identificacion =
            txtIdentificacion.getText() == null
                ? ""
                : txtIdentificacion.getText().trim();

        String telefono = txtTelefono.getText() == null
            ? ""
            : txtTelefono.getText().trim();

        String correo = txtCorreo.getText() == null
            ? ""
            : txtCorreo.getText().trim();

        // =================================================
        // VALIDAR NOMBRE
        // =================================================

        if (nombre.isBlank()) {

            mostrarError(
                "El nombre del cliente es obligatorio."
            );

            txtNombre.requestFocus();
            return;
        }

        // =================================================
        // VALIDAR IDENTIFICACIÓN
        // =================================================

        if (identificacion.isBlank()) {

            mostrarError(
                "El número de identificación es obligatorio."
            );

            txtIdentificacion.requestFocus();
            return;
        }

        if (!identificacion.matches("[0-9]{1,10}")) {

            mostrarError(
                "La identificación debe contener únicamente números "
                + "y tener máximo 10 dígitos."
            );

            txtIdentificacion.requestFocus();
            return;
        }

        // =================================================
        // VALIDAR TELÉFONO
        // =================================================

        if (!telefono.isBlank()
                && !telefono.matches("[0-9]{10}")) {

            mostrarError(
                "El número de teléfono debe contener "
                + "exactamente 10 dígitos."
            );

            txtTelefono.requestFocus();
            return;
        }

        // =================================================
        // VALIDAR CORREO ELECTRÓNICO
        // =================================================

        if (!correo.isBlank()
                && !correo.matches(
                    "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
                )) {

            mostrarError(
                "El correo electrónico no es válido."
            );

            txtCorreo.requestFocus();
            return;
        }

        // =================================================
        // REGISTRAR O EDITAR CLIENTE
        // =================================================

        try {

            if (cliente == null) {

                // Consultar si la cédula ya existe.
                Cliente existente =
                    clienteRepositorio.consultarPorIdentificacion(
                        identificacion
                    );

                boolean reactivacion =
                    existente != null
                    && !clienteRepositorio.estaActivo(
                        identificacion
                    );

                // Si existe y está activo, impedir duplicado.
                if (existente != null && !reactivacion) {

                    mostrarError(
                        "Ya existe un cliente activo "
                        + "con este número de identificación."
                    );

                    txtIdentificacion.requestFocus();
                    return;
                }

                // Crear objeto con los datos ingresados.
                Cliente nuevoCliente = new Cliente();

                Persona persona = new Persona();

                persona.colocarNombre(nombre);

                persona.colocarNumeroIdentidad(
                    identificacion
                );

                nuevoCliente.colocarPersona(persona);

                nuevoCliente.colocarTelefono(telefono);

                nuevoCliente.colocarCorreoElectronico(correo);

                nuevoCliente.colocarSaldo(0.0);

                // El repositorio decide si inserta o reactiva.
                clienteServicio.agregarCliente(nuevoCliente);

                cliente = nuevoCliente;

                if (reactivacion) {

                    mostrarInformacion(
                        "El cliente estaba desactivado.\n"
                        + "Se ha reactivado y sus datos "
                        + "se actualizaron correctamente."
                    );

                } else {

                    mostrarInformacion(
                        "Cliente registrado correctamente."
                    );
                }

            } else {

                // EDITAR CLIENTE

                cliente.conseguirPersona()
                    .colocarNombre(nombre);

                cliente.conseguirPersona()
                    .colocarNumeroIdentidad(
                        identificacion
                    );

                cliente.colocarTelefono(telefono);

                cliente.colocarCorreoElectronico(correo);

                clienteServicio.editarCliente(cliente);

                mostrarInformacion(
                    "Cliente actualizado correctamente."
                );
            }

            cerrarVentana();

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo guardar el cliente.\n"
                + e.getMessage()
            );
        }
    }

    // =====================================================
    // CANCELAR
    // =====================================================

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    // =====================================================
    // CERRAR VENTANA
    // =====================================================

    private void cerrarVentana() {

        Stage ventana = (Stage) botonCancelar
            .getScene()
            .getWindow();

        ventana.close();
    }

    // =====================================================
    // MOSTRAR ERRORES
    // =====================================================

    private void mostrarError(String mensaje) {

        new Alert(
            Alert.AlertType.ERROR,
            mensaje
        ).showAndWait();
    }

    // =====================================================
    // MOSTRAR INFORMACIÓN
    // =====================================================

    private void mostrarInformacion(String mensaje) {

        new Alert(
            Alert.AlertType.INFORMATION,
            mensaje
        ).showAndWait();
    }
}
