package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.ast.NodoAST;
import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.parser.Parser;
import java.io.StringReader;

public class PromtZalP2 {

    public static void main(String[] args) {
        String entrada = """
                @modelo "claude-sonnet-4-6"
                @rol analista
                AGENTE analista {
                  contexto = "Eres un analista"
                  variable ventas = = CARGAR("ventas.csv")
                  PREGUNTAR "tendencias?" SOBRE ventas tendencias
                  RESUMIR tendencias EN 100 palabras -> resumen
                }
                AGENTE traductor {
                  contexto = "Eres un traductor"
                  TRADUCIR "hola" EN "ingles" -> resultado
                EJECUTAR analista
                EJECUTAR
                EXPORTAR resumen
                """;

        ManejadorErrores errores = new ManejadorErrores();
        Lexer lexer = new Lexer(new StringReader(entrada), errores, false);
        Parser parser = new Parser(lexer, errores);
        Programa programa = parser.parsear();

        System.out.println("===== AST =====");
        imprimir(programa, 0);

        System.out.println();
        System.out.println("===== ERRORES =====");
        if (!errores.hayErrores()) {
            System.out.println("Sin errores");
        }
        for (ErrorCompilacion e : errores.getErrores()) {
            System.out.println(e);
        }
    }

    /** Imprime el arbol con sangria segun el nivel de profundidad. */
    private static void imprimir(NodoAST nodo, int nivel) {
        System.out.println("  ".repeat(nivel) + nodo.getEtiqueta());
        for (NodoAST hijo : nodo.getHijos()) {
            imprimir(hijo, nivel + 1);
        }
    }
}