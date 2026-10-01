package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Usuario;
import com.gestion.service.ContrasenaServicio;

public class UsuarioRepositorio {

    private static final String CREAR_TABLA = """
        CREATE TABLE IF NOT EXISTS usuario (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre_usuario TEXT NOT NULL UNIQUE,
            contrasena_hash TEXT NOT NULL
        );
        """;

    private static final String CONTAR_USUARIOS = """
        SELECT COUNT(*) AS cantidad
        FROM usuario;
        """;

    private static final String INSERTAR_USUARIO = """
        INSERT INTO usuario (
            nombre_usuario,
            contrasena_hash
        )
        VALUES (?, ?);
        """;

    private static final String CONSULTAR_POR_NOMBRE = """
        SELECT
            id,
            nombre_usuario,
            contrasena_hash
        FROM usuario
        WHERE nombre_usuario = ?
        LIMIT 1;
        """;

    private static final String CONSULTAR_PRIMERO = """
        SELECT
            id,
            nombre_usuario,
            contrasena_hash
        FROM usuario
        ORDER BY id
        LIMIT 1;
        """;

    private static final String ACTUALIZAR_CREDENCIALES = """
        UPDATE usuario
        SET nombre_usuario = ?,
            contrasena_hash = ?
        WHERE id = ?;
        """;

    private final ContrasenaServicio contrasenaServicio =
        new ContrasenaServicio();

    public UsuarioRepositorio() {

        crearTabla();

        crearAdministradorInicial();
    }

    private void crearTabla() {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            Statement sentencia =
                conexion.createStatement()
        ) {

            sentencia.execute(CREAR_TABLA);

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo crear la tabla de usuarios.",
                e
            );
        }
    }

    private void crearAdministradorInicial() {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar()
        ) {

            int cantidad = 0;

            try (
                Statement sentencia =
                    conexion.createStatement();

                ResultSet resultado =
                    sentencia.executeQuery(
                        CONTAR_USUARIOS
                    )
            ) {

                if (resultado.next()) {
                    cantidad =
                        resultado.getInt(
                            "cantidad"
                        );
                }
            }

            if (cantidad > 0) {
                return;
            }

            String hash =
                contrasenaServicio.generarHash(
                    "admin"
                );

            try (
                PreparedStatement sentencia =
                    conexion.prepareStatement(
                        INSERTAR_USUARIO
                    )
            ) {

                sentencia.setString(
                    1,
                    "admin"
                );

                sentencia.setString(
                    2,
                    hash
                );

                sentencia.executeUpdate();
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo crear el administrador inicial.",
                e
            );
        }
    }

    public Usuario consultarPorNombre(
        String nombreUsuario
    ) {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_POR_NOMBRE
                )
        ) {

            sentencia.setString(
                1,
                nombreUsuario
            );

            try (
                ResultSet resultado =
                    sentencia.executeQuery()
            ) {

                if (!resultado.next()) {
                    return null;
                }

                return convertirUsuario(
                    resultado
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo consultar el usuario.",
                e
            );
        }
    }

    public Usuario consultarAdministrador() {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_PRIMERO
                );

            ResultSet resultado =
                sentencia.executeQuery()
        ) {

            if (!resultado.next()) {
                return null;
            }

            return convertirUsuario(
                resultado
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo consultar el administrador.",
                e
            );
        }
    }

    public void actualizarCredenciales(
        Usuario usuario
    ) {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(
                    ACTUALIZAR_CREDENCIALES
                )
        ) {

            sentencia.setString(
                1,
                usuario.conseguirNombreUsuario()
            );

            sentencia.setString(
                2,
                usuario.conseguirContrasena()
            );

            sentencia.setInt(
                3,
                usuario.conseguirId()
            );

            int filas =
                sentencia.executeUpdate();

            if (filas == 0) {
                throw new RuntimeException(
                    "No se encontró el usuario."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudieron actualizar las credenciales.",
                e
            );
        }
    }

    private Usuario convertirUsuario(
        ResultSet resultado
    ) throws SQLException {

        Usuario usuario =
            new Usuario();

        usuario.colocarId(
            resultado.getInt("id")
        );

        usuario.colocarNombreUsuario(
            resultado.getString(
                "nombre_usuario"
            )
        );

        usuario.colocarContrasena(
            resultado.getString(
                "contrasena_hash"
            )
        );

        return usuario;
    }
}