package com.gestion.repository;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConexionBaseDatos {
    private static final String enlace = "jdbc:sqlite:database/gestion_productos_ventas.db";

    public static Connection conectar() throws SQLException {
        return DriverManager.getConnection(enlace);
    }
}
