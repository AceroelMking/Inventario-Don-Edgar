
package com.gestion.controller;

import com.gestion.model.Usuario;
import com.gestion.service.UsuarioServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class ConfiguracionUsuarioControlador {

    // =====================================================
    // SERVICIO
    // =====================================================

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    private boolean cambioObligatorio = false;

    private Runnable accionAlGuardar;

    // =====================================================
    // CAMPOS FXML
    // =====================================================

    @FXML
    private TextField txtNuevoUsuario;

    @FXML
    private TextField txtCorreoElectronico;

    @FXML
    private PasswordField txtContrasenaActual;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    // =====================================================
    // VALIDACIONES VISUALES
    // =====================================================

    @FXML
    private Label checkUsuario;

    @FXML
    private Label checkCorreo;

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

    // =====================================================
    // MENSAJES
    // =====================================================

    @FXML
    private Label lblMensaje;

    @FXML
    private Label lblObligatorio;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        Usuario usuario =
            usuarioServicio.consultarAdministrador();

        if (usuario != null) {

            txtNuevoUsuario.setText(
                usuario.conseguirNombreUsuario()
            );

            String correo =
                usuario.conseguirCorreoElectronico();

            if (correo != null) {

                txtCorreoElectronico.setText(
                    correo
                );
            }
        }

        txtNuevoUsuario.textProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    actualizarChecks()
            );

        txtCorreoElectronico.textProperty()
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

    // =====================================================
    // CONFIGURAR CAMBIO OBLIGATORIO
    // =====================================================

    public void configurarCambioObligatorio(
            boolean obligatorio) {

        this.cambioObligatorio = obligatorio;

        if (obligatorio) {

            lblObligatorio.setText(
                "Por seguridad debe cambiar el usuario "
                + "y la contraseña predeterminados, "
                + "y registrar un correo electrónico "
                + "para recuperar el acceso al sistema."
            );

        } else {

            lblObligatorio.setText("");
        }
    }

    // =====================================================
    // ACCIÓN DESPUÉS DE GUARDAR
    // =====================================================

    public void colocarAccionAlGuardar(
            Runnable accion) {

        this.accionAlGuardar = accion;
    }

    // =====================================================
    // ACTUALIZAR VALIDACIONES VISUALES
    // =====================================================

    private void actualizarChecks() {

        String usuario =
            txtNuevoUsuario.getText() == null
                ? ""
                : txtNuevoUsuario.getText().trim();

        String correo =
            txtCorreoElectronico.getText() == null
                ? ""
                : txtCorreoElectronico.getText().trim();

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

        boolean correoValido =
            correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
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
            checkCorreo,
            correoValido,
            "Correo electrónico válido"
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

    // =====================================================
    // COLOCAR ESTADO DE VALIDACIÓN
    // =====================================================

    private void colocarEstado(
            Label label,
            boolean correcto,
            String texto) {

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

    // =====================================================
    // GUARDAR CONFIGURACIÓN
    // =====================================================

    @FXML
    private void guardar() {

        lblMensaje.setText("");

        String nuevoUsuario =
            txtNuevoUsuario.getText();

        String correo =
            txtCorreoElectronico.getText();

        String actual =
            txtContrasenaActual.getText();

        String nueva =
            txtNuevaContrasena.getText();

        String confirmar =
            txtConfirmarContrasena.getText();

        actualizarChecks();

        // ================================================
        // VALIDAR USUARIO
        // ================================================

        if (nuevoUsuario == null
                || nuevoUsuario.isBlank()) {

            mostrarError(
                "Revise el campo de nuevo usuario."
            );

            txtNuevoUsuario.requestFocus();

            return;
        }

        // ================================================
        // VALIDAR CORREO
        // ================================================

        if (correo == null
                || correo.isBlank()) {

            mostrarError(
                "Debe registrar un correo electrónico "
                + "para recuperar su contraseña."
            );

            txtCorreoElectronico.requestFocus();

            return;
        }

        correo = correo.trim();

        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            mostrarError(
                "Ingrese un correo electrónico válido."
            );

            txtCorreoElectronico.requestFocus();

            return;
        }

        // ================================================
        // VALIDAR CONTRASEÑA ACTUAL
        // ================================================

        if (actual == null
                || actual.isBlank()) {

            mostrarError(
                "Ingrese la contraseña actual."
            );

            txtContrasenaActual.requestFocus();

            return;
        }

        // ================================================
        // VALIDAR NUEVA CONTRASEÑA
        // ================================================

        if (nueva == null
                || nueva.isBlank()) {

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

            mostrarError(e.getMessage());

            return;
        }

        // ================================================
        // CONFIRMAR CONTRASEÑA
        // ================================================

        if (!nueva.equals(confirmar)) {

            mostrarError(
                "Las contraseñas nuevas no coinciden."
            );

            txtConfirmarContrasena.requestFocus();

            return;
        }

        // ================================================
        // CAMBIO OBLIGATORIO
        // ================================================

        if (cambioObligatorio
                && "admin".equalsIgnoreCase(
                    nuevoUsuario.trim())) {

            mostrarError(
                "Debe cambiar el nombre de usuario "
                + "predeterminado."
            );

            txtNuevoUsuario.requestFocus();

            return;
        }

        // ================================================
        // GUARDAR CAMBIOS
        // ================================================

        try {

            /*
             * Primero se validan y actualizan
             * las credenciales.
             */
            usuarioServicio.cambiarCredenciales(
                actual,
                nuevoUsuario,
                nueva,
                confirmar
            );

            /*
             * Después se registra el correo
             * electrónico del administrador.
             */
            usuarioServicio.actualizarCorreoAdministrador(
                correo
            );

            lblMensaje.setStyle(
                "-fx-text-fill: #2e7d32; "
                + "-fx-font-weight: bold;"
            );

            lblMensaje.setText(
                "Credenciales y correo electrónico "
                + "actualizados correctamente."
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
                e.getMessage() == null
                    ? "No se pudo guardar la configuración."
                    : e.getMessage()
            );
        }
    }

    // =====================================================
    // MOSTRAR ERROR
    // =====================================================

    private void mostrarError(
            String mensaje) {

        lblMensaje.setStyle(
            "-fx-text-fill: #c62828; "
            + "-fx-font-weight: bold;"
        );

        lblMensaje.setText(mensaje);
    }
}
