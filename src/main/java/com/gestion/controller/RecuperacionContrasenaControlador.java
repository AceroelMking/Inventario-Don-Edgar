
package com.gestion.controller;

import java.io.IOException;

import com.gestion.service.RecuperacionContrasenaServicio;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class RecuperacionContrasenaControlador {

    // =====================================================
    // SERVICIO
    // =====================================================

    private final RecuperacionContrasenaServicio
        recuperacionServicio =
            new RecuperacionContrasenaServicio();

    // =====================================================
    // COMPONENTES FXML
    // =====================================================

    @FXML
    private TextField txtClaveEmergencia;

    @FXML
    private PasswordField txtNuevaContrasena;

    @FXML
    private PasswordField txtConfirmarContrasena;

    @FXML
    private VBox panelClave;

    @FXML
    private VBox panelNuevaContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    private Button btnVerificarClave;

    @FXML
    private Button btnCambiarContrasena;

    // =====================================================
    // ESTADO DEL PROCESO
    // =====================================================

    private String claveIngresada;

    private boolean claveVerificada = false;

    // =====================================================
    // INICIALIZACIÓN
    // =====================================================

    @FXML
    public void initialize() {

        mostrarPanel(panelClave);

        lblMensaje.setText("");

        Platform.runLater(
            () -> txtClaveEmergencia.requestFocus()
        );
    }

    // =====================================================
    // VERIFICAR CLAVE DE EMERGENCIA
    // =====================================================

    @FXML
    private void verificarClave() {

        String clave = txtClaveEmergencia.getText();

        if (clave == null || clave.isBlank()) {

            mostrarError(
                "Debe ingresar su clave de emergencia."
            );

            return;
        }

        btnVerificarClave.setDisable(true);

        try {

            boolean correcta =
                recuperacionServicio.verificarClaveEmergencia(
                    clave
                );

            if (!correcta) {

                claveVerificada = false;
                claveIngresada = null;

                txtClaveEmergencia.clear();

                mostrarError(
                    "La clave de emergencia es incorrecta."
                );

                return;
            }

            claveIngresada = clave;
            claveVerificada = true;

            txtClaveEmergencia.clear();

            mostrarPanel(panelNuevaContrasena);

            mostrarInformacion(
                "Clave verificada. Establezca una "
                + "nueva contraseña segura."
            );

            txtNuevaContrasena.requestFocus();

        } catch (IllegalStateException e) {

            mostrarError(e.getMessage());

        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo verificar la clave de emergencia."
            );

        } finally {

            btnVerificarClave.setDisable(false);
        }
    }

    // =====================================================
    // CAMBIAR CONTRASEÑA
    // =====================================================

    @FXML
    private void cambiarContrasena() {

        if (!claveVerificada || claveIngresada == null) {

            mostrarError(
                "Primero debe verificar su clave de emergencia."
            );

            mostrarPanel(panelClave);

            return;
        }

        String nuevaContrasena =
            txtNuevaContrasena.getText();

        String confirmarContrasena =
            txtConfirmarContrasena.getText();

        if (nuevaContrasena == null
                || nuevaContrasena.isBlank()) {

            mostrarError(
                "Debe ingresar una nueva contraseña."
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

        if (!nuevaContrasena.equals(confirmarContrasena)) {

            mostrarError(
                "Las contraseñas no coinciden."
            );

            return;
        }

        btnCambiarContrasena.setDisable(true);

        try {

            /*
             * El servicio deberá actualizar la contraseña,
             * invalidar la clave anterior y guardar el hash
             * de una nueva clave en una sola transacción.
             *
             * La clave nueva se devuelve para mostrarla
             * una única vez al administrador.
             */
            String nuevaClave =
                recuperacionServicio
                    .restablecerYRenovarClave(
                        claveIngresada,
                        nuevaContrasena,
                        confirmarContrasena
                    );

            claveIngresada = null;
            claveVerificada = false;

            txtClaveEmergencia.clear();
            txtNuevaContrasena.clear();
            txtConfirmarContrasena.clear();

            mostrarNuevaClave(nuevaClave);

            regresarAcceso();

        } catch (IllegalArgumentException e) {

            mostrarError(e.getMessage());

        } catch (IllegalStateException e) {

            mostrarError(e.getMessage());

            claveIngresada = null;
            claveVerificada = false;

            mostrarPanel(panelClave);

        } catch (RuntimeException e) {

            mostrarError(
                "No se pudo restablecer la contraseña."
            );

        } finally {

            btnCambiarContrasena.setDisable(false);
        }
    }

    // =====================================================
    // MOSTRAR NUEVA CLAVE DE EMERGENCIA
    // =====================================================

    private void mostrarNuevaClave(String clave) {

        Alert alerta = new Alert(
            Alert.AlertType.INFORMATION
        );

        alerta.setTitle("Nueva clave de emergencia");

        alerta.setHeaderText(
            "Contraseña restablecida correctamente"
        );

        Label descripcion = new Label(
            "La clave anterior ha sido invalidada.\n\n"
            + "Guarde esta NUEVA clave de emergencia "
            + "en un lugar seguro. La necesitará si "
            + "vuelve a olvidar su contraseña.\n\n"
            + "Esta clave no se volverá a mostrar."
        );

        descripcion.setWrapText(true);

        TextArea campoClave = new TextArea(clave);

        campoClave.setEditable(false);
        campoClave.setWrapText(true);
        campoClave.setPrefRowCount(2);
        campoClave.setPrefWidth(450);

        CheckBox confirmacion = new CheckBox(
            "Confirmo que guardé mi nueva clave."
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
    // REGRESAR AL LOGIN
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

            mostrarError(
                "No se pudo regresar al inicio de sesión."
            );
        }
    }

    // =====================================================
    // MOSTRAR PANEL ACTIVO
    // =====================================================

    private void mostrarPanel(VBox panelActivo) {

        configurarPanel(
            panelClave,
            panelActivo == panelClave
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
    // MENSAJES
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
}
