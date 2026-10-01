package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.DetalleVenta;
import com.gestion.model.Venta;
import com.gestion.model.Cliente;
import com.gestion.model.Persona;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class VentaRepositorio {

    private static final String CREAR_TABLA_VENTA =
        "CREATE TABLE IF NOT EXISTS venta ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "cliente_id INTEGER, "
        + "fecha TEXT NOT NULL, "
        + "subtotal REAL NOT NULL, "
        + "total REAL NOT NULL, "
        + "metodo_pago TEXT NOT NULL, "
        + "monto_pagado REAL NOT NULL DEFAULT 0, "
        + "saldo_pendiente REAL NOT NULL DEFAULT 0, "
        + "FOREIGN KEY (cliente_id) REFERENCES cliente(id)"
        + ");";

    private static final String CREAR_TABLA_DETALLE =
        "CREATE TABLE IF NOT EXISTS detalle_venta ("
        + "id INTEGER PRIMARY KEY AUTOINCREMENT, "
        + "venta_id INTEGER NOT NULL, "
        + "producto_id INTEGER NOT NULL, "
        + "cantidad REAL NOT NULL, "
        + "precio_unitario REAL NOT NULL, "
        + "subtotal REAL NOT NULL, "
        + "FOREIGN KEY (venta_id) REFERENCES venta(id), "
        + "FOREIGN KEY (producto_id) REFERENCES producto(id)"
        + ");";

    private static final String INSERTAR_VENTA =
        "INSERT INTO venta ("
        + "cliente_id, fecha, subtotal, total, metodo_pago, "
        + "monto_pagado, saldo_pendiente"
        + ") VALUES (?, ?, ?, ?, ?, ?, ?);";

    private static final String INSERTAR_DETALLE =
        "INSERT INTO detalle_venta ("
        + "venta_id, producto_id, cantidad, precio_unitario, subtotal"
        + ") VALUES (?, ?, ?, ?, ?);";

    private static final String CONSULTAR_EXISTENCIAS =
        "SELECT existencias "
        + "FROM producto "
        + "WHERE id = ? AND activo = 1;";

    private static final String DESCONTAR_EXISTENCIAS =
        "UPDATE producto "
        + "SET existencias = existencias - ? "
        + "WHERE id = ? "
        + "AND activo = 1 "
        + "AND existencias >= ?;";

    private static final String ACTUALIZAR_SALDO_CLIENTE =
        "UPDATE cliente "
        + "SET saldo = COALESCE(saldo, 0) + ? "
        + "WHERE id = ? "
        + "AND activo = 1;";

    private static final String CONSULTAR_VENTAS =
        "SELECT "
        + "v.id, "
        + "v.fecha, "
        + "v.subtotal, "
        + "v.total, "
        + "v.metodo_pago, "
        + "v.monto_pagado, "
        + "v.saldo_pendiente, "
        + "v.cliente_id, "
        + "c.nombre AS cliente_nombre "
        + "FROM venta v "
        + "LEFT JOIN cliente c ON c.id = v.cliente_id "
        + "ORDER BY v.id DESC;";


    public VentaRepositorio() {
        generarTablas();
    }


    private void generarTablas() {

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            Statement sentencia =
                conexion.createStatement()
        ) {

            sentencia.execute(
                CREAR_TABLA_VENTA
            );

            sentencia.execute(
                CREAR_TABLA_DETALLE
            );

        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudieron crear las tablas de ventas.",
                e
            );
        }
    }


    public int registrarVenta(
        Venta venta,
        ObservableList<DetalleVenta> detalles
    ) {

        Connection conexion = null;

        try {

            conexion =
                ConexionBaseDatos.conectar();

            conexion.setAutoCommit(false);


            /*
             * 1. Verificar existencias actuales.
             */
            validarExistencias(
                conexion,
                detalles
            );


            /*
             * 2. Registrar encabezado de venta.
             */
            int idVenta =
                insertarVenta(
                    conexion,
                    venta
                );


            /*
             * 3. Registrar productos vendidos.
             */
            insertarDetalles(
                conexion,
                idVenta,
                detalles
            );


            /*
             * 4. Descontar inventario.
             */
            descontarExistencias(
                conexion,
                detalles
            );


            /*
             * 5. Si quedó deuda, actualizar
             * el saldo del cliente.
             */
            actualizarSaldoCliente(
                conexion,
                venta
            );


            /*
             * Todo salió correctamente.
             */
            conexion.commit();

            return idVenta;


        } catch (SQLException e) {

            if (conexion != null) {

                try {

                    conexion.rollback();

                } catch (SQLException rollbackError) {

                    rollbackError.printStackTrace();
                }
            }

            throw new RuntimeException(
                "No se pudo registrar la venta. "
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


    private void validarExistencias(
        Connection conexion,
        ObservableList<DetalleVenta> detalles
    ) throws SQLException {

        try (
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_EXISTENCIAS
                )
        ) {

            for (DetalleVenta detalle : detalles) {

                sentencia.setInt(
                    1,
                    detalle
                        .conseguirProducto()
                        .conseguirId()
                );

                try (
                    ResultSet resultado =
                        sentencia.executeQuery()
                ) {

                    if (!resultado.next()) {

                        throw new SQLException(
                            "El producto "
                            + detalle
                                .conseguirProducto()
                                .conseguirNombre()
                            + " no existe o está inactivo."
                        );
                    }

                    double existencias =
                        resultado.getDouble(
                            "existencias"
                        );

                    if (
                        detalle.conseguirCantidad()
                        > existencias
                    ) {

                        throw new SQLException(
                            "Existencias insuficientes para "
                            + detalle
                                .conseguirProducto()
                                .conseguirNombre()
                            + ". Disponible: "
                            + existencias
                        );
                    }
                }
            }
        }
    }


    private int insertarVenta(
        Connection conexion,
        Venta venta
    ) throws SQLException {

        try (
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    INSERTAR_VENTA,
                    Statement.RETURN_GENERATED_KEYS
                )
        ) {

            if (
                venta.conseguirCliente() == null
                || venta
                    .conseguirCliente()
                    .conseguirId() <= 0
            ) {

                sentencia.setNull(
                    1,
                    java.sql.Types.INTEGER
                );

            } else {

                sentencia.setInt(
                    1,
                    venta
                        .conseguirCliente()
                        .conseguirId()
                );
            }

            sentencia.setString(
                2,
                venta.conseguirFecha()
            );

            sentencia.setDouble(
                3,
                venta.conseguirSubtotal()
            );

            sentencia.setDouble(
                4,
                venta.conseguirTotal()
            );

            sentencia.setString(
                5,
                venta.conseguirMetodoPago()
            );

            sentencia.setDouble(
                6,
                venta.conseguirMontoPagado()
            );

            sentencia.setDouble(
                7,
                venta.conseguirSaldoPendiente()
            );

            sentencia.executeUpdate();


            try (
                ResultSet claves =
                    sentencia.getGeneratedKeys()
            ) {

                if (claves.next()) {

                    return claves.getInt(1);
                }
            }
        }

        throw new SQLException(
            "No se pudo obtener el ID de la venta."
        );
    }


    private void insertarDetalles(
        Connection conexion,
        int idVenta,
        ObservableList<DetalleVenta> detalles
    ) throws SQLException {

        try (
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    INSERTAR_DETALLE
                )
        ) {

            for (
                DetalleVenta detalle
                : detalles
            ) {

                sentencia.setInt(
                    1,
                    idVenta
                );

                sentencia.setInt(
                    2,
                    detalle
                        .conseguirProducto()
                        .conseguirId()
                );

                sentencia.setDouble(
                    3,
                    detalle.conseguirCantidad()
                );

                sentencia.setDouble(
                    4,
                    detalle.conseguirPrecioUnitario()
                );

                sentencia.setDouble(
                    5,
                    detalle.conseguirSubtotal()
                );

                sentencia.addBatch();
            }

            sentencia.executeBatch();
        }
    }


    private void descontarExistencias(
        Connection conexion,
        ObservableList<DetalleVenta> detalles
    ) throws SQLException {

        try (
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    DESCONTAR_EXISTENCIAS
                )
        ) {

            for (
                DetalleVenta detalle
                : detalles
            ) {

                double cantidad =
                    detalle.conseguirCantidad();

                int idProducto =
                    detalle
                        .conseguirProducto()
                        .conseguirId();


                sentencia.setDouble(
                    1,
                    cantidad
                );

                sentencia.setInt(
                    2,
                    idProducto
                );

                sentencia.setDouble(
                    3,
                    cantidad
                );


                int filas =
                    sentencia.executeUpdate();


                if (filas == 0) {

                    throw new SQLException(
                        "No se pudo descontar el inventario de "
                        + detalle
                            .conseguirProducto()
                            .conseguirNombre()
                        + "."
                    );
                }
            }
        }
    }


    private void actualizarSaldoCliente(
        Connection conexion,
        Venta venta
    ) throws SQLException {

        if (
            venta.conseguirSaldoPendiente() == null
            || venta.conseguirSaldoPendiente() <= 0
        ) {

            return;
        }


        if (
            venta.conseguirCliente() == null
            || venta
                .conseguirCliente()
                .conseguirId() <= 0
        ) {

            throw new SQLException(
                "Debe seleccionar un cliente "
                + "cuando la venta tiene saldo pendiente."
            );
        }


        try (
            PreparedStatement sentencia =
                conexion.prepareStatement(
                    ACTUALIZAR_SALDO_CLIENTE
                )
        ) {

            sentencia.setDouble(
                1,
                venta.conseguirSaldoPendiente()
            );

            sentencia.setInt(
                2,
                venta
                    .conseguirCliente()
                    .conseguirId()
            );


            int filas =
                sentencia.executeUpdate();


            if (filas == 0) {

                throw new SQLException(
                    "No se pudo actualizar "
                    + "el saldo del cliente."
                );
            }
        }
    }


    public ObservableList<Venta> consultarVentas() {

        ObservableList<Venta> ventas =
            FXCollections.observableArrayList();


        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(
                    CONSULTAR_VENTAS
                );

            ResultSet resultados =
                sentencia.executeQuery()
        ) {

            while (resultados.next()) {

                Venta venta =
                    new Venta();


                venta.colocarId(
                    resultados.getInt("id")
                );


                venta.colocarFecha(
                    resultados.getString("fecha")
                );


                venta.colocarSubtotal(
                    resultados.getDouble("subtotal")
                );


                venta.colocarTotal(
                    resultados.getDouble("total")
                );


                venta.colocarMetodoPago(
                    resultados.getString("metodo_pago")
                );


                venta.colocarMontoPagado(
                    resultados.getDouble("monto_pagado")
                );


                venta.colocarSaldoPendiente(
                    resultados.getDouble("saldo_pendiente")
                );


                /*
                * Una venta puede no tener cliente,
                * por ejemplo una venta de contado.
                */
                int idCliente =
                    resultados.getInt("cliente_id");


                if (!resultados.wasNull()) {

                    Cliente cliente =
                        new Cliente();


                    Persona persona =
                        new Persona();


                    persona.colocarNombre(
                        resultados.getString(
                            "cliente_nombre"
                        )
                    );


                    cliente.colocarId(
                        idCliente
                    );


                    cliente.colocarPersona(
                        persona
                    );


                    venta.colocarCliente(
                        cliente
                    );
                }


                ventas.add(
                    venta
                );
            }


        } catch (SQLException e) {

            throw new RuntimeException(
                "No se pudieron consultar las ventas.",
                e
            );
        }


        return ventas;
    }
}