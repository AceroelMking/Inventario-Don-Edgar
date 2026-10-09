package com.gestion.model;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UnidadesMedida {
    public static final String KILOGRAMO = "Kilogramo(s)";
    public static final String GRAMO = "Gramo(s)";
    public static final String LIBRA = "Libra(s)";
    public static final String LITRO = "Litro(s)";
    public static final String MILILITRO = "Mililitro(s)";
    public static final String METRO = "Metro(s)";
    public static final String CENTIMETRO = "Centimetro(s)";
    public static final String UNIDAD = "Unidad(es)";

    public static final ObservableList<String> UNIDADES_ORIGINALES =
        FXCollections.observableArrayList(
            KILOGRAMO,
            GRAMO,
            LIBRA,
            LITRO,
            MILILITRO,
            UNIDAD,
            METRO,
            CENTIMETRO
        )
    ;
}
