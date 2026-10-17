
package com.gestion.service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

import com.gestion.model.Usuario;
import com.gestion.repository.UsuarioRepositorio;

public class RecuperacionContrasenaServicio {

    // =====================================================
    // CONFIGURACIÓN
    // =====================================================

    private static final int MINUTOS_VIGENCIA = 10;
    private static final int MAX_INTENTOS = 5;
    private static final int SEGUNDOS_REENVIO = 60;

    private static final SecureRandom RANDOM =
        new SecureRandom();

    // =====================================================
    // CÓDIGO TEMPORAL EN MEMORIA RAM
    // =====================================================

    private static String codigoRecuperacion;
    private static String correoRecuperacion;
    private static LocalDateTime vencimientoCodigo;
    private static LocalDateTime ultimoEnvio;
    private static int intentosFallidos = 0;

    // =====================================================
    // SERVICIOS
    // =====================================================

    private final UsuarioRepositorio usuarioRepositorio =
        new UsuarioRepositorio();

    private final UsuarioServicio usuarioServicio =
        new UsuarioServicio();

    private final ContrasenaServicio contrasenaServicio =
        new ContrasenaServicio();

    private final CorreoServicio correoServicio =
        new CorreoServicio();

    // =====================================================
    // SOLICITAR CÓDIGO
    // =====================================================

    public synchronized void solicitarCodigo(
            String correoElectronico) {

        if (correoElectronico == null
                || correoElectronico.isBlank()) {

            throw new IllegalArgumentException(
                "Debe ingresar su correo electrónico."
            );
        }

        String correo = correoElectronico.trim();

        if (!correo.matches(
                "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {

            throw new IllegalArgumentException(
                "Ingrese un correo electrónico válido."
            );
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorCorreo(correo);

        // No revelar si el correo existe.
        if (usuario == null) {
            return;
        }

        LocalDateTime ahora = LocalDateTime.now();

        if (ultimoEnvio != null) {

            long segundos =
                Duration.between(
                    ultimoEnvio,
                    ahora
                ).getSeconds();

            if (segundos >= 0
                    && segundos < SEGUNDOS_REENVIO) {

                throw new IllegalStateException(
                    "Espere un minuto antes de solicitar "
                    + "otro código."
                );
            }
        }

        // Generar código aleatorio de seis dígitos.
        String nuevoCodigo = String.format(
            "%06d",
            RANDOM.nextInt(1_000_000)
        );

        /*
         * Enviar primero el código.
         * Si falla el envío, no reemplazamos
         * el código anterior.
         */
        correoServicio.enviarCodigoRecuperacion(
            usuario.conseguirCorreoElectronico(),
            nuevoCodigo
        );

        // Guardar el código únicamente en RAM.
        codigoRecuperacion = nuevoCodigo;

        correoRecuperacion =
            usuario.conseguirCorreoElectronico();

        vencimientoCodigo =
            LocalDateTime.now()
                .plusMinutes(MINUTOS_VIGENCIA);

        ultimoEnvio = LocalDateTime.now();

        intentosFallidos = 0;
    }

    // =====================================================
    // VERIFICAR CÓDIGO
    // =====================================================

    public synchronized boolean verificarCodigo(
            String correoElectronico,
            String codigoIngresado) {

        if (correoElectronico == null
                || codigoIngresado == null) {
            return false;
        }

        if (!codigoIngresado.matches("^[0-9]{6}$")) {
            return false;
        }

        if (codigoRecuperacion == null
                || correoRecuperacion == null
                || vencimientoCodigo == null) {
            return false;
        }

        if (!LocalDateTime.now().isBefore(
                vencimientoCodigo)) {

            limpiarCodigo();
            return false;
        }

        if (!correoRecuperacion.equalsIgnoreCase(
                correoElectronico.trim())) {

            return false;
        }

        if (intentosFallidos >= MAX_INTENTOS) {

            limpiarCodigo();
            return false;
        }

        if (!codigoRecuperacion.equals(codigoIngresado)) {

            intentosFallidos++;

            if (intentosFallidos >= MAX_INTENTOS) {
                limpiarCodigo();
            }

            return false;
        }

        return true;
    }

    // =====================================================
    // RESTABLECER CONTRASEÑA
    // =====================================================

    public synchronized void restablecerContrasena(
            String correoElectronico,
            String codigoIngresado,
            String nuevaContrasena,
            String confirmarContrasena) {

        if (nuevaContrasena == null
                || confirmarContrasena == null
                || !nuevaContrasena.equals(
                    confirmarContrasena)) {

            throw new IllegalArgumentException(
                "Las contraseñas no coinciden."
            );
        }

        usuarioServicio.validarContrasena(
            nuevaContrasena
        );

        /*
         * Comprobamos nuevamente el código
         * antes de actualizar la contraseña.
         */
        if (!verificarCodigo(
                correoElectronico,
                codigoIngresado)) {

            throw new IllegalStateException(
                "El código es incorrecto, "
                + "ha expirado o ya no está disponible."
            );
        }

        Usuario usuario =
            usuarioRepositorio.consultarPorCorreo(
                correoElectronico.trim()
            );

        if (usuario == null) {

            throw new IllegalStateException(
                "No se encontró la cuenta."
            );
        }

        String nuevaContrasenaHash =
            contrasenaServicio.generarHash(
                nuevaContrasena
            );

        /*
         * El repositorio anterior permite
         * actualizar la contraseña utilizando
         * un código almacenado en SQLite.
         *
         * Como ahora el código solo está en RAM,
         * necesitamos un método de repositorio
         * que actualice directamente la contraseña.
         */
        usuarioRepositorio.actualizarContrasenaRecuperada(
            usuario.conseguirId(),
            nuevaContrasenaHash
        );

        // Invalidar el código después de usarlo.
        limpiarCodigo();
    }

    // =====================================================
    // LIMPIAR CÓDIGO
    // =====================================================

    private static void limpiarCodigo() {

        codigoRecuperacion = null;
        correoRecuperacion = null;
        vencimientoCodigo = null;
        intentosFallidos = 0;
    }
}
