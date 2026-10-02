package com.gestion.controller;

import java.io.IOException;

import com.gestion.model.Usuario;
import com.gestion.service.UsuarioServicio;

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

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

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

    @FXML
    public void initialize() {

        /*
         * El tamaño del fondo queda enlazado
         * al tamaño del contenedor principal.
         *
         * Cuando la ventana aumenta o disminuye,
         * la imagen cambia automáticamente
         * de tamaño.
         */
        imagenFondo
            .fitWidthProperty()
            .bind(
                panelAcceso.widthProperty()
            );

        imagenFondo
            .fitHeightProperty()
            .bind(
                panelAcceso.heightProperty()
            );

        /*
         * Coloca el cursor automáticamente
         * en el campo de usuario.
         */
        txtUsuario.requestFocus();
    }

    @FXML
    private void iniciarSesion() {

        lblMensaje.setText("");

        String nombreUsuario =
            txtUsuario.getText();

        String contrasena =
            txtContrasena.getText();

        /*
         * Validación del usuario.
         */
        if (
            nombreUsuario == null
            || nombreUsuario.isBlank()
        ) {

            mostrarError(
                "Debe ingresar el nombre de usuario."
            );

            txtUsuario.requestFocus();

            return;
        }

        /*
         * Validación de la contraseña.
         */
        if (
            contrasena == null
            || contrasena.isBlank()
        ) {

            mostrarError(
                "Debe ingresar la contraseña."
            );

            txtContrasena.requestFocus();

            return;
        }

        try {

            /*
             * Autenticación del usuario.
             */
            Usuario usuarioAutenticado =
                usuarioServicio.autenticar(
                    nombreUsuario.trim(),
                    contrasena
                );

            /*
             * Credenciales incorrectas.
             */
            if (usuarioAutenticado == null) {

                mostrarError(
                    "Usuario o contraseña incorrectos."
                );

                txtContrasena.clear();
                txtContrasena.requestFocus();

                return;
            }

            /*
             * Comprueba si todavía utiliza
             * las credenciales iniciales.
             */
            boolean credencialesPredeterminadas =
                usuarioServicio
                    .usaCredencialesPredeterminadas(
                        usuarioAutenticado,
                        contrasena
                    );

            /*
             * Si todavía utiliza admin/admin,
             * obliga a cambiar las credenciales.
             */
            if (credencialesPredeterminadas) {

                abrirCambioObligatorio();

                return;
            }

            /*
             * Acceso normal al sistema.
             */
            abrirSistema();

        } catch (RuntimeException e) {

            e.printStackTrace();

            mostrarError(
                "No se pudo validar el acceso al sistema."
            );
        }
    }

    /*
     * Abre el menú principal.
     */
    private void abrirSistema() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/MenuLateral.fxml"
                    )
                );

            Parent root =
                loader.load();

            Stage ventana =
                obtenerVentana();

            Scene escena =
                new Scene(root);

            ventana.setScene(
                escena
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

    /*
     * Abre la pantalla de cambio obligatorio
     * de credenciales.
     */
    private void abrirCambioObligatorio() {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/ConfiguracionUsuario.fxml"
                    )
                );

            Parent root =
                loader.load();

            ConfiguracionUsuarioControlador controlador =
                loader.getController();

            controlador
                .configurarCambioObligatorio(
                    true
                );

            Stage ventana =
                obtenerVentana();

            controlador
                .colocarAccionAlGuardar(
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

    /*
     * Abre el menú después de cambiar
     * correctamente las credenciales.
     */
    private void abrirMenuDespuesDelCambio(
        Stage ventana
    ) {

        try {

            FXMLLoader loader =
                new FXMLLoader(
                    getClass().getResource(
                        "/com/gestion/view/MenuLateral.fxml"
                    )
                );

            Parent menu =
                loader.load();

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

    /*
     * Obtiene la ventana actual.
     */
    private Stage obtenerVentana() {

        return (Stage) txtUsuario
            .getScene()
            .getWindow();
    }

    /*
     * Muestra un mensaje de error
     * en el formulario.
     */
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