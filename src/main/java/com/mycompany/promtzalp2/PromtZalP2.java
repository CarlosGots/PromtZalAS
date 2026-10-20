package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.lexer.TipoToken;
import com.mycompany.promtzalp2.lexer.Token;
import java.io.StringReader;

public class PromtZalP2 {

    public static void main(String[] args) throws Exception {
        String entrada = """
                @modelo "claude-sonnet-4-6"
                @color "azul"
                // comentario de linea
                /* comentario
                   de bloque */
                AGENTE analista {
                  contexto = "Eres un analista de datos experto"
                  variable ventas = CARGAR("ventas.csv")
                  RESUMIR ventas EN 100 palabras -> resumen $
                  PREGUNTAR "cadena sin cerrar
                }
                EJECUTAR analista
                EXPORTAR resumen
                """;

        ManejadorErrores errores = new ManejadorErrores();
        Lexer lexer = new Lexer(new StringReader(entrada), errores, false);

        System.out.println("===== TOKENS =====");
        Token t;
        do {
            t = lexer.yylex();
            System.out.println(t);
        } while (t.getTipo() != TipoToken.EOF);

        System.out.println();
        System.out.println("===== ERRORES =====");
        if (!errores.hayErrores()) {
            System.out.println("Sin errores lexicos");
        }
        for (ErrorCompilacion e : errores.getErrores()) {
            System.out.println(e);
        }
    }
}