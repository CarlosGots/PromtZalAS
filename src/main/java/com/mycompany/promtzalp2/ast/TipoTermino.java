package com.mycompany.promtzalp2.ast;

/**
 * Tipos de termino que puede contener una expresion.
 * Corresponde a la regla: termino -> CADENA | ID | NUMERO unidad | cargar
 */
public enum TipoTermino {
    CADENA,
    ID,
    NUMERO,
    CARGAR
}