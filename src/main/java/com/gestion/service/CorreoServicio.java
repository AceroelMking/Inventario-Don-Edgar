package com.gestion.service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

public class CorreoServicio {

    // =====================================================
    // CONFIGURACIÓN SMTP
    // =====================================================

    private static final String SERVIDOR_SMTP =
        obtenerConfiguracion(
            "DON_EDGAR_SMTP_HOST",
            "smtp.gmail.com"
        );

    private static final String PUERTO_SMTP =
        obtenerConfiguracion(
            "DON_EDGAR_SMTP_PORT",
            "587"
        );

    private static final int TIEMPO_ESPERA = 15000;

    // =====================================================
    // ENVIAR CÓDIGO DE RECUPERACIÓN
    // =====================================================

    public void enviarCodigoRecuperacion(
            String correoDestino,
            String codigo) {

        if (correoDestino == null
                || correoDestino.isBlank()) {

            throw new IllegalArgumentException(
                "El correo de destino es obligatorio."
            );
        }

        if (codigo == null
                || !codigo.matches("^[0-9]{6}$")) {

            throw new IllegalArgumentException(
                "El código de recuperación no es válido."
            );
        }

        String correoRemitente =
            System.getenv(
                "DON_EDGAR_SMTP_USER"
            );

        String contrasenaAplicacion =
            System.getenv(
                "DON_EDGAR_SMTP_PASSWORD"
            );

        if (correoRemitente == null
                || correoRemitente.isBlank()) {

            throw new IllegalStateException(
                "No se ha configurado el correo remitente."
            );
        }

        if (contrasenaAplicacion == null
                || contrasenaAplicacion.isBlank()) {

            throw new IllegalStateException(
                "No se ha configurado la contraseña SMTP."
            );
        }

        // =================================================
        // PROPIEDADES DE CONEXIÓN
        // =================================================

        Properties propiedades =
            new Properties();

        propiedades.put(
            "mail.smtp.auth",
            "true"
        );

        propiedades.put(
            "mail.smtp.starttls.enable",
            "true"
        );

        propiedades.put(
            "mail.smtp.starttls.required",
            "true"
        );

        propiedades.put(
            "mail.smtp.ssl.protocols",
            "TLSv1.2 TLSv1.3"
        );

        propiedades.put(
            "mail.smtp.host",
            SERVIDOR_SMTP
        );

        propiedades.put(
            "mail.smtp.port",
            PUERTO_SMTP
        );

        propiedades.put(
            "mail.smtp.connectiontimeout",
            String.valueOf(TIEMPO_ESPERA)
        );

        propiedades.put(
            "mail.smtp.timeout",
            String.valueOf(TIEMPO_ESPERA)
        );

        propiedades.put(
            "mail.smtp.writetimeout",
            String.valueOf(TIEMPO_ESPERA)
        );

        // =================================================
        // AUTENTICACIÓN SMTP
        // =================================================

        Session sesion =
            Session.getInstance(
                propiedades,
                new Authenticator() {

                    @Override
                    protected PasswordAuthentication
                            getPasswordAuthentication() {

                        return new PasswordAuthentication(
                            correoRemitente,
                            contrasenaAplicacion
                        );
                    }
                }
            );

        // =================================================
        // CONSTRUIR Y ENVIAR MENSAJE
        // =================================================

        try {

            MimeMessage mensaje =
                new MimeMessage(sesion);

            mensaje.setFrom(
                new InternetAddress(
                    correoRemitente,
                    "Inventario Don Edgar",
                    StandardCharsets.UTF_8.name()
                )
            );

            mensaje.setRecipient(
                Message.RecipientType.TO,
                new InternetAddress(
                    correoDestino.trim()
                )
            );

            mensaje.setSubject(
                "Código de recuperación - Inventario Don Edgar",
                StandardCharsets.UTF_8.name()
            );

            String contenido = """
                Recuperación de contraseña

                Se ha solicitado restablecer la contraseña
                del sistema Inventario Don Edgar.

                Su código de recuperación es:

                %s

                Este código tiene una vigencia de 10 minutos.

                Si usted no solicitó este cambio,
                ignore este mensaje.

                Por seguridad, no comparta el código
                con ninguna persona.

                Inventario Don Edgar
                """.formatted(codigo);

            mensaje.setText(
                contenido,
                StandardCharsets.UTF_8.name()
            );

            Transport.send(mensaje);

        } catch (MessagingException
                 | java.io.UnsupportedEncodingException e) {

            throw new RuntimeException(
                "No se pudo enviar el correo de recuperación.",
                e
            );
        }
    }

    // =====================================================
    // OBTENER CONFIGURACIÓN
    // =====================================================

    private static String obtenerConfiguracion(
            String variable,
            String valorPredeterminado) {

        String valor =
            System.getenv(variable);

        if (valor == null || valor.isBlank()) {
            return valorPredeterminado;
        }

        return valor.trim();
    }
}
