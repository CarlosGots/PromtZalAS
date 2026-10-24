package com.mycompany.promtzalp2.semantico;

/**
 * Clasificacion de los simbolos de la tabla.
 */
public enum TipoSimbolo {
    /** Declarado con AGENTE nombre { ... } */
    AGENTE,
    /** Declarado con variable nombre = ... */
    VARIABLE,
    /** Declarado como destino de un comando: ... -> nombre */
    RESULTADO
}