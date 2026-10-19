
package com.gestion.repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;

import com.gestion.model.Usuario;
import com.gestion.service.ContrasenaServicio;

public class UsuarioRepositorio {

    // =====================================================
    // CREACIÓN DE LA TABLA
    // =====================================================

    private static final String CREAR_TABLA = """
        CREATE TABLE IF NOT EXISTS usuario (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            nombre_usuario TEXT NOT NULL UNIQUE,
            contrasena_hash TEXT NOT NULL,
            correo_electronico TEXT,
            intentos_fallidos INTEGER NOT NULL DEFAULT 0,
            bloqueado_hasta TEXT,
            codigo_recuperacion_hash TEXT,
            codigo_recuperacion_expira TEXT,
            clave_recuperacion_hash TEXT,
            intentos_recuperacion_fallidos INTEGER NOT NULL DEFAULT 0,
            recuperacion_bloqueada_hasta TEXT
        )
        """;

    private final ContrasenaServicio contrasenaServicio =
        new ContrasenaServicio();

    // =====================================================
    // CONSTRUCTOR
    // =====================================================

    public UsuarioRepositorio() {
        crearTabla();
        actualizarEstructuraTabla();
        crearAdministradorInicial();
    }

    // =====================================================
    // CREAR TABLA
    // =====================================================

    private void crearTabla() {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            Statement sentencia = conexion.createStatement()
        ) {
            sentencia.execute(CREAR_TABLA);

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo crear la tabla de usuarios.", e
            );
        }
    }

    // =====================================================
    // MIGRACIÓN DE LA BASE DE DATOS
    // =====================================================

    private void actualizarEstructuraTabla() {

        try (Connection conexion = ConexionBaseDatos.conectar()) {

            agregarColumnaSiNoExiste(
                conexion, "correo_electronico", "TEXT"
            );

            agregarColumnaSiNoExiste(
                conexion, "intentos_fallidos",
                "INTEGER NOT NULL DEFAULT 0"
            );

            agregarColumnaSiNoExiste(
                conexion, "bloqueado_hasta", "TEXT"
            );

            // Columnas anteriores, conservadas por compatibilidad.
            agregarColumnaSiNoExiste(
                conexion, "codigo_recuperacion_hash", "TEXT"
            );

            agregarColumnaSiNoExiste(
                conexion, "codigo_recuperacion_expira", "TEXT"
            );

            // Nuevas columnas de recuperación local.
            agregarColumnaSiNoExiste(
                conexion, "clave_recuperacion_hash", "TEXT"
            );

            agregarColumnaSiNoExiste(
                conexion, "intentos_recuperacion_fallidos",
                "INTEGER NOT NULL DEFAULT 0"
            );

            agregarColumnaSiNoExiste(
                conexion, "recuperacion_bloqueada_hasta", "TEXT"
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo actualizar la tabla usuario.", e
            );
        }
    }

    private void agregarColumnaSiNoExiste(
            Connection conexion,
            String nombreColumna,
            String definicion) throws SQLException {

        boolean existe = false;

        try (
            Statement sentencia = conexion.createStatement();
            ResultSet resultado = sentencia.executeQuery(
                "PRAGMA table_info(usuario)"
            )
        ) {
            while (resultado.next()) {
                if (nombreColumna.equalsIgnoreCase(
                        resultado.getString("name"))) {
                    existe = true;
                    break;
                }
            }
        }

        if (!existe) {
            try (Statement sentencia = conexion.createStatement()) {
                sentencia.execute(
                    "ALTER TABLE usuario ADD COLUMN "
                    + nombreColumna + " " + definicion
                );
            }
        }
    }

    // =====================================================
    // CREAR ADMINISTRADOR INICIAL
    // =====================================================

    private void crearAdministradorInicial() {

        try (Connection conexion = ConexionBaseDatos.conectar()) {

            int cantidad;

            try (
                Statement sentencia = conexion.createStatement();
                ResultSet resultado = sentencia.executeQuery(
                    "SELECT COUNT(*) FROM usuario"
                )
            ) {
                cantidad = resultado.next()
                    ? resultado.getInt(1) : 0;
            }

            if (cantidad > 0) {
                return;
            }

            String hash = contrasenaServicio.generarHash("admin");

            try (
                PreparedStatement sentencia =
                    conexion.prepareStatement("""
                        INSERT INTO usuario (
                            nombre_usuario, contrasena_hash
                        ) VALUES (?, ?)
                        """)
            ) {
                sentencia.setString(1, "admin");
                sentencia.setString(2, hash);
                sentencia.executeUpdate();
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo crear el administrador inicial.", e
            );
        }
    }

    // =====================================================
    // CONSULTAR POR NOMBRE
    // =====================================================

    public Usuario consultarPorNombre(String nombreUsuario) {

        return consultarUno(
            "SELECT * FROM usuario WHERE nombre_usuario = ? LIMIT 1",
            nombreUsuario
        );
    }

    // =====================================================
    // CONSULTAR ADMINISTRADOR
    // =====================================================

    public Usuario consultarAdministrador() {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement("""
                    SELECT * FROM usuario
                    ORDER BY id
                    LIMIT 1
                    """);
            ResultSet resultado = sentencia.executeQuery()
        ) {
            return resultado.next()
                ? convertirUsuario(resultado)
                : null;

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo consultar el administrador.", e
            );
        }
    }

    // =====================================================
    // CONSULTAR POR CORREO
    // =====================================================

    public Usuario consultarPorCorreo(String correoElectronico) {

        if (correoElectronico == null
                || correoElectronico.isBlank()) {
            return null;
        }

        return consultarUno(
            """
            SELECT * FROM usuario
            WHERE LOWER(correo_electronico) = LOWER(?)
            LIMIT 1
            """,
            correoElectronico.trim()
        );
    }

    private Usuario consultarUno(String sql, String parametro) {

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, parametro);

            try (ResultSet resultado = sentencia.executeQuery()) {
                return resultado.next()
                    ? convertirUsuario(resultado)
                    : null;
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo consultar el usuario.", e
            );
        }
    }

    // =====================================================
    // ACTUALIZAR CREDENCIALES
    // =====================================================

    public void actualizarCredenciales(Usuario usuario) {

        String sql = """
            UPDATE usuario
            SET nombre_usuario = ?, contrasena_hash = ?
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(
                1, usuario.conseguirNombreUsuario()
            );
            sentencia.setString(
                2, usuario.conseguirContrasena()
            );
            sentencia.setInt(3, usuario.conseguirId());

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudieron actualizar las credenciales.", e
            );
        }
    }

    // =====================================================
    // ACTUALIZAR CORREO
    // =====================================================

    public void actualizarCorreo(
            int idUsuario,
            String correoElectronico) {

        String sql = """
            UPDATE usuario SET correo_electronico = ?
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, correoElectronico);
            sentencia.setInt(2, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo actualizar el correo.", e
            );
        }
    }

    // =====================================================
    // BLOQUEO DEL INICIO DE SESIÓN
    // =====================================================

    public void actualizarBloqueo(
            int idUsuario,
            int intentosFallidos,
            LocalDateTime bloqueadoHasta) {

        String sql = """
            UPDATE usuario
            SET intentos_fallidos = ?,
                bloqueado_hasta = ?
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, intentosFallidos);
            colocarFecha(sentencia, 2, bloqueadoHasta);
            sentencia.setInt(3, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo actualizar el bloqueo.", e
            );
        }
    }

    public void reiniciarIntentosFallidos(int idUsuario) {

        String sql = """
            UPDATE usuario
            SET intentos_fallidos = 0,
                bloqueado_hasta = NULL
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudieron reiniciar los intentos.", e
            );
        }
    }

    // =====================================================
    // COMPATIBILIDAD CON RECUPERACIÓN ANTERIOR
    // =====================================================

    public void guardarCodigoRecuperacion(
            int idUsuario,
            String codigoHash,
            LocalDateTime fechaExpiracion) {

        String sql = """
            UPDATE usuario
            SET codigo_recuperacion_hash = ?,
                codigo_recuperacion_expira = ?
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, codigoHash);
            colocarFecha(sentencia, 2, fechaExpiracion);
            sentencia.setInt(3, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo guardar el código anterior.", e
            );
        }
    }

    public void limpiarCodigoRecuperacion(int idUsuario) {

        String sql = """
            UPDATE usuario
            SET codigo_recuperacion_hash = NULL,
                codigo_recuperacion_expira = NULL
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo limpiar el código anterior.", e
            );
        }
    }

    public boolean cambiarContrasenaPorRecuperacion(
            int idUsuario,
            String nuevaContrasenaHash,
            String codigoRecuperacionHash) {

        String sql = """
            UPDATE usuario
            SET contrasena_hash = ?,
                codigo_recuperacion_hash = NULL,
                codigo_recuperacion_expira = NULL,
                intentos_fallidos = 0,
                bloqueado_hasta = NULL
            WHERE id = ?
              AND codigo_recuperacion_hash = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nuevaContrasenaHash);
            sentencia.setInt(2, idUsuario);
            sentencia.setString(3, codigoRecuperacionHash);

            return sentencia.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo cambiar la contraseña.", e
            );
        }
    }

    public void actualizarContrasenaRecuperada(
            int idUsuario,
            String nuevaContrasenaHash) {

        String sql = """
            UPDATE usuario
            SET contrasena_hash = ?,
                intentos_fallidos = 0,
                bloqueado_hasta = NULL,
                codigo_recuperacion_hash = NULL,
                codigo_recuperacion_expira = NULL
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nuevaContrasenaHash);
            sentencia.setInt(2, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo actualizar la contraseña.", e
            );
        }
    }

    // =====================================================
    // GUARDAR HASH DE CLAVE DE EMERGENCIA
    // =====================================================

    public void guardarClaveRecuperacion(
            int idUsuario,
            String hash) {

        if (hash == null || hash.isBlank()) {
            throw new IllegalArgumentException(
                "El hash de recuperación es obligatorio."
            );
        }

        String sql = """
            UPDATE usuario
            SET clave_recuperacion_hash = ?,
                intentos_recuperacion_fallidos = 0,
                recuperacion_bloqueada_hasta = NULL
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, hash);
            sentencia.setInt(2, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo guardar la clave de emergencia.", e
            );
        }
    }

    // =====================================================
    // BLOQUEO DE RECUPERACIÓN
    // =====================================================

    public void actualizarBloqueoRecuperacion(
            int idUsuario,
            int intentosFallidos,
            LocalDateTime bloqueadoHasta) {

        String sql = """
            UPDATE usuario
            SET intentos_recuperacion_fallidos = ?,
                recuperacion_bloqueada_hasta = ?
            WHERE id = ?
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setInt(1, intentosFallidos);
            colocarFecha(sentencia, 2, bloqueadoHasta);
            sentencia.setInt(3, idUsuario);

            verificarActualizacion(
                sentencia, "No se encontró el usuario."
            );

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo actualizar el bloqueo de recuperación.",
                e
            );
        }
    }

    // =====================================================
    // RESTABLECER CONTRASEÑA CON CLAVE DE EMERGENCIA
    // =====================================================

    public boolean restablecerConClaveEmergencia(
            int idUsuario,
            String hashActual,
            String nuevaContrasenaHash) {

        if (hashActual == null || hashActual.isBlank()
                || nuevaContrasenaHash == null
                || nuevaContrasenaHash.isBlank()) {
            throw new IllegalArgumentException(
                "Los hashes son obligatorios."
            );
        }

        String sql = """
            UPDATE usuario
            SET contrasena_hash = ?,
                clave_recuperacion_hash = NULL,
                intentos_fallidos = 0,
                bloqueado_hasta = NULL,
                intentos_recuperacion_fallidos = 0,
                recuperacion_bloqueada_hasta = NULL,
                codigo_recuperacion_hash = NULL,
                codigo_recuperacion_expira = NULL
            WHERE id = ?
              AND clave_recuperacion_hash = ?
              AND (
                  recuperacion_bloqueada_hasta IS NULL
                  OR recuperacion_bloqueada_hasta <= ?
              )
            """;

        try (
            Connection conexion = ConexionBaseDatos.conectar();
            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {
            sentencia.setString(1, nuevaContrasenaHash);
            sentencia.setInt(2, idUsuario);
            sentencia.setString(3, hashActual);
            sentencia.setString(4, LocalDateTime.now().toString());

            return sentencia.executeUpdate() == 1;

        } catch (SQLException e) {
            throw new RuntimeException(
                "No se pudo restablecer la contraseña.", e
            );
        }
    }

    // =====================================================
    // CONVERTIR RESULTADO SQL A USUARIO
    // =====================================================

    private Usuario convertirUsuario(
            ResultSet resultado) throws SQLException {

        Usuario usuario = new Usuario();

        usuario.colocarId(resultado.getInt("id"));

        usuario.colocarNombreUsuario(
            resultado.getString("nombre_usuario")
        );

        usuario.colocarContrasena(
            resultado.getString("contrasena_hash")
        );

        usuario.colocarCorreoElectronico(
            resultado.getString("correo_electronico")
        );

        usuario.colocarIntentosFallidos(
            resultado.getInt("intentos_fallidos")
        );

        usuario.colocarBloqueadoHasta(
            convertirFecha(
                resultado.getString("bloqueado_hasta")
            )
        );

        usuario.colocarCodigoRecuperacionHash(
            resultado.getString("codigo_recuperacion_hash")
        );

        usuario.colocarCodigoRecuperacionExpira(
            convertirFecha(
                resultado.getString("codigo_recuperacion_expira")
            )
        );

        usuario.colocarClaveRecuperacionHash(
            resultado.getString("clave_recuperacion_hash")
        );

        usuario.colocarIntentosRecuperacionFallidos(
            resultado.getInt("intentos_recuperacion_fallidos")
        );

        usuario.colocarRecuperacionBloqueadaHasta(
            convertirFecha(
                resultado.getString("recuperacion_bloqueada_hasta")
            )
        );

        return usuario;
    }

    // =====================================================
    // CONVERTIR FECHAS
    // =====================================================

    private LocalDateTime convertirFecha(String fecha) {

        if (fecha == null || fecha.isBlank()) {
            return null;
        }

        return LocalDateTime.parse(fecha);
    }

    private void colocarFecha(
            PreparedStatement sentencia,
            int posicion,
            LocalDateTime fecha) throws SQLException {

        if (fecha == null) {
            sentencia.setNull(posicion, Types.VARCHAR);
        } else {
            sentencia.setString(posicion, fecha.toString());
        }
    }

    // =====================================================
    // VERIFICAR ACTUALIZACIÓN
    // =====================================================

    private void verificarActualizacion(
            PreparedStatement sentencia,
            String mensaje) throws SQLException {

        if (sentencia.executeUpdate() != 1) {
            throw new IllegalStateException(mensaje);
        }
    }


/**
 * Restablece la contraseña del administrador y renueva
 * su clave de emergencia mediante una única operación
 * atómica en SQLite.
 *
 * La clave anterior debe seguir vigente para que
 * la actualización se realice.
 *
 * @param idUsuario Identificador del administrador.
 * @param hashClaveAnterior Hash de la clave utilizada.
 * @param hashNuevaContrasena Hash de la nueva contraseña.
 * @param hashNuevaClave Hash de la nueva clave de emergencia.
 * @return true si se actualizó exactamente un usuario.
*/

/**
 * Restablece la contraseña del administrador
 * y renueva su clave de emergencia.
 *
 * Ambas modificaciones se realizan mediante
 * una única sentencia UPDATE de SQLite.
 */
    public boolean restablecerYRotarClaveEmergencia(
            int idUsuario,
            String hashClaveAnterior,
            String hashNuevaContrasena,
            String hashNuevaClave) {

        if (idUsuario <= 0
                || hashClaveAnterior == null
                || hashClaveAnterior.isBlank()
                || hashNuevaContrasena == null
                || hashNuevaContrasena.isBlank()
                || hashNuevaClave == null
                || hashNuevaClave.isBlank()) {

            throw new IllegalArgumentException(
                "Los datos para restablecer la contraseña "
                + "no son válidos."
            );
        }

        String sql = """
            UPDATE usuario
            SET
                contrasena_hash = ?,
                clave_recuperacion_hash = ?,
                intentos_recuperacion_fallidos = 0,
                recuperacion_bloqueada_hasta = NULL,
                intentos_fallidos = 0,
                bloqueado_hasta = NULL,
                codigo_recuperacion_hash = NULL,
                codigo_recuperacion_expira = NULL
            WHERE
                id = ?
                AND clave_recuperacion_hash = ?
                AND (
                    recuperacion_bloqueada_hasta IS NULL
                    OR recuperacion_bloqueada_hasta <= ?
                )
            """;

        try (
            Connection conexion =
                ConexionBaseDatos.conectar();

            PreparedStatement sentencia =
                conexion.prepareStatement(sql)
        ) {

            sentencia.setString(
                1,
                hashNuevaContrasena
            );

            sentencia.setString(
                2,
                hashNuevaClave
            );

            sentencia.setInt(
                3,
                idUsuario
            );

            sentencia.setString(
                4,
                hashClaveAnterior
            );

            sentencia.setString(
                5,
                LocalDateTime.now().toString()
            );

            int filasActualizadas =
                sentencia.executeUpdate();

            return filasActualizadas == 1;

        } catch (SQLException e) {

            throw new IllegalStateException(
                "No se pudo restablecer la contraseña "
                + "y renovar la clave de emergencia.",
                e
            );
        }
    }


}
