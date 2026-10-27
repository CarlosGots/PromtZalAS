package com.mycompany.promtzalp2.gui;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;

/**
 * Barra inferior estilo IDE: estado de la compilacion, posicion del cursor
 * y tiempo. Se pinta de rojo cuando la ultima compilacion tuvo errores.
 */
public class BarraEstado extends HBox {

    private final Label estado = new Label("Listo");
    private final Label posicion = new Label("Ln 1, Col 1");
    private final Label tiempo = new Label("");

    public BarraEstado() {
        getStyleClass().add("barra-estado");
        Region espacio = new Region();
        HBox.setHgrow(espacio, Priority.ALWAYS);
        getChildren().addAll(estado, espacio, posicion, tiempo, new Label("UTF-8"), new Label("PromptZal"));
    }

    public void setEstado(String texto, boolean conErrores) {
        estado.setText(texto);
        getStyleClass().remove("con-errores");
        if (conErrores) {
            getStyleClass().add("con-errores");
        }
    }

    public void setPosicion(int fila, int columna) {
        posicion.setText("Ln " + fila + ", Col " + columna);
    }

    public void setTiempo(long ms) {
        tiempo.setText(ms < 0 ? "" : ms + " ms");
    }
}