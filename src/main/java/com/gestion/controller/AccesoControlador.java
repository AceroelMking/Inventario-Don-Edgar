package com.gestion.controller;

import java.io.IOException;

import com.gestion.model.Usuario;
import com.gestion.service.UsuarioServicio;
import com.gestion.service.UsuarioServicio.CuentaBloqueadaException;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class AccesoControlador {

    // =====================================================
    // SERVICIO DE USUARIOS
    // =====================================================

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    // =====================================================
    // COMPONENTES FXML
    // =====================================================

    @FXML
    private StackPane panelAcceso;

    @FXML
    private ImageView imagenFondo;

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        // Ajustar imagen al tamaño de la ventana.
        imagenFondo
            .fitWidthProperty()
            .bind(panelAcceso.widthProperty());

        imagenFondo
            .fitHeightProperty()
            .bind(panelAcceso.heightProperty());

        lblMensaje.setText("");

        // Limpiar mensajes al cambiar el usuario.
        txtUsuario.textProperty().addListener(
            (observable, anterior, nuevo) -> {
                lblMensaje.setText("");
            }
        );

        Platform.runLater(
            () -> txtUsuario.requestFocus()
        );
    }

    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    @FXML
    private void iniciarSesion() {

        lblMensaje.setText("");

        String nombreUsuario =
            txtUsuario.getText();

        String contrasena =
            txtContrasena.getText();

        // Validar nombre de usuario.
        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            mostrarError(
                "Debe ingresar el nombre de usuario."
            );

            txtUsuario.requestFocus();
            return;
        }

        // Validar contraseña.
        if (contrasena == null
                || contrasena.isBlank()) {

            mostrarError(
                "Debe ingresar la contraseña."
            );

            txtContrasena.requestFocus();
            return;
        }

        nombreUsuario = nombreUsuario.trim();

        try {

            // Autenticar usuario.
            Usuario usuarioAutenticado =
                usuarioServicio.autenticar(
                    nombreUsuario,
                    contrasena
                );

            // Credenciales incorrectas.
            if (usuarioAutenticado == null) {

                int intentosRestantes =
                    usuarioServicio
                        .consultarIntentosRestantes(
                            nombreUsuario
                        );

                Usuario administrador =
                    usuarioServicio
                        .consultarAdministrador();

                boolean esAdministrador =
                    administrador != null
                    && nombreUsuario.equals(
                        administrador.conseguirNombreUsuario()
                    );

                if (esAdministrador) {

                    mostrarError(
                        "Usuario o contraseña incorrectos.\n"
                        + "Intentos restantes: "
                        + intentosRestantes
                    );

                } else {

                    mostrarError(
                        "Usuario o contraseña incorrectos."
                    );
                }

                txtContrasena.clear();
                txtContrasena.requestFocus();

                return;
            }

            // Comprobar credenciales predeterminadas.
            boolean credencialesPredeterminadas =
                usuarioServicio
                    .usaCredencialesPredeterminadas(
                        usuarioAutenticado,
                        contrasena
                    );

            if (credencialesPredeterminadas) {

                abrirCambioObligatorio();
                return;
            }

            // Acceso normal al sistema.
            abrirSistema();

        } catch (CuentaBloqueadaException e) {

            mostrarError(e.getMessage());

            txtContrasena.clear();
            txtContrasena.requestFocus();

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo validar el acceso al sistema."
            );
        }
    }

    // =====================================================
    // ABRIR RECUPERACIÓN DE CONTRASEÑA
    // =====================================================

    @FXML
    private void abrirRecuperacion() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/RecuperacionContrasena.fxml"
                    )
                );

            Parent root = loader.load();

            Stage ventana = obtenerVentana();

            ventana.setScene(
                new Scene(root)
            );

            ventana.setTitle(
                "Recuperación de contraseña - Don Edgar"
            );

            ventana.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo abrir la recuperación "
                + "de contraseña."
            );

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                "Ocurrió un error al abrir "
                + "la recuperación de contraseña."
            );
        }
    }

    // =====================================================
    // ABRIR MENÚ PRINCIPAL
    // =====================================================

    private void abrirSistema() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/MenuLateral.fxml"
                    )
                );

            Parent root = loader.load();

            Stage ventana = obtenerVentana();

            ventana.setScene(
                new Scene(root)
            );

            ventana.setTitle(
                "Víveres y Cárnicos Don Edgar"
            );

            ventana.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo abrir el menú principal."
            );
        }
    }

    // =====================================================
    // CAMBIO OBLIGATORIO DE CREDENCIALES
    // =====================================================

    private void abrirCambioObligatorio() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/ConfiguracionUsuario.fxml"
                    )
                );

            Parent root = loader.load();

            ConfiguracionUsuarioControlador controlador =
                loader.getController();

            controlador.configurarCambioObligatorio(
                true
            );

            Stage ventana = obtenerVentana();

            controlador.colocarAccionAlGuardar(
                () -> abrirMenuDespuesDelCambio(
                    ventana
                )
            );

            ventana.setScene(
                new Scene(root)
            );

            ventana.setTitle(
                "Cambio obligatorio de credenciales"
            );

            ventana.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo abrir la configuración de acceso."
            );
        }
    }

    // =====================================================
    // ABRIR MENÚ DESPUÉS DEL CAMBIO
    // =====================================================

    private void abrirMenuDespuesDelCambio(
            Stage ventana) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/MenuLateral.fxml"
                    )
                );

            Parent menu = loader.load();

            ventana.setScene(
                new Scene(menu)
            );

            ventana.setTitle(
                "Víveres y Cárnicos Don Edgar"
            );

            ventana.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            System.err.println(
                "No se pudo abrir el menú principal "
                + "después de cambiar las credenciales."
            );
        }
    }

    // =====================================================
    // OBTENER VENTANA ACTUAL
    // =====================================================

    private Stage obtenerVentana() {

        return (Stage) txtUsuario
            .getScene()
            .getWindow();
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
