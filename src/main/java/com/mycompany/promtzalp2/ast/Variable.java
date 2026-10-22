package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Declaracion de variable dentro de un agente.
 * Ej: variable ventas = CARGAR("ventas.csv")
 */
public class Variable extends NodoAST {

    private final String nombre;
    /** Valor asignado: un literal, un id, una concatenacion o CARGAR. */
    private final Expresion valor;

    public Variable(String nombre, Expresion valor, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
        this.valor = valor;
    }

    public String getNombre() {
        return nombre;
    }

    public Expresion getValor() {
        return valor;
    }

    @Override
    public String getEtiqueta() {
        return "variable " + nombre;
    }

    @Override
    public List<NodoAST> getHijos() {
        List<NodoAST> hijos = new ArrayList<>();
        agregarSiExiste(hijos, valor);
        return hijos;
    }
}