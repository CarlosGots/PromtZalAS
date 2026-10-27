package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.lexer.Token;
import java.util.List;
import java.util.function.Function;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

/**
 * Tabla de tokens reconocidos: numero, lexema, tipo, fila y columna.
 */
public class PanelTokens extends TableView<Token> {

    public PanelTokens() {
        // Columna # : numero correlativo segun la posicion de la fila.
        TableColumn<Token, Void> numero = new TableColumn<>("#");
        numero.setPrefWidth(55);
        numero.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });

        getColumns().add(numero);
        getColumns().add(columna("Lexema", Token::getLexema, 300));
        getColumns().add(columna("Tipo", t -> t.getTipo().name(), 140));
        getColumns().add(columna("Fila", Token::getFila, 70));
        getColumns().add(columna("Columna", Token::getColumna, 80));

        setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        setPlaceholder(new Label("Compila para ver los tokens."));
    }

    public void mostrar(List<Token> tokens) {
        getItems().setAll(tokens);
    }

    public void limpiar() {
        getItems().clear();
    }

    private static <T> TableColumn<Token, T> columna(String titulo, Function<Token, T> valor, double ancho) {
        TableColumn<Token, T> col = new TableColumn<>(titulo);
        col.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(valor.apply(c.getValue())));
        col.setPrefWidth(ancho);
        return col;
    }
}