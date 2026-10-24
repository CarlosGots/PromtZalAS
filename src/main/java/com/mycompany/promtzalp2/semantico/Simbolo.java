package com.mycompany.promtzalp2.semantico;

/**
 * Entrada de la tabla de simbolos: un agente o una variable declarada.
 */
public class Simbolo {

    private final String nombre;
    private final TipoSimbolo tipo;
    /** Agente al que pertenece, o "global" para los agentes. */
    private final String ambito;
    /** Linea donde se declaro. */
    private final int fila;
    private final int columna;

    public Simbolo(String nombre, TipoSimbolo tipo, String ambito, int fila, int columna) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.ambito = ambito;
        this.fila = fila;
        this.columna = columna;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoSimbolo getTipo() {
        return tipo;
    }

    public String getAmbito() {
        return ambito;
    }

    public int getFila() {
        return fila;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return String.format("%-15s %-10s %-12s fila %d", nombre, tipo, ambito, fila);
    }
}