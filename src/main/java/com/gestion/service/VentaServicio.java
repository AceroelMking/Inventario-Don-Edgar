package com.gestion.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.gestion.model.DetalleVenta;
import com.gestion.model.Producto;
import com.gestion.model.Venta;
import com.gestion.repository.VentaRepositorio;

import javafx.collections.ObservableList;

public class VentaServicio {

    private final VentaRepositorio ventaRepositorio;

    public VentaServicio() {
        this.ventaRepositorio = new VentaRepositorio();
    }

    public int registrarVenta(
        Venta venta,
        ObservableList<DetalleVenta> detalles
    ) {

        validarVenta(venta, detalles);

        double total = calcularTotal(detalles);

        venta.colocarSubtotal(total);
        venta.colocarTotal(total);

        /*
         * Si la venta no tiene fecha,
         * se asigna la fecha y hora actual.
         */
        if (venta.conseguirFecha() == null
                || venta.conseguirFecha().isBlank()) {

            DateTimeFormatter formato =
                DateTimeFormatter.ofPattern(
                    "yyyy-MM-dd HH:mm:ss"
                );

            venta.colocarFecha(
                LocalDateTime.now().format(formato)
            );
        }

        /*
         * Obtener el monto pagado.
         */
        double montoPagado =
            venta.conseguirMontoPagado() == null
                ? 0.0
                : venta.conseguirMontoPagado();

        if (montoPagado < 0) {

            throw new IllegalArgumentException(
                "El monto pagado no puede ser negativo."
            );
        }

        if (montoPagado > total) {

            throw new IllegalArgumentException(
                "El monto pagado no puede ser mayor "
                + "al total de la venta."
            );
        }

        venta.colocarMontoPagado(
            montoPagado
        );

        /*
         * Calcular deuda pendiente.
         */
        double saldoPendiente =
            total - montoPagado;

        venta.colocarSaldoPendiente(
            saldoPendiente
        );

        /*
         * Si queda deuda, debe existir
         * un cliente asociado.
         */
        if (saldoPendiente > 0
                && venta.conseguirCliente() == null) {

            throw new IllegalArgumentException(
                "Debe seleccionar un cliente "
                + "cuando existe saldo pendiente."
            );
        }

        /*
         * VentaRepositorio se encarga de:
         *
         * 1. Registrar la venta.
         * 2. Registrar los detalles.
         * 3. Descontar las existencias.
         * 4. Actualizar el saldo del cliente.
         * 5. Confirmar la transacción.
         */
        int idVenta =
            ventaRepositorio.registrarVenta(
                venta,
                detalles
            );

        venta.colocarId(
            idVenta
        );

        return idVenta;
    }


    private void validarVenta(
        Venta venta,
        ObservableList<DetalleVenta> detalles
    ) {

        if (venta == null) {

            throw new IllegalArgumentException(
                "La venta no puede ser nula."
            );
        }

        if (detalles == null
                || detalles.isEmpty()) {

            throw new IllegalArgumentException(
                "La venta debe tener al menos "
                + "un producto."
            );
        }

        if (venta.conseguirMetodoPago() == null
                || venta.conseguirMetodoPago().isBlank()) {

            throw new IllegalArgumentException(
                "Debe seleccionar un método de pago."
            );
        }

        for (DetalleVenta detalle : detalles) {

            validarDetalle(
                detalle
            );
        }
    }


    private void validarDetalle(
        DetalleVenta detalle
    ) {

        if (detalle == null) {

            throw new IllegalArgumentException(
                "Existe un detalle de venta inválido."
            );
        }

        Producto producto =
            detalle.conseguirProducto();

        if (producto == null) {

            throw new IllegalArgumentException(
                "El producto del detalle es obligatorio."
            );
        }

        if (producto.conseguirId() == null
                || producto.conseguirId() <= 0) {

            throw new IllegalArgumentException(
                "El producto no tiene un ID válido."
            );
        }

        if (detalle.conseguirCantidad() == null
                || detalle.conseguirCantidad() <= 0) {

            throw new IllegalArgumentException(
                "La cantidad del producto "
                + producto.conseguirNombre()
                + " debe ser mayor que cero."
            );
        }

        if (detalle.conseguirPrecioUnitario() == null
                || detalle.conseguirPrecioUnitario() < 0) {

            throw new IllegalArgumentException(
                "El precio del producto "
                + producto.conseguirNombre()
                + " no es válido."
            );
        }

        /*
         * Esta validación sirve para dar una respuesta
         * rápida al usuario.
         *
         * VentaRepositorio volverá a comprobar el stock
         * directamente en la base de datos antes de
         * confirmar la venta.
         */
        Double existencias =
            producto.conseguirExistencias();

        if (existencias != null
                && detalle.conseguirCantidad()
                > existencias) {

            throw new IllegalArgumentException(
                "No hay existencias suficientes de "
                + producto.conseguirNombre()
                + ". Disponible: "
                + existencias
            );
        }

        double subtotal =
            detalle.conseguirCantidad()
            * detalle.conseguirPrecioUnitario();

        detalle.colocarSubtotal(
            subtotal
        );
    }


    public double calcularTotal(
        ObservableList<DetalleVenta> detalles
    ) {

        if (detalles == null) {
            return 0.0;
        }

        double total = 0.0;

        for (DetalleVenta detalle : detalles) {

            if (detalle != null
                    && detalle.conseguirCantidad() != null
                    && detalle.conseguirPrecioUnitario() != null) {

                double subtotal =
                    detalle.conseguirCantidad()
                    * detalle.conseguirPrecioUnitario();

                detalle.colocarSubtotal(
                    subtotal
                );

                total += subtotal;
            }
        }

        return total;
    }


    public ObservableList<Venta> consultarVentas() {

        return ventaRepositorio.consultarVentas();
    }
}