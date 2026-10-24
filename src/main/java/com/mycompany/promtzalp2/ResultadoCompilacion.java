package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.lexer.Token;
import com.mycompany.promtzalp2.semantico.TablaSimbolos;
import java.util.List;

/**
 * Todo lo que produce una compilacion. Es lo que recibe la GUI
 * para llenar sus paneles y lo que usan los reportes.
 */
public class ResultadoCompilacion {

    private final List<Token> tokens;
    private final Programa programa;
    private final TablaSimbolos tablaSimbolos;
    private final ManejadorErrores errores;
    private final String promptzal;
    private final long tiempoMs;

    public ResultadoCompilacion(List<Token> tokens, Programa programa, TablaSimbolos tablaSimbolos,
            ManejadorErrores errores, String promptzal, long tiempoMs) {
        this.tokens = tokens;
        this.programa = programa;
        this.tablaSimbolos = tablaSimbolos;
        this.errores = errores;
        this.promptzal = promptzal;
        this.tiempoMs = tiempoMs;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    public Programa getPrograma() {
        return programa;
    }

    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    public ManejadorErrores getErrores() {
        return errores;
    }

    public String getPromptzal() {
        return promptzal;
    }

    public long getTiempoMs() {
        return tiempoMs;
    }

    /** true si la compilacion no tuvo errores de ningun tipo. */
    public boolean esExitosa() {
        return !errores.hayErrores();
    }
}