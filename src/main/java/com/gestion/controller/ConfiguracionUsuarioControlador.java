
package com.gestion.controller;

import com.gestion.model.Usuario;
import com.gestion.service.ContrasenaServicio;
import com.gestion.service.RecuperacionContrasenaServicio;
import com.gestion.service.UsuarioServicio;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;

public class ConfiguracionUsuarioControlador {

    // =====================================================
    // SERVICIOS
    // =====================================================

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    private final ContrasenaServicio contrasenaServicio =
        new ContrasenaServicio();

    private final RecuperacionContrasenaServicio
        recuperacionServicio =
            new RecuperacionContrasenaServicio();

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
                txtCorreoElectronico.setText(correo);
            }
        }

        txtNuevoUsuario.textProperty().addListener(
            (obs, anterior, nuevo) -> actualizarChecks()
        );

        txtCorreoElectronico.textProperty().addListener(
            (obs, anterior, nuevo) -> actualizarChecks()
        );

        txtContrasenaActual.textProperty().addListener(
            (obs, anterior, nuevo) -> actualizarChecks()
        );

        txtNuevaContrasena.textProperty().addListener(
            (obs, anterior, nuevo) -> actualizarChecks()
        );

        txtConfirmarContrasena.textProperty().addListener(
            (obs, anterior, nuevo) -> actualizarChecks()
        );

        actualizarChecks();
    }

    // =====================================================
    // CAMBIO OBLIGATORIO
    // =====================================================

    public void configurarCambioObligatorio(
            boolean obligatorio) {

        this.cambioObligatorio = obligatorio;

        if (obligatorio) {

            lblObligatorio.setText(
                "Por seguridad debe cambiar el usuario "
                + "y la contraseña predeterminados. "
                + "También deberá guardar una clave "
                + "de emergencia para recuperar el acceso "
                + "sin conexión a internet."
            );

        } else {

            lblObligatorio.setText("");
        }
    }

    // =====================================================
    // ACCIÓN DESPUÉS DE GUARDAR
    // =====================================================

    public void colocarAccionAlGuardar(Runnable accion) {
        this.accionAlGuardar = accion;
    }

    // =====================================================
    // ACTUALIZAR VALIDACIONES
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
            && usuario.matches("^[A-Za-z0-9._-]+$");

        boolean correoValido =
            correo.isBlank()
            || correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$"
            );

        colocarEstado(
            checkUsuario,
            usuarioValido,
            "Mínimo 4 caracteres y caracteres permitidos"
        );

        colocarEstado(
            checkCorreo,
            correoValido,
            "Correo opcional (si lo ingresa, debe ser válido)"
        );

        colocarEstado(
            checkActual,
            !actual.isBlank(),
            "Contraseña actual requerida"
        );

        colocarEstado(
            checkLongitud,
            nueva.length() >= 12,
            "Mínimo 12 caracteres"
        );

        colocarEstado(
            checkMayuscula,
            nueva.matches(".*[A-Z].*"),
            "Una letra mayúscula"
        );

        colocarEstado(
            checkMinuscula,
            nueva.matches(".*[a-z].*"),
            "Una letra minúscula"
        );

        colocarEstado(
            checkNumero,
            nueva.matches(".*[0-9].*"),
            "Un número"
        );

        colocarEstado(
            checkEspecial,
            nueva.matches(".*[^A-Za-z0-9].*"),
            "Un carácter especial"
        );

        colocarEstado(
            checkCoinciden,
            !nueva.isEmpty() && nueva.equals(confirmar),
            "Las contraseñas deben coincidir"
        );
    }

    // =====================================================
    // ESTADO DE VALIDACIÓN
    // =====================================================

    private void colocarEstado(
            Label label,
            boolean correcto,
            String texto) {

        if (correcto) {

            label.setText("✓ " + texto);

            label.setStyle(
                "-fx-text-fill: #2e7d32;"
            );

        } else {

            label.setText("✗ " + texto);

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

        try {

            usuarioServicio.validarNombreUsuario(
                nuevoUsuario
            );

            usuarioServicio.validarContrasena(
                nueva
            );

            if (actual == null || actual.isBlank()) {

                throw new IllegalArgumentException(
                    "Ingrese la contraseña actual."
                );
            }

            if (!nueva.equals(confirmar)) {

                throw new IllegalArgumentException(
                    "Las contraseñas nuevas no coinciden."
                );
            }

            if (cambioObligatorio
                    && "admin".equalsIgnoreCase(
                        nuevoUsuario.trim())) {

                throw new IllegalArgumentException(
                    "Debe cambiar el nombre de usuario "
                    + "predeterminado."
                );
            }

            String correoNormalizado =
                correo == null ? "" : correo.trim();

            if (!correoNormalizado.isEmpty()
                    && !correoNormalizado.matches(
                        "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

                throw new IllegalArgumentException(
                    "Ingrese un correo electrónico válido."
                );
            }

            // Actualizar credenciales.
            usuarioServicio.cambiarCredenciales(
                actual,
                nuevoUsuario,
                nueva,
                confirmar
            );

            // El correo ya no es obligatorio.
            usuarioServicio.guardarCorreoOpcional(
                correoNormalizado
            );

            txtContrasenaActual.clear();
            txtNuevaContrasena.clear();
            txtConfirmarContrasena.clear();

            actualizarChecks();

            // Si no hay clave, generarla.
            if (!recuperacionServicio.tieneClaveEmergencia()) {

                String clave =
                    recuperacionServicio
                        .generarYGuardarClaveEmergencia();

                mostrarClaveEmergencia(clave);
            }

            lblMensaje.setStyle(
                "-fx-text-fill: #2e7d32; "
                + "-fx-font-weight: bold;"
            );

            lblMensaje.setText(
                "Configuración guardada correctamente."
            );

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
    // MOSTRAR CLAVE DE EMERGENCIA
    // =====================================================

    private void mostrarClaveEmergencia(String clave) {

        Alert alerta = new Alert(
            Alert.AlertType.INFORMATION
        );

        alerta.setTitle("Clave de emergencia");

        alerta.setHeaderText(
            "Guarde su clave de recuperación"
        );

        Label descripcion = new Label(
            "Esta clave permitirá recuperar la contraseña "
            + "sin correo electrónico ni internet.\n\n"
            + "Cópiela o escríbala en un lugar seguro. "
            + "No se volverá a mostrar automáticamente."
        );

        descripcion.setWrapText(true);

        TextArea campoClave = new TextArea(clave);

        campoClave.setEditable(false);
        campoClave.setWrapText(true);
        campoClave.setPrefRowCount(2);
        campoClave.setPrefWidth(450);

        CheckBox confirmacion = new CheckBox(
            "Confirmo que guardé la clave de emergencia."
        );

        VBox contenido = new VBox(
            15,
            descripcion,
            campoClave,
            confirmacion
        );

        alerta.getDialogPane().setContent(contenido);

        ButtonType botonContinuar =
            new ButtonType("Continuar");

        alerta.getButtonTypes().setAll(
            botonContinuar
        );

        javafx.scene.Node boton =
            alerta.getDialogPane().lookupButton(
                botonContinuar
            );

        boton.disableProperty().bind(
            confirmacion.selectedProperty().not()
        );

        alerta.showAndWait();
    }

    // =====================================================
    // MOSTRAR ERROR
    // =====================================================

    private void mostrarError(String mensaje) {

        lblMensaje.setStyle(
            "-fx-text-fill: #c62828; "
            + "-fx-font-weight: bold;"
        );

        lblMensaje.setText(mensaje);
    }
}
