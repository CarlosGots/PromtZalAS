package com.mycompany.promtzalp2.gui;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.LineNumberFactory;

/**
 * Punto de arranque de la interfaz grafica (JavaFX).
 * Por ahora muestra el editor con numeros de linea y una barra de estado;
 * los demas paneles del IDE se agregan en los siguientes pasos.
 */
public class App extends Application {

    @Override
    public void start(Stage stage) {
        // Tema oscuro moderno para todos los controles.
        Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());

        // Editor de codigo con numeros de linea.
        CodeArea editor = new CodeArea();
        editor.setParagraphGraphicFactory(LineNumberFactory.get(editor));
        editor.setStyle("-fx-font-family: 'Consolas'; -fx-font-size: 14px;");
        editor.replaceText("// Escribe tu programa PromptZal aqui\n");

        Label estado = new Label("Listo");

        BorderPane raiz = new BorderPane();
        raiz.setCenter(new VirtualizedScrollPane<>(editor));
        raiz.setBottom(estado);

        stage.setTitle("PromptZal IDE");
        stage.setScene(new Scene(raiz, 1100, 700));
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}