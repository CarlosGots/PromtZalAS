package com.mycompany.promtzalp2.lexer;

public class Token {

    private final TipoToken tipo;
    private final String lexema;
    private final int fila;
    private final int columna;
    private final int offset;
    private final int longitud;

    public Token(TipoToken tipo, String lexema, int fila, int columna, int offset, int longitud) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.fila = fila;
        this.columna = columna;
        this.offset = offset;
        this.longitud = longitud;
    }

    public TipoToken getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    public int getOffset() {
        return offset;
    }

    public int getLongitud() {
        return longitud;
    }

    @Override
    public String toString() {
        return String.format("%-12s %-30s fila %d, col %d", tipo, lexema, fila, columna);
    }
}