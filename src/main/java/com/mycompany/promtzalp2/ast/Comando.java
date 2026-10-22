package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Comando de IA dentro de un agente.
 * Estructura: COMANDO_IA argumento [conectores] -> destino
 * Ej: PREGUNTAR "pregunta" SOBRE ventas -> tendencias
 *
 * Se llena por partes (setters) porque el parser lo construye
 * conforme va leyendo los tokens.
 */
public class Comando extends NodoAST {

    /** Nombre del comando (PREGUNTAR, GENERAR, RESUMIR, etc.). */
    private final String nombre;
    /** Argumento principal, el que va justo despues del comando. */
    private Expresion argumento;
    private final List<Conector> conectores = new ArrayList<>();
    /** Variable donde se guarda el resultado (lo que va despues de ->). */
    private String destino;

    public Comando(String nombre, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Expresion getArgumento() {
        return argumento;
    }

    public void setArgumento(Expresion argumento) {
        this.argumento = argumento;
    }

    public List<Conector> getConectores() {
        return conectores;
    }

    public void agregarConector(Conector conector) {
        if (conector != null) {
            conectores.add(conector);
        }
    }

    /** Busca un conector por nombre (ej. "EN"). Devuelve null si no existe. */
    public Conector buscarConector(String nombreConector) {
        for (Conector c : conectores) {
            if (c.getNombre().equals(nombreConector)) {
                return c;
            }
        }
        return null;
    }

    public String getDestino() {
        return destino;
    }

    public void setDestino(String destino) {
        this.destino = destino;
    }

    @Override
    public String getEtiqueta() {
        return nombre + (destino == null ? "" : " -> " + destino);
    }

    @Override
    public List<NodoAST> getHijos() {
        List<NodoAST> hijos = new ArrayList<>();
        agregarSiExiste(hijos, argumento);
        hijos.addAll(conectores);
        return hijos;
    }
}