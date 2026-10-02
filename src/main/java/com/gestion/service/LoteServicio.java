package com.gestion.service;

import com.gestion.model.Lote;
import com.gestion.repository.LoteRepositorio;

import javafx.collections.ObservableList;

public class LoteServicio {

    private final LoteRepositorio loteRepositorio =
        new LoteRepositorio();


    public LoteServicio() {
    }


    public ObservableList<Lote> consultarTodo() {

        return loteRepositorio.consultarTodo();
    }


    public void agregarLote(Lote lote) {

        validarLote(lote);

        loteRepositorio.agregarLote(lote);
    }


    public void editarLote(Lote lote) {

        validarLote(lote);

        if (
            lote.conseguirId() == null
            || lote.conseguirId() <= 0
        ) {

            throw new IllegalArgumentException(
                "El lote no tiene un ID válido."
            );
        }

        loteRepositorio.editarLote(lote);
    }


    public Lote consultarLote(int id) {

        if (id <= 0) {

            throw new IllegalArgumentException(
                "El ID del lote no es válido."
            );
        }

        return loteRepositorio.consultarLote(id);
    }


    private void validarLote(Lote lote) {

        if (lote == null) {

            throw new IllegalArgumentException(
                "El lote no puede estar vacío."
            );
        }


        if (
            lote.conseguirProducto() == null
            || lote
                .conseguirProducto()
                .conseguirId() == null
        ) {

            throw new IllegalArgumentException(
                "Debe seleccionar un producto."
            );
        }


        if (
            lote.conseguirLote() == null
            || lote
                .conseguirLote()
                .isBlank()
        ) {

            throw new IllegalArgumentException(
                "Debe ingresar el número o código del lote."
            );
        }


        if (
            lote.conseguirFechaRegistro() == null
            || lote
                .conseguirFechaRegistro()
                .isBlank()
        ) {

            throw new IllegalArgumentException(
                "La fecha de registro es obligatoria."
            );
        }


        if (
            lote.conseguirFechaVencimiento() == null
            || lote
                .conseguirFechaVencimiento()
                .isBlank()
        ) {

            throw new IllegalArgumentException(
                "La fecha de vencimiento es obligatoria."
            );
        }


        if (
            lote.conseguirCantidadMovimiento() == null
            || lote.conseguirCantidadMovimiento() <= 0
        ) {

            throw new IllegalArgumentException(
                "La cantidad de entrada debe ser mayor que cero."
            );
        }


        if (
            lote.conseguirValorMovimiento() != null
            && lote.conseguirValorMovimiento() < 0
        ) {

            throw new IllegalArgumentException(
                "El precio de compra unitario no puede ser negativo."
            );
        }
    }
}
