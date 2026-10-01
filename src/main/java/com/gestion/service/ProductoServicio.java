package com.gestion.service;

import com.gestion.model.Producto;
import com.gestion.repository.ProductoRepositorio;

import javafx.collections.ObservableList;

public class ProductoServicio {

    private final ProductoRepositorio productoRepositorio =
        new ProductoRepositorio();

    public ProductoServicio() {
    }

    public ObservableList<Producto> consultarTodo() {
        return productoRepositorio.consultarTodo();
    }

    public Producto consultarCodigoBarras(String codigoBarras) {

        if (codigoBarras == null || codigoBarras.isBlank()) {
            throw new IllegalArgumentException(
                "El código de barras no puede estar vacío."
            );
        }

        return productoRepositorio.consultarCodigoBarrasUnico(
            codigoBarras.trim()
        );
    }
}