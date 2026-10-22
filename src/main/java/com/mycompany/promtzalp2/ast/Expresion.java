package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Secuencia de terminos unidos por el operador + (concatenacion).
 * Ej: "grafica de " + tendencias  ->  [Cadena, Id]
 * El + no se guarda: la lista ya representa la concatenacion.
 */
public class Expresion extends NodoAST {

    private final List<Termino> terminos = new ArrayList<>();

    public Expresion(int fila, int columna) {
        super(fila, columna);
    }

    public void agregarTermino(Termino termino) {
        if (termino != null) {
            terminos.add(termino);
        }
    }

    public List<Termino> getTerminos() {
        return terminos;
    }

    /** Une el texto de todos los terminos en una sola cadena. */
    public String toTexto() {
        StringBuilder sb = new StringBuilder();
        for (Termino t : terminos) {
            sb.append(t.toTexto());
        }
        return sb.toString();
    }

    @Override
    public String getEtiqueta() {
        return "Expresion";
    }

    @Override
    public List<NodoAST> getHijos() {
        return new ArrayList<>(terminos);
    }
}