package com.mycompany.promtzalp2.gui;

import atlantafx.base.theme.Styles;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

/**
 * Panel que muestra el promptzal generado, con opciones para
 * copiarlo al portapapeles y guardarlo como .md o .txt.
 */
public class PanelPromptzal extends BorderPane {

    private final TextArea texto = new TextArea();
    private final Label aviso = new Label("Generado con errores: revisa el panel de Problemas.");
    private final Button copiar = new Button("Copiar");
    private final Button guardar = new Button("Guardar");

    /** Nombre sugerido al guardar (el del .pz abierto). */
    private Supplier<String> nombreBase = () -> "promptzal";
    /** Accion al guardar con exito (para escribir en la consola). */
    private Consumer<String> alGuardar = ruta -> { };

    public PanelPromptzal() {
        texto.setEditable(false);
        texto.setWrapText(true);
        texto.getStyleClass().add("promptzal-texto");

        Label titulo = new Label("PROMPTZAL");
        titulo.getStyleClass().add("titulo-panel");

        copiar.getStyleClass().add(Styles.SMALL);
        guardar.getStyleClass().addAll(Styles.SMALL, Styles.ACCENT);
        copiar.setOnAction(e -> copiar());
        guardar.setOnAction(e -> guardar());

        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        HBox barra = new HBox(6, titulo, espacio, copiar, guardar);
        barra.setStyle("-fx-alignment: center-left; -fx-padding: 0 8 0 0;");

        aviso.getStyleClass().add("aviso-promptzal");
        aviso.setWrapText(true);

        setTop(new VBox(barra, aviso));
        setCenter(texto);
        limpiar();
    }

    /** Muestra el prompt; si hubo errores, se ve el aviso. */
    public void mostrar(String prompt, boolean conErrores) {
        texto.setText(prompt);
        aviso.setVisible(conErrores);
        aviso.setManaged(conErrores);
        boolean vacio = prompt == null || prompt.isBlank();
        copiar.setDisable(vacio);
        guardar.setDisable(vacio);
    }

    public void limpiar() {
        mostrar("", false);
    }

    private void copiar() {
        ClipboardContent contenido = new ClipboardContent();
        contenido.putString(texto.getText());
        Clipboard.getSystemClipboard().setContent(contenido);
    }

    private void guardar() {
        FileChooser selector = new FileChooser();
        selector.setTitle("Guardar promptzal");
        selector.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Markdown (*.md)", "*.md"),
                new FileChooser.ExtensionFilter("Texto (*.txt)", "*.txt"));
        selector.setInitialFileName(nombreBase.get() + ".md");

        File destino = selector.showSaveDialog(getScene().getWindow());
        if (destino == null) {
            return;
        }
        try {
            Files.writeString(destino.toPath(), texto.getText(), StandardCharsets.UTF_8);
            alGuardar.accept(destino.getAbsolutePath());
        } catch (IOException ex) {
            new Alert(Alert.AlertType.ERROR, "No se pudo guardar el archivo:\n" + ex.getMessage()).showAndWait();
        }
    }

    public void setNombreBase(Supplier<String> nombreBase) {
        this.nombreBase = nombreBase;
    }

    public void setAlGuardar(Consumer<String> accion) {
        this.alGuardar = accion;
    }
}
