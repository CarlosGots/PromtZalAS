package com.mycompany.promtzalp2.semantico;

import com.mycompany.promtzalp2.ast.Agente;
import com.mycompany.promtzalp2.ast.Comando;
import com.mycompany.promtzalp2.ast.Conector;
import com.mycompany.promtzalp2.ast.Expresion;
import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.ast.Referencia;
import com.mycompany.promtzalp2.ast.Termino;
import com.mycompany.promtzalp2.ast.TipoTermino;
import com.mycompany.promtzalp2.ast.Variable;
import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.errores.TipoError;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Recorre el AST, llena la tabla de simbolos y aplica las validaciones semanticas:
 *  1. Un identificador usado debe estar declarado antes, en el mismo agente.
 *  2. No puede haber dos agentes con el mismo nombre.
 *  3. EJECUTAR solo puede referirse a un agente que exista.
 *  4. EXPORTAR solo puede referirse a variables de algun agente ejecutado.
 *
 * Excepciones: el primer identificador de GENERAR (tipo de contenido) y la
 * unidad de un numero (ej. "palabras") no se validan como variables.
 */
public class AnalizadorSemantico {

    private final TablaSimbolos tabla;
    private final ManejadorErrores errores;

    public AnalizadorSemantico(TablaSimbolos tabla, ManejadorErrores errores) {
        this.tabla = tabla;
        this.errores = errores;
    }

    /** Punto de entrada: analiza el programa completo. */
    public void analizar(Programa programa) {
        if (programa == null) {
            return;
        }
        for (Agente agente : programa.getAgentes()) {
            analizarAgente(agente);
        }
        Set<String> ejecutados = validarEjecuciones(programa);
        validarExportaciones(programa, ejecutados);
    }

    /** Registra el agente y valida su cuerpo en el orden en que esta escrito. */
    private void analizarAgente(Agente agente) {
        String nombre = agente.getNombre();

        // Agente sin nombre por un error sintactico: no se puede analizar.
        if (nombre.startsWith("(")) {
            return;
        }

        // Regla 2: agentes duplicados. Su cuerpo no se analiza para no mezclar ambitos.
        Simbolo previo = tabla.buscarAgente(nombre);
        if (previo != null) {
            reportar(nombre, "El agente '" + nombre + "' ya fue declarado en la fila " + previo.getFila(),
                    agente.getFila(), agente.getColumna());
            return;
        }
        tabla.agregar(new Simbolo(nombre, TipoSimbolo.AGENTE, TablaSimbolos.AMBITO_GLOBAL,
                agente.getFila(), agente.getColumna()));

        // Variables: primero se valida su valor y despues se declaran.
        for (Variable v : agente.getVariables()) {
            validarExpresion(v.getValor(), nombre, false);
            tabla.agregar(new Simbolo(v.getNombre(), TipoSimbolo.VARIABLE, nombre,
                    v.getFila(), v.getColumna()));
        }

        // Comandos: se validan sus argumentos y despues se declara su destino.
        for (Comando c : agente.getComandos()) {
            boolean esGenerar = c.getNombre().equals("GENERAR");
            validarExpresion(c.getArgumento(), nombre, esGenerar);
            for (Conector con : c.getConectores()) {
                validarExpresion(con.getArgumento(), nombre, false);
            }
            if (c.getDestino() != null) {
                tabla.agregar(new Simbolo(c.getDestino(), TipoSimbolo.RESULTADO, nombre,
                        c.getFila(), c.getColumna()));
            }
        }
    }

    /**
     * Regla 1: cada identificador de la expresion debe estar declarado en el ambito.
     * @param primerIdEsTipo true en GENERAR: su primer identificador es el tipo de contenido.
     */
    private void validarExpresion(Expresion expresion, String ambito, boolean primerIdEsTipo) {
        if (expresion == null) {
            return;
        }
        List<Termino> terminos = expresion.getTerminos();
        for (int i = 0; i < terminos.size(); i++) {
            Termino t = terminos.get(i);
            if (t.getTipo() != TipoTermino.ID) {
                continue;
            }
            if (i == 0 && primerIdEsTipo) {
                continue;
            }
            if (tabla.buscar(t.getValor(), ambito) == null) {
                reportar(t.getValor(), "La variable '" + t.getValor()
                        + "' no esta declarada antes de usarse en el agente '" + ambito + "'",
                        t.getFila(), t.getColumna());
            }
        }
    }

    /** Regla 3: cada EJECUTAR debe referirse a un agente existente. Devuelve los ejecutados validos. */
    private Set<String> validarEjecuciones(Programa programa) {
        Set<String> ejecutados = new LinkedHashSet<>();
        for (Referencia ref : programa.getEjecuciones()) {
            if (tabla.buscarAgente(ref.getNombre()) == null) {
                reportar(ref.getNombre(), "EJECUTAR se refiere al agente '" + ref.getNombre()
                        + "', que no existe", ref.getFila(), ref.getColumna());
            } else {
                ejecutados.add(ref.getNombre());
            }
        }
        return ejecutados;
    }

    /** Regla 4: cada variable exportada debe existir en algun agente ejecutado. */
    private void validarExportaciones(Programa programa, Set<String> ejecutados) {
        for (Referencia ref : programa.getExportaciones()) {
            boolean existe = false;
            for (String agente : ejecutados) {
                if (tabla.buscar(ref.getNombre(), agente) != null) {
                    existe = true;
                    break;
                }
            }
            if (!existe) {
                reportar(ref.getNombre(), "EXPORTAR se refiere a '" + ref.getNombre()
                        + "', que no existe en ningun agente ejecutado", ref.getFila(), ref.getColumna());
            }
        }
    }

    private void reportar(String lexema, String descripcion, int fila, int columna) {
        errores.agregar(new ErrorCompilacion(TipoError.SEMANTICO, lexema, descripcion, fila, columna));
    }
}