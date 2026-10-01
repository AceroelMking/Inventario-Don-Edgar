package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Cliente;
import com.gestion.model.PagoCliente;
import com.gestion.model.Persona;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PagoClienteRepositorio {

    private static final String CREAR_TABLA =
        "CREATE TABLE IF NOT EXISTS pago_cliente ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "cliente_id INTEGER NOT NULL, "
        + "fecha TEXT NOT NULL, "
        + "monto REAL NOT NULL, "
        + "observacion TEXT, "
        + "FOREIGN KEY (cliente_id) REFERENCES cliente(id)"
        + ");";

    private static final String INSERTAR =
        "INSERT INTO pago_cliente "
        + "(cliente_id, fecha, monto, observacion) "
        + "VALUES (?, ?, ?, ?);";

    private static final String ACTUALIZAR_SALDO =
        "UPDATE cliente "
        + "SET saldo = saldo - ? "
        + "WHERE id = ? "
        + "AND activo = 1 "
        + "AND saldo >= ?;";

    private static final String CONSULTAR_CLIENTE =
        "SELECT "
        + "p.id, "
        + "p.fecha, "
        + "p.monto, "
        + "p.observacion, "
        + "c.id AS cliente_id, "
        + "c.nombre, "
        + "c.numero_identidad, "
        + "c.correo_electronico, "
        + "c.telefono, "
        + "c.saldo "
        + "FROM pago_cliente p "
        + "INNER JOIN cliente c "
        + "ON c.id = p.cliente_id "
        + "WHERE p.cliente_id = ? "
        + "ORDER BY p.id DESC;";


    public PagoClienteRepositorio() {
        generarTabla();
    }


    private void generarTabla() {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            Statement sentencia =
                conexion.createStatement()
        ) {

            sentencia.execute(
                CREAR_TABLA
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudo crear la tabla de pagos.",
                e
            );
        }
    }


    public int registrarPago(
        PagoCliente pago
    ) {

        Connection conexion = null;

        try {

            conexion =
                ConexionBaseDatos.conectar();

            conexion.setAutoCommit(false);


            /*
             * Primero descontamos el saldo.
             *
             * La condición saldo >= ? evita que
             * el cliente termine con saldo negativo.
             */
            try (
                PreparedStatement sentencia =
                    conexion.prepareStatement(
                        ACTUALIZAR_SALDO
                    )
            ) {

                sentencia.setDouble(
                    1,
                    pago.conseguirMonto()
                );

                sentencia.setInt(
                    2,
                    pago.conseguirCliente().conseguirId()
                );

                sentencia.setDouble(
                    3,
                    pago.conseguirMonto()
                );

                int filas =
                    sentencia.executeUpdate();

                if (filas == 0) {

                    throw new SQLException(
                        "El abono supera el saldo pendiente "
                        + "o el cliente no está disponible."
                    );
                }
            }


            int idPago;

            try (
                PreparedStatement sentencia =
                    conexion.prepareStatement(
                        INSERTAR,
                        Statement.RETURN_GENERATED_KEYS
                    )
            ) {

                sentencia.setInt(
                    1,
                    pago.conseguirCliente().conseguirId()
                );

                sentencia.setString(
                    2,
                    pago.conseguirFecha()
                );

                sentencia.setDouble(
                    3,
                    pago.conseguirMonto()
                );

                sentencia.setString(
                    4,
                    pago.conseguirObservacion()
                );

                sentencia.executeUpdate();


                try (
                    ResultSet claves =
                        sentencia.getGeneratedKeys()
                ) {

                    if (!claves.next()) {

                        throw new SQLException(
                            "No se pudo obtener el ID del pago."
                        );
                    }

                    idPago =
                        claves.getInt(1);
                }
            }


            conexion.commit();

            return idPago;


        } catch (SQLException e) {

            if (conexion != null) {

                try {
                    conexion.rollback();
                } catch (SQLException errorRollback) {
                    errorRollback.printStackTrace();
                }
            }

            throw new RuntimeException(
                "No se pudo registrar el abono. "
                + e.getMessage(),
                e
            );


        } finally {

            if (conexion != null) {

                try {

                    conexion.setAutoCommit(true);
                    conexion.close();

                } catch (SQLException e) {

                    e.printStackTrace();
                }
            }
        }
    }


    public ObservableList<PagoCliente> consultarPagosCliente(
        int idCliente
    ) {

        ObservableList<PagoCliente> pagos =
            FXCollections.observableArrayList();

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_CLIENTE
                )
        ) {

            sentencia.setInt(
                1,
                idCliente
            );

            try (
                ResultSet resultados =
                    sentencia.executeQuery()
            ) {

                while (resultados.next()) {

                    PagoCliente pago =
                        new PagoCliente();

                    Cliente cliente =
                        new Cliente();

                    Persona persona =
                        new Persona();


                    persona.colocarNombre(
                        resultados.getString(
                            "nombre"
                        )
                    );

                    persona.colocarNumeroIdentidad(
                        resultados.getString(
                            "numero_identidad"
                        )
                    );


                    cliente.colocarId(
                        resultados.getInt(
                            "cliente_id"
                        )
                    );

                    cliente.colocarPersona(
                        persona
                    );

                    cliente.colocarCorreoElectronico(
                        resultados.getString(
                            "correo_electronico"
                        )
                    );

                    cliente.colocarTelefono(
                        resultados.getString(
                            "telefono"
                        )
                    );

                    cliente.colocarSaldo(
                        resultados.getDouble(
                            "saldo"
                        )
                    );


                    pago.colocarId(
                        resultados.getInt(
                            "id"
                        )
                    );

                    pago.colocarCliente(
                        cliente
                    );

                    pago.colocarFecha(
                        resultados.getString(
                            "fecha"
                        )
                    );

                    pago.colocarMonto(
                        resultados.getDouble(
                            "monto"
                        )
                    );

                    pago.colocarObservacion(
                        resultados.getString(
                            "observacion"
                        )
                    );


                    pagos.add(
                        pago
                    );
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudieron consultar "
                + "los pagos del cliente.",
                e
            );
        }

        return pagos;
    }
}