package com.mycompany.promtzalp2.ast;

import java.util.List;

/**
 * Unidad minima de una expresion: una cadena, un identificador,
 * un numero (con unidad opcional) o una llamada a CARGAR.
 * Es una hoja del arbol: no tiene hijos.
 */
public class Termino extends NodoAST {

    private final TipoTermino tipo;
    /** Valor del termino. En cadenas y CARGAR se guarda sin comillas. */
    private final String valor;
    /** Palabra que describe a un numero (ej. "palabras" en 100 palabras). Null si no aplica. */
    private final String unidad;

    public Termino(TipoTermino tipo, String valor, String unidad, int fila, int columna) {
        super(fila, columna);
        this.tipo = tipo;
        this.valor = valor;
        this.unidad = unidad;
    }

    public TipoTermino getTipo() {
        return tipo;
    }

    public String getValor() {
        return valor;
    }

    public String getUnidad() {
        return unidad;
    }

    /** Texto del termino tal como se escribira en el promptzal. */
    public String toTexto() {
        switch (tipo) {
            case NUMERO:
                return unidad == null ? valor : valor + " " + unidad;
            case CARGAR:
                return "el archivo \"" + valor + "\"";
            default:
                return valor;
        }
    }

    @Override
    public String getEtiqueta() {
        switch (tipo) {
            case CADENA:
                return "Cadena: \"" + valor + "\"";
            case ID:
                return "Id: " + valor;
            case NUMERO:
                return "Numero: " + (unidad == null ? valor : valor + " " + unidad);
            case CARGAR:
                return "CARGAR(\"" + valor + "\")";
            default:
                return valor;
        }
    }

    @Override
    public List<NodoAST> getHijos() {
        return List.of();
    }
}