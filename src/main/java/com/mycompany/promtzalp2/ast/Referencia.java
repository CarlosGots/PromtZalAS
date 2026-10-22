package com.mycompany.promtzalp2.ast;

import java.util.List;

/**
 * Referencia por nombre a un agente o a una variable.
 * Se usa para EJECUTAR analista y para cada nombre de EXPORTAR resumen, script.
 * Guarda su posicion para que el semantico pueda reportar referencias invalidas.
 */
public class Referencia extends NodoAST {

    /** Palabra que la origina: "EJECUTAR" o "EXPORTAR". */
    private final String palabra;
    /** Nombre del agente o variable referenciado. */
    private final String nombre;

    public Referencia(String palabra, String nombre, int fila, int columna) {
        super(fila, columna);
        this.palabra = palabra;
        this.nombre = nombre;
    }

    public String getPalabra() {
        return palabra;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String getEtiqueta() {
        return palabra + " " + nombre;
    }

    @Override
    public List<NodoAST> getHijos() {
        return List.of();
    }
}