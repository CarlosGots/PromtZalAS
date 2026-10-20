package com.mycompany.promtzalp2.errores;

public class ErrorCompilacion {

    private final TipoError tipo;
    private final String lexema;
    private final String descripcion;
    private final int fila;
    private final int columna;

    public ErrorCompilacion(TipoError tipo, String lexema, String descripcion, int fila, int columna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.descripcion = descripcion;
        this.fila = fila;
        this.columna = columna;
    }

    public TipoError getTipo() {
        return tipo;
    }

    public String getLexema() {
        return lexema;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("[%s] fila %d, col %d: %s -> '%s'", tipo, fila, columna, descripcion, lexema);
    }
}