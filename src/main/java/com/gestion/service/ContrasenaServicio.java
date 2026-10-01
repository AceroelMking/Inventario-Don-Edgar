package com.gestion.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;

public class ContrasenaServicio {

    private static final int TAMANO_SALT = 16;
    private static final int TAMANO_HASH = 32;

    private static final int MEMORIA_KB = 19456;
    private static final int ITERACIONES = 2;
    private static final int PARALELISMO = 1;

    private final SecureRandom secureRandom =
        new SecureRandom();

    public String generarHash(
        String contrasena
    ) {

        if (
            contrasena == null
            || contrasena.isBlank()
        ) {
            throw new IllegalArgumentException(
                "La contraseña no puede estar vacía."
            );
        }

        byte[] salt =
            new byte[TAMANO_SALT];

        secureRandom.nextBytes(salt);

        byte[] hash =
            calcularHash(
                contrasena,
                salt
            );

        return "argon2id"
            + "$"
            + MEMORIA_KB
            + "$"
            + ITERACIONES
            + "$"
            + PARALELISMO
            + "$"
            + Base64.getEncoder()
                .encodeToString(salt)
            + "$"
            + Base64.getEncoder()
                .encodeToString(hash);
    }

    public boolean verificar(
        String contrasena,
        String hashGuardado
    ) {

        if (
            contrasena == null
            || hashGuardado == null
            || hashGuardado.isBlank()
        ) {
            return false;
        }

        try {

            String[] partes =
                hashGuardado.split("\\$");

            if (partes.length != 6) {
                return false;
            }

            if (!partes[0].equals("argon2id")) {
                return false;
            }

            int memoria =
                Integer.parseInt(partes[1]);

            int iteraciones =
                Integer.parseInt(partes[2]);

            int paralelismo =
                Integer.parseInt(partes[3]);

            byte[] salt =
                Base64.getDecoder()
                    .decode(partes[4]);

            byte[] hashEsperado =
                Base64.getDecoder()
                    .decode(partes[5]);

            byte[] hashCalculado =
                calcularHash(
                    contrasena,
                    salt,
                    memoria,
                    iteraciones,
                    paralelismo,
                    hashEsperado.length
                );

            return MessageDigest.isEqual(
                hashEsperado,
                hashCalculado
            );

        } catch (
            IllegalArgumentException e
        ) {
            return false;
        }
    }

    private byte[] calcularHash(
        String contrasena,
        byte[] salt
    ) {

        return calcularHash(
            contrasena,
            salt,
            MEMORIA_KB,
            ITERACIONES,
            PARALELISMO,
            TAMANO_HASH
        );
    }

    private byte[] calcularHash(
        String contrasena,
        byte[] salt,
        int memoria,
        int iteraciones,
        int paralelismo,
        int tamanoHash
    ) {

        Argon2Parameters parametros =
            new Argon2Parameters.Builder(
                Argon2Parameters.ARGON2_id
            )
            .withVersion(
                Argon2Parameters.ARGON2_VERSION_13
            )
            .withSalt(salt)
            .withMemoryAsKB(memoria)
            .withIterations(iteraciones)
            .withParallelism(paralelismo)
            .build();

        Argon2BytesGenerator generador =
            new Argon2BytesGenerator();

        generador.init(parametros);

        byte[] resultado =
            new byte[tamanoHash];

        byte[] passwordBytes =
            contrasena.getBytes(
                StandardCharsets.UTF_8
            );

        try {

            generador.generateBytes(
                passwordBytes,
                resultado
            );

            return resultado;

        } finally {

            java.util.Arrays.fill(
                passwordBytes,
                (byte) 0
            );
        }
    }
}