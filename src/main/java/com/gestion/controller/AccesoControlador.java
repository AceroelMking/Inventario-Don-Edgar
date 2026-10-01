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
import javafx.stage.Stage;

public class AccesoControlador {

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtContrasena;

    @FXML
    private Label lblMensaje;

    @FXML
    public void initialize() {

        /*
         * Coloca el cursor automáticamente
         * en el campo de usuario.
         */
        txtUsuario.requestFocus();
    }

    @FXML
    private void iniciarSesion() {

        /*
         * Limpiamos cualquier mensaje anterior.
         */
        lblMensaje.setText("");

        String nombreUsuario =
            txtUsuario.getText();

        String contrasena =
            txtContrasena.getText();

        /*
         * Validación del campo usuario.
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
         * Validación del campo contraseña.
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
             * Se busca el usuario y se verifica
             * la contraseña utilizando Argon2id.
             */
            Usuario usuarioAutenticado =
                usuarioServicio.autenticar(
                    nombreUsuario.trim(),
                    contrasena
                );

            /*
             * Si autenticar devuelve null,
             * las credenciales no son correctas.
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
             * Verificamos si todavía se están
             * utilizando las credenciales iniciales:
             *
             * Usuario: admin
             * Contraseña: admin
             */
            boolean credencialesPredeterminadas =
                usuarioServicio
                    .usaCredencialesPredeterminadas(
                        usuarioAutenticado,
                        contrasena
                    );

            /*
             * Si todavía usa admin/admin,
             * no puede acceder al menú.
             */
            if (credencialesPredeterminadas) {

                abrirCambioObligatorio();

                return;
            }

            /*
             * Si las credenciales ya fueron
             * modificadas, entra normalmente.
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
     * Abre la pantalla principal del sistema.
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
     * Se ejecuta únicamente cuando el usuario
     * todavía utiliza admin / admin.
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

            /*
             * Obtenemos el controlador de la
             * pantalla de configuración.
             */
            ConfiguracionUsuarioControlador controlador =
                loader.getController();

            /*
             * Indicamos que este cambio es obligatorio.
             *
             * Esto permite que la pantalla muestre
             * el mensaje correspondiente y que no
             * permita conservar las credenciales
             * predeterminadas.
             */
            controlador
                .configurarCambioObligatorio(
                    true
                );

            Stage ventana =
                obtenerVentana();

            /*
             * Esta acción se ejecutará solamente
             * después de que las nuevas credenciales
             * hayan sido guardadas correctamente.
             */
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
     * Después de cambiar correctamente admin/admin,
     * abre el menú principal.
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

            /*
             * Como en este momento ya no estamos
             * necesariamente en Acceso.fxml,
             * mostramos el error por consola.
             */
            System.err.println(
                "No se pudo abrir el menú principal "
                + "después de cambiar las credenciales."
            );
        }
    }

    /*
     * Obtiene la ventana actual a partir
     * de uno de los controles del login.
     */
    private Stage obtenerVentana() {

        return (Stage) txtUsuario
            .getScene()
            .getWindow();
    }

    /*
     * Método auxiliar para mostrar errores
     * en la pantalla de inicio de sesión.
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