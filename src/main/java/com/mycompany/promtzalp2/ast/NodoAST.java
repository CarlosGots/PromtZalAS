package com.mycompany.promtzalp2.ast;

import java.util.List;

/**
 * Clase base de todos los nodos del AST.
 * Cada nodo guarda su posicion en el codigo y sabe describirse a si mismo
 * (etiqueta) y entregar sus hijos, lo que permite recorrer el arbol completo
 * con una sola funcion recursiva (usada en la GUI y en el reporte del AST).
 */
public abstract class NodoAST {

    private final int fila;
    private final int columna;

    protected NodoAST(int fila, int columna) {
        this.fila = fila;
        this.columna = columna;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    /** Texto que representa al nodo en el arbol (ej. "AGENTE analista"). */
    public abstract String getEtiqueta();

    /** Nodos hijos en el orden en que aparecen en el codigo. */
    public abstract List<NodoAST> getHijos();

    /**
     * Agrega un hijo solo si no es null.
     * Tras un error sintactico algunas partes pueden quedar vacias.
     */
    protected static void agregarSiExiste(List<NodoAST> lista, NodoAST nodo) {
        if (nodo != null) {
            lista.add(nodo);
        }
    }
}