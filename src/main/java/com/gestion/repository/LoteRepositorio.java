package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Lote;
import com.gestion.model.Producto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class LoteRepositorio {

    private static final String[] CAMPOS = {"id",
        "id_producto",
        "lote",
        "fecha_registro",
        "fecha_expedicion",
        "fecha_vencimiento",
        "cantidad_movimiento",
        "valor_movimiento",
        "motivo"
    };

    // Create

    private static final String CREAR_TABLA = "CREATE TABLE IF NOT EXISTS lote ( "
        + "id INTEGER PRIMARY KEY, "
        + "id_producto INTEGER NOT NULL, "
        + "lote TEXT NOT NULL, "
        + "fecha_registro TEXT NOT NULL, "
        + "fecha_expedicion TEXT, "
        + "fecha_vencimiento TEXT NOT NULL, "
        + "cantidad_movimiento REAL NOT NULL, "
        + "valor_movimiento REAL, "
        + "motivo TEXT, "
        + "FOREIGN KEY (id_producto) REFERENCES producto(id) );"
    ;

    private static final String CONSULTA_INSERTAR = "INSERT INTO lote "
        + "(id_producto, "
        + "lote, "
        + "fecha_registro, "
        + "fecha_expedicion, "
        + "fecha_vencimiento, "
        + "cantidad_movimiento, "
        + "valor_movimiento, "
        + "motivo) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?);"
    ;

    // Read

    private static final String CONSULTA_TODO = "SELECT "
        + "l.id, "
        + "l.id_producto, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "l.lote, "
        + "l.fecha_registro, "
        + "l.fecha_expedicion, "
        + "l.fecha_vencimiento, "
        + "l.cantidad_movimiento, "
        + "l.valor_movimiento, "
        + "l.motivo "
        + "FROM lote l "
        + "INNER JOIN producto p "
        + "ON l.id_producto = p.id;"
    ;

    private static final String CONSULTA_LOTE = "SELECT "
        + "l.id, "
        + "l.id_producto, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "l.lote, "
        + "l.fecha_registro, "
        + "l.fecha_expedicion, "
        + "l.fecha_vencimiento, "
        + "l.cantidad_movimiento, "
        + "l.valor_movimiento, "
        + "l.motivo "
        + "FROM lote l "
        + "INNER JOIN producto p "
        + "ON l.id_producto = p.id "
        + "WHERE l.id = ?;"
    ;

    // Update

    private static final String CONSULTA_ACTUALIZAR = "UPDATE lote "
        + "SET id_producto = ?, "
        + "lote = ?, "
        + "fecha_registro = ?, "
        + "fecha_expedicion = ?, "
        + "fecha_vencimiento = ?, "
        + "cantidad_movimiento = ?, "
        + "valor_movimiento = ?, "
        + "motivo = ? "
        + "WHERE id = ?;"
    ;

    // Constructor

    public LoteRepositorio() {
        generarTabla();
    }

    // Metodos

    private Lote convertirLote(ResultSet conjuntoResultados) throws SQLException {
        Lote lote = new Lote();
        lote.colocarId(conjuntoResultados.getInt("id"));
        Producto producto = new Producto();
        producto.colocarId(conjuntoResultados.getInt("id_producto"));
        producto.colocarCodigoBarras(conjuntoResultados.getString("codigo_barras"));
        producto.colocarNombre(conjuntoResultados.getString("nombre_producto"));
        producto.colocarMarca(conjuntoResultados.getString("marca"));
        lote.colocarProducto(producto);
        lote.colocarLote(conjuntoResultados.getString("lote"));
        lote.colocarFechaRegistro(conjuntoResultados.getString("fecha_registro"));
        lote.colocarFechaExpedicion(conjuntoResultados.getString("fecha_expedicion"));
        lote.colocarFechaVencimiento(conjuntoResultados.getString("fecha_vencimiento"));
        lote.colocarCantidadMovimiento(conjuntoResultados.getDouble("cantidad_movimiento"));
        lote.colocarValorMovimiento((Double) conjuntoResultados.getObject("valor_movimiento"));
        lote.colocarMotivo(conjuntoResultados.getString("motivo"));
        return lote;
    }

    private void actualizarTablaLote(
        PreparedStatement sentenciaPreparada,
        Lote lote
    ) throws SQLException {
        sentenciaPreparada.setInt(1, lote.conseguirProducto().conseguirId());
        sentenciaPreparada.setString(2, lote.conseguirLote());
        sentenciaPreparada.setString(3, lote.conseguirFechaRegistro());
        sentenciaPreparada.setString(4, lote.conseguirFechaExpedicion());
        sentenciaPreparada.setString(5, lote.conseguirFechaVencimiento());
        sentenciaPreparada.setDouble(6, lote.conseguirCantidadMovimiento());
        sentenciaPreparada.setObject(7, lote.conseguirValorMovimiento());
        sentenciaPreparada.setString(8, lote.conseguirMotivo());
    }

    // Create
    private void generarTabla() {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement()
        ) {
            sentencia.execute(CREAR_TABLA);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la tabla lote ", e);
        }
    }

    public void agregarLote(Lote lote) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(CONSULTA_INSERTAR)
        ) {
            actualizarTablaLote(sentenciaPreparada, lote);
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            System.out.print("ERROR SQL: " + e);
            throw new RuntimeException("No se pudo agregar lote ", e);
        }
    }

    // Read

    public boolean existe(int id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM lote WHERE id = ?)";

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, id);

            try (ResultSet conjuntoResultados = sentencia.executeQuery()) {
                return conjuntoResultados.next()
                    && conjuntoResultados.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar si existe el lote", e);
        }
    }

    public Lote consultarLote(int id) {
        Lote lote = new Lote();

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(CONSULTA_LOTE)
        ) {
            sentenciaPreparada.setInt(1, id);

            try (ResultSet conjuntoResultados = sentenciaPreparada.executeQuery()) {
                if (conjuntoResultados.next()) {
                    lote = convertirLote(conjuntoResultados);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar lote ", e);
        }

        return lote;
    }

    public ObservableList<Lote> consultarTodo() {
        return consultar(CONSULTA_TODO);
    }

    public ObservableList<String> consultarColumnaId() {
        return consultarColumna(CAMPOS[0]);
    }

    public ObservableList<String> consultarColumnaIdProducto() {
        return consultarColumna(CAMPOS[1]);
    }

    public ObservableList<String> consultarColumnaLote() {
        return consultarColumna(CAMPOS[2]);
    }

    public ObservableList<String> consultarColumnaFechaRegistro() {
        return consultarColumna(CAMPOS[3]);
    }

    public ObservableList<String> consultarColumnaFechaExpedicion() {
        return consultarColumna(CAMPOS[4]);
    }

    public ObservableList<String> consultarColumnaFechaVencimiento() {
        return consultarColumna(CAMPOS[5]);
    }

    public ObservableList<String> consultarColumnaCantidadMovimiento() {
        return consultarColumna(CAMPOS[6]);
    }

    public ObservableList<String> consultarColumnaValorMovimiento() {
        return consultarColumna(CAMPOS[7]);
    }

    public ObservableList<String> consultarColumnaMotivo() {
        return consultarColumna(CAMPOS[8]);
    }

    private ObservableList<String> consultarColumna(String nombreColumna) {
        ObservableList<String> columna = FXCollections.observableArrayList();

        String consultaFinal = "SELECT "
            + nombreColumna
            + " FROM lote;";

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados = sentencia.executeQuery(consultaFinal)
        ) {
            while (conjuntoResultados.next()) {
                columna.add(conjuntoResultados.getString(nombreColumna));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar la columna " + nombreColumna, e);
        }

        return columna;
    }

    private ObservableList<Lote> consultar(String consultaFinal) {
        ObservableList<Lote> lotes = FXCollections.observableArrayList();

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados = sentencia.executeQuery(consultaFinal)
        ) {
            while (conjuntoResultados.next()) {
                lotes.add(convertirLote(conjuntoResultados));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar lotes ", e);
        }

        return lotes;
    }

    // Update

    public void editarLote(Lote lote) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(CONSULTA_ACTUALIZAR)
        ) {
            actualizarTablaLote(sentenciaPreparada, lote);
            sentenciaPreparada.setInt(9, lote.conseguirId());
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar lote ", e);
        }
    }

}