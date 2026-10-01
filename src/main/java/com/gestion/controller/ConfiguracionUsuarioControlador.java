package com.gestion.controller;

import com.gestion.model.Usuario;
import com.gestion.service.UsuarioServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ConfiguracionUsuarioControlador {

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    private boolean cambioObligatorio = false;

    private Runnable accionAlGuardar;

    @FXML
    private TextField txtNuevoUsuario;

    @FXML
    private PasswordField txtContrasenaActual;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private Label checkUsuario;

    @FXML
    private Label checkActual;

    @FXML
    private Label checkLongitud;

    @FXML
    private Label checkMayuscula;

    @FXML
    private Label checkMinuscula;

    @FXML
    private Label checkNumero;

    @FXML
    private Label checkEspecial;

    @FXML
    private Label checkCoinciden;

    @FXML
    private Label lblMensaje;

    @FXML
    private Label lblObligatorio;

    @FXML
    public void initialize() {

        Usuario usuario =
            usuarioServicio.consultarAdministrador();

        if (usuario != null) {
            txtNuevoUsuario.setText(
                usuario.conseguirNombreUsuario()
            );
        }

        txtNuevoUsuario.textProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    actualizarChecks()
            );

        txtContrasenaActual.textProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    actualizarChecks()
            );

        txtNuevaContrasena.textProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    actualizarChecks()
            );

        txtConfirmarContrasena.textProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    actualizarChecks()
            );

        actualizarChecks();
    }

    public void configurarCambioObligatorio(
        boolean obligatorio
    ) {

        this.cambioObligatorio =
            obligatorio;

        if (obligatorio) {

            lblObligatorio.setText(
                "Por seguridad debe cambiar el usuario "
                + "y la contraseña predeterminados antes "
                + "de utilizar el sistema."
            );

        } else {

            lblObligatorio.setText("");
        }
    }

    public void colocarAccionAlGuardar(
        Runnable accion
    ) {
        this.accionAlGuardar = accion;
    }

    private void actualizarChecks() {

        String usuario =
            txtNuevoUsuario.getText() == null
                ? ""
                : txtNuevoUsuario.getText().trim();

        String actual =
            txtContrasenaActual.getText() == null
                ? ""
                : txtContrasenaActual.getText();

        String nueva =
            txtNuevaContrasena.getText() == null
                ? ""
                : txtNuevaContrasena.getText();

        String confirmar =
            txtConfirmarContrasena.getText() == null
                ? ""
                : txtConfirmarContrasena.getText();

        boolean usuarioValido =
            usuario.length() >= 4
            && usuario.matches(
                "^[A-Za-z0-9._-]+$"
            );

        boolean actualIngresada =
            !actual.isBlank();

        boolean longitud =
            nueva.length() >= 12;

        boolean mayuscula =
            nueva.matches(".*[A-Z].*");

        boolean minuscula =
            nueva.matches(".*[a-z].*");

        boolean numero =
            nueva.matches(".*[0-9].*");

        boolean especial =
            nueva.matches(
                ".*[^A-Za-z0-9].*"
            );

        boolean coinciden =
            !nueva.isEmpty()
            && nueva.equals(confirmar);

        colocarEstado(
            checkUsuario,
            usuarioValido,
            "Mínimo 4 caracteres y caracteres permitidos"
        );

        colocarEstado(
            checkActual,
            actualIngresada,
            "Contraseña actual requerida"
        );

        colocarEstado(
            checkLongitud,
            longitud,
            "Mínimo 12 caracteres"
        );

        colocarEstado(
            checkMayuscula,
            mayuscula,
            "Una letra mayúscula"
        );

        colocarEstado(
            checkMinuscula,
            minuscula,
            "Una letra minúscula"
        );

        colocarEstado(
            checkNumero,
            numero,
            "Un número"
        );

        colocarEstado(
            checkEspecial,
            especial,
            "Un carácter especial"
        );

        colocarEstado(
            checkCoinciden,
            coinciden,
            "Las contraseñas deben coincidir"
        );
    }

    private void colocarEstado(
        Label label,
        boolean correcto,
        String texto
    ) {

        if (correcto) {

            label.setText(
                "✓ " + texto
            );

            label.setStyle(
                "-fx-text-fill: #2e7d32;"
            );

        } else {

            label.setText(
                "✗ " + texto
            );

            label.setStyle(
                "-fx-text-fill: #c62828;"
            );
        }
    }

    @FXML
    private void guardar() {

        lblMensaje.setText("");

        String nuevoUsuario =
            txtNuevoUsuario.getText();

        String actual =
            txtContrasenaActual.getText();

        String nueva =
            txtNuevaContrasena.getText();

        String confirmar =
            txtConfirmarContrasena.getText();

        actualizarChecks();

        if (
            nuevoUsuario == null
            || nuevoUsuario.isBlank()
        ) {
            mostrarError(
                "Revise el campo de nuevo usuario."
            );
            txtNuevoUsuario.requestFocus();
            return;
        }

        if (
            actual == null
            || actual.isBlank()
        ) {
            mostrarError(
                "Ingrese la contraseña actual."
            );
            txtContrasenaActual.requestFocus();
            return;
        }

        if (
            nueva == null
            || nueva.isBlank()
        ) {
            mostrarError(
                "Ingrese una nueva contraseña."
            );
            txtNuevaContrasena.requestFocus();
            return;
        }

        try {

            usuarioServicio.validarNombreUsuario(
                nuevoUsuario
            );

            usuarioServicio.validarContrasena(
                nueva
            );

        } catch (IllegalArgumentException e) {

            mostrarError(
                e.getMessage()
            );

            return;
        }

        if (!nueva.equals(confirmar)) {

            mostrarError(
                "Las contraseñas nuevas no coinciden."
            );

            txtConfirmarContrasena
                .requestFocus();

            return;
        }

        if (
            cambioObligatorio
            && "admin".equalsIgnoreCase(
                nuevoUsuario.trim()
            )
        ) {

            mostrarError(
                "Debe cambiar el nombre de usuario predeterminado."
            );

            txtNuevoUsuario.requestFocus();

            return;
        }

        try {

            usuarioServicio.cambiarCredenciales(
                actual,
                nuevoUsuario,
                nueva,
                confirmar
            );

            lblMensaje.setStyle(
                "-fx-text-fill: #2e7d32; "
                + "-fx-font-weight: bold;"
            );

            lblMensaje.setText(
                "Credenciales actualizadas correctamente."
            );

            txtContrasenaActual.clear();
            txtNuevaContrasena.clear();
            txtConfirmarContrasena.clear();

            actualizarChecks();

            if (accionAlGuardar != null) {
                accionAlGuardar.run();
            }

        } catch (RuntimeException e) {

            mostrarError(
                e.getMessage()
            );
        }
    }

    private void mostrarError(
        String mensaje
    ) {

        lblMensaje.setStyle(
            "-fx-text-fill: #c62828; "
            + "-fx-font-weight: bold;"
        );

        lblMensaje.setText(
            mensaje
        );
    }
}