
package com.gestion.model;

import java.time.LocalDateTime;

public class Usuario {

    private Integer id;
    private Persona persona = new Persona();

    private String nombreUsuario;
    private String correoElectronico;
    private String telefono;
    private String contrasena;

    // Seguridad del inicio de sesión
    private int intentosFallidos;
    private LocalDateTime bloqueadoHasta;

    // Clave de recuperación de emergencia
    private String claveRecuperacionHash;
    private int intentosRecuperacionFallidos;
    private LocalDateTime recuperacionBloqueadaHasta;

    // Campos antiguos: compatibilidad temporal
    private String codigoRecuperacionHash;
    private LocalDateTime codigoRecuperacionExpira;

    public Usuario() {
        this.intentosFallidos = 0;
        this.intentosRecuperacionFallidos = 0;
    }

    // GETTERS

    public Integer conseguirId() {
        return id;
    }

    public Persona conseguirPersona() {
        return persona;
    }

    public String conseguirNombreUsuario() {
        return nombreUsuario;
    }

    public String conseguirCorreoElectronico() {
        return correoElectronico;
    }

    public String conseguirTelefono() {
        return telefono;
    }

    public String conseguirContrasena() {
        return contrasena;
    }

    public int conseguirIntentosFallidos() {
        return intentosFallidos;
    }

    public LocalDateTime conseguirBloqueadoHasta() {
        return bloqueadoHasta;
    }

    public String conseguirClaveRecuperacionHash() {
        return claveRecuperacionHash;
    }

    public int conseguirIntentosRecuperacionFallidos() {
        return intentosRecuperacionFallidos;
    }

    public LocalDateTime conseguirRecuperacionBloqueadaHasta() {
        return recuperacionBloqueadaHasta;
    }

    public String conseguirCodigoRecuperacionHash() {
        return codigoRecuperacionHash;
    }

    public LocalDateTime conseguirCodigoRecuperacionExpira() {
        return codigoRecuperacionExpira;
    }

    // SETTERS

    public void colocarId(Integer id) {
        this.id = id;
    }

    public void colocarNombre(Persona persona) {
        this.persona = persona;
    }

    public void colocarNombreUsuario(String nombreUsuario) {
        this.nombreUsuario = nombreUsuario;
    }

    public void colocarCorreoElectronico(String correoElectronico) {
        this.correoElectronico = correoElectronico;
    }

    public void colocarTelefono(String telefono) {
        this.telefono = telefono;
    }

    public void colocarContrasena(String contrasena) {
        this.contrasena = contrasena;
    }

    public void colocarIntentosFallidos(int intentosFallidos) {
        if (intentosFallidos < 0) {
            throw new IllegalArgumentException(
                "Los intentos fallidos no pueden ser negativos."
            );
        }

        this.intentosFallidos = intentosFallidos;
    }

    public void colocarBloqueadoHasta(
            LocalDateTime bloqueadoHasta) {
        this.bloqueadoHasta = bloqueadoHasta;
    }

    public void colocarClaveRecuperacionHash(
            String claveRecuperacionHash) {
        this.claveRecuperacionHash = claveRecuperacionHash;
    }

    public void colocarIntentosRecuperacionFallidos(
            int intentos) {
        if (intentos < 0) {
            throw new IllegalArgumentException(
                "Los intentos de recuperación no pueden ser negativos."
            );
        }

        this.intentosRecuperacionFallidos = intentos;
    }

    public void colocarRecuperacionBloqueadaHasta(
            LocalDateTime fecha) {
        this.recuperacionBloqueadaHasta = fecha;
    }

    // Compatibilidad con el mecanismo anterior

    public void colocarCodigoRecuperacionHash(String hash) {
        this.codigoRecuperacionHash = hash;
    }

    public void colocarCodigoRecuperacionExpira(
            LocalDateTime fecha) {
        this.codigoRecuperacionExpira = fecha;
    }

    // SEGURIDAD

    public boolean estaBloqueado() {
        return bloqueadoHasta != null
            && LocalDateTime.now().isBefore(bloqueadoHasta);
    }

    public boolean estaRecuperacionBloqueada() {
        return recuperacionBloqueadaHasta != null
            && LocalDateTime.now().isBefore(
                recuperacionBloqueadaHasta
            );
    }

    public int conseguirIntentosRestantes() {
        return Math.max(0, 5 - intentosFallidos);
    }

    public int conseguirIntentosRecuperacionRestantes() {
        return Math.max(
            0,
            5 - intentosRecuperacionFallidos
        );
    }

    public void reiniciarIntentosFallidos() {
        this.intentosFallidos = 0;
        this.bloqueadoHasta = null;
    }

    public void reiniciarIntentosRecuperacion() {
        this.intentosRecuperacionFallidos = 0;
        this.recuperacionBloqueadaHasta = null;
    }

    public void limpiarCodigoRecuperacion() {
        this.codigoRecuperacionHash = null;
        this.codigoRecuperacionExpira = null;
    }
}
