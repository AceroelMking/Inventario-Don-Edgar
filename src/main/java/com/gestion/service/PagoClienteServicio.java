package com.gestion.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.gestion.model.Cliente;
import com.gestion.model.PagoCliente;
import com.gestion.repository.PagoClienteRepositorio;

import javafx.collections.ObservableList;

public class PagoClienteServicio {

    private final PagoClienteRepositorio pagoRepositorio;

    public PagoClienteServicio() {

        this.pagoRepositorio =
            new PagoClienteRepositorio();
    }


    public int registrarPago(
        Cliente cliente,
        double monto,
        String observacion
    ) {

        /*
         * Validar que exista un cliente.
         */
        if (cliente == null) {

            throw new IllegalArgumentException(
                "Debe seleccionar un cliente."
            );
        }


        /*
         * conseguirId() devuelve int,
         * por eso no se compara con null.
         */
        if (cliente.conseguirId() <= 0) {

            throw new IllegalArgumentException(
                "El cliente seleccionado no es válido."
            );
        }


        /*
         * El abono debe ser positivo.
         */
        if (monto <= 0) {

            throw new IllegalArgumentException(
                "El monto del abono debe ser mayor que cero."
            );
        }


        double saldoActual =
            cliente.conseguirSaldo() == null
                ? 0.0
                : cliente.conseguirSaldo();


        /*
         * No permitir abonos si no existe deuda.
         */
        if (saldoActual <= 0) {

            throw new IllegalArgumentException(
                "El cliente no tiene saldo pendiente."
            );
        }


        /*
         * No permitir pagar más de lo que debe.
         */
        if (monto > saldoActual) {

            throw new IllegalArgumentException(
                "El abono no puede ser mayor "
                + "al saldo pendiente.\n"
                + "Saldo actual: $"
                + String.format(
                    "%,.0f",
                    saldoActual
                )
            );
        }


        /*
         * Crear el pago.
         */
        PagoCliente pago =
            new PagoCliente();


        pago.colocarCliente(
            cliente
        );


        pago.colocarMonto(
            monto
        );


        pago.colocarObservacion(
            observacion == null
                ? ""
                : observacion.trim()
        );


        /*
         * Registrar fecha y hora automáticamente.
         */
        DateTimeFormatter formato =
            DateTimeFormatter.ofPattern(
                "yyyy-MM-dd HH:mm:ss"
            );


        pago.colocarFecha(
            LocalDateTime
                .now()
                .format(formato)
        );


        /*
         * El repositorio registra el pago
         * y descuenta el saldo dentro de
         * una misma transacción.
         */
        int idPago =
            pagoRepositorio.registrarPago(
                pago
            );


        /*
         * Actualizamos el objeto Cliente
         * que está actualmente en memoria.
         */
        cliente.colocarSaldo(
            saldoActual - monto
        );


        pago.colocarId(
            idPago
        );


        return idPago;
    }


    public ObservableList<PagoCliente> consultarPagos(
        Cliente cliente
    ) {

        if (cliente == null) {

            throw new IllegalArgumentException(
                "Debe seleccionar un cliente."
            );
        }


        if (cliente.conseguirId() <= 0) {

            throw new IllegalArgumentException(
                "El cliente seleccionado no es válido."
            );
        }


        return pagoRepositorio.consultarPagosCliente(
            cliente.conseguirId()
        );
    }
}