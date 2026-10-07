package com.gestion.repository;

import com.gestion.util.RutaAplicacion;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBaseDatos {

    private ConexionBaseDatos() {
        /*
         * Evita crear objetos de esta clase,
         * ya que solamente contiene métodos
         * estáticos para manejar la conexión.
         */
    }

    /**
     * Crea una conexión con la base de datos SQLite.
     *
     * La ubicación de la base de datos depende
     * automáticamente del sistema operativo.
     */
    public static Connection conectar()
            throws SQLException {

        String enlace = construirEnlace();

        Connection conexion =
            DriverManager.getConnection(enlace);

        try (
            Statement sentencia =
                conexion.createStatement()
        ) {

            /*
             * SQLite requiere activar las claves
             * foráneas en cada conexión.
             */
            sentencia.execute(
                "PRAGMA foreign_keys = ON;"
            );

        } catch (SQLException e) {

            /*
             * Si no se puede activar la integridad
             * referencial, cerramos la conexión
             * para evitar devolver una conexión
             * configurada incorrectamente.
             */
            try {
                conexion.close();

            } catch (SQLException cierreError) {

                e.addSuppressed(
                    cierreError
                );
            }

            throw e;
        }

        return conexion;
    }

    /**
     * Construye el enlace JDBC utilizando
     * la ruta correspondiente al sistema operativo.
     */
    private static String construirEnlace()
            throws SQLException {

        try {

            Path rutaBaseDatos =
                RutaAplicacion
                    .conseguirRutaBaseDatos();

            return "jdbc:sqlite:"
                + rutaBaseDatos
                    .toAbsolutePath();

        } catch (IOException e) {

            throw new SQLException(
                "No fue posible crear el directorio "
                + "para almacenar la base de datos.",
                e
            );
        }
    }

    /**
     * Permite consultar la ubicación actual
     * de la base de datos.
     *
     * Este método nos servirá para verificar
     * dónde se está almacenando SQLite.
     */
    public static String conseguirRutaBaseDatos()
            throws SQLException {

        try {

            return RutaAplicacion
                .conseguirRutaBaseDatos()
                .toAbsolutePath()
                .toString();

        } catch (IOException e) {

            throw new SQLException(
                "No fue posible determinar la ruta "
                + "de la base de datos.",
                e
            );
        }
    }
}