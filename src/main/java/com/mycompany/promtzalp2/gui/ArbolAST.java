package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.ast.NodoAST;
import com.mycompany.promtzalp2.ast.Programa;
import java.util.function.Consumer;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

/**
 * Vista en arbol del AST. Se construye con una sola funcion recursiva
 * gracias a getEtiqueta() y getHijos() de NodoAST.
 * Doble clic en un nodo lleva el cursor del editor a su posicion.
 */
public class ArbolAST extends TreeView<NodoAST> {

    private Consumer<NodoAST> alSeleccionar = n -> { };

    public ArbolAST() {
        setCellFactory(arbol -> new TreeCell<>() {
            @Override
            protected void updateItem(NodoAST item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item.getEtiqueta());
            }
        });

        setOnMouseClicked(ev -> {
            TreeItem<NodoAST> seleccionado = getSelectionModel().getSelectedItem();
            if (ev.getClickCount() == 2 && seleccionado != null) {
                alSeleccionar.accept(seleccionado.getValue());
            }
        });
    }

    public void mostrar(Programa programa) {
        setRoot(programa == null ? null : construir(programa));
    }

    public void limpiar() {
        setRoot(null);
    }

    /** Crea el TreeItem del nodo y, recursivamente, los de todos sus hijos. */
    private TreeItem<NodoAST> construir(NodoAST nodo) {
        TreeItem<NodoAST> item = new TreeItem<>(nodo);
        item.setExpanded(true);
        for (NodoAST hijo : nodo.getHijos()) {
            item.getChildren().add(construir(hijo));
        }
        return item;
    }

    public void setAlSeleccionar(Consumer<NodoAST> accion) {
        this.alSeleccionar = accion;
    }
}