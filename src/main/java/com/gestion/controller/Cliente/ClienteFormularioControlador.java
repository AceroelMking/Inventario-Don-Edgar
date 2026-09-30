package com.gestion.controller.cliente;

import com.gestion.model.Cliente;
import com.gestion.model.Persona;
import com.gestion.service.ClienteServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ClienteFormularioControlador {

    private Cliente cliente;

    private final ClienteServicio clienteServicio = new ClienteServicio();

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

    public void cargarCliente(Cliente cliente) {
        this.cliente = cliente;

        txtNombre.setText(
            cliente.conseguirPersona().conseguirNombre()
        );

        txtIdentificacion.setText(
            cliente.conseguirPersona().conseguirNumeroIdentidad()
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

    @FXML
    private void guardar() {

        String nombre = txtNombre.getText() == null
            ? ""
            : txtNombre.getText().trim();

        String identificacion = txtIdentificacion.getText() == null
            ? ""
            : txtIdentificacion.getText().trim();

        String telefono = txtTelefono.getText() == null
            ? ""
            : txtTelefono.getText().trim();

        String correo = txtCorreo.getText() == null
            ? ""
            : txtCorreo.getText().trim();

        if (nombre.isBlank()) {
            mostrarError("El nombre del cliente es obligatorio.");
            return;
        }

        if (identificacion.isBlank()) {
            mostrarError("El número de identificación es obligatorio.");
            return;
        }

        if (!telefono.isBlank() && !telefono.matches("[0-9+ ]+")) {
            mostrarError("El teléfono contiene caracteres no válidos.");
            return;
        }

        if (!correo.isBlank()
                && !correo.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            mostrarError("El correo electrónico no es válido.");
            return;
        }

        try {

            if (cliente == null) {

                cliente = new Cliente();

                Persona persona = new Persona();

                persona.colocarNombre(nombre);
                persona.colocarNumeroIdentidad(identificacion);

                cliente.colocarPersona(persona);
                cliente.colocarTelefono(telefono);
                cliente.colocarCorreoElectronico(correo);
                cliente.colocarSaldo(0.0);

                clienteServicio.agregarCliente(cliente);

                mostrarInformacion(
                    "Cliente registrado correctamente."
                );

            } else {

                cliente.conseguirPersona().colocarNombre(nombre);

                cliente.conseguirPersona()
                    .colocarNumeroIdentidad(identificacion);

                cliente.colocarTelefono(telefono);
                cliente.colocarCorreoElectronico(correo);

                clienteServicio.editarCliente(cliente);

                mostrarInformacion(
                    "Cliente actualizado correctamente."
                );
            }

            cerrarVentana();

        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo guardar el cliente.\n" + e.getMessage()
            );
        }
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private void cerrarVentana() {
        Stage ventana =
            (Stage) botonCancelar.getScene().getWindow();

        ventana.close();
    }

    private void mostrarError(String mensaje) {
        new Alert(
            Alert.AlertType.ERROR,
            mensaje
        ).showAndWait();
    }

    private void mostrarInformacion(String mensaje) {
        new Alert(
            Alert.AlertType.INFORMATION,
            mensaje
        ).showAndWait();
    }
}