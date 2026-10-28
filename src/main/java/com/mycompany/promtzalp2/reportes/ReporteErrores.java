package com.mycompany.promtzalp2.reportes;

import com.mycompany.promtzalp2.ResultadoCompilacion;
import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.errores.TipoError;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Reporte HTML de todos los errores de la compilacion (lexicos,
 * sintacticos y semanticos). Si no hay errores, lo indica explicitamente.
 */
public final class ReporteErrores {

    private ReporteErrores() {
    }

    public static String generar(ResultadoCompilacion resultado, String archivo) {
        ManejadorErrores errores = resultado.getErrores();
        StringBuilder c = new StringBuilder();

        c.append("<div class=\"tarjetas\">")
         .append(PlantillaHTML.tarjeta("Total", errores.getErrores().size(), ""))
         .append(PlantillaHTML.tarjeta("Léxicos", errores.cantidad(TipoError.LEXICO), "lexico"))
         .append(PlantillaHTML.tarjeta("Sintácticos", errores.cantidad(TipoError.SINTACTICO), "sintactico"))
         .append(PlantillaHTML.tarjeta("Semánticos", errores.cantidad(TipoError.SEMANTICO), "semantico"))
         .append("</div>");

        if (!errores.hayErrores()) {
            c.append("<div class=\"vacio\">&#10004; No se encontraron errores l&eacute;xicos, ")
             .append("sint&aacute;cticos ni sem&aacute;nticos.</div>");
            return PlantillaHTML.documento("Reporte de errores", archivo, c.toString());
        }

        // Ordenados por posicion en el archivo.
        List<ErrorCompilacion> lista = new ArrayList<>(errores.getErrores());
        lista.sort(Comparator.comparingInt(ErrorCompilacion::getFila)
                .thenComparingInt(ErrorCompilacion::getColumna));

        c.append("<table><tr><th>#</th><th>Tipo</th><th>Lexema</th><th>Descripción</th>")
         .append("<th>Fila</th><th>Columna</th></tr>");
        int n = 1;
        for (ErrorCompilacion e : lista) {
            c.append("<tr><td>").append(n++).append("</td>")
             .append("<td><span class=\"tipo ").append(e.getTipo().name()).append("\">")
             .append(e.getTipo().name()).append("</span></td>")
             .append("<td><code>").append(PlantillaHTML.escapar(e.getLexema())).append("</code></td>")
             .append("<td>").append(PlantillaHTML.escapar(e.getDescripcion())).append("</td>")
             .append("<td>").append(e.getFila()).append("</td>")
             .append("<td>").append(e.getColumna()).append("</td></tr>");
        }
        c.append("</table>");

        return PlantillaHTML.documento("Reporte de errores", archivo, c.toString());
    }
}