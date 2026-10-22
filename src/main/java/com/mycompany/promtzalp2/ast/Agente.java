package com.mycompany.promtzalp2.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Agente: unidad que agrupa un contexto, sus variables y sus comandos.
 * Estructura: AGENTE nombre { contexto variables comandos }
 * Cada agente es un ambito propio: sus variables no son visibles en otros agentes.
 */
public class Agente extends NodoAST {

    private final String nombre;
    /** Rol que asume la IA. Se guarda como Termino de tipo CADENA para conservar su fila. */
    private Termino contexto;
    private final List<Variable> variables = new ArrayList<>();
    private final List<Comando> comandos = new ArrayList<>();

    public Agente(String nombre, int fila, int columna) {
        super(fila, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    public Termino getContexto() {
        return contexto;
    }

    public void setContexto(Termino contexto) {
        this.contexto = contexto;
    }

    public List<Variable> getVariables() {
        return variables;
    }

    public void agregarVariable(Variable variable) {
        if (variable != null) {
            variables.add(variable);
        }
    }

    public List<Comando> getComandos() {
        return comandos;
    }

    public void agregarComando(Comando comando) {
        if (comando != null) {
            comandos.add(comando);
        }
    }

    @Override
    public String getEtiqueta() {
        return "AGENTE " + nombre;
    }

    @Override
    public List<NodoAST> getHijos() {
        List<NodoAST> hijos = new ArrayList<>();
        agregarSiExiste(hijos, contexto);
        hijos.addAll(variables);
        hijos.addAll(comandos);
        return hijos;
    }
}