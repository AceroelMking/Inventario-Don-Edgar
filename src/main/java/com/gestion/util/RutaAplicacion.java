package com.gestion.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public final class RutaAplicacion {

    private static final String NOMBRE_APLICACION =
        "InventarioDonEdgar";

    private static final String NOMBRE_BASE_DATOS =
        "gestion_productos_ventas.db";

    private RutaAplicacion() {
        // Evita instanciar esta clase.
    }

    /**
     * Identifica el sistema operativo actual.
     */
    public static SistemaOperativo conseguirSistemaOperativo() {

        String sistemaOperativo =
            System.getProperty("os.name")
                .toLowerCase();

        if (sistemaOperativo.contains("win")) {
            return SistemaOperativo.WINDOWS;
        }

        if (
            sistemaOperativo.contains("linux")
            || sistemaOperativo.contains("nix")
            || sistemaOperativo.contains("nux")
            || sistemaOperativo.contains("aix")
        ) {
            return SistemaOperativo.LINUX;
        }

        if (
            sistemaOperativo.contains("mac")
            || sistemaOperativo.contains("darwin")
        ) {
            return SistemaOperativo.MACOS;
        }

        return SistemaOperativo.OTRO;
    }

    /**
     * Obtiene el directorio principal donde
     * se almacenarán los datos de la aplicación.
     */
    public static Path conseguirDirectorioAplicacion() {

        SistemaOperativo sistemaOperativo =
            conseguirSistemaOperativo();

        switch (sistemaOperativo) {

            case WINDOWS:
                return conseguirDirectorioWindows();

            case LINUX:
                return conseguirDirectorioLinux();

            case MACOS:
                return conseguirDirectorioMacOS();

            default:
                return conseguirDirectorioAlternativo();
        }
    }

    /**
     * Windows:
     *
     * C:\Users\Usuario\AppData\Local\InventarioDonEdgar
     */
    private static Path conseguirDirectorioWindows() {

        String localAppData =
            System.getenv("LOCALAPPDATA");

        if (
            localAppData != null
            && !localAppData.isBlank()
        ) {
            return Paths.get(
                localAppData,
                NOMBRE_APLICACION
            );
        }

        /*
         * Ruta alternativa si LOCALAPPDATA
         * no está disponible.
         */
        return Paths.get(
            System.getProperty("user.home"),
            "AppData",
            "Local",
            NOMBRE_APLICACION
        );
    }

    /**
     * Linux:
     *
     * Usa XDG_DATA_HOME si está configurado.
     *
     * De lo contrario:
     * ~/.local/share/InventarioDonEdgar
     */
    private static Path conseguirDirectorioLinux() {

        String xdgDataHome =
            System.getenv("XDG_DATA_HOME");

        if (
            xdgDataHome != null
            && !xdgDataHome.isBlank()
        ) {
            return Paths.get(
                xdgDataHome,
                NOMBRE_APLICACION
            );
        }

        return Paths.get(
            System.getProperty("user.home"),
            ".local",
            "share",
            NOMBRE_APLICACION
        );
    }

    /**
     * macOS:
     *
     * ~/Library/Application Support/InventarioDonEdgar
     */
    private static Path conseguirDirectorioMacOS() {

        return Paths.get(
            System.getProperty("user.home"),
            "Library",
            "Application Support",
            NOMBRE_APLICACION
        );
    }

    /**
     * Ruta de respaldo para sistemas
     * operativos no reconocidos.
     */
    private static Path conseguirDirectorioAlternativo() {

        return Paths.get(
            System.getProperty("user.home"),
            "." + NOMBRE_APLICACION
        );
    }

    /**
     * Crea el directorio de la aplicación
     * si todavía no existe.
     */
    public static Path crearDirectorioAplicacion()
            throws IOException {

        Path directorio =
            conseguirDirectorioAplicacion();

        Files.createDirectories(
            directorio
        );

        return directorio;
    }

    /**
     * Obtiene la ruta completa del archivo
     * de base de datos SQLite.
     */
    public static Path conseguirRutaBaseDatos()
            throws IOException {

        return crearDirectorioAplicacion()
            .resolve(
                NOMBRE_BASE_DATOS
            );
    }

    /**
     * Devuelve la ruta de SQLite como texto.
     */
    public static String conseguirRutaBaseDatosTexto()
            throws IOException {

        return conseguirRutaBaseDatos()
            .toAbsolutePath()
            .toString();
    }

    /**
     * Devuelve el nombre del sistema operativo
     * detectado.
     */
    public static String conseguirNombreSistemaOperativo() {

        return conseguirSistemaOperativo()
            .name();
    }

    public enum SistemaOperativo {
        WINDOWS,
        LINUX,
        MACOS,
        OTRO
    }
}