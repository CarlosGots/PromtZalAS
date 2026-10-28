package com.mycompany.promtzalp2.reportes;

import com.mycompany.promtzalp2.ResultadoCompilacion;
import com.mycompany.promtzalp2.semantico.Simbolo;
import com.mycompany.promtzalp2.semantico.TipoSimbolo;
import java.util.List;

/**
 * Reporte HTML de la tabla de simbolos: agentes y variables
 * con su tipo, ambito y posicion de declaracion.
 */
public final class ReporteTablaSimbolos {

    private ReporteTablaSimbolos() {
    }

    public static String generar(ResultadoCompilacion resultado, String archivo) {
        List<Simbolo> simbolos = resultado.getTablaSimbolos().getSimbolos();
        StringBuilder c = new StringBuilder();

        c.append("<div class=\"tarjetas\">")
         .append(PlantillaHTML.tarjeta("Símbolos", simbolos.size(), ""))
         .append(PlantillaHTML.tarjeta("Agentes", contar(simbolos, TipoSimbolo.AGENTE), "agente"))
         .append(PlantillaHTML.tarjeta("Variables", contar(simbolos, TipoSimbolo.VARIABLE), "variable"))
         .append(PlantillaHTML.tarjeta("Resultados", contar(simbolos, TipoSimbolo.RESULTADO), "resultado"))
         .append("</div>");

        if (simbolos.isEmpty()) {
            c.append("<div class=\"vacio\">La tabla de s&iacute;mbolos est&aacute; vac&iacute;a.</div>");
            return PlantillaHTML.documento("Tabla de símbolos", archivo, c.toString());
        }

        c.append("<table><tr><th>#</th><th>Nombre</th><th>Tipo</th><th>Ámbito</th>")
         .append("<th>Fila</th><th>Columna</th></tr>");
        int n = 1;
        for (Simbolo s : simbolos) {
            c.append("<tr><td>").append(n++).append("</td>")
             .append("<td><code>").append(PlantillaHTML.escapar(s.getNombre())).append("</code></td>")
             .append("<td><span class=\"tipo ").append(s.getTipo().name()).append("\">")
             .append(s.getTipo().name()).append("</span></td>")
             .append("<td>").append(PlantillaHTML.escapar(s.getAmbito())).append("</td>")
             .append("<td>").append(s.getFila()).append("</td>")
             .append("<td>").append(s.getColumna()).append("</td></tr>");
        }
        c.append("</table>");

        return PlantillaHTML.documento("Tabla de símbolos", archivo, c.toString());
    }

    private static int contar(List<Simbolo> simbolos, TipoSimbolo tipo) {
        int total = 0;
        for (Simbolo s : simbolos) {
            if (s.getTipo() == tipo) {
                total++;
            }
        }
        return total;
    }
}