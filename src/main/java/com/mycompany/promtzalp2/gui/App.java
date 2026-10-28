package com.mycompany.promtzalp2.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Punto de arranque de la interfaz grafica: aplica el tema,
 * carga estilos.css y muestra la ventana principal.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // getHostServices() permite abrir los reportes HTML en el navegador.
        VentanaPrincipal ventana = new VentanaPrincipal(stage, getHostServices());
        Scene escena = new Scene(ventana, 1280, 800);
        escena.getStylesheets().add(App.class.getResource("/estilos.css").toExternalForm());

        stage.setScene(escena);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}