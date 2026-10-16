
package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Cliente;
import com.gestion.model.Persona;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ClienteRepositorio {

    private static final String CREAR_TABLA =
        "CREATE TABLE IF NOT EXISTS cliente ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "nombre TEXT NOT NULL, "
        + "numero_identidad TEXT NOT NULL UNIQUE, "
        + "correo_electronico TEXT, "
        + "telefono TEXT, "
        + "saldo REAL NOT NULL DEFAULT 0, "
        + "activo INTEGER NOT NULL DEFAULT 1"
        + ");";

    private static final String INSERTAR =
        "INSERT INTO cliente "
        + "(nombre, numero_identidad, correo_electronico, telefono, saldo) "
        + "VALUES (?, ?, ?, ?, ?);";

    private static final String CONSULTAR_TODO =
        "SELECT id, nombre, numero_identidad, correo_electronico, "
        + "telefono, saldo FROM cliente "
        + "WHERE activo = 1 ORDER BY nombre;";

    private static final String CONSULTAR_ID =
        "SELECT id, nombre, numero_identidad, correo_electronico, "
        + "telefono, saldo FROM cliente WHERE id = ?;";

    private static final String CONSULTAR_IDENTIFICACION =
        "SELECT id, nombre, numero_identidad, correo_electronico, "
        + "telefono, saldo, activo FROM cliente "
        + "WHERE numero_identidad = ?;";

    private static final String ACTUALIZAR =
        "UPDATE cliente SET "
        + "nombre = ?, "
        + "numero_identidad = ?, "
        + "correo_electronico = ?, "
        + "telefono = ?, "
        + "saldo = ? "
        + "WHERE id = ?;";

    private static final String REACTIVAR =
        "UPDATE cliente SET "
        + "nombre = ?, "
        + "correo_electronico = ?, "
        + "telefono = ?, "
        + "activo = 1 "
        + "WHERE numero_identidad = ? AND activo = 0;";

    private static final String ELIMINAR_CLIENTE =
        "UPDATE cliente SET activo = 0 WHERE id = ?;";

    private static final String AGREGAR_COLUMNA_ACTIVO =
        "ALTER TABLE cliente ADD COLUMN activo "
        + "INTEGER NOT NULL DEFAULT 1;";

    public ClienteRepositorio() {
        generarTabla();
    }

    private void generarTabla() {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement()
        ) {

            sentencia.execute(CREAR_TABLA);

            // Verificar si la columna activo existe.
            boolean existeActivo = false;

            try (
                ResultSet columnas =
                    sentencia.executeQuery(
                        "PRAGMA table_info(cliente);"
                    )
            ) {

                while (columnas.next()) {

                    if ("activo".equalsIgnoreCase(
                            columnas.getString("name"))) {

                        existeActivo = true;
                        break;
                    }
                }
            }

            if (!existeActivo) {
                sentencia.execute(AGREGAR_COLUMNA_ACTIVO);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo crear o actualizar la tabla cliente",
                e
            );
        }
    }

    // =====================================================
    // AGREGAR CLIENTE O REACTIVARLO
    // =====================================================

    public void agregarCliente(Cliente cliente) {

        String identificacion =
            cliente.conseguirPersona().conseguirNumeroIdentidad();

        try (
            Connection conexion = ConexionBaseDatos.conectar()
        ) {

            conexion.setAutoCommit(false);

            try {

                Integer idExistente = null;
                boolean activo = false;

                // Buscar la cédula, incluyendo desactivados.
                try (
                    PreparedStatement consulta =
                        conexion.prepareStatement(
                            CONSULTAR_IDENTIFICACION
                        )
                ) {

                    consulta.setString(1, identificacion);

                    try (
                        ResultSet resultado =
                            consulta.executeQuery()
                    ) {

                        if (resultado.next()) {

                            idExistente = resultado.getInt("id");

                            activo =
                                resultado.getInt("activo") == 1;
                        }
                    }
                }

                if (idExistente != null) {

                    if (activo) {

                        throw new IllegalArgumentException(
                            "Ya existe un cliente activo "
                            + "con este número de identificación."
                        );
                    }

                    // Reactivar conservando ID y saldo.
                    try (
                        PreparedStatement sentencia =
                            conexion.prepareStatement(REACTIVAR)
                    ) {

                        sentencia.setString(
                            1,
                            cliente.conseguirPersona()
                                .conseguirNombre()
                        );

                        sentencia.setString(
                            2,
                            cliente.conseguirCorreoElectronico()
                        );

                        sentencia.setString(
                            3,
                            cliente.conseguirTelefono()
                        );

                        sentencia.setString(
                            4,
                            identificacion
                        );

                        int filas = sentencia.executeUpdate();

                        if (filas != 1) {
                            throw new SQLException(
                                "No se pudo reactivar el cliente."
                            );
                        }
                    }

                    cliente.colocarId(idExistente);

                } else {

                    // Registrar un cliente nuevo.
                    try (
                        PreparedStatement sentencia =
                            conexion.prepareStatement(
                                INSERTAR,
                                Statement.RETURN_GENERATED_KEYS
                            )
                    ) {

                        sentencia.setString(
                            1,
                            cliente.conseguirPersona()
                                .conseguirNombre()
                        );

                        sentencia.setString(
                            2,
                            identificacion
                        );

                        sentencia.setString(
                            3,
                            cliente.conseguirCorreoElectronico()
                        );

                        sentencia.setString(
                            4,
                            cliente.conseguirTelefono()
                        );

                        sentencia.setDouble(
                            5,
                            cliente.conseguirSaldo() == null
                                ? 0.0
                                : cliente.conseguirSaldo()
                        );

                        sentencia.executeUpdate();

                        try (
                            ResultSet claves =
                                sentencia.getGeneratedKeys()
                        ) {

                            if (claves.next()) {
                                cliente.colocarId(
                                    claves.getInt(1)
                                );
                            }
                        }
                    }
                }

                conexion.commit();

            } catch (SQLException | RuntimeException e) {

                try {
                    conexion.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo registrar o reactivar el cliente.",
                e
            );
        }
    }

    // =====================================================
    // CONSULTAR CLIENTES ACTIVOS
    // =====================================================

    public ObservableList<Cliente> consultarTodo() {

        ObservableList<Cliente> clientes =
            FXCollections.observableArrayList();

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet resultados =
                sentencia.executeQuery(CONSULTAR_TODO)
        ) {

            while (resultados.next()) {
                clientes.add(convertirCliente(resultados));
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "Error al consultar los clientes",
                e
            );
        }

        return clientes;
    }

    // =====================================================
    // CONSULTAR CLIENTE POR ID
    // =====================================================

    public Cliente consultarCliente(int id) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(CONSULTAR_ID)
        ) {

            sentencia.setInt(1, id);

            try (
                ResultSet resultados =
                    sentencia.executeQuery()
            ) {

                if (resultados.next()) {
                    return convertirCliente(resultados);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "Error al consultar el cliente",
                e
            );
        }

        return null;
    }

    // =====================================================
    // CONSULTAR POR IDENTIFICACIÓN
    // INCLUYE CLIENTES DESACTIVADOS
    // =====================================================

    public Cliente consultarPorIdentificacion(
            String identificacion) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_IDENTIFICACION
                )
        ) {

            sentencia.setString(1, identificacion);

            try (
                ResultSet resultados =
                    sentencia.executeQuery()
            ) {

                if (resultados.next()) {
                    return convertirCliente(resultados);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "Error al consultar la identificación.",
                e
            );
        }

        return null;
    }

    // =====================================================
    // CONSULTAR SI ESTÁ ACTIVO
    // =====================================================

    public boolean estaActivo(String identificacion) {

        String sql =
            "SELECT activo FROM cliente "
            + "WHERE numero_identidad = ?;";

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {

            sentencia.setString(1, identificacion);

            try (
                ResultSet resultado =
                    sentencia.executeQuery()
            ) {

                return resultado.next()
                    && resultado.getInt("activo") == 1;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo consultar el estado del cliente.",
                e
            );
        }
    }

    // =====================================================
    // EDITAR CLIENTE
    // =====================================================

    public void editarCliente(Cliente cliente) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(ACTUALIZAR)
        ) {

            sentencia.setString(
                1,
                cliente.conseguirPersona().conseguirNombre()
            );

            sentencia.setString(
                2,
                cliente.conseguirPersona()
                    .conseguirNumeroIdentidad()
            );

            sentencia.setString(
                3,
                cliente.conseguirCorreoElectronico()
            );

            sentencia.setString(
                4,
                cliente.conseguirTelefono()
            );

            sentencia.setDouble(
                5,
                cliente.conseguirSaldo() == null
                    ? 0.0
                    : cliente.conseguirSaldo()
            );

            sentencia.setInt(
                6,
                cliente.conseguirId()
            );

            sentencia.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo editar el cliente",
                e
            );
        }
    }

    // =====================================================
    // ACTUALIZAR SALDO
    // =====================================================

    public void actualizarSaldo(
            int idCliente,
            double saldo) {

        String sql =
            "UPDATE cliente SET saldo = ? WHERE id = ?;";

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {

            sentencia.setDouble(1, saldo);
            sentencia.setInt(2, idCliente);

            sentencia.executeUpdate();

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo actualizar el saldo del cliente",
                e
            );
        }
    }

    // =====================================================
    // CONVERTIR RESULTADO A CLIENTE
    // =====================================================

    private Cliente convertirCliente(ResultSet resultados)
            throws SQLException {

        Cliente cliente = new Cliente();
        Persona persona = new Persona();

        persona.colocarNombre(
            resultados.getString("nombre")
        );

        persona.colocarNumeroIdentidad(
            resultados.getString("numero_identidad")
        );

        cliente.colocarId(
            resultados.getInt("id")
        );

        cliente.colocarPersona(persona);

        cliente.colocarCorreoElectronico(
            resultados.getString("correo_electronico")
        );

        cliente.colocarTelefono(
            resultados.getString("telefono")
        );

        cliente.colocarSaldo(
            resultados.getDouble("saldo")
        );

        return cliente;
    }

    // =====================================================
    // DESACTIVAR CLIENTE
    // =====================================================

    public void eliminarCliente(int idCliente) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(ELIMINAR_CLIENTE)
        ) {

            sentencia.setInt(1, idCliente);

            int filas = sentencia.executeUpdate();

            if (filas == 0) {

                throw new RuntimeException(
                    "No se encontró el cliente."
                );
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo eliminar el cliente.",
                e
            );
        }
    }
}
