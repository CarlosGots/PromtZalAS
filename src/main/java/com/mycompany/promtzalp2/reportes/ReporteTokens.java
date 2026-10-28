package com.mycompany.promtzalp2.reportes;

import com.mycompany.promtzalp2.ResultadoCompilacion;
import com.mycompany.promtzalp2.lexer.Token;
import java.util.List;

/**
 * Reporte HTML de los tokens reconocidos por el analizador lexico.
 * Cada token se colorea segun su categoria (los mismos colores del editor).
 */
public final class ReporteTokens {

    /** Categorias de token: etiqueta visible y clase CSS de su color. */
    private static final String[][] CATEGORIAS = {
        {"Directivas", "tk-directiva"},
        {"Reservadas", "tk-reservada"},
        {"Comandos", "tk-comando"},
        {"Conectores", "tk-conector"},
        {"Cadenas", "tk-cadena"},
        {"Números", "tk-numero"},
        {"Identificadores", "tk-id"},
        {"Símbolos", "tk-simbolo"}
    };

    private ReporteTokens() {
    }

    public static String generar(ResultadoCompilacion resultado, String archivo) {
        List<Token> tokens = resultado.getTokens();
        StringBuilder c = new StringBuilder();

        // Tarjetas: total y cantidad por categoria (funcionan tambien como leyenda de colores).
        c.append("<div class=\"tarjetas\">")
         .append(PlantillaHTML.tarjeta("Total", tokens.size(), ""));
        for (String[] categoria : CATEGORIAS) {
            c.append(PlantillaHTML.tarjeta(categoria[0], contar(tokens, categoria[1]), "tk " + categoria[1]));
        }
        c.append("</div>");

        if (tokens.isEmpty()) {
            c.append("<div class=\"vacio\">No se reconoci&oacute; ning&uacute;n token.</div>");
            return PlantillaHTML.documento("Reporte de tokens", archivo, c.toString());
        }

        c.append("<table><tr><th>#</th><th>Lexema</th><th>Tipo</th><th>Fila</th><th>Columna</th></tr>");
        int n = 1;
        for (Token t : tokens) {
            String clase = "tk " + PlantillaHTML.claseToken(t.getTipo());
            c.append("<tr><td>").append(n++).append("</td>")
             .append("<td><code class=\"").append(clase).append("\">")
             .append(PlantillaHTML.escapar(t.getLexema())).append("</code></td>")
             .append("<td><span class=\"tipo ").append(clase).append("\">")
             .append(t.getTipo().name()).append("</span></td>")
             .append("<td>").append(t.getFila()).append("</td>")
             .append("<td>").append(t.getColumna()).append("</td></tr>");
        }
        c.append("</table>");

        return PlantillaHTML.documento("Reporte de tokens", archivo, c.toString());
    }

    /** Cuantos tokens pertenecen a la categoria con esa clase CSS. */
    private static int contar(List<Token> tokens, String claseCategoria) {
        int total = 0;
        for (Token t : tokens) {
            if (PlantillaHTML.claseToken(t.getTipo()).equals(claseCategoria)) {
                total++;
            }
        }
        return total;
    }
}