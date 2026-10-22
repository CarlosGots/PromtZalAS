package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Conector de un comando con su argumento (SOBRE, DESDE, EN, COMO).
 * Ej: en "RESUMIR x EN 100 palabras", el conector es EN y su argumento 100 palabras.
 */
public class Conector extends NodoAST {

    private final String nombre;
    private final Expresion argumento;

    public Conector(String nombre, Expresion argumento, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
        this.argumento = argumento;
    }

    public String getNombre() {
        return nombre;
    }

    public Expresion getArgumento() {
        return argumento;
    }

    @Override
    public String getEtiqueta() {
        return "Conector: " + nombre;
    }

    @Override
    public List<NodoAST> getHijos() {
        List<NodoAST> hijos = new ArrayList<>();
        agregarSiExiste(hijos, argumento);
        return hijos;
    }
}