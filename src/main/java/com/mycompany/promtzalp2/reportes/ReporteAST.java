package com.mycompany.promtzalp2.reportes;

import com.mycompany.promtzalp2.ResultadoCompilacion;
import com.mycompany.promtzalp2.ast.NodoAST;
import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.ast.Termino;
import java.util.List;

/**
 * Reporte HTML del arbol de sintaxis abstracta.
 * Se genera con un recorrido recursivo usando getEtiqueta() y getHijos(),
 * igual que el arbol de la interfaz. Cada nodo se colorea segun su tipo.
 */
public final class ReporteAST {

    /** Leyenda de colores: nombre visible y clase CSS del nodo. */
    private static final String[][] LEYENDA = {
        {"Programa", "n-programa"},
        {"Directiva", "n-directiva"},
        {"Agente", "n-agente"},
        {"Variable", "n-variable"},
        {"Comando", "n-comando"},
        {"Conector", "n-conector"},
        {"Expresion", "n-expresion"},
        {"Cadena", "n-termino-cadena"},
        {"Numero", "n-termino-numero"},
        {"Id", "n-termino-id"},
        {"CARGAR", "n-termino-cargar"},
        {"Referencia", "n-referencia"}
    };

    private ReporteAST() {
    }

    public static String generar(ResultadoCompilacion resultado, String archivo) {
        Programa programa = resultado.getPrograma();
        StringBuilder c = new StringBuilder();

        if (programa == null) {
            c.append("<div class=\"vacio\">No se construy&oacute; ning&uacute;n AST.</div>");
            return PlantillaHTML.documento("Árbol de sintaxis abstracta", archivo, c.toString());
        }

        c.append("<div class=\"tarjetas\">")
         .append(PlantillaHTML.tarjeta("Nodos", contarNodos(programa), ""))
         .append(PlantillaHTML.tarjeta("Agentes", programa.getAgentes().size(), "agente"))
         .append(PlantillaHTML.tarjeta("Directivas", programa.getDirectivas().size(), "variable"))
         .append("</div>");

        // Leyenda de colores de los nodos.
        c.append("<div class=\"leyenda\">");
        for (String[] item : LEYENDA) {
            c.append("<span class=\"").append(item[1]).append("\"><span class=\"clase\">")
             .append(item[0]).append("</span></span>");
        }
        c.append("</div>");

        c.append("<ul class=\"arbol\">");
        nodo(programa, c);
        c.append("</ul>");

        return PlantillaHTML.documento("Árbol de sintaxis abstracta", archivo, c.toString());
    }

    /** Escribe el nodo como elemento de lista y, dentro, la lista de sus hijos. */
    private static void nodo(NodoAST nodo, StringBuilder c) {
        c.append("<li><span class=\"nodo ").append(claseNodo(nodo)).append("\"><span class=\"clase\">")
         .append(nodo.getClass().getSimpleName()).append("</span>")
         .append(PlantillaHTML.escapar(nodo.getEtiqueta())).append("</span>");

        List<NodoAST> hijos = nodo.getHijos();
        if (!hijos.isEmpty()) {
            c.append("<ul>");
            for (NodoAST hijo : hijos) {
                nodo(hijo, c);
            }
            c.append("</ul>");
        }
        c.append("</li>");
    }

    /**
     * Clase CSS del nodo segun su tipo. Los Termino se distinguen
     * ademas por lo que contienen (cadena, numero, id o CARGAR).
     */
    private static String claseNodo(NodoAST nodo) {
        if (nodo instanceof Termino termino) {
            return "n-termino-" + termino.getTipo().name().toLowerCase();
        }
        return "n-" + nodo.getClass().getSimpleName().toLowerCase();
    }

    private static int contarNodos(NodoAST nodo) {
        int total = 1;
        for (NodoAST hijo : nodo.getHijos()) {
            total += contarNodos(hijo);
        }
        return total;
    }
}