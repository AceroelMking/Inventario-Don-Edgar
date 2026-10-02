package com.gestion.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class ConexionBaseDatos {

    private static final String enlace =
        "jdbc:sqlite:database/gestion_productos_ventas.db";

    public static Connection conectar()
            throws SQLException {

        Connection conexion =
            DriverManager.getConnection(
                enlace
            );

        try (
            Statement sentencia =
                conexion.createStatement()
        ) {

            /*
             * SQLite requiere activar las claves
             * foráneas para cada conexión.
             */
            sentencia.execute(
                "PRAGMA foreign_keys = ON;"
            );

        } catch (SQLException e) {

            /*
             * Si no se puede activar la integridad
             * referencial, cerramos la conexión
             * para no devolver una conexión
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
}