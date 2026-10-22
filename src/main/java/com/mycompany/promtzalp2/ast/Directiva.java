package com.mycompany.promtzalp2.ast;

import java.util.List;

/**
 * Directiva de encabezado del prompt.
 * Ej: @modelo "claude-sonnet-4-6"
 */
public class Directiva extends NodoAST {

    /** Nombre con arroba incluida (@modelo, @rol, @formato). */
    private final String nombre;
    /** Valor sin comillas. */
    private final String valor;

    public Directiva(String nombre, String valor, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String getEtiqueta() {
        return nombre + " = \"" + valor + "\"";
    }

    @Override
    public List<NodoAST> getHijos() {
        return List.of();
    }
}