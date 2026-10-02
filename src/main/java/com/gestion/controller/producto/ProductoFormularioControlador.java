package com.gestion.controller.producto;

import java.util.Optional;

import com.gestion.model.Categoria;
import com.gestion.model.Producto;
import com.gestion.model.UnidadesMedida;
import com.gestion.repository.CategoriaRepositorio;
import com.gestion.repository.ProductoRepositorio;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.stage.Stage;

public class ProductoFormularioControlador {

    private Producto producto = new Producto();

    private final ProductoRepositorio productoRepositorio =
        new ProductoRepositorio();

    private final CategoriaRepositorio categoriaRepositorio =
        new CategoriaRepositorio();

    private final ObservableList<Producto> unidadesAgrupadas =
        productoRepositorio.consultarTodo();

    private final ObservableList<Categoria> categorias =
        categoriaRepositorio.consultarTodo();

    private final ObservableList<String> UNIDADES_ORIGINALES =
        UnidadesMedida.UNIDADES_ORIGINALES;

    private final ObservableList<String> unidadesProdRepo =
        productosACadena();

    private final FilteredList<String> unidadesFiltradasProdRepo =
        new FilteredList<>(unidadesProdRepo);

    @FXML
    private TextField txtCodigoBarras;

    @FXML
    private TextField txtNombreProducto;

    @FXML
    private TextField txtMarca;

    @FXML
    private ChoiceBox<Categoria> seleccionCategoria;

    @FXML
    private RadioButton unidadMedida;

    @FXML
    private RadioButton unidadAgrupada;

    @FXML
    private ToggleGroup grupoTipoUnidad;

    @FXML
    private ChoiceBox<String> seleccionUnidadMedida;

    @FXML
    private ComboBox<String> txtUnidadAgrupada;

    @FXML
    private TextField txtPrecio;

    @FXML
    private TextField txtMinimoExistencias;

    @FXML
    private Button botonCancelar;


    public void colocarProducto(
        Producto producto
    ) {
        this.producto = producto;
    }


    @FXML
    public void initialize() {

        seleccionCategoria.setItems(
            categorias
        );

        configurarTipoUnidad();

        grupoTipoUnidad
            .selectedToggleProperty()
            .addListener(
                (obs, anterior, nuevo) ->
                    configurarTipoUnidad()
            );

        seleccionUnidadMedida.setItems(
            UNIDADES_ORIGINALES
        );

        txtUnidadAgrupada.setItems(
            unidadesFiltradasProdRepo
        );

        txtUnidadAgrupada
            .getEditor()
            .textProperty()
            .addListener(
                (obs, anterior, nuevo) -> {

                    if (
                        nuevo == null
                        || nuevo.isEmpty()
                    ) {

                        unidadesFiltradasProdRepo
                            .setPredicate(
                                p -> true
                            );

                    } else {

                        String busqueda =
                            nuevo.toLowerCase();

                        unidadesFiltradasProdRepo
                            .setPredicate(
                                texto ->
                                    texto
                                        .toLowerCase()
                                        .contains(
                                            busqueda
                                        )
                            );
                    }
                }
            );
    }


    public void cargarProductoCampoTexto(
        Producto producto
    ) {

        colocarProducto(
            producto
        );

        txtCodigoBarras.setText(
            producto.conseguirCodigoBarras()
        );

        txtNombreProducto.setText(
            producto.conseguirNombre()
        );

        txtMarca.setText(
            producto.conseguirMarca()
        );

        if (
            producto.conseguirCategoria()
            != null
        ) {

            if (
                !categorias.contains(
                    producto.conseguirCategoria()
                )
            ) {
                categorias.add(
                    producto.conseguirCategoria()
                );
            }

            seleccionCategoria.setValue(
                producto.conseguirCategoria()
            );
        }

        seleccionUnidadMedida.setValue(
            producto.conseguirUnidadMedida()
        );

        /*
         * Si el producto no tiene producto padre,
         * utiliza una unidad de medida normal.
         */
        if (
            producto.conseguirUnidadAgrupada()
            == null
        ) {

            unidadMedida.setSelected(
                true
            );

            unidadAgrupada.setSelected(
                false
            );

            txtUnidadAgrupada.setValue(
                null
            );

        } else {

            unidadMedida.setSelected(
                false
            );

            unidadAgrupada.setSelected(
                true
            );

            Producto productoPadre =
                productoRepositorio
                    .consultarProductoDesactivado(
                        producto
                            .conseguirUnidadAgrupada()
                    );

            /*
             * Si no está desactivado,
             * buscamos el producto activo.
             */
            if (
                productoPadre == null
                || productoPadre.conseguirId()
                    == null
            ) {

                productoPadre =
                    productoRepositorio
                        .consultarProductoActivado(
                            producto
                                .conseguirUnidadAgrupada()
                        );
            }

            if (
                productoPadre != null
                && productoPadre.conseguirId()
                    != null
            ) {

                String productoPadreCadena =
                    productoACadena(
                        productoPadre
                    );

                if (
                    !unidadesProdRepo.contains(
                        productoPadreCadena
                    )
                ) {

                    unidadesProdRepo.add(
                        productoPadreCadena
                    );
                }

                txtUnidadAgrupada.setValue(
                    productoPadreCadena
                );
            }
        }

        txtPrecio.setText(
            producto.conseguirPrecio()
                == null
                ? ""
                : String.valueOf(
                    producto.conseguirPrecio()
                )
        );

        txtMinimoExistencias.setText(
            producto
                .conseguirMinimoExistencias()
                == null
                ? ""
                : String.valueOf(
                    producto
                        .conseguirMinimoExistencias()
                )
        );

        verifiarHijoPadre();

        configurarTipoUnidad();
    }


    private void verifiarHijoPadre() {

        if (
            producto != null
            && producto.conseguirId() != null
        ) {

            unidadesProdRepo.remove(
                productoACadena(
                    producto
                )
            );
        }
    }


    private String productoACadena(
        Producto prod
    ) {

        if (prod == null) {
            return "";
        }

        return prod.conseguirId()
            + " - "
            + valorSeguro(
                prod.conseguirCodigoBarras()
            )
            + " - "
            + valorSeguro(
                prod.conseguirNombre()
            )
            + " - "
            + valorSeguro(
                prod.conseguirMarca()
            );
    }


    private String valorSeguro(
        String valor
    ) {

        return valor == null
            ? ""
            : valor;
    }


    private ObservableList<String>
    productosACadena() {

        ObservableList<String> cadenas =
            FXCollections.observableArrayList();

        for (
            Producto prod
            : unidadesAgrupadas
        ) {

            if (
                prod.conseguirUnidadAgrupada()
                == null
            ) {

                cadenas.add(
                    productoACadena(
                        prod
                    )
                );
            }
        }

        return cadenas;
    }


    private void configurarTipoUnidad() {

        if (
            unidadMedida.isSelected()
        ) {

            seleccionUnidadMedida
                .setDisable(
                    false
                );

            txtUnidadAgrupada
                .setDisable(
                    true
                );

        } else if (
            unidadAgrupada.isSelected()
        ) {

            seleccionUnidadMedida
                .setDisable(
                    true
                );

            txtUnidadAgrupada
                .setDisable(
                    false
                );
        }
    }


    private boolean mostrarAlertaAdvertencia(
        String titulo,
        String cabecera,
        String cuerpo
    ) {

        Alert alerta =
            new Alert(
                AlertType.CONFIRMATION
            );

        alerta.setTitle(
            titulo
        );

        alerta.setHeaderText(
            cabecera
        );

        alerta.setContentText(
            cuerpo
        );

        ButtonType botonContinuar =
            new ButtonType(
                "Sí",
                ButtonBar.ButtonData.OK_DONE
            );

        ButtonType botonRevisar =
            new ButtonType(
                "No",
                ButtonBar.ButtonData.CANCEL_CLOSE
            );

        alerta
            .getButtonTypes()
            .setAll(
                botonContinuar,
                botonRevisar
            );

        Optional<ButtonType> resultado =
            alerta.showAndWait();

        return resultado.isPresent()
            && resultado.get()
                == botonContinuar;
    }


    private Integer productoCadenaId(
        String cadena
    ) {

        return Integer.parseInt(
            cadena
                .split("-")[0]
                .trim()
        );
    }


    @FXML
    private void cancelar() {
        cerrarVentana();
    }


    private void cerrarVentana() {

        Stage ventana =
            (Stage) botonCancelar
                .getScene()
                .getWindow();

        ventana.close();
    }


    @FXML
    private void borrarProducto() {

        if (
            producto == null
            || producto.conseguirId()
                == null
        ) {

            new Alert(
                AlertType.ERROR,
                "No existe un producto para eliminar."
            ).showAndWait();

            return;
        }

        if (
            mostrarAlertaAdvertencia(
                "Aviso",
                "Se eliminará el producto.",
                "¿Está seguro de esta acción?"
            )
        ) {

            productoRepositorio
                .borrarProducto(
                    producto.conseguirId()
                );

            cerrarVentana();
        }
    }


    @FXML
    private void aceptar() {

        /*
         * Código de barras.
         */
        if (
            txtCodigoBarras.getText()
                == null
            || txtCodigoBarras
                .getText()
                .isBlank()
        ) {

            boolean continuar =
                mostrarAlertaAdvertencia(
                    "Alerta",
                    "Código de barras vacío",
                    "¿Desea guardar el producto sin código de barras?"
                );

            if (!continuar) {
                return;
            }

            producto.colocarCodigoBarras(
                null
            );

        } else {

            producto.colocarCodigoBarras(
                txtCodigoBarras
                    .getText()
                    .trim()
            );
        }


        /*
         * Nombre.
         */
        if (
            txtNombreProducto.getText()
                == null
            || txtNombreProducto
                .getText()
                .isBlank()
        ) {

            mostrarError(
                "El nombre del producto es obligatorio."
            );

            return;
        }

        producto.colocarNombre(
            txtNombreProducto
                .getText()
                .trim()
        );


        /*
         * Marca.
         */
        if (
            txtMarca.getText()
                == null
            || txtMarca
                .getText()
                .isBlank()
        ) {

            mostrarError(
                "La marca o distribuidor es obligatorio."
            );

            return;
        }

        producto.colocarMarca(
            txtMarca
                .getText()
                .trim()
        );


        /*
         * Categoría.
         */
        Categoria categoria =
            seleccionCategoria
                .getSelectionModel()
                .getSelectedItem();

        if (categoria == null) {

            mostrarError(
                "Debe seleccionar una categoría."
            );

            return;
        }

        producto.colocarCategoria(
            categoria
        );


        /*
         * Unidad de medida o
         * producto agrupado.
         */
        if (
            unidadMedida.isSelected()
        ) {

            String seleccion =
                seleccionUnidadMedida
                    .getSelectionModel()
                    .getSelectedItem();

            if (
                seleccion == null
                || !UNIDADES_ORIGINALES
                    .contains(
                        seleccion
                    )
            ) {

                mostrarError(
                    "Debe seleccionar una unidad de medida válida."
                );

                return;
            }

            producto.colocarUnidadMedida(
                seleccion
            );

            producto.colocarUnidadAgrupada(
                null
            );

        } else if (
            unidadAgrupada.isSelected()
        ) {

            String productoPadre =
                txtUnidadAgrupada
                    .getSelectionModel()
                    .getSelectedItem();

            if (
                productoPadre == null
                || !unidadesProdRepo
                    .contains(
                        productoPadre
                    )
            ) {

                txtUnidadAgrupada
                    .getEditor()
                    .clear();

                mostrarError(
                    "Debe seleccionar un producto padre válido."
                );

                return;
            }

            if (
                producto.conseguirId()
                    != null
                && productoRepositorio
                    .consultarColumnaUnidadAgrupada()
                    .contains(
                        String.valueOf(
                            producto.conseguirId()
                        )
                    )
            ) {

                mostrarError(
                    "Este producto cuenta con unidades hijas y no puede convertirse en una unidad agrupada."
                );

                return;
            }

            Integer idProductoPadre;

            try {

                idProductoPadre =
                    productoCadenaId(
                        productoPadre
                    );

            } catch (
                NumberFormatException e
            ) {

                mostrarError(
                    "El producto padre seleccionado no es válido."
                );

                return;
            }

            if (
                producto.conseguirId()
                    != null
                && producto.conseguirId()
                    .equals(
                        idProductoPadre
                    )
            ) {

                mostrarError(
                    "Un producto no puede ser su propio producto padre."
                );

                return;
            }

            producto.colocarUnidadMedida(
                null
            );

            producto.colocarUnidadAgrupada(
                idProductoPadre
            );

        } else {

            mostrarError(
                "Debe seleccionar el tipo de unidad."
            );

            return;
        }


        /*
         * Precio.
         */
        if (
            txtPrecio.getText() == null
            || txtPrecio
                .getText()
                .isBlank()
        ) {

            mostrarError(
                "El precio es obligatorio."
            );

            return;
        }

        try {

            double precio =
                Double.parseDouble(
                    txtPrecio
                        .getText()
                        .trim()
                );

            if (precio <= 0) {

                mostrarError(
                    "El precio debe ser mayor que 0."
                );

                return;
            }

            producto.colocarPrecio(
                precio
            );

        } catch (
            NumberFormatException e
        ) {

            mostrarError(
                "El precio debe ser un número válido."
            );

            return;
        }


        /*
         * Existencias.
         *
         * Solamente se establecen en cero
         * cuando el producto es nuevo.
         *
         * Al editar se conserva el stock
         * existente.
         */
        if (
            producto.conseguirId()
                == null
        ) {

            producto.colocarExistencias(
                0.0
            );
        }


        /*
         * Mínimo de existencias.
         */
        if (
            txtMinimoExistencias.getText()
                == null
            || txtMinimoExistencias
                .getText()
                .isBlank()
        ) {

            mostrarError(
                "Debe indicar el mínimo de existencias."
            );

            return;
        }

        try {

            double minimo =
                Double.parseDouble(
                    txtMinimoExistencias
                        .getText()
                        .trim()
                );

            if (minimo < 0) {

                mostrarError(
                    "El mínimo de existencias no puede ser negativo."
                );

                return;
            }

            producto
                .colocarMinimoExistencias(
                    minimo
                );

        } catch (
            NumberFormatException e
        ) {

            mostrarError(
                "El mínimo de existencias debe ser un número válido."
            );

            return;
        }


        /*
         * Guardar o editar.
         */
        try {

            if (
                producto.conseguirId()
                    == null
            ) {

                productoRepositorio
                    .agregarProducto(
                        producto
                    );

                new Alert(
                    AlertType.INFORMATION,
                    "Producto guardado correctamente."
                ).showAndWait();

            } else {

                productoRepositorio
                    .editarProducto(
                        producto
                    );

                new Alert(
                    AlertType.INFORMATION,
                    "Producto editado correctamente."
                ).showAndWait();
            }

        } catch (
            RuntimeException e
        ) {

            e.printStackTrace();

            mostrarError(
                "No se pudo guardar el producto: "
                + e.getMessage()
            );

            return;
        }

        cerrarVentana();
    }


    private void mostrarError(
        String mensaje
    ) {

        new Alert(
            AlertType.ERROR,
            mensaje
        ).showAndWait();
    }
}