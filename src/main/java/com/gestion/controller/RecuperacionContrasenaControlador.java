
package com.gestion.controller;

import com.gestion.service.RecuperacionContrasenaServicio;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class RecuperacionContrasenaControlador {

    // =====================================================
    // SERVICIO DE RECUPERACIÓN
    // =====================================================

    private final RecuperacionContrasenaServicio
        recuperacionServicio =
            new RecuperacionContrasenaServicio();

    // =====================================================
    // COMPONENTES FXML
    // =====================================================

    @FXML
    private TextField txtCorreo;

    @FXML
    private TextField txtCodigo;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private VBox panelCorreo;

    @FXML
    private VBox panelCodigo;

    @FXML
    private VBox panelNuevaContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnEnviarCodigo;

    @FXML
    private Button btnVerificarCodigo;

    @FXML
    private Button btnCambiarContrasena;

    // =====================================================
    // ESTADO DEL PROCESO
    // =====================================================

    private String correoRecuperacion;

    private String codigoVerificado;

    private boolean codigoValidado = false;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        mostrarPanel(panelCorreo);

        lblMensaje.setText("");

        Platform.runLater(
            () -> txtCorreo.requestFocus()
        );
    }

    // =====================================================
    // ENVIAR CÓDIGO POR CORREO
    // =====================================================

    @FXML
    private void enviarCodigo() {

        String correo = txtCorreo.getText();

        if (correo == null || correo.isBlank()) {

            mostrarError(
                "Debe ingresar su correo electrónico."
            );

            return;
        }

        correo = correo.trim();

        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            mostrarError(
                "Ingrese un correo electrónico válido."
            );

            return;
        }

        final String correoSolicitado = correo;

        btnEnviarCodigo.setDisable(true);

        mostrarInformacion(
            "Procesando solicitud de recuperación..."
        );

        Task<Void> tarea = new Task<>() {

            @Override
            protected Void call() {

                recuperacionServicio.solicitarCodigo(
                    correoSolicitado
                );

                return null;
            }
        };

        tarea.setOnSucceeded(evento -> {

            btnEnviarCodigo.setDisable(false);

            correoRecuperacion = correoSolicitado;

            codigoVerificado = null;

            codigoValidado = false;

            txtCodigo.clear();

            mostrarPanel(panelCodigo);

            mostrarInformacion(
                "Si el correo está registrado, "
                + "recibirá un código de recuperación. "
                + "Revise también la carpeta de spam."
            );

            txtCodigo.requestFocus();
        });

        tarea.setOnFailed(evento -> {

            btnEnviarCodigo.setDisable(false);

            mostrarError(
                "No se pudo procesar la solicitud. "
                + "Compruebe la conexión e inténtelo nuevamente."
            );

            tarea.getException().printStackTrace();
        });

        iniciarTarea(tarea);
    }

    // =====================================================
    // VERIFICAR CÓDIGO
    // =====================================================

    @FXML
    private void verificarCodigo() {

        String codigo = txtCodigo.getText();

        if (codigo == null || codigo.isBlank()) {

            mostrarError(
                "Debe ingresar el código de recuperación."
            );

            return;
        }

        codigo = codigo.trim();

        if (!codigo.matches("^[0-9]{6}$")) {

            mostrarError(
                "El código debe contener 6 dígitos."
            );

            return;
        }

        btnVerificarCodigo.setDisable(true);

        try {

            boolean valido =
                recuperacionServicio.verificarCodigo(
                    correoRecuperacion,
                    codigo
                );

            if (!valido) {

                codigoValidado = false;

                codigoVerificado = null;

                mostrarError(
                    "El código es incorrecto, ha expirado "
                    + "o ya no está disponible."
                );

                txtCodigo.clear();

                return;
            }

            codigoVerificado = codigo;

            codigoValidado = true;

            mostrarPanel(panelNuevaContrasena);

            mostrarInformacion(
                "Código verificado. "
                + "Ingrese su nueva contraseña."
            );

            txtNuevaContrasena.requestFocus();

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo verificar el código. "
                + "Inténtelo nuevamente."
            );

        } finally {

            btnVerificarCodigo.setDisable(false);
        }
    }

    // =====================================================
    // CAMBIAR CONTRASEÑA
    // =====================================================

    @FXML
    private void cambiarContrasena() {

        if (!codigoValidado
                || codigoVerificado == null) {

            mostrarError(
                "Primero debe verificar el código."
            );

            mostrarPanel(panelCodigo);

            return;
        }

        String nuevaContrasena =
            txtNuevaContrasena.getText();

        String confirmarContrasena =
            txtConfirmarContrasena.getText();

        if (nuevaContrasena == null
                || nuevaContrasena.isBlank()) {

            mostrarError(
                "Debe ingresar la nueva contraseña."
            );

            return;
        }

        if (confirmarContrasena == null
                || confirmarContrasena.isBlank()) {

            mostrarError(
                "Debe confirmar la nueva contraseña."
            );

            return;
        }

        if (!nuevaContrasena.equals(
                confirmarContrasena)) {

            mostrarError(
                "Las contraseñas no coinciden."
            );

            return;
        }

        btnCambiarContrasena.setDisable(true);

        try {

            recuperacionServicio.restablecerContrasena(
                correoRecuperacion,
                codigoVerificado,
                nuevaContrasena,
                confirmarContrasena
            );

            codigoValidado = false;

            codigoVerificado = null;

            txtCodigo.clear();

            txtNuevaContrasena.clear();

            txtConfirmarContrasena.clear();

            mostrarInformacion(
                "Contraseña actualizada correctamente."
            );

            regresarAcceso();

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (IllegalStateException e) {

            mostrarError(e.getMessage());

            codigoValidado = false;

            codigoVerificado = null;

            mostrarPanel(panelCodigo);

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo actualizar la contraseña."
            );

        } finally {

            btnCambiarContrasena.setDisable(false);
        }
    }

    // =====================================================
    // VOLVER AL FORMULARIO DE CORREO
    // =====================================================

    @FXML
    private void volverCorreo() {

        correoRecuperacion = null;

        codigoVerificado = null;

        codigoValidado = false;

        txtCodigo.clear();

        txtNuevaContrasena.clear();

        txtConfirmarContrasena.clear();

        mostrarPanel(panelCorreo);

        lblMensaje.setText("");
    }

    // =====================================================
    // REGRESAR AL INICIO DE SESIÓN
    // =====================================================

    @FXML
    private void regresarAcceso() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/Acceso.fxml"
                    )
                );

            Parent root = loader.load();

            Stage ventana =
                (Stage) lblMensaje
                    .getScene()
                    .getWindow();

            ventana.setScene(
                new Scene(root)
            );

            ventana.setTitle(
                "Iniciar sesión - Don Edgar"
            );

            ventana.centerOnScreen();

        } catch (IOException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo regresar al inicio de sesión."
            );
        }
    }

    // =====================================================
    // MOSTRAR PANEL CORRESPONDIENTE
    // =====================================================

    private void mostrarPanel(VBox panelActivo) {

        configurarPanel(
            panelCorreo,
            panelActivo == panelCorreo
        );

        configurarPanel(
            panelCodigo,
            panelActivo == panelCodigo
        );

        configurarPanel(
            panelNuevaContrasena,
            panelActivo == panelNuevaContrasena
        );
    }

    private void configurarPanel(
            VBox panel,
            boolean visible) {

        panel.setVisible(visible);

        panel.setManaged(visible);
    }

    // =====================================================
    // MOSTRAR MENSAJES
    // =====================================================

    private void mostrarError(String mensaje) {

        lblMensaje.setStyle(
            "-fx-text-fill: #c62828; "
            + "-fx-font-weight: bold;"
        );

        lblMensaje.setText(mensaje);
    }

    private void mostrarInformacion(String mensaje) {

        lblMensaje.setStyle(
            "-fx-text-fill: #1565c0; "
            + "-fx-font-weight: bold;"
        );

        lblMensaje.setText(mensaje);
    }

    // =====================================================
    // EJECUTAR TAREA SIN BLOQUEAR JAVAFX
    // =====================================================

    private void iniciarTarea(Task<?> tarea) {

        Thread hilo = new Thread(tarea);

        hilo.setDaemon(true);

        hilo.start();
    }
}
