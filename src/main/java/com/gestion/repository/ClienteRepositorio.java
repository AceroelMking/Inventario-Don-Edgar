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

    private static final String ACTUALIZAR =
        "UPDATE cliente SET "
        + "nombre = ?, "
        + "numero_identidad = ?, "
        + "correo_electronico = ?, "
        + "telefono = ?, "
        + "saldo = ? "
        + "WHERE id = ?;";

    private static final String ELIMINAR_CLIENTE =
        "UPDATE cliente SET activo = 0 WHERE id = ?;";

    private static final String AGREGAR_COLUMNA_ACTIVO =
        "ALTER TABLE cliente ADD COLUMN activo INTEGER NOT NULL DEFAULT 1;";

    public ClienteRepositorio() {
        generarTabla();
    }

    private void generarTabla() {
    try (
        Connection conexion = ConexionBaseDatos.conectar();
        Statement sentencia = conexion.createStatement()
    ) {
        sentencia.execute(CREAR_TABLA);

        try {
            sentencia.execute(AGREGAR_COLUMNA_ACTIVO);
        } catch (SQLException e) {
            // La columna ya existe.
        }

    } catch (SQLException e) {
        throw new RuntimeException(
            "No se pudo crear o actualizar la tabla cliente",
            e
        );
    }
} 

    public void agregarCliente(Cliente cliente) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(INSERTAR)
        ) {

            sentencia.setString(
                1,
                cliente.conseguirPersona().conseguirNombre()
            );

            sentencia.setString(
                2,
                cliente.conseguirPersona().conseguirNumeroIdentidad()
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

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo agregar el cliente", e
            );
        }
    }

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
                "Error al consultar los clientes", e
            );
        }

        return clientes;
    }

    public Cliente consultarCliente(int id) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(CONSULTAR_ID)
        ) {

            sentencia.setInt(1, id);

            try (ResultSet resultados = sentencia.executeQuery()) {

                if (resultados.next()) {
                    return convertirCliente(resultados);
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al consultar el cliente", e
            );
        }

        return null;
    }

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
                cliente.conseguirPersona().conseguirNumeroIdentidad()
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
                "No se pudo editar el cliente", e
            );
        }
    }

    public void actualizarSaldo(int idCliente, double saldo) {

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
                "No se pudo actualizar el saldo del cliente", e
            );
        }
    }

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