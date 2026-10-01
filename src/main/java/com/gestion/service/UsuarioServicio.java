package com.gestion.service;

import com.gestion.model.Usuario;
import com.gestion.repository.UsuarioRepositorio;

public class UsuarioServicio {

    private final UsuarioRepositorio usuarioRepositorio;
    private final ContrasenaServicio contrasenaServicio;

    public UsuarioServicio() {
        usuarioRepositorio = new UsuarioRepositorio();
        contrasenaServicio = new ContrasenaServicio();
    }

    public Usuario autenticar(
        String nombreUsuario,
        String contrasena
    ) {

        if (nombreUsuario == null || nombreUsuario.isBlank()) {
            return null;
        }

        if (contrasena == null || contrasena.isBlank()) {
            return null;
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorNombre(
                nombreUsuario.trim()
            );

        if (usuario == null) {
            return null;
        }

        boolean correcta =
            contrasenaServicio.verificar(
                contrasena,
                usuario.conseguirContrasena()
            );

        if (!correcta) {
            return null;
        }

        return usuario;
    }

    public boolean iniciarSesion(
        String nombreUsuario,
        String contrasena
    ) {
        return autenticar(
            nombreUsuario,
            contrasena
        ) != null;
    }

    public boolean usaCredencialesPredeterminadas(
        Usuario usuario,
        String contrasenaIngresada
    ) {

        if (usuario == null) {
            return false;
        }

        return "admin".equals(
                    usuario.conseguirNombreUsuario()
               )
               &&
               "admin".equals(
                    contrasenaIngresada
               );
    }

    public Usuario consultarAdministrador() {
        return usuarioRepositorio
            .consultarAdministrador();
    }

    public void cambiarCredenciales(
        String contrasenaActual,
        String nuevoUsuario,
        String nuevaContrasena,
        String confirmarContrasena
    ) {

        Usuario usuario =
            usuarioRepositorio
                .consultarAdministrador();

        if (usuario == null) {
            throw new IllegalStateException(
                "No existe un administrador."
            );
        }

        if (
            contrasenaActual == null
            || contrasenaActual.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Debe ingresar la contraseña actual."
            );
        }

        if (
            !contrasenaServicio.verificar(
                contrasenaActual,
                usuario.conseguirContrasena()
            )
        ) {
            throw new IllegalArgumentException(
                "La contraseña actual es incorrecta."
            );
        }

        validarNombreUsuario(
            nuevoUsuario
        );

        validarContrasena(
            nuevaContrasena
        );

        if (
            confirmarContrasena == null
            || confirmarContrasena.isBlank()
        ) {
            throw new IllegalArgumentException(
                "Debe confirmar la nueva contraseña."
            );
        }

        if (
            !nuevaContrasena.equals(
                confirmarContrasena
            )
        ) {
            throw new IllegalArgumentException(
                "Las contraseñas nuevas no coinciden."
            );
        }

        if (
            "admin".equalsIgnoreCase(
                nuevoUsuario.trim()
            )
            && "admin".equals(
                nuevaContrasena
            )
        ) {
            throw new IllegalArgumentException(
                "No puede utilizar las credenciales predeterminadas."
            );
        }

        usuario.colocarNombreUsuario(
            nuevoUsuario.trim()
        );

        usuario.colocarContrasena(
            contrasenaServicio.generarHash(
                nuevaContrasena
            )
        );

        usuarioRepositorio
            .actualizarCredenciales(
                usuario
            );
    }

    public void validarNombreUsuario(
        String usuario
    ) {

        if (
            usuario == null
            || usuario.isBlank()
        ) {
            throw new IllegalArgumentException(
                "El nuevo nombre de usuario es obligatorio."
            );
        }

        if (usuario.trim().length() < 4) {
            throw new IllegalArgumentException(
                "El usuario debe tener mínimo 4 caracteres."
            );
        }

        if (
            !usuario.trim().matches(
                "^[A-Za-z0-9._-]+$"
            )
        ) {
            throw new IllegalArgumentException(
                "El usuario solo puede contener letras, números, punto, guion y guion bajo."
            );
        }
    }

    public void validarContrasena(
        String contrasena
    ) {

        if (
            contrasena == null
            || contrasena.isBlank()
        ) {
            throw new IllegalArgumentException(
                "La nueva contraseña es obligatoria."
            );
        }

        if (contrasena.length() < 12) {
            throw new IllegalArgumentException(
                "La contraseña debe tener mínimo 12 caracteres."
            );
        }

        if (
            !contrasena.matches(
                ".*[A-Z].*"
            )
        ) {
            throw new IllegalArgumentException(
                "La contraseña debe contener una letra mayúscula."
            );
        }

        if (
            !contrasena.matches(
                ".*[a-z].*"
            )
        ) {
            throw new IllegalArgumentException(
                "La contraseña debe contener una letra minúscula."
            );
        }

        if (
            !contrasena.matches(
                ".*[0-9].*"
            )
        ) {
            throw new IllegalArgumentException(
                "La contraseña debe contener un número."
            );
        }

        if (
            !contrasena.matches(
                ".*[^A-Za-z0-9].*"
            )
        ) {
            throw new IllegalArgumentException(
                "La contraseña debe contener un carácter especial."
            );
        }
    }
}