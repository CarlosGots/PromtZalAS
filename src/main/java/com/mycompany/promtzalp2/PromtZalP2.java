package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.semantico.Simbolo;

public class PromtZalP2 {

    public static void main(String[] args) {
        String valido = """
                @modelo "claude-sonnet-4-6"
                @rol "analista de datos"
                @formato "markdown"
                // Agente que prepara el analisis de un archivo de ventas
                AGENTE analista {
                  contexto = "Eres un analista de datos experto"
                  variable ventas = CARGAR("ventas.csv")
                  PREGUNTAR "Cuales son las 3 tendencias principales?" SOBRE ventas -> tendencias
                  GENERAR codigo DESDE "grafica de barras de " + tendencias -> script
                  RESUMIR tendencias EN 100 palabras -> resumen
                }
                EJECUTAR analista
                EXPORTAR resumen, script
                """;

        String conErrores = """
                @modelo "claude-sonnet-4-6"
                AGENTE analista {
                  contexto = "Eres un analista"
                  variable ventas = CARGAR("ventas.csv")
                  RESUMIR datos EN 100 palabras -> resumen
                }
                AGENTE traductor {
                  contexto = "Eres un traductor"
                  TRADUCIR resumen EN "ingles" -> traduccion
                }
                AGENTE analista {
                  contexto = "Duplicado"
                  RESUMIR "texto" -> otro
                }
                AGENTE escritor {
                  contexto = "Eres escritor"
                  GENERAR portada DESDE "un titulo llamativo" -> titulo
                }
                EJECUTAR analista
                EJECUTAR traductor
                EJECUTAR revisor
                EXPORTAR resumen, titulo
                """;

        Compilador compilador = new Compilador();
        mostrar("PROGRAMA VALIDO", compilador.compilar(valido));
        mostrar("PROGRAMA CON ERRORES SEMANTICOS", compilador.compilar(conErrores));
    }

    private static void mostrar(String titulo, ResultadoCompilacion r) {
        System.out.println("################ " + titulo + " ################");
        System.out.println("Tokens: " + r.getTokens().size() + " | Tiempo: " + r.getTiempoMs() + " ms");

        System.out.println("\n===== TABLA DE SIMBOLOS =====");
        for (Simbolo s : r.getTablaSimbolos().getSimbolos()) {
            System.out.println(s);
        }

        System.out.println("\n===== ERRORES =====");
        if (r.esExitosa()) {
            System.out.println("Sin errores");
        }
        for (ErrorCompilacion e : r.getErrores().getErrores()) {
            System.out.println(e);
        }

        System.out.println("\n===== PROMPTZAL =====");
        System.out.println(r.getPromptzal());
    }
}