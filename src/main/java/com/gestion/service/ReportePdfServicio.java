package com.gestion.service;

import java.io.File;
import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.gestion.model.Producto;
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

    /*
     * =========================================================
     * REPORTE DE VENTAS
     * =========================================================
     */

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

        if (
            ventas == null
            || ventas.isEmpty()
        ) {

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
                new FileOutputStream(
                    archivo
                )
            );


            documento.open();


            Font fuenteTitulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18
                );


            Font fuenteSubtitulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13
                );


            Font fuenteNormal =
                FontFactory.getFont(
                    FontFactory.HELVETICA,
                    10
                );


            Font fuenteEncabezado =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    9
                );


            /*
             * TÍTULO DE LA EMPRESA
             */

            Paragraph empresa =
                new Paragraph(
                    "VÍVERES Y CÁRNICOS DON EDGAR",
                    fuenteTitulo
                );


            empresa.setAlignment(
                Element.ALIGN_CENTER
            );


            documento.add(
                empresa
            );


            /*
             * TÍTULO DEL REPORTE
             */

            Paragraph tituloReporte =
                new Paragraph(
                    "REPORTE DE VENTAS",
                    fuenteSubtitulo
                );


            tituloReporte.setAlignment(
                Element.ALIGN_CENTER
            );


            tituloReporte.setSpacingAfter(
                20
            );


            documento.add(
                tituloReporte
            );


            /*
             * PERIODO
             */

            Paragraph periodo =
                new Paragraph(
                    construirPeriodo(
                        fechaDesde,
                        fechaHasta
                    ),
                    fuenteNormal
                );


            periodo.setSpacingAfter(
                5
            );


            documento.add(
                periodo
            );


            /*
             * MÉTODO DE PAGO
             */

            String metodo =
                metodoPago == null
                || metodoPago.isBlank()
                ? "Todos"
                : metodoPago;


            Paragraph metodoSeleccionado =
                new Paragraph(
                    "Método de pago: "
                    + metodo,
                    fuenteNormal
                );


            metodoSeleccionado.setSpacingAfter(
                15
            );


            documento.add(
                metodoSeleccionado
            );


            /*
             * TABLA
             */

            PdfPTable tabla =
                new PdfPTable(7);


            tabla.setWidthPercentage(
                100
            );


            tabla.setWidths(
                new float[] {
                    0.6f,
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
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Fecha",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Cliente",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Método",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Total",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Pagado",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Pendiente",
                fuenteEncabezado
            );


            double totalVendido =
                0.0;

            double totalPagado =
                0.0;

            double totalPendiente =
                0.0;


            /*
             * RECORRER LAS VENTAS
             */

            for (
                Venta venta
                : ventas
            ) {

                String idVenta =
                    venta.conseguirId() == null
                    ? ""
                    : String.valueOf(
                        venta.conseguirId()
                    );


                agregarCelda(
                    tabla,
                    idVenta,
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    textoSeguro(
                        venta.conseguirFecha()
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    conseguirNombreCliente(
                        venta
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    textoSeguro(
                        venta
                            .conseguirMetodoPago()
                    ),
                    fuenteNormal
                );


                double total =
                    valorSeguro(
                        venta.conseguirTotal()
                    );


                double pagado =
                    valorSeguro(
                        venta
                            .conseguirMontoPagado()
                    );


                double pendiente =
                    valorSeguro(
                        venta
                            .conseguirSaldoPendiente()
                    );


                agregarCelda(
                    tabla,
                    formatoDinero(
                        total
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    formatoDinero(
                        pagado
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    formatoDinero(
                        pendiente
                    ),
                    fuenteNormal
                );


                totalVendido +=
                    total;


                totalPagado +=
                    pagado;


                totalPendiente +=
                    pendiente;
            }


            documento.add(
                tabla
            );


            /*
             * RESUMEN
             */

            Paragraph resumen =
                new Paragraph(
                    "",
                    fuenteNormal
                );


            resumen.setSpacingBefore(
                20
            );


            resumen.add(
                new Phrase(
                    "Cantidad de ventas: "
                    + ventas.size()
                    + "\n",
                    fuenteNormal
                )
            );


            resumen.add(
                new Phrase(
                    "Total vendido: "
                    + formatoDinero(
                        totalVendido
                    )
                    + "\n",
                    fuenteNormal
                )
            );


            resumen.add(
                new Phrase(
                    "Total pagado: "
                    + formatoDinero(
                        totalPagado
                    )
                    + "\n",
                    fuenteNormal
                )
            );


            resumen.add(
                new Phrase(
                    "Saldo pendiente generado: "
                    + formatoDinero(
                        totalPendiente
                    ),
                    fuenteNormal
                )
            );


            documento.add(
                resumen
            );


        } catch (Exception e) {

            throw new RuntimeException(
                "No se pudo generar el reporte PDF de ventas.",
                e
            );


        } finally {

            if (
                documento.isOpen()
            ) {

                documento.close();
            }
        }
    }


    /*
     * =========================================================
     * REPORTE DE INVENTARIO
     * =========================================================
     */

    public void generarReporteInventario(
        File archivo,
        ObservableList<Producto> productos
    ) {

        if (archivo == null) {

            throw new IllegalArgumentException(
                "Debe seleccionar un archivo."
            );
        }


        if (
            productos == null
            || productos.isEmpty()
        ) {

            throw new IllegalArgumentException(
                "No existen productos para generar el reporte."
            );
        }


        Document documento =
            new Document(
                PageSize.A4.rotate(),
                25,
                25,
                30,
                30
            );


        try {

            PdfWriter.getInstance(
                documento,
                new FileOutputStream(
                    archivo
                )
            );


            documento.open();


            Font fuenteTitulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    18
                );


            Font fuenteSubtitulo =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    13
                );


            Font fuenteNormal =
                FontFactory.getFont(
                    FontFactory.HELVETICA,
                    9
                );


            Font fuenteEncabezado =
                FontFactory.getFont(
                    FontFactory.HELVETICA_BOLD,
                    9
                );


            /*
             * TÍTULO DE LA EMPRESA
             */

            Paragraph empresa =
                new Paragraph(
                    "VÍVERES Y CÁRNICOS DON EDGAR",
                    fuenteTitulo
                );


            empresa.setAlignment(
                Element.ALIGN_CENTER
            );


            documento.add(
                empresa
            );


            /*
             * TÍTULO DEL REPORTE
             */

            Paragraph titulo =
                new Paragraph(
                    "REPORTE DE INVENTARIO",
                    fuenteSubtitulo
                );


            titulo.setAlignment(
                Element.ALIGN_CENTER
            );


            titulo.setSpacingAfter(
                20
            );


            documento.add(
                titulo
            );


            /*
             * TABLA DE INVENTARIO
             */

            PdfPTable tabla =
                new PdfPTable(8);


            tabla.setWidthPercentage(
                100
            );


            tabla.setWidths(
                new float[] {
                    1.3f,
                    2.0f,
                    1.3f,
                    1.4f,
                    1.0f,
                    1.0f,
                    1.0f,
                    1.3f
                }
            );


            /*
             * ENCABEZADOS
             */

            agregarEncabezado(
                tabla,
                "Código",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Producto",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Marca",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Categoría",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Existencias",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Mínimo",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Unidad",
                fuenteEncabezado
            );


            agregarEncabezado(
                tabla,
                "Estado",
                fuenteEncabezado
            );


            int stockBajo =
                0;


            int sinExistencias =
                0;


            /*
             * RECORRER LOS PRODUCTOS
             */

            for (
                Producto producto
                : productos
            ) {

                double existencias =
                    valorSeguro(
                        producto
                            .conseguirExistencias()
                    );


                double minimo =
                    valorSeguro(
                        producto
                            .conseguirMinimoExistencias()
                    );


                /*
                 * CATEGORÍA
                 */

                String categoria =
                    "";


                if (
                    producto
                        .conseguirCategoria()
                    != null
                    && producto
                        .conseguirCategoria()
                        .conseguirNombre()
                    != null
                ) {

                    categoria =
                        producto
                            .conseguirCategoria()
                            .conseguirNombre();
                }


                /*
                 * ESTADO DEL STOCK
                 */

                String estado;


                if (
                    existencias <= 0
                ) {

                    estado =
                        "Sin existencias";


                    sinExistencias++;


                } else if (
                    minimo > 0
                    && existencias <= minimo
                ) {

                    estado =
                        "Stock bajo";


                    stockBajo++;


                } else {

                    estado =
                        "Disponible";
                }


                /*
                 * AGREGAR FILA
                 */

                agregarCelda(
                    tabla,
                    textoSeguro(
                        producto
                            .conseguirCodigoBarras()
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    textoSeguro(
                        producto
                            .conseguirNombre()
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    textoSeguro(
                        producto
                            .conseguirMarca()
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    categoria,
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    formatoCantidad(
                        existencias
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    formatoCantidad(
                        minimo
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    textoSeguro(
                        producto
                            .conseguirUnidadMedida()
                    ),
                    fuenteNormal
                );


                agregarCelda(
                    tabla,
                    estado,
                    fuenteNormal
                );
            }


            documento.add(
                tabla
            );


            /*
             * RESUMEN DEL INVENTARIO
             */

            Paragraph resumen =
                new Paragraph(
                    "",
                    fuenteNormal
                );


            resumen.setSpacingBefore(
                20
            );


            resumen.add(
                new Phrase(
                    "Productos mostrados: "
                    + productos.size()
                    + "\n",
                    fuenteNormal
                )
            );


            resumen.add(
                new Phrase(
                    "Productos con stock bajo: "
                    + stockBajo
                    + "\n",
                    fuenteNormal
                )
            );


            resumen.add(
                new Phrase(
                    "Productos sin existencias: "
                    + sinExistencias,
                    fuenteNormal
                )
            );


            documento.add(
                resumen
            );


        } catch (Exception e) {

            throw new RuntimeException(
                "No se pudo generar el reporte de inventario.",
                e
            );


        } finally {

            if (
                documento.isOpen()
            ) {

                documento.close();
            }
        }
    }


    /*
     * =========================================================
     * MÉTODOS AUXILIARES
     * =========================================================
     */


    private void agregarEncabezado(
        PdfPTable tabla,
        String texto,
        Font fuente
    ) {

        PdfPCell celda =
            new PdfPCell(
                new Phrase(
                    textoSeguro(
                        texto
                    ),
                    fuente
                )
            );


        celda.setHorizontalAlignment(
            Element.ALIGN_CENTER
        );


        celda.setVerticalAlignment(
            Element.ALIGN_MIDDLE
        );


        celda.setPadding(
            7
        );


        tabla.addCell(
            celda
        );
    }


    private void agregarCelda(
        PdfPTable tabla,
        String texto,
        Font fuente
    ) {

        PdfPCell celda =
            new PdfPCell(
                new Phrase(
                    textoSeguro(
                        texto
                    ),
                    fuente
                )
            );


        celda.setVerticalAlignment(
            Element.ALIGN_MIDDLE
        );


        celda.setPadding(
            6
        );


        tabla.addCell(
            celda
        );
    }


    private String conseguirNombreCliente(
        Venta venta
    ) {

        if (
            venta == null
            || venta.conseguirCliente() == null
            || venta
                .conseguirCliente()
                .conseguirPersona() == null
            || venta
                .conseguirCliente()
                .conseguirPersona()
                .conseguirNombre() == null
            || venta
                .conseguirCliente()
                .conseguirPersona()
                .conseguirNombre()
                .isBlank()
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

        if (
            valor == null
        ) {

            return 0.0;
        }


        return valor;
    }


    private String textoSeguro(
        String texto
    ) {

        if (
            texto == null
        ) {

            return "";
        }


        return texto;
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


    private String formatoCantidad(
        double cantidad
    ) {

        if (
            cantidad
            == Math.floor(
                cantidad
            )
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


        if (
            desde != null
            && hasta != null
        ) {

            return "Periodo: "
                + desde.format(
                    formato
                )
                + " - "
                + hasta.format(
                    formato
                );
        }


        if (
            desde != null
        ) {

            return "Periodo: Desde "
                + desde.format(
                    formato
                );
        }


        return "Periodo: Hasta "
            + hasta.format(
                formato
            );
    }
}