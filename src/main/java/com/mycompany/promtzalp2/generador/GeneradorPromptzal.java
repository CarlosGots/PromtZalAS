package com.mycompany.promtzalp2.generador;

import com.mycompany.promtzalp2.ast.Agente;
import com.mycompany.promtzalp2.ast.Comando;
import com.mycompany.promtzalp2.ast.Conector;
import com.mycompany.promtzalp2.ast.Expresion;
import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.ast.Referencia;
import com.mycompany.promtzalp2.ast.Termino;
import com.mycompany.promtzalp2.ast.TipoTermino;
import com.mycompany.promtzalp2.ast.Variable;
import java.util.HashSet;
import java.util.Set;

/**
 * Recorre el AST y redacta el promptzal: el prompt final en texto (Markdown).
 * Estructura: encabezado con directivas, una seccion por cada agente
 * ejecutado (en orden de EJECUTAR) y la lista de resultados de EXPORTAR.
 */
public class GeneradorPromptzal {

    /** Punto de entrada: devuelve el texto completo del prompt. */
    public String generar(Programa programa) {
        if (programa == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        encabezado(programa, sb);
        agentes(programa, sb);
        resultados(programa, sb);
        return sb.toString();
    }

    /** Titulo y directivas declaradas (@modelo, @rol, @formato). */
    private void encabezado(Programa programa, StringBuilder sb) {
        sb.append("# Prompt generado con PromptZal\n\n");
        agregarLinea(sb, "Modelo destino", programa.getValorDirectiva("@modelo"));
        agregarLinea(sb, "Rol general", programa.getValorDirectiva("@rol"));
        agregarLinea(sb, "Formato de respuesta", programa.getValorDirectiva("@formato"));
        sb.append("\n");
    }

    /** Una seccion por cada agente de EJECUTAR, en ese orden y sin repetir. */
    private void agentes(Programa programa, StringBuilder sb) {
        Set<String> generados = new HashSet<>();
        for (Referencia ref : programa.getEjecuciones()) {
            Agente agente = programa.buscarAgente(ref.getNombre());
            if (agente == null || !generados.add(agente.getNombre())) {
                continue;
            }
            agente(agente, sb);
        }
    }

    /** Contexto, variables e instrucciones de un agente. */
    private void agente(Agente agente, StringBuilder sb) {
        sb.append("## Agente: ").append(agente.getNombre()).append("\n\n");

        if (agente.getContexto() != null) {
            sb.append("Contexto: ").append(agente.getContexto().getValor()).append("\n\n");
        }

        if (!agente.getVariables().isEmpty()) {
            sb.append("Variables disponibles:\n");
            for (Variable v : agente.getVariables()) {
                sb.append("- ").append(v.getNombre()).append(": ")
                  .append(describirVariable(v)).append("\n");
            }
            sb.append("\n");
        }

        sb.append("Instrucciones:\n");
        int numero = 1;
        for (Comando c : agente.getComandos()) {
            sb.append(numero++).append(". ").append(instruccion(c)).append("\n");
        }
        sb.append("\n");
    }

    /** Lista de resultados pedidos en EXPORTAR. */
    private void resultados(Programa programa, StringBuilder sb) {
        if (programa.getExportaciones().isEmpty()) {
            return;
        }
        sb.append("## Resultado solicitado\n");
        for (Referencia ref : programa.getExportaciones()) {
            sb.append("- ").append(ref.getNombre()).append("\n");
        }
    }

    /** Origen del valor de una variable: un archivo de CARGAR o un literal. */
    private String describirVariable(Variable v) {
        Expresion valor = v.getValor();
        if (valor == null || valor.getTerminos().isEmpty()) {
            return "(sin valor)";
        }
        if (valor.getTerminos().size() == 1) {
            Termino unico = valor.getTerminos().get(0);
            if (unico.getTipo() == TipoTermino.CARGAR) {
                return "contenido del archivo \"" + unico.getValor() + "\" (adjuntar el archivo)";
            }
        }
        return valor.toTexto();
    }

    /**
     * Traduce un comando a una instruccion en lenguaje natural.
     * Para agregar un comando nuevo basta con agregar un case.
     */
    private String instruccion(Comando c) {
        String arg = texto(c.getArgumento());
        String sobre = textoConector(c, "SOBRE");
        String desde = textoConector(c, "DESDE");
        String en = textoConector(c, "EN");
        String como = textoConector(c, "COMO");

        String base;
        switch (c.getNombre()) {
            case "PREGUNTAR":
                base = (sobre != null ? "Basandote en " + sobre + ", responde: " : "Responde: ") + arg;
                break;
            case "GENERAR":
                base = "Genera " + arg + (desde != null ? " a partir de: " + desde : "");
                break;
            case "RESUMIR":
                base = "Resume " + arg + (en != null ? " en un maximo de " + en : "");
                break;
            case "ANALIZAR":
                base = "Analiza " + arg + (sobre != null ? " considerando: " + sobre : "");
                break;
            case "TRADUCIR":
                base = "Traduce " + arg + (en != null ? " al idioma " + en : "");
                break;
            case "CLASIFICAR":
                base = "Clasifica " + arg + (en != null ? " en las categorias: " + en : "");
                break;
            case "EXTRAER":
                base = "Extrae " + arg + (desde != null ? " a partir de " + desde : "");
                break;
            default:
                // Comando nuevo sin traduccion propia: instruccion generica.
                base = "Realiza la accion " + c.getNombre() + " sobre " + arg;
        }

        StringBuilder sb = new StringBuilder(oracion(base));
        if (c.getDestino() != null) {
            String que = c.getNombre().equals("PREGUNTAR") ? "la respuesta" : "el resultado";
            sb.append(" Guarda ").append(que).append(" como ").append(c.getDestino()).append(".");
        }
        if (como != null) {
            sb.append(" Entrega el resultado en formato ").append(como).append(".");
        }
        return sb.toString();
    }

    /** Texto del argumento de un conector, o null si el comando no lo tiene. */
    private String textoConector(Comando c, String nombre) {
        Conector con = c.buscarConector(nombre);
        return con == null ? null : texto(con.getArgumento());
    }

    private String texto(Expresion e) {
        return e == null ? "" : e.toTexto();
    }

    /** Agrega punto final solo si la frase no termina ya en . ? o ! */
    private String oracion(String texto) {
        String t = texto.trim();
        if (t.endsWith(".") || t.endsWith("?") || t.endsWith("!")) {
            return t;
        }
        return t + ".";
    }

    private void agregarLinea(StringBuilder sb, String etiqueta, String valor) {
        if (valor != null) {
            sb.append(etiqueta).append(": ").append(valor).append("\n");
        }
    }
}