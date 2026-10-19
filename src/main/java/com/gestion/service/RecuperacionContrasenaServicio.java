
package com.gestion.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HexFormat;

import com.gestion.model.Usuario;
import com.gestion.repository.UsuarioRepositorio;

public class RecuperacionContrasenaServicio {

    // =====================================================
    // CONFIGURACIÓN DE SEGURIDAD
    // =====================================================

    private static final int MAX_INTENTOS = 5;
    private static final int MINUTOS_BLOQUEO = 15;
    private static final int TAMANO_CLAVE_BYTES = 16;

    private static final SecureRandom RANDOM =
        new SecureRandom();

    // =====================================================
    // DEPENDENCIAS
    // =====================================================

    private final UsuarioRepositorio usuarioRepositorio =
        new UsuarioRepositorio();

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    private final ContrasenaServicio contrasenaServicio =
        new ContrasenaServicio();

    // =====================================================
    // GENERAR CLAVE ALEATORIA
    // =====================================================

    private String generarClaveAleatoria() {

        byte[] bytes = new byte[TAMANO_CLAVE_BYTES];

        RANDOM.nextBytes(bytes);

        String hexadecimal =
            HexFormat.of()
                .withUpperCase()
                .formatHex(bytes);

        StringBuilder clave = new StringBuilder();

        for (int i = 0; i < hexadecimal.length(); i += 4) {

            if (i > 0) {
                clave.append("-");
            }

            clave.append(
                hexadecimal,
                i,
                i + 4
            );
        }

        return clave.toString();
    }

    // =====================================================
    // NORMALIZAR CLAVE
    // =====================================================

    private String normalizarClave(String clave) {

        if (clave == null) {
            return "";
        }

        String limpia = clave.trim()
            .toUpperCase()
            .replaceAll("[\\s-]", "");

        if (!limpia.matches("[0-9A-F]{32}")) {
            return "";
        }

        StringBuilder resultado = new StringBuilder();

        for (int i = 0; i < limpia.length(); i += 4) {

            if (i > 0) {
                resultado.append("-");
            }

            resultado.append(
                limpia,
                i,
                i + 4
            );
        }

        return resultado.toString();
    }

    // =====================================================
    // CONSULTAR CLAVE CONFIGURADA
    // =====================================================

    public boolean tieneClaveEmergencia() {

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        return usuario != null
            && usuario.conseguirClaveRecuperacionHash() != null
            && !usuario.conseguirClaveRecuperacionHash().isBlank();
    }

    // =====================================================
    // GENERAR Y GUARDAR CLAVE INICIAL
    // =====================================================

    /**
     * Solo debe invocarse después de autenticar al
     * administrador o durante la configuración inicial.
     *
     * El controlador debe mostrar la clave una sola vez.
     */
    public synchronized String generarYGuardarClaveEmergencia() {

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        if (usuario == null) {
            throw new IllegalStateException(
                "No existe un administrador registrado."
            );
        }

        String clave = generarClaveAleatoria();

        String hash =
            contrasenaServicio.generarHash(clave);

        usuarioRepositorio.guardarClaveRecuperacion(
            usuario.conseguirId(),
            hash
        );

        return clave;
    }

    // =====================================================
    // COMPROBAR BLOQUEO
    // =====================================================

    private void comprobarBloqueo(Usuario usuario) {

        if (!usuario.estaRecuperacionBloqueada()) {
            return;
        }

        long segundos =
            Duration.between(
                LocalDateTime.now(),
                usuario.conseguirRecuperacionBloqueadaHasta()
            ).getSeconds();

        long minutos =
            Math.max(1, (segundos + 59) / 60);

        throw new IllegalStateException(
            "La recuperación está bloqueada por seguridad. "
            + "Intente nuevamente en aproximadamente "
            + minutos + " minuto(s)."
        );
    }

    // =====================================================
    // REINICIAR BLOQUEO VENCIDO
    // =====================================================

    private void reiniciarBloqueoVencido(Usuario usuario) {

        LocalDateTime bloqueo =
            usuario.conseguirRecuperacionBloqueadaHasta();

        if (bloqueo == null) {
            return;
        }

        if (bloqueo.isAfter(LocalDateTime.now())) {
            return;
        }

        usuarioRepositorio.actualizarBloqueoRecuperacion(
            usuario.conseguirId(),
            0,
            null
        );

        usuario.colocarIntentosRecuperacionFallidos(0);
        usuario.colocarRecuperacionBloqueadaHasta(null);
    }

    // =====================================================
    // REGISTRAR INTENTO FALLIDO
    // =====================================================

    private void registrarIntentoFallido(Usuario usuario) {

        int intentos =
            usuario.conseguirIntentosRecuperacionFallidos() + 1;

        if (intentos >= MAX_INTENTOS) {

            LocalDateTime bloqueadoHasta =
                LocalDateTime.now()
                    .plusMinutes(MINUTOS_BLOQUEO);

            usuarioRepositorio.actualizarBloqueoRecuperacion(
                usuario.conseguirId(),
                MAX_INTENTOS,
                bloqueadoHasta
            );

            throw new IllegalStateException(
                "Ha alcanzado cinco intentos fallidos. "
                + "La recuperación estará bloqueada "
                + "durante 15 minutos."
            );
        }

        usuarioRepositorio.actualizarBloqueoRecuperacion(
            usuario.conseguirId(),
            intentos,
            null
        );
    }

    // =====================================================
    // VERIFICAR CLAVE DE EMERGENCIA
    // =====================================================

    public synchronized boolean verificarClaveEmergencia(
            String claveIngresada) {

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        if (usuario == null) {
            throw new IllegalStateException(
                "No existe un administrador registrado."
            );
        }

        String hashGuardado =
            usuario.conseguirClaveRecuperacionHash();

        if (hashGuardado == null || hashGuardado.isBlank()) {
            throw new IllegalStateException(
                "No existe una clave de emergencia configurada."
            );
        }

        comprobarBloqueo(usuario);
        reiniciarBloqueoVencido(usuario);

        String claveNormalizada =
            normalizarClave(claveIngresada);

        boolean correcta =
            !claveNormalizada.isEmpty()
            && contrasenaServicio.verificar(
                claveNormalizada,
                hashGuardado
            );

        if (!correcta) {

            registrarIntentoFallido(usuario);

            return false;
        }

        return true;
    }

    // =====================================================
    // RESTABLECER CONTRASEÑA Y RENOVAR CLAVE
    // =====================================================

    /**
     * Devuelve la nueva clave de emergencia para
     * mostrarla una única vez.
     *
     * El repositorio debe actualizar conjuntamente:
     *
     * - Hash de contraseña.
     * - Hash de la nueva clave de emergencia.
     * - Intentos fallidos.
     * - Fecha de bloqueo.
     *
     * También debe comprobar que el hash anterior
     * siga vigente.
     */
    public synchronized String restablecerYRenovarClave(
            String claveEmergencia,
            String nuevaContrasena,
            String confirmarContrasena) {

        if (nuevaContrasena == null
                || confirmarContrasena == null
                || !nuevaContrasena.equals(confirmarContrasena)) {

            throw new IllegalArgumentException(
                "Las contraseñas no coinciden."
            );
        }

        usuarioServicio.validarContrasena(
            nuevaContrasena
        );

        Usuario usuario =
            usuarioRepositorio.consultarAdministrador();

        if (usuario == null) {
            throw new IllegalStateException(
                "No existe un administrador registrado."
            );
        }

        // La verificación se repite en el momento
        // de restablecer la contraseña.
        if (!verificarClaveEmergencia(claveEmergencia)) {

            throw new IllegalArgumentException(
                "La clave de emergencia es incorrecta."
            );
        }

        // Volver a consultar el estado vigente.
        usuario =
            usuarioRepositorio.consultarAdministrador();

        String hashClaveAnterior =
            usuario.conseguirClaveRecuperacionHash();

        String hashNuevaContrasena =
            contrasenaServicio.generarHash(
                nuevaContrasena
            );

        String nuevaClave =
            generarClaveAleatoria();

        String hashNuevaClave =
            contrasenaServicio.generarHash(
                nuevaClave
            );

        boolean actualizado =
            usuarioRepositorio.restablecerYRotarClaveEmergencia(
                usuario.conseguirId(),
                hashClaveAnterior,
                hashNuevaContrasena,
                hashNuevaClave
            );

        if (!actualizado) {

            throw new IllegalStateException(
                "No se pudo completar la recuperación. "
                + "La clave podría haber sido utilizada "
                + "o modificada anteriormente."
            );
        }

        return nuevaClave;
    }
}
