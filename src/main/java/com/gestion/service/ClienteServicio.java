package com.gestion.service;

import com.gestion.model.Cliente;
import com.gestion.repository.ClienteRepositorio;

import javafx.collections.ObservableList;

public class ClienteServicio {

    private final ClienteRepositorio clienteRepositorio;

    public ClienteServicio() {
        this.clienteRepositorio = new ClienteRepositorio();
    }

    public void agregarCliente(Cliente cliente) {

        validarCliente(cliente);

        if (cliente.conseguirSaldo() == null) {
            cliente.colocarSaldo(0.0);
        }

        clienteRepositorio.agregarCliente(cliente);
    }

    public ObservableList<Cliente> consultarTodo() {
        return clienteRepositorio.consultarTodo();
    }

    public Cliente consultarCliente(int id) {

        if (id <= 0) {
            throw new IllegalArgumentException(
                "El ID del cliente no es válido."
            );
        }

        return clienteRepositorio.consultarCliente(id);
    }

    public void editarCliente(Cliente cliente) {

        if (cliente.conseguirId() <= 0) {
            throw new IllegalArgumentException(
                "El cliente no tiene un ID válido."
            );
        }

        validarCliente(cliente);

        clienteRepositorio.editarCliente(cliente);
    }

    public void actualizarSaldo(int idCliente, double nuevoSaldo) {

        if (idCliente <= 0) {
            throw new IllegalArgumentException(
                "El ID del cliente no es válido."
            );
        }

        if (nuevoSaldo < 0) {
            throw new IllegalArgumentException(
                "El saldo no puede ser negativo."
            );
        }

        clienteRepositorio.actualizarSaldo(
            idCliente,
            nuevoSaldo
        );
    }

    private void validarCliente(Cliente cliente) {

        if (cliente == null) {
            throw new IllegalArgumentException(
                "El cliente no puede ser nulo."
            );
        }

        if (cliente.conseguirPersona() == null) {
            throw new IllegalArgumentException(
                "Los datos personales son obligatorios."
            );
        }

        if (cliente.conseguirPersona().conseguirNombre() == null
                || cliente.conseguirPersona()
                    .conseguirNombre().isBlank()) {

            throw new IllegalArgumentException(
                "El nombre del cliente es obligatorio."
            );
        }

        if (cliente.conseguirPersona()
                .conseguirNumeroIdentidad() == null
                || cliente.conseguirPersona()
                    .conseguirNumeroIdentidad().isBlank()) {

            throw new IllegalArgumentException(
                "El número de identidad es obligatorio."
            );
        }

        if (cliente.conseguirSaldo() != null
                && cliente.conseguirSaldo() < 0) {

            throw new IllegalArgumentException(
                "El saldo no puede ser negativo."
            );
        }
    }

    public void eliminarCliente(int idCliente) {

    if (idCliente <= 0) {
        throw new IllegalArgumentException(
            "El ID del cliente no es válido."
        );
    }

    clienteRepositorio.eliminarCliente(idCliente);
    }
}