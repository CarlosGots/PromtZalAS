package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.semantico.Simbolo;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Tabla de simbolos: agentes y variables con su tipo, ambito y linea.
 */
public class PanelSimbolos extends TableView<Simbolo> {

    public PanelSimbolos() {
        getColumns().add(columna("Nombre", Simbolo::getNombre, 200));
        getColumns().add(columna("Tipo", s -> s.getTipo().name(), 130));
        getColumns().add(columna("Ámbito", Simbolo::getAmbito, 160));
        getColumns().add(columna("Fila", Simbolo::getFila, 70));
        getColumns().add(columna("Columna", Simbolo::getColumna, 80));

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setPlaceholder(new Label("Compila para ver la tabla de símbolos."));
    }

    public void mostrar(List<Simbolo> simbolos) {
        getItems().setAll(simbolos);
    }

    public void limpiar() {
        getItems().clear();
    }

    private static <T> TableColumn<Simbolo, T> columna(String titulo, Function<Simbolo, T> valor, double ancho) {
        TableColumn<Simbolo, T> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(valor.apply(c.getValue())));
        col.setPrefWidth(ancho);
        return col;
    }
}