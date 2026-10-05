package com.gestion.service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;

import com.gestion.model.Alerta;
import com.gestion.model.Lote;
import com.gestion.model.Producto;
import com.gestion.repository.LoteRepositorio;
import com.gestion.repository.ProductoRepositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class AlertaServicio {

    private static final int DIAS_PROXIMO_VENCIMIENTO = 30;

    private final ProductoRepositorio productoRepositorio;
    private final LoteRepositorio loteRepositorio;


    public AlertaServicio() {

        productoRepositorio =
            new ProductoRepositorio();

        loteRepositorio =
            new LoteRepositorio();
    }


    public ObservableList<Alerta> consultarAlertas() {

        ObservableList<Alerta> alertas =
            FXCollections.observableArrayList();

        alertas.addAll(
            consultarAlertasStock()
        );

        alertas.addAll(
            consultarAlertasVencimiento()
        );

        return alertas;
    }


    public ObservableList<Alerta> consultarAlertasStock() {

        ObservableList<Alerta> alertas =
            FXCollections.observableArrayList();

        ObservableList<Producto> productos =
            productoRepositorio.consultarTodo();


        for (Producto producto : productos) {

            if (producto == null) {
                continue;
            }


            /*
             * Si el producto está marcado como
             * inactivo, no se genera una alerta.
             */
            if (
                Boolean.FALSE.equals(
                    producto.conseguirActivo()
                )
            ) {
                continue;
            }


            double existencias =
                valorSeguro(
                    producto.conseguirExistencias()
                );

            double minimo =
                valorSeguro(
                    producto.conseguirMinimoExistencias()
                );


            /*
             * Un producto sin existencias siempre
             * debe generar una alerta, incluso si
             * el mínimo configurado también es 0.
             */
            boolean sinExistencias =
                existencias <= 0;


            /*
             * Si todavía tiene existencias,
             * consideramos stock bajo cuando existe
             * un mínimo mayor a 0 y las existencias
             * son iguales o inferiores a ese mínimo.
             */
            boolean stockBajo =
                existencias > 0
                && minimo > 0
                && existencias <= minimo;


            if (
                sinExistencias
                || stockBajo
            ) {

                String estado;
                String detalle;


                if (sinExistencias) {

                    estado =
                        "Sin existencias";

                    detalle =
                        "El producto no tiene existencias disponibles."
                        + " Mínimo configurado: "
                        + formatearCantidad(minimo)
                        + ".";

                } else {

                    estado =
                        "Stock bajo";

                    detalle =
                        "Existencias actuales: "
                        + formatearCantidad(existencias)
                        + ". Mínimo configurado: "
                        + formatearCantidad(minimo)
                        + ".";
                }


                Alerta alerta =
                    new Alerta(
                        "Stock",
                        textoSeguro(
                            producto.conseguirNombre()
                        ),
                        textoSeguro(
                            producto.conseguirCodigoBarras()
                        ),
                        "",
                        "",
                        detalle,
                        estado
                    );


                alertas.add(alerta);
            }
        }


        return alertas;
    }


    public ObservableList<Alerta> consultarAlertasVencimiento() {

        ObservableList<Alerta> alertas =
            FXCollections.observableArrayList();

        ObservableList<Lote> lotes =
            loteRepositorio.consultarTodo();

        LocalDate hoy =
            LocalDate.now();

        LocalDate limite =
            hoy.plusDays(
                DIAS_PROXIMO_VENCIMIENTO
            );


        for (Lote lote : lotes) {

            if (lote == null) {
                continue;
            }


            LocalDate fechaVencimiento =
                convertirFecha(
                    lote.conseguirFechaVencimiento()
                );


            if (fechaVencimiento == null) {
                continue;
            }


            Producto producto =
                lote.conseguirProducto();


            String nombreProducto =
                producto != null
                    ? textoSeguro(
                        producto.conseguirNombre()
                    )
                    : "";


            String codigoBarras =
                producto != null
                    ? textoSeguro(
                        producto.conseguirCodigoBarras()
                    )
                    : "";


            /*
             * LOTE VENCIDO
             */
            if (
                fechaVencimiento.isBefore(hoy)
            ) {

                long diasVencido =
                    ChronoUnit.DAYS.between(
                        fechaVencimiento,
                        hoy
                    );


                String detalle =
                    "El registro del lote venció hace "
                    + diasVencido
                    + (
                        diasVencido == 1
                            ? " día."
                            : " días."
                    );


                Alerta alerta =
                    new Alerta(
                        "Vencimiento",
                        nombreProducto,
                        codigoBarras,
                        textoSeguro(
                            lote.conseguirLote()
                        ),
                        textoSeguro(
                            lote.conseguirFechaVencimiento()
                        ),
                        detalle,
                        "Vencido"
                    );


                alertas.add(alerta);

                continue;
            }


            /*
             * PRÓXIMO A VENCER
             *
             * Incluye el día de hoy y hasta
             * 30 días en el futuro.
             */
            if (
                !fechaVencimiento.isAfter(
                    limite
                )
            ) {

                long diasRestantes =
                    ChronoUnit.DAYS.between(
                        hoy,
                        fechaVencimiento
                    );


                String detalle;


                if (diasRestantes == 0) {

                    detalle =
                        "El registro del lote vence hoy.";

                } else if (diasRestantes == 1) {

                    detalle =
                        "El registro del lote vence mañana.";

                } else {

                    detalle =
                        "El registro del lote vence en "
                        + diasRestantes
                        + " días.";
                }


                Alerta alerta =
                    new Alerta(
                        "Vencimiento",
                        nombreProducto,
                        codigoBarras,
                        textoSeguro(
                            lote.conseguirLote()
                        ),
                        textoSeguro(
                            lote.conseguirFechaVencimiento()
                        ),
                        detalle,
                        "Próximo a vencer"
                    );


                alertas.add(alerta);
            }
        }


        return alertas;
    }


    private LocalDate convertirFecha(
        String fecha
    ) {

        if (
            fecha == null
            || fecha.isBlank()
        ) {
            return null;
        }


        String valor =
            fecha.trim();


        /*
         * Formato principal:
         * yyyy-MM-dd
         */
        try {

            return LocalDate.parse(
                valor
            );

        } catch (
            DateTimeParseException e
        ) {

            // Intentar siguiente formato.
        }


        /*
         * Formato:
         * dd/MM/yyyy
         */
        try {

            return LocalDate.parse(
                valor,
                DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy"
                )
            );

        } catch (
            DateTimeParseException e
        ) {

            // Intentar siguiente formato.
        }


        /*
         * Formato:
         * dd-MM-yyyy
         */
        try {

            return LocalDate.parse(
                valor,
                DateTimeFormatter.ofPattern(
                    "dd-MM-yyyy"
                )
            );

        } catch (
            DateTimeParseException e
        ) {

            return null;
        }
    }


    private double valorSeguro(
        Double valor
    ) {

        return valor == null
            ? 0.0
            : valor;
    }


    private String textoSeguro(
        String texto
    ) {

        return texto == null
            ? ""
            : texto;
    }


    private String formatearCantidad(
        double cantidad
    ) {

        if (
            cantidad
            == Math.rint(cantidad)
        ) {

            return String.format(
                "%.0f",
                cantidad
            );
        }


        return String.format(
            "%.2f",
            cantidad
        );
    }
}