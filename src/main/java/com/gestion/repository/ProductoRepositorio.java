package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Categoria;
import com.gestion.model.Producto;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ProductoRepositorio {
    private static final String[] CAMPOS = {"id",
        "codigo_barras",
        "nombre_producto",
        "marca",
        "id_categoria",
        "cantidad_producto",
        "unidad_medida",
        "unidad_agrupada",
        "precio_venta",
        "existencias",
        "minimo_existencias",
        "activo" 
    };
    // Create
    private static final String CREAR_TABLA = "CREATE TABLE IF NOT EXISTS producto ( "
        + "id INTEGER PRIMARY KEY, "
        + "codigo_barras TEXT UNIQUE, "
        + "nombre_producto TEXT NOT NULL, "
        + "marca TEXT NOT NULL, "
        + "id_categoria INTEGER NOT NULL, "
        + "cantidad_producto REAL NOT NULL, "
        + "unidad_medida TEXT, "
        + "unidad_agrupada INTEGER, "
        + "precio_venta REAL NOT NULL, "
        + "existencias REAL NOT NULL, "
        + "minimo_existencias REAL NOT NULL, "
        + "activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1) ), "
        + "FOREIGN KEY (id_categoria) REFERENCES categoria(id), "
        + "FOREIGN KEY (unidad_agrupada) REFERENCES producto(id) );"
    ;
    private static final String CONSULTA_INSERTAR = "INSERT INTO producto "
        + "(codigo_barras, "
        + "nombre_producto, "
        + "marca, "
        + "id_categoria, "
        + "cantidad_producto, "
        + "unidad_medida, "
        + "unidad_agrupada, "
        + "precio_venta, "
        + "existencias, "
        + "minimo_existencias) "
        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);"
    ;
    private static final String CONSULTA_ACTIVAR = "UPDATE producto SET activo = 1 WHERE id = ?;";
    // Read
    private static final String CONSULTA_TODO = "SELECT "
        + "p.id, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "p.id_categoria, "
        + "c.nombre_categoria AS categoria, "
        + "p.cantidad_producto, "
        + "p.unidad_medida, "
        + "p.unidad_agrupada, "
        + "p.precio_venta, "
        + "p.existencias, "
        + "p.minimo_existencias, "
        + "agrupado.codigo_barras || ' - ' || "
        + "agrupado.nombre_producto || ' - ' || "
        + "agrupado.marca "
        + "AS producto_padre "
        + "FROM producto p "
        + "LEFT JOIN producto agrupado "
        + "ON p.unidad_agrupada = agrupado.id "
        + "LEFT JOIN categoria c "
        + "ON p.id_categoria = c.id "
        + "WHERE p.activo = 1;"
    ;
    private static final String CONSULTA_PRODUCTO_ACTIVADO = "SELECT "
        + "p.id, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "p.id_categoria, "
        + "c.nombre_categoria AS categoria, "
        + "p.cantidad_producto, "
        + "p.unidad_medida, "
        + "p.unidad_agrupada, "
        + "agrupado.codigo_barras || ' - ' || "
        + "agrupado.nombre_producto || ' - ' || "
        + "agrupado.marca AS producto_padre, "
        + "p.precio_venta, "
        + "p.existencias, "
        + "p.minimo_existencias "
        + "FROM producto p "
        + "LEFT JOIN categoria c "
        + "ON p.id_categoria = c.id "
        + "LEFT JOIN producto agrupado "
        + "ON p.unidad_agrupada = agrupado.id "
        + "WHERE p.activo = 1 AND p.id = ?;"
    ;
    private static final String CONSULTA_PRODUCTO_DESACTIVADO = "SELECT "
        + "p.id, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "p.id_categoria, "
        + "c.nombre_categoria AS categoria, "
        + "p.cantidad_producto, "
        + "p.unidad_medida, "
        + "p.unidad_agrupada, "
        + "agrupado.codigo_barras || ' - ' || "
        + "agrupado.nombre_producto || ' - ' || "
        + "agrupado.marca AS producto_padre, "
        + "p.precio_venta, "
        + "p.existencias, "
        + "p.minimo_existencias "
        + "FROM producto p "
        + "LEFT JOIN categoria c "
        + "ON p.id_categoria = c.id "
        + "LEFT JOIN producto agrupado "
        + "ON p.unidad_agrupada = agrupado.id "
        + "WHERE p.activo = 0 AND p.id = ?;"
    ;
    private static final String CONSULTA_PRODUCTO_CODIGO_BARRAS = "SELECT "
        + "p.id, "
        + "p.codigo_barras, "
        + "p.nombre_producto, "
        + "p.marca, "
        + "p.id_categoria, "
        + "c.nombre_categoria AS categoria, "
        + "p.cantidad_producto, "
        + "p.unidad_medida, "
        + "p.unidad_agrupada, "
        + "agrupado.codigo_barras || ' - ' || "
        + "agrupado.nombre_producto || ' - ' || "
        + "agrupado.marca AS producto_padre, "
        + "p.precio_venta, "
        + "p.existencias, "
        + "p.minimo_existencias "
        + "FROM producto p "
        + "LEFT JOIN categoria c "
        + "ON p.id_categoria = c.id "
        + "LEFT JOIN producto agrupado "
        + "ON p.unidad_agrupada = agrupado.id "
        + "WHERE p.codigo_barras = ?;"
    ;
    // Update
    private static final String CONSULTA_ACTUALIZAR = "UPDATE producto " 
        + "SET codigo_barras = ?, "
        + "nombre_producto = ?, "
        + "marca = ?,"
        + "id_categoria = ?, "
        + "cantidad_producto = ?, "
        + "unidad_medida = ?, "
        + "unidad_agrupada = ?, "
        + "precio_venta = ?, "
        + "existencias = ?, " 
        + "minimo_existencias = ? " 
        + "WHERE id = ?;"
    ; 
    // Delete    
    private static final String CONSULTA_DESACTIVAR = "UPDATE producto SET activo = 0 WHERE id = ?;";
    
    // Constructor
    public ProductoRepositorio(){
        generarTabla();
    }

    // Metodos
    private Producto convertirProducto(ResultSet conjuntoResultados) throws SQLException {
        Producto producto = new Producto();
        producto.colocarId(conjuntoResultados.getInt("id"));
        producto.colocarCodigoBarras(conjuntoResultados.getString("codigo_barras"));
        producto.colocarNombre(conjuntoResultados.getString("nombre_producto"));
        producto.colocarMarca(conjuntoResultados.getString("marca"));
        Categoria categoria = new Categoria();
        categoria.colocarId(conjuntoResultados.getInt("id_categoria"));
        categoria.colocarNombre(conjuntoResultados.getString("categoria"));
        producto.colocarCategoria(categoria);
        producto.colocarCantidadProducto(conjuntoResultados.getDouble("cantidad_producto"));
        producto.colocarUnidadMedida(conjuntoResultados.getString("unidad_medida"));
        producto.colocarUnidadAgrupada((Integer) conjuntoResultados.getObject("unidad_agrupada"));
        producto.colocarProductoPadre(
            conjuntoResultados.getString("producto_padre")
        );
        producto.colocarPrecio(conjuntoResultados.getDouble("precio_venta"));
        producto.colocarExistencias(conjuntoResultados.getDouble("existencias"));
        producto.colocarMinimoExistencias(conjuntoResultados.getDouble("minimo_existencias"));
        return producto;
    }

    private void actualizarTablaProducto(PreparedStatement sentenciaPreparada, 
        Producto producto
    ) throws SQLException {
        sentenciaPreparada.setString(1, producto.conseguirCodigoBarras());
        sentenciaPreparada.setString(2, producto.conseguirNombre());
        sentenciaPreparada.setString(3, producto.conseguirMarca());
        sentenciaPreparada.setInt(4, producto.conseguirCategoria().conseguirId());
        sentenciaPreparada.setObject(5, producto.conseguirCantidadProducto());
        sentenciaPreparada.setString(6, producto.conseguirUnidadMedida());
        sentenciaPreparada.setObject(7, producto.conseguirUnidadAgrupada());
        sentenciaPreparada.setDouble(8, producto.conseguirPrecio());
        sentenciaPreparada.setDouble(9, producto.conseguirExistencias());
        sentenciaPreparada.setDouble(10, producto.conseguirMinimoExistencias());
    }
    // Create P
    private void generarTabla(){
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement()) {
            sentencia.execute(CREAR_TABLA);
        } catch (SQLException e) {
            throw new RuntimeException("No se pudo crear la tabla producto ", e);
        }
    }

    public void agregarProducto(Producto producto) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(CONSULTA_INSERTAR)
        ) {      
            actualizarTablaProducto(sentenciaPreparada,producto);
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            System.out.print("ERROR SQL: " + e);
            throw new RuntimeException("No se pudo agregar producto ", e);
        }
    }

    public void activarProducto(int id){
        modificarActivoProducto(id, CONSULTA_ACTIVAR);
    }

    // Read    
    public boolean existe(int id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM producto WHERE id = ?)";
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, id);
            try (ResultSet conjuntoResultados = sentencia.executeQuery()) {
                return conjuntoResultados.next() && conjuntoResultados.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al comprobar si existe el producto", e);
        }
    }

    public boolean estaActivo(int id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM producto WHERE id = ? AND activo = 1)";
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia = conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, id);
            try (ResultSet conjuntoResultados = sentencia.executeQuery()) {
                return conjuntoResultados.next() && conjuntoResultados.getInt(1) == 1;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al verificar si el producto está activo", e);
        }
    }

    //UNICOS
    public Producto consultarCodigoBarrasUnico(int codigoBarras) {
        return consultarProducto(codigoBarras, CONSULTA_PRODUCTO_CODIGO_BARRAS);
    }

    public Producto consultarProductoDesactivado(int id) {
        return consultarProducto(id, CONSULTA_PRODUCTO_DESACTIVADO);
    }

    public Producto consultarProductoActivado(int id) {
        return consultarProducto(id, CONSULTA_PRODUCTO_ACTIVADO);
    }

    public ObservableList<Producto> consultarTodo() {
        return consultar(CONSULTA_TODO);
    }

    public ObservableList<String> consultarColumnaId(){
        return consultarColumna(CAMPOS[0]);
    }

    public ObservableList<String> consultarColumnaCodigoBarras(){
        return consultarColumna(CAMPOS[1]);
    }

    public ObservableList<String> consultarColumnaNombreProducto(){
        return consultarColumna(CAMPOS[2]);
    }

    public ObservableList<String> consultarColumnaMarca(){
        return consultarColumna(CAMPOS[3]);
    }

    public ObservableList<Integer> consultarColumnaCategoria() {
        ObservableList<String> categoriasString = consultarColumna(CAMPOS[4]);
        ObservableList<Integer> categorias = FXCollections.observableArrayList();
        for (String categoria : categoriasString) {
            categorias.add(Integer.parseInt(categoria));
        }
        return categorias;
    }

    public ObservableList<String> consultarColumnaCantidadProducto(){
        return consultarColumna(CAMPOS[5]);
    }

    public ObservableList<String> consultarColumnaUnidadMedida(){
        return consultarColumna(CAMPOS[6]);
    }

    public ObservableList<String> consultarColumnaUnidadAgrupada(){
        return consultarColumna(CAMPOS[7]);
    }

    public ObservableList<String> consultarColumnaPrecio(){
        return consultarColumna(CAMPOS[8]);
    }

    public ObservableList<String> consultarColumnaExistencias(){
        return consultarColumna(CAMPOS[9]);
    }

    public ObservableList<String> consultarColumnaMinimoExistencias(){
        return consultarColumna(CAMPOS[10]);
    }

    private ObservableList<String> consultarColumna(String nombreColumna){
        ObservableList<String> columna = FXCollections.observableArrayList();
        String consultaFinal = "SELECT " 
            + nombreColumna
            + " FROM producto WHERE activo = 1; "
        ;
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

    private Producto consultarProducto(int id, String consulta ){
        Producto producto = new Producto();
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(consulta)
        ) {
            sentenciaPreparada.setInt(1, id);
            try (ResultSet conjuntoResultados = sentenciaPreparada.executeQuery()) {
                if (conjuntoResultados.next()) {
                    producto = convertirProducto(conjuntoResultados);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar producto ", e);
        }
        return producto;
    }

    private ObservableList<Producto> consultar(String consultaFinal){
        ObservableList<Producto> productos = FXCollections.observableArrayList();
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados = sentencia.executeQuery(consultaFinal)
        ) {
            while (conjuntoResultados.next()) {
                productos.add(convertirProducto(conjuntoResultados));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar productos ", e);
        }
        return productos;
    }

    // Update
    public void editarProducto(Producto producto) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(CONSULTA_ACTUALIZAR)
        ) {
            actualizarTablaProducto(sentenciaPreparada,producto);
            sentenciaPreparada.setInt(11, producto.conseguirId());  
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al editar producto ", e);
        }
    }

    // Delete
    public void borrarProducto(Integer id) {
        modificarActivoProducto(id, CONSULTA_DESACTIVAR);
    }

    public void modificarActivoProducto(Integer id, String consulta) {
        try ( 
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = conexion.prepareStatement(consulta)
        ) { 
            sentenciaPreparada.setInt(1, id); 
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error al borrar producto ", e);
        }
    }
}