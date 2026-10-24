package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.generador.GeneradorPromptzal;
import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.lexer.TipoToken;
import com.mycompany.promtzalp2.lexer.Token;
import com.mycompany.promtzalp2.parser.Parser;
import com.mycompany.promtzalp2.semantico.AnalizadorSemantico;
import com.mycompany.promtzalp2.semantico.TablaSimbolos;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquesta las cuatro fases del compilador sobre un texto PromptZal:
 * lexico -> sintactico -> semantico -> generacion del promptzal.
 * Todas las fases registran sus errores en el mismo ManejadorErrores.
 */
public class Compilador {

    public ResultadoCompilacion compilar(String codigo) {
        long inicio = System.nanoTime();
        ManejadorErrores errores = new ManejadorErrores();

        // 1. Lexico: lista de tokens y errores lexicos.
        List<Token> tokens = tokenizar(codigo, errores);

        // 2. Sintactico: un lexer nuevo alimenta al parser.
        //    Se le pasa null como manejador para no duplicar los errores lexicos.
        Lexer lexerParser = new Lexer(new StringReader(codigo), null, false);
        Programa programa = new Parser(lexerParser, errores).parsear();

        // 3. Semantico: tabla de simbolos y validaciones.
        TablaSimbolos tabla = new TablaSimbolos();
        new AnalizadorSemantico(tabla, errores).analizar(programa);

        // 4. Generacion del promptzal a partir del AST.
        String promptzal = new GeneradorPromptzal().generar(programa);

        long tiempoMs = (System.nanoTime() - inicio) / 1_000_000;
        return new ResultadoCompilacion(tokens, programa, tabla, errores, promptzal, tiempoMs);
    }

    /** Recorre todo el texto con el lexer y devuelve los tokens (sin EOF). */
    private List<Token> tokenizar(String codigo, ManejadorErrores errores) {
        List<Token> tokens = new ArrayList<>();
        Lexer lexer = new Lexer(new StringReader(codigo), errores, false);
        try {
            Token t = lexer.yylex();
            while (t.getTipo() != TipoToken.EOF) {
                tokens.add(t);
                t = lexer.yylex();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return tokens;
    }
}