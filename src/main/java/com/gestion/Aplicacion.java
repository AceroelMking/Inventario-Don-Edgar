package com.gestion;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Aplicacion extends Application {

    @Override
    public void start(
        Stage stage
    ) throws Exception {

        FXMLLoader loader =
            new FXMLLoader(
                getClass().getResource(
                    "/com/gestion/view/Acceso.fxml"
                )
            );

        Scene scene =
            new Scene(
                loader.load()
            );

        stage.setTitle(
            "Víveres y Cárnicos Don Edgar - Acceso"
        );

        stage.setScene(
            scene
        );

        stage.setMinWidth(800);
        stage.setMinHeight(550);

        stage.centerOnScreen();

        stage.show();
    }

    public static void main(
        String[] args
    ) {

        launch(args);
    }
}