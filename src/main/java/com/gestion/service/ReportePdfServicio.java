package com.gestion.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.gestion.model.Venta;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import javafx.collections.ObservableList;

public class ReportePdfServicio {

    public void generarReporteVentas(
        File archivo,
        ObservableList<Venta> ventas,
        LocalDate fechaDesde,
        LocalDate fechaHasta,
        String metodoPago
    ) {

        if (archivo == null) {
            throw new IllegalArgumentException(
                "Debe seleccionar un archivo."
            );
        }

        if (ventas == null || ventas.isEmpty()) {
            throw new IllegalArgumentException(
                "No existen ventas para generar el reporte."
            );
        }

        Document documento =
            new Document(
                PageSize.A4.rotate(),
                30,
                30,
                30,
                30
            );

        try {

            PdfWriter.getInstance(
                documento,
                new FileOutputStream(archivo)
            );

            documento.open();

            Font titulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18
                );

            Font subtitulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13
                );

            Font normal =
                FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10
                );

            Font encabezado =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    9
                );


            Paragraph empresa =
                new Paragraph(
                    "VÍVERES Y CÁRNICOS DON EDGAR",
                    titulo
                );

            empresa.setAlignment(
                Element.ALIGN_CENTER
            );

            documento.add(empresa);


            Paragraph nombreReporte =
                new Paragraph(
                    "Reporte de ventas",
                    subtitulo
                );

            nombreReporte.setAlignment(
                Element.ALIGN_CENTER
            );

            nombreReporte.setSpacingAfter(15);

            documento.add(nombreReporte);


            documento.add(
                new Paragraph(
                    construirPeriodo(
                        fechaDesde,
                        fechaHasta
                    ),
                    normal
                )
            );


            documento.add(
                new Paragraph(
                    "Método de pago: "
                    + (
                        metodoPago == null
                        ? "Todos"
                        : metodoPago
                    ),
                    normal
                )
            );


            Paragraph espacio =
                new Paragraph(" ");

            espacio.setSpacingAfter(5);

            documento.add(espacio);


            PdfPTable tabla =
                new PdfPTable(7);

            tabla.setWidthPercentage(100);

            tabla.setWidths(
                new float[] {
                    0.7f,
                    1.6f,
                    2.0f,
                    1.3f,
                    1.2f,
                    1.2f,
                    1.2f
                }
            );


            agregarEncabezado(
                tabla,
                "ID",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Fecha",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Cliente",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Método",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Total",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Pagado",
                encabezado
            );

            agregarEncabezado(
                tabla,
                "Pendiente",
                encabezado
            );


            double totalVendido = 0.0;
            double totalPagado = 0.0;
            double totalPendiente = 0.0;


            for (Venta venta : ventas) {

                tabla.addCell(
                    crearCelda(
                        String.valueOf(
                            venta.conseguirId()
                        ),
                        normal
                    )
                );


                tabla.addCell(
                    crearCelda(
                        venta.conseguirFecha(),
                        normal
                    )
                );


                tabla.addCell(
                    crearCelda(
                        conseguirNombreCliente(
                            venta
                        ),
                        normal
                    )
                );


                tabla.addCell(
                    crearCelda(
                        venta.conseguirMetodoPago(),
                        normal
                    )
                );


                double total =
                    valorSeguro(
                        venta.conseguirTotal()
                    );

                double pagado =
                    valorSeguro(
                        venta.conseguirMontoPagado()
                    );

                double pendiente =
                    valorSeguro(
                        venta.conseguirSaldoPendiente()
                    );


                tabla.addCell(
                    crearCelda(
                        formatoDinero(total),
                        normal
                    )
                );


                tabla.addCell(
                    crearCelda(
                        formatoDinero(pagado),
                        normal
                    )
                );


                tabla.addCell(
                    crearCelda(
                        formatoDinero(pendiente),
                        normal
                    )
                );


                totalVendido += total;
                totalPagado += pagado;
                totalPendiente += pendiente;
            }


            documento.add(tabla);


            Paragraph resumen =
                new Paragraph();

            resumen.setSpacingBefore(15);

            resumen.add(
                new Phrase(
                    "Cantidad de ventas: "
                    + ventas.size()
                    + "\n",
                    normal
                )
            );

            resumen.add(
                new Phrase(
                    "Total vendido: "
                    + formatoDinero(totalVendido)
                    + "\n",
                    normal
                )
            );

            resumen.add(
                new Phrase(
                    "Total pagado: "
                    + formatoDinero(totalPagado)
                    + "\n",
                    normal
                )
            );

            resumen.add(
                new Phrase(
                    "Saldo pendiente generado: "
                    + formatoDinero(totalPendiente),
                    normal
                )
            );

            documento.add(resumen);

        } catch (Exception e) {

            throw new RuntimeException(
                "No se pudo generar el reporte PDF.",
                e
            );

        } finally {

            if (documento.isOpen()) {
                documento.close();
            }
        }
    }


    private void agregarEncabezado(
        PdfPTable tabla,
        String texto,
        Font fuente
    ) {

        PdfPCell celda =
            new PdfPCell(
                new Phrase(
                    texto,
                    fuente
                )
            );

        celda.setHorizontalAlignment(
            Element.ALIGN_CENTER
        );

        celda.setPadding(6);

        tabla.addCell(celda);
    }


    private PdfPCell crearCelda(
        String texto,
        Font fuente
    ) {

        PdfPCell celda =
            new PdfPCell(
                new Phrase(
                    texto == null
                    ? ""
                    : texto,
                    fuente
                )
            );

        celda.setPadding(5);

        return celda;
    }


    private String conseguirNombreCliente(
        Venta venta
    ) {

        if (
            venta.conseguirCliente() == null
            || venta
                .conseguirCliente()
                .conseguirPersona() == null
            || venta
                .conseguirCliente()
                .conseguirPersona()
                .conseguirNombre() == null
        ) {

            return "Venta de contado";
        }

        return venta
            .conseguirCliente()
            .conseguirPersona()
            .conseguirNombre();
    }


    private double valorSeguro(
        Double valor
    ) {

        return valor == null
            ? 0.0
            : valor;
    }


    private String formatoDinero(
        double valor
    ) {

        return "$"
            + String.format(
                "%,.0f",
                valor
            );
    }


    private String construirPeriodo(
        LocalDate desde,
        LocalDate hasta
    ) {

        DateTimeFormatter formato =
            DateTimeFormatter.ofPattern(
                "dd/MM/yyyy"
            );

        if (
            desde == null
            && hasta == null
        ) {

            return "Periodo: Todas las ventas";
        }

        if (desde != null && hasta != null) {

            return "Periodo: "
                + desde.format(formato)
                + " - "
                + hasta.format(formato);
        }

        if (desde != null) {

            return "Periodo: Desde "
                + desde.format(formato);
        }

        return "Periodo: Hasta "
            + hasta.format(formato);
    }
}