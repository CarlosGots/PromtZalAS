package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;

/**
 * Tabla de errores lexicos, sintacticos y semanticos de la ultima compilacion.
 * Doble clic en un error lleva el cursor del editor a su posicion.
 */
public class PanelProblemas extends TableView<ErrorCompilacion> {

    private Consumer<ErrorCompilacion> alSeleccionar = e -> { };

    public PanelProblemas() {
        TableColumn<ErrorCompilacion, String> tipo = columna("Tipo", e -> e.getTipo().name(), 110);
        // El texto del tipo se colorea segun la fase (clases tipo-LEXICO, etc. en estilos.css).
        tipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("tipo-LEXICO", "tipo-SINTACTICO", "tipo-SEMANTICO");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item);
                    getStyleClass().add("tipo-" + item);
                }
            }
        });

        getColumns().add(tipo);
        getColumns().add(columna("Fila", ErrorCompilacion::getFila, 60));
        getColumns().add(columna("Columna", ErrorCompilacion::getColumna, 75));
        getColumns().add(columna("Lexema", ErrorCompilacion::getLexema, 130));
        getColumns().add(columna("Descripción", ErrorCompilacion::getDescripcion, 600));

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setPlaceholder(new Label("Sin problemas. Presiona F5 para compilar."));

        setRowFactory(tabla -> {
            TableRow<ErrorCompilacion> fila = new TableRow<>();
            fila.setOnMouseClicked(ev -> {
                if (ev.getClickCount() == 2 && !fila.isEmpty()) {
                    alSeleccionar.accept(fila.getItem());
                }
            });
            return fila;
        });
    }

    /** Muestra los errores ordenados por fila y columna. */
    public void mostrar(List<ErrorCompilacion> errores) {
        List<ErrorCompilacion> ordenados = new ArrayList<>(errores);
        ordenados.sort(Comparator.comparingInt(ErrorCompilacion::getFila)
                .thenComparingInt(ErrorCompilacion::getColumna));
        getItems().setAll(ordenados);
    }

    public void limpiar() {
        getItems().clear();
    }

    public void setAlSeleccionar(Consumer<ErrorCompilacion> accion) {
        this.alSeleccionar = accion;
    }

    private static <T> TableColumn<ErrorCompilacion, T> columna(String titulo,
            Function<ErrorCompilacion, T> valor, double ancho) {
        TableColumn<ErrorCompilacion, T> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(valor.apply(c.getValue())));
        col.setPrefWidth(ancho);
        return col;
    }
}