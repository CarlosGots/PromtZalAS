package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Raiz del AST: representa el archivo .pz completo.
 * Estructura: directivas agentes ejecuciones exportar
 */
public class Programa extends NodoAST {

    private final List<Directiva> directivas = new ArrayList<>();
    private final List<Agente> agentes = new ArrayList<>();
    /** Agentes indicados en EJECUTAR, en el orden en que aparecen. */
    private final List<Referencia> ejecuciones = new ArrayList<>();
    /** Variables indicadas en EXPORTAR. */
    private final List<Referencia> exportaciones = new ArrayList<>();

    public Programa() {
        super(1, 1);
    }

    public List<Directiva> getDirectivas() {
        return directivas;
    }

    public void agregarDirectiva(Directiva directiva) {
        if (directiva != null) {
            directivas.add(directiva);
        }
    }

    /** Devuelve el valor de una directiva (ej. "@modelo") o null si no se declaro. */
    public String getValorDirectiva(String nombre) {
        for (Directiva d : directivas) {
            if (d.getNombre().equals(nombre)) {
                return d.getValor();
            }
        }
        return null;
    }

    public List<Agente> getAgentes() {
        return agentes;
    }

    public void agregarAgente(Agente agente) {
        if (agente != null) {
            agentes.add(agente);
        }
    }

    /** Busca un agente por nombre. Devuelve null si no existe. */
    public Agente buscarAgente(String nombre) {
        for (Agente a : agentes) {
            if (a.getNombre().equals(nombre)) {
                return a;
            }
        }
        return null;
    }

    public List<Referencia> getEjecuciones() {
        return ejecuciones;
    }

    public void agregarEjecucion(Referencia ejecucion) {
        if (ejecucion != null) {
            ejecuciones.add(ejecucion);
        }
    }

    public List<Referencia> getExportaciones() {
        return exportaciones;
    }

    public void agregarExportacion(Referencia exportacion) {
        if (exportacion != null) {
            exportaciones.add(exportacion);
        }
    }

    @Override
    public String getEtiqueta() {
        return "Programa";
    }

    @Override
    public List<NodoAST> getHijos() {
        List<NodoAST> hijos = new ArrayList<>();
        hijos.addAll(directivas);
        hijos.addAll(agentes);
        hijos.addAll(ejecuciones);
        hijos.addAll(exportaciones);
        return hijos;
    }
}