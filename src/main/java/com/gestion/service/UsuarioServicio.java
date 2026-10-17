
package com.gestion.service;

import java.time.Duration;
import java.time.LocalDateTime;

import com.gestion.model.Usuario;
import com.gestion.repository.UsuarioRepositorio;

public class UsuarioServicio {

    // =====================================================
    // CONFIGURACIÓN DE SEGURIDAD
    // =====================================================

    private static final int MAX_INTENTOS = 5;

    private static final int MINUTOS_BLOQUEO = 15;

    // =====================================================
    // SERVICIOS Y REPOSITORIOS
    // =====================================================

    private final UsuarioRepositorio usuarioRepositorio;

    private final ContrasenaServicio contrasenaServicio;

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public UsuarioServicio() {

        usuarioRepositorio = new UsuarioRepositorio();

        contrasenaServicio = new ContrasenaServicio();
    }

    // =====================================================
    // AUTENTICACIÓN DEL USUARIO
    // =====================================================

    public Usuario autenticar(
            String nombreUsuario,
            String contrasena) {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return null;
        }

        if (contrasena == null
                || contrasena.isBlank()) {

            return null;
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorNombre(
                nombreUsuario.trim()
            );

        if (usuario == null) {
            return null;
        }

        // Comprobar el bloqueo antes de verificar
        // la contraseña.
        if (usuario.estaBloqueado()) {

            long segundosRestantes =
                Duration.between(
                    LocalDateTime.now(),
                    usuario.conseguirBloqueadoHasta()
                ).getSeconds();

            long minutosRestantes =
                Math.max(
                    1,
                    (segundosRestantes + 59) / 60
                );

            throw new CuentaBloqueadaException(
                "La cuenta está bloqueada por seguridad. "
                + "Intente nuevamente en aproximadamente "
                + minutosRestantes
                + " minuto(s)."
            );
        }

        // Si el bloqueo anterior ya terminó,
        // reiniciar el contador.
        if (usuario.conseguirBloqueadoHasta() != null) {

            usuarioRepositorio.reiniciarIntentosFallidos(
                usuario.conseguirId()
            );

            usuario.reiniciarIntentosFallidos();
        }

        // Verificar la contraseña.
        boolean correcta =
            contrasenaServicio.verificar(
                contrasena,
                usuario.conseguirContrasena()
            );

        if (!correcta) {

            registrarIntentoFallido(usuario);

            return null;
        }

        // Inicio de sesión correcto.
        // Reiniciar cualquier intento fallido.
        if (usuario.conseguirIntentosFallidos() > 0) {

            usuarioRepositorio.reiniciarIntentosFallidos(
                usuario.conseguirId()
            );

            usuario.reiniciarIntentosFallidos();
        }

        return usuario;
    }

    // =====================================================
    // REGISTRAR INTENTO FALLIDO
    // =====================================================

    private void registrarIntentoFallido(
            Usuario usuario) {

        int intentos =
            usuario.conseguirIntentosFallidos() + 1;

        if (intentos >= MAX_INTENTOS) {

            LocalDateTime bloqueadoHasta =
                LocalDateTime.now()
                    .plusMinutes(MINUTOS_BLOQUEO);

            usuarioRepositorio.actualizarBloqueo(
                usuario.conseguirId(),
                MAX_INTENTOS,
                bloqueadoHasta
            );

            throw new CuentaBloqueadaException(
                "Ha alcanzado los "
                + MAX_INTENTOS
                + " intentos fallidos. "
                + "Su cuenta ha sido bloqueada durante "
                + MINUTOS_BLOQUEO
                + " minutos."
            );
        }

        usuarioRepositorio.actualizarBloqueo(
            usuario.conseguirId(),
            intentos,
            null
        );
    }

    // =====================================================
    // CONSULTAR INTENTOS RESTANTES
    // =====================================================

    public int consultarIntentosRestantes(
            String nombreUsuario) {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return MAX_INTENTOS;
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorNombre(
                nombreUsuario.trim()
            );

        if (usuario == null) {
            return MAX_INTENTOS;
        }

        if (usuario.estaBloqueado()) {
            return 0;
        }

        if (usuario.conseguirBloqueadoHasta() != null) {
            return MAX_INTENTOS;
        }

        return Math.max(
            0,
            MAX_INTENTOS
                - usuario.conseguirIntentosFallidos()
        );
    }

    // =====================================================
    // COMPROBAR SI LA CUENTA ESTÁ BLOQUEADA
    // =====================================================

    public boolean estaCuentaBloqueada(
            String nombreUsuario) {

        if (nombreUsuario == null
                || nombreUsuario.isBlank()) {

            return false;
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorNombre(
                nombreUsuario.trim()
            );

        return usuario != null
            && usuario.estaBloqueado();
    }

    // =====================================================
    // INICIAR SESIÓN
    // =====================================================

    public boolean iniciarSesion(
            String nombreUsuario,
            String contrasena) {

        return autenticar(
            nombreUsuario,
            contrasena
        ) != null;
    }

    // =====================================================
    // CREDENCIALES PREDETERMINADAS
    // =====================================================

    public boolean usaCredencialesPredeterminadas(
            Usuario usuario,
            String contrasenaIngresada) {

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

    // =====================================================
    // CONSULTAR ADMINISTRADOR
    // =====================================================

    public Usuario consultarAdministrador() {

        return usuarioRepositorio
            .consultarAdministrador();
    }

    // =====================================================
    // CAMBIAR CREDENCIALES
    // =====================================================

    public void cambiarCredenciales(
            String contrasenaActual,
            String nuevoUsuario,
            String nuevaContrasena,
            String confirmarContrasena) {

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        if (usuario == null) {

            throw new IllegalStateException(
                "No existe un administrador."
            );
        }

        if (contrasenaActual == null
                || contrasenaActual.isBlank()) {

            throw new IllegalArgumentException(
                "Debe ingresar la contraseña actual."
            );
        }

        if (!contrasenaServicio.verificar(
                contrasenaActual,
                usuario.conseguirContrasena())) {

            throw new IllegalArgumentException(
                "La contraseña actual es incorrecta."
            );
        }

        validarNombreUsuario(nuevoUsuario);

        validarContrasena(nuevaContrasena);

        if (confirmarContrasena == null
                || confirmarContrasena.isBlank()) {

            throw new IllegalArgumentException(
                "Debe confirmar la nueva contraseña."
            );
        }

        if (!nuevaContrasena.equals(
                confirmarContrasena)) {

            throw new IllegalArgumentException(
                "Las contraseñas nuevas no coinciden."
            );
        }

        if ("admin".equalsIgnoreCase(
                nuevoUsuario.trim())
                && "admin".equals(nuevaContrasena)) {

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

        usuarioRepositorio.actualizarCredenciales(
            usuario
        );
    }

    // =====================================================
    // ACTUALIZAR CORREO DEL ADMINISTRADOR
    // =====================================================

    public void actualizarCorreoAdministrador(
            String correoElectronico) {

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        if (usuario == null) {

            throw new IllegalStateException(
                "No existe un administrador."
            );
        }

        if (correoElectronico == null
                || correoElectronico.isBlank()) {

            throw new IllegalArgumentException(
                "El correo electrónico es obligatorio."
            );
        }

        String correo = correoElectronico.trim();

        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException(
                "El correo electrónico no es válido."
            );
        }

        usuarioRepositorio.actualizarCorreo(
            usuario.conseguirId(),
            correo
        );
    }

    // =====================================================
    // VALIDAR NOMBRE DE USUARIO
    // =====================================================

    public void validarNombreUsuario(
            String usuario) {

        if (usuario == null
                || usuario.isBlank()) {

            throw new IllegalArgumentException(
                "El nuevo nombre de usuario es obligatorio."
            );
        }

        if (usuario.trim().length() < 4) {

            throw new IllegalArgumentException(
                "El usuario debe tener mínimo 4 caracteres."
            );
        }

        if (!usuario.trim().matches(
                "^[A-Za-z0-9._-]+$")) {

            throw new IllegalArgumentException(
                "El usuario solo puede contener letras, "
                + "números, punto, guion y guion bajo."
            );
        }
    }

    // =====================================================
    // VALIDAR CONTRASEÑA
    // =====================================================

    public void validarContrasena(
            String contrasena) {

        if (contrasena == null
                || contrasena.isBlank()) {

            throw new IllegalArgumentException(
                "La nueva contraseña es obligatoria."
            );
        }

        if (contrasena.length() < 12) {

            throw new IllegalArgumentException(
                "La contraseña debe tener mínimo 12 caracteres."
            );
        }

        if (!contrasena.matches(".*[A-Z].*")) {

            throw new IllegalArgumentException(
                "La contraseña debe contener una letra mayúscula."
            );
        }

        if (!contrasena.matches(".*[a-z].*")) {

            throw new IllegalArgumentException(
                "La contraseña debe contener una letra minúscula."
            );
        }

        if (!contrasena.matches(".*[0-9].*")) {

            throw new IllegalArgumentException(
                "La contraseña debe contener un número."
            );
        }

        if (!contrasena.matches(".*[^A-Za-z0-9].*")) {

            throw new IllegalArgumentException(
                "La contraseña debe contener un carácter especial."
            );
        }
    }

    // =====================================================
    // EXCEPCIÓN ESPECÍFICA DE BLOQUEO
    // =====================================================

    public static class CuentaBloqueadaException
            extends RuntimeException {

        public CuentaBloqueadaException(
                String mensaje) {

            super(mensaje);
        }
    }
}
