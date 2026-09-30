package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.gestion.model.Categoria;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class CategoriaRepositorio {

    private static final String[] CAMPOS = {
        "id",
        "nombre_categoria",
        "activo"
    };

    // Create
    private static final String CREAR_TABLA = "CREATE TABLE IF NOT EXISTS categoria ( "
        + "id INTEGER PRIMARY KEY, "
        + "nombre_categoria TEXT NOT NULL UNIQUE, "
        + "activo INTEGER NOT NULL DEFAULT 1 CHECK (activo IN (0, 1) ) );"
    ;
    private static final String CONSULTA_INSERTAR = "INSERT INTO categoria "
        + "(nombre_categoria) "
        + "VALUES (?);"
    ;

    // Read
    private static final String CONSULTA_TODO = "SELECT "
        + String.join(", ", CAMPOS)
        + " FROM categoria "
        + "WHERE activo = 1;"
    ;
    private static final String CONSULTA_CATEGORIA_ACTIVADA = "SELECT "
        + String.join(", ", CAMPOS)
        + " FROM categoria "
        + "WHERE activo = 1 AND id = ?;"
    ;
    private static final String CONSULTA_CATEGORIA_DESACTIVADA = "SELECT "
        + String.join(", ", CAMPOS)
        + " FROM categoria "
        + "WHERE activo = 0 AND id = ?;"
    ;

    // Update
    private static final String CONSULTA_ACTUALIZAR = "UPDATE categoria SET "
        + "nombre_categoria = ? "
        + "WHERE id = ?;"
    ;

    // Delete
    private static final String CONSULTA_BORRAR = "UPDATE categoria SET " 
        + "activo = 0 " 
        + "WHERE id = ?;" 
    ; 
    private static final String CONSULTA_BORRAR_FISICAMENTE = "DELETE FROM categoria " 
        + "WHERE id = ? " 
        + "AND NOT EXISTS ( " 
        + "SELECT 1 FROM producto " 
        + "WHERE id_categoria = ? " 
        + ");" 
    ;

    // Constructor
    public CategoriaRepositorio() {
        generarTabla();
        crearCategoriaInicial();
    }

    // Métodos

    // Create
    private void generarTabla() {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement()
        ) {
            sentencia.execute(CREAR_TABLA);
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo crear la tabla categoria ", e
            );
        }
    }

    private void crearCategoriaInicial(){
        int estaVacia;
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados = sentencia.executeQuery(
                "SELECT NOT EXISTS (SELECT 1 FROM categoria) AS esta_vacia"
            )
        ) {
            conjuntoResultados.next();
            estaVacia = conjuntoResultados.getInt("esta_vacia");
        } catch (SQLException e) {
            throw new RuntimeException("Error al consultar categorias ", e);
        }
        if (estaVacia == 1){
            Categoria categoriaInicial = new Categoria();
            categoriaInicial.colocarNombre("Otros");
            agregarCategoria(categoriaInicial);
        }
    }

    public void agregarCategoria(Categoria categoria) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada =
                conexion.prepareStatement(CONSULTA_INSERTAR)
        ) {
            sentenciaPreparada.setString(1, categoria.conseguirNombre());
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo agregar categoria ",
                e
            );
        }
    }

    // Read
    public static boolean existe(int id) {
        String sql = "SELECT EXISTS(SELECT 1 FROM categoria WHERE id = ?)";
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
            throw new RuntimeException(
                "Error al comprobar si existe la categoria",
                e
            );
        }
    }

    public static boolean estaActiva(int id) {
        String sql = "SELECT EXISTS("
            + "SELECT 1 FROM categoria "
            + "WHERE id = ? AND activo = 1"
            + ")";
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
            throw new RuntimeException(
                "Error al verificar si la categoria está activa",
                e
            );
        }
    }

    public Categoria consultarCategoriaDesactivada(int id) {
        return consultarCategoria(id, CONSULTA_CATEGORIA_DESACTIVADA);
    }

    public Categoria consultarCategoriaActivada(int id) {
        return consultarCategoria(id, CONSULTA_CATEGORIA_ACTIVADA);
    }

    public ObservableList<Categoria> consultarTodo() {
        return consultar(CONSULTA_TODO);
    }

    public ObservableList<String> consultarColumnaId() {
        return consultarColumna(CAMPOS[0]);
    }

    public ObservableList<String> consultarColumnaNombreCategoria() {
        return consultarColumna(CAMPOS[1]);
    }

    public ObservableList<String> consultarColumnaActivo() {
        return consultarColumna(CAMPOS[2]);
    }

    private ObservableList<String> consultarColumna(String nombreColumna) {
        ObservableList<String> columna = FXCollections.observableArrayList();
        String consultaFinal = "SELECT "
            + nombreColumna
            + " FROM categoria "
            + "WHERE activo = 1;"
        ;
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados = sentencia.executeQuery(consultaFinal)
        ) {
            while (conjuntoResultados.next()) {
                columna.add(
                    conjuntoResultados.getString(nombreColumna)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al consultar la columna " + nombreColumna,
                e
            );
        }
        return columna;
    }

    private Categoria consultarCategoria(int id, String consulta) {
        Categoria categoria = new Categoria();
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada =
                conexion.prepareStatement(consulta)
        ) {
            sentenciaPreparada.setInt(1, id);
            try (ResultSet conjuntoResultados =
                    sentenciaPreparada.executeQuery()) {
                if (conjuntoResultados.next()) {
                    categoria = convertirCategoria(conjuntoResultados);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al consultar categoria ",
                e
            );
        }
        return categoria;
    }

    private ObservableList<Categoria> consultar(String consultaFinal) {
        ObservableList<Categoria> categorias =
            FXCollections.observableArrayList();
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement();
            ResultSet conjuntoResultados =
                sentencia.executeQuery(consultaFinal)
        ) {
            while (conjuntoResultados.next()) {
                categorias.add(
                    convertirCategoria(conjuntoResultados)
                );
            }
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al consultar categorias ",
                e
            );
        }
        return categorias;
    }

    private Categoria convertirCategoria(ResultSet conjuntoResultados)
            throws SQLException {
        Categoria categoria = new Categoria();
        categoria.colocarId(conjuntoResultados.getInt("id"));
        categoria.colocarNombre(
            conjuntoResultados.getString("nombre_categoria")
        );
        categoria.colocarActivo(
            conjuntoResultados.getInt("activo")
        );
        return categoria;
    }

    // Update
    public void editarCategoria(Categoria categoria) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada =
                conexion.prepareStatement(CONSULTA_ACTUALIZAR)
        ) {
            sentenciaPreparada.setString(
                1,
                categoria.conseguirNombre()
            );
            sentenciaPreparada.setInt(
                2,
                categoria.conseguirId()
            );
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al editar categoria ",
                e
            );
        }
    }

    // Delete
    public void borrarCategoria(Integer id) {
        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada =
                conexion.prepareStatement(CONSULTA_BORRAR)
        ) {
            sentenciaPreparada.setInt(1, id);
            sentenciaPreparada.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(
                "Error al borrar categoria ",
                e
            );
        }
    }

    public boolean borrarCategoriaFisicamente(Integer id) { 
        try ( Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentenciaPreparada = 
                conexion.prepareStatement(CONSULTA_BORRAR_FISICAMENTE) 
        ) { 
            sentenciaPreparada.setInt(1, id);
            sentenciaPreparada.setInt(2, id); 
            return sentenciaPreparada.executeUpdate() > 0; 
        } catch (SQLException e) { 
            throw new RuntimeException( "Error al borrar fisicamente categoria ", e ); 
        } 
    }
}
