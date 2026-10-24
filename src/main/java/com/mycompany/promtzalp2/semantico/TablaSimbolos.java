package com.mycompany.promtzalp2.semantico;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Registro de todos los agentes y variables del programa.
 * Cada variable pertenece al ambito de su agente: una variable
 * declarada en un agente no es visible desde otro.
 */
public class TablaSimbolos {

    /** Ambito de los agentes (no pertenecen a ningun otro agente). */
    public static final String AMBITO_GLOBAL = "global";

    /** Simbolos en el orden en que se declararon. */
    private final List<Simbolo> simbolos = new ArrayList<>();

    public void agregar(Simbolo simbolo) {
        simbolos.add(simbolo);
    }

    /** Busca una variable o resultado dentro de un ambito. Devuelve null si no existe. */
    public Simbolo buscar(String nombre, String ambito) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() != TipoSimbolo.AGENTE
                    && s.getNombre().equals(nombre)
                    && s.getAmbito().equals(ambito)) {
                return s;
            }
        }
        return null;
    }

    /** Busca un agente por nombre. Devuelve null si no existe. */
    public Simbolo buscarAgente(String nombre) {
        for (Simbolo s : simbolos) {
            if (s.getTipo() == TipoSimbolo.AGENTE && s.getNombre().equals(nombre)) {
                return s;
            }
        }
        return null;
    }

    public List<Simbolo> getSimbolos() {
        return Collections.unmodifiableList(simbolos);
    }
}