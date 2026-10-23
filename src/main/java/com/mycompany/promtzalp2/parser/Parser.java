package com.mycompany.promtzalp2.parser;

import com.mycompany.promtzalp2.ast.Agente;
import com.mycompany.promtzalp2.ast.Comando;
import com.mycompany.promtzalp2.ast.Conector;
import com.mycompany.promtzalp2.ast.Directiva;
import com.mycompany.promtzalp2.ast.Expresion;
import com.mycompany.promtzalp2.ast.Programa;
import com.mycompany.promtzalp2.ast.Referencia;
import com.mycompany.promtzalp2.ast.Termino;
import com.mycompany.promtzalp2.ast.TipoTermino;
import com.mycompany.promtzalp2.ast.Variable;
import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.errores.TipoError;
import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.lexer.TipoToken;
import com.mycompany.promtzalp2.lexer.Token;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Analizador sintactico descendente recursivo escrito a mano.
 * Cada no terminal de la gramatica corresponde a un metodo de esta clase.
 * Lee los tokens del Lexer de uno en uno (lookahead de 1) y construye el AST.
 *
 * Ante un error sintactico usa recuperacion en modo panico: reporta el error,
 * descarta tokens hasta uno de sincronizacion y continua, de modo que todos
 * los errores del archivo se reportan en una sola corrida.
 */
public class Parser {

    private final Lexer lexer;
    private final ManejadorErrores errores;

    /** Token que se esta analizando en este momento. */
    private Token actual;

    /** Raiz del AST. Es atributo para conservar lo construido aunque haya errores. */
    private Programa ast;

    public Parser(Lexer lexer, ManejadorErrores errores) {
        this.lexer = lexer;
        this.errores = errores;
        avanzar();
    }

    /** Punto de entrada: analiza todo el archivo y devuelve el AST. */
    public Programa parsear() {
        try {
            programa();
        } catch (ErrorSintactico e) {
            // Respaldo: los errores normales se recuperan dentro de cada metodo.
        }
        return ast;
    }

    // =====================================================================
    //  Metodos de la gramatica (uno por no terminal)
    // =====================================================================

    /** programa -> directivas agentes ejecuciones exportar EOF */
    private void programa() {
        ast = new Programa();
        directivas();
        agentes();
        ejecuciones();
        exportar();
        if (!verificar(TipoToken.EOF)) {
            reportarError("fin del archivo");
        }
    }

    /** directivas -> DIRECTIVA CADENA directivas | ε */
    private void directivas() {
        if (verificar(TipoToken.DIRECTIVA)) {
            try {
                Token directiva = consumir(TipoToken.DIRECTIVA, "una directiva");
                Token valor = consumir(TipoToken.CADENA, "una cadena con el valor de la directiva");
                ast.agregarDirectiva(new Directiva(directiva.getLexema(), sinComillas(valor.getLexema()),
                        directiva.getFila(), directiva.getColumna()));
            } catch (ErrorSintactico e) {
                sincronizar();
            }
            directivas();
        }
    }

    /** agentes -> agente mas_agentes */
    private void agentes() {
        agente();
        masAgentes();
    }

    /** mas_agentes -> agente mas_agentes | ε */
    private void masAgentes() {
        if (verificar(TipoToken.AGENTE)) {
            agente();
            masAgentes();
        }
    }

    /** agente -> AGENTE ID LLAVE_A contexto variables comandos LLAVE_C */
    private void agente() {
        // Si ni siquiera empieza con AGENTE, no hay agente que analizar.
        if (!verificar(TipoToken.AGENTE)) {
            reportarError("AGENTE");
            sincronizar();
            return;
        }

        Token inicio = actual;
        String nombre = "(sin nombre)";

        // Encabezado: AGENTE ID {
        try {
            consumir(TipoToken.AGENTE, "AGENTE");
            nombre = consumir(TipoToken.ID, "el nombre del agente").getLexema();
            consumir(TipoToken.LLAVE_A, "'{'");
        } catch (ErrorSintactico e) {
            sincronizar();
        }

        Agente agente = new Agente(nombre, inicio.getFila(), inicio.getColumna());
        ast.agregarAgente(agente);

        // Cuerpo: cada parte tiene su propia recuperacion.
        contexto(agente);
        variables(agente);
        comandos(agente);

        // Cierre: }
        try {
            consumir(TipoToken.LLAVE_C, "'}'");
        } catch (ErrorSintactico e) {
            sincronizar();
        }
    }

    /** contexto -> CONTEXTO IGUAL CADENA */
    private void contexto(Agente agente) {
        try {
            consumir(TipoToken.CONTEXTO, "'contexto' como primera instruccion del agente");
            consumir(TipoToken.IGUAL, "'='");
            Token valor = consumir(TipoToken.CADENA, "una cadena con el contexto");
            agente.setContexto(new Termino(TipoTermino.CADENA, sinComillas(valor.getLexema()), null,
                    valor.getFila(), valor.getColumna()));
        } catch (ErrorSintactico e) {
            sincronizar();
        }
    }

    /** variables -> VARIABLE ID IGUAL expresion variables | ε */
    private void variables(Agente agente) {
        if (verificar(TipoToken.VARIABLE)) {
            try {
                consumir(TipoToken.VARIABLE, "'variable'");
                Token nombre = consumir(TipoToken.ID, "el nombre de la variable");
                consumir(TipoToken.IGUAL, "'='");
                Expresion valor = expresion();
                agente.agregarVariable(new Variable(nombre.getLexema(), valor,
                        nombre.getFila(), nombre.getColumna()));
            } catch (ErrorSintactico e) {
                sincronizar();
            }
            variables(agente);
        }
    }

    /** comandos -> comando mas_comandos */
    private void comandos(Agente agente) {
        comando(agente);
        masComandos(agente);
    }

    /**
     * mas_comandos -> comando mas_comandos | ε
     * Si aparece algo que no es comando ni cierre del agente, se reporta,
     * se descarta y se sigue buscando comandos.
     */
    private void masComandos(Agente agente) {
        if (verificar(TipoToken.COMANDO_IA)) {
            comando(agente);
            masComandos(agente);
        } else if (!esFinDeAgente()) {
            reportarError("un comando de IA o '}'");
            avanzar();      // consume al menos un token para no ciclarse
            sincronizar();
            masComandos(agente);
        }
    }

    /** comando -> COMANDO_IA expresion conectores FLECHA ID */
    private void comando(Agente agente) {
        try {
            Token cmd = consumir(TipoToken.COMANDO_IA, "un comando de IA (PREGUNTAR, GENERAR, RESUMIR...)");

            Comando comando = new Comando(cmd.getLexema(), cmd.getFila(), cmd.getColumna());
            agente.agregarComando(comando);

            comando.setArgumento(expresion());
            conectores(comando);
            consumir(TipoToken.FLECHA, "'->'");
            Token destino = consumir(TipoToken.ID, "el nombre de la variable destino despues de '->'");
            comando.setDestino(destino.getLexema());
        } catch (ErrorSintactico e) {
            sincronizar();
        }
    }

    /** conectores -> CONECTOR expresion conectores | ε */
    private void conectores(Comando comando) {
        if (verificar(TipoToken.CONECTOR)) {
            Token conector = consumir(TipoToken.CONECTOR, "un conector");
            Expresion argumento = expresion();
            comando.agregarConector(new Conector(conector.getLexema(), argumento,
                    conector.getFila(), conector.getColumna()));
            conectores(comando);
        }
    }

    /** expresion -> termino mas_terminos */
    private Expresion expresion() {
        Expresion expresion = new Expresion(actual.getFila(), actual.getColumna());
        expresion.agregarTermino(termino());
        masTerminos(expresion);
        return expresion;
    }

    /** mas_terminos -> MAS termino mas_terminos | ε */
    private void masTerminos(Expresion expresion) {
        if (verificar(TipoToken.MAS)) {
            consumir(TipoToken.MAS, "'+'");
            expresion.agregarTermino(termino());
            masTerminos(expresion);
        }
    }

    /** termino -> CADENA | ID | NUMERO unidad | cargar */
    private Termino termino() {
        Token t = actual;
        switch (t.getTipo()) {
            case CADENA:
                consumir(TipoToken.CADENA, "una cadena");
                return new Termino(TipoTermino.CADENA, sinComillas(t.getLexema()), null,
                        t.getFila(), t.getColumna());
            case ID:
                consumir(TipoToken.ID, "un identificador");
                return new Termino(TipoTermino.ID, t.getLexema(), null,
                        t.getFila(), t.getColumna());
            case NUMERO:
                consumir(TipoToken.NUMERO, "un numero");
                String unidad = unidad();
                return new Termino(TipoTermino.NUMERO, t.getLexema(), unidad,
                        t.getFila(), t.getColumna());
            case CARGAR:
                return cargar();
            default:
                throw errorSintactico("un valor (cadena, identificador, numero o CARGAR)");
        }
    }

    /** unidad -> ID | ε   (ej. "palabras" en 100 palabras) */
    private String unidad() {
        if (verificar(TipoToken.ID)) {
            return consumir(TipoToken.ID, "una unidad").getLexema();
        }
        return null;
    }

    /** cargar -> CARGAR PAR_A CADENA PAR_C */
    private Termino cargar() {
        Token inicio = consumir(TipoToken.CARGAR, "CARGAR");
        consumir(TipoToken.PAR_A, "'('");
        Token archivo = consumir(TipoToken.CADENA, "el nombre del archivo entre comillas");
        consumir(TipoToken.PAR_C, "')'");
        return new Termino(TipoTermino.CARGAR, sinComillas(archivo.getLexema()), null,
                inicio.getFila(), inicio.getColumna());
    }

    /** ejecuciones -> EJECUTAR ID mas_ejecuciones */
    private void ejecuciones() {
        try {
            consumir(TipoToken.EJECUTAR, "EJECUTAR");
            Token nombre = consumir(TipoToken.ID, "el nombre del agente a ejecutar");
            ast.agregarEjecucion(new Referencia("EJECUTAR", nombre.getLexema(),
                    nombre.getFila(), nombre.getColumna()));
        } catch (ErrorSintactico e) {
            sincronizar();
        }
        masEjecuciones();
    }

    /** mas_ejecuciones -> EJECUTAR ID mas_ejecuciones | ε */
    private void masEjecuciones() {
        if (verificar(TipoToken.EJECUTAR)) {
            try {
                consumir(TipoToken.EJECUTAR, "EJECUTAR");
                Token nombre = consumir(TipoToken.ID, "el nombre del agente a ejecutar");
                ast.agregarEjecucion(new Referencia("EJECUTAR", nombre.getLexema(),
                        nombre.getFila(), nombre.getColumna()));
            } catch (ErrorSintactico e) {
                sincronizar();
            }
            masEjecuciones();
        }
    }

    /** exportar -> EXPORTAR ID mas_ids */
    private void exportar() {
        try {
            consumir(TipoToken.EXPORTAR, "EXPORTAR");
            Token nombre = consumir(TipoToken.ID, "el nombre de la variable a exportar");
            ast.agregarExportacion(new Referencia("EXPORTAR", nombre.getLexema(),
                    nombre.getFila(), nombre.getColumna()));
            masIds();
        } catch (ErrorSintactico e) {
            sincronizar();
        }
    }

    /** mas_ids -> COMA ID mas_ids | ε */
    private void masIds() {
        if (verificar(TipoToken.COMA)) {
            consumir(TipoToken.COMA, "','");
            Token nombre = consumir(TipoToken.ID, "el nombre de la variable a exportar");
            ast.agregarExportacion(new Referencia("EXPORTAR", nombre.getLexema(),
                    nombre.getFila(), nombre.getColumna()));
            masIds();
        }
    }

    // =====================================================================
    //  Recuperacion de errores (modo panico)
    // =====================================================================

    /** Descarta tokens hasta encontrar uno de sincronizacion (o el fin del archivo). */
    private void sincronizar() {
        while (!esTokenSincronizacion(actual.getTipo())) {
            avanzar();
        }
    }

    /** Tokens que marcan con seguridad el inicio de una nueva parte del programa. */
    private static boolean esTokenSincronizacion(TipoToken tipo) {
        return switch (tipo) {
            case AGENTE, LLAVE_C, COMANDO_IA, VARIABLE, EJECUTAR, EXPORTAR,
                 CONTEXTO, DIRECTIVA, EOF -> true;
            default -> false;
        };
    }

    /** Tokens con los que termina el cuerpo de un agente (o el archivo). */
    private boolean esFinDeAgente() {
        return switch (actual.getTipo()) {
            case LLAVE_C, AGENTE, EJECUTAR, EXPORTAR, EOF -> true;
            default -> false;
        };
    }

    // =====================================================================
    //  Metodos auxiliares
    // =====================================================================

    /** Pide al lexer el siguiente token. */
    private void avanzar() {
        try {
            actual = lexer.yylex();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Indica si el token actual es del tipo dado, sin consumirlo. */
    private boolean verificar(TipoToken tipo) {
        return actual.getTipo() == tipo;
    }

    /**
     * Si el token actual es del tipo esperado, lo consume y lo devuelve.
     * Si no, reporta el error y lanza la excepcion para iniciar la recuperacion.
     */
    private Token consumir(TipoToken tipo, String esperado) {
        if (verificar(tipo)) {
            Token consumido = actual;
            avanzar();
            return consumido;
        }
        throw errorSintactico(esperado);
    }

    /** Registra un error sintactico indicando lo esperado y lo encontrado. */
    private void reportarError(String esperado) {
        String encontrado = verificar(TipoToken.EOF) ? "fin del archivo" : "'" + actual.getLexema() + "'";
        errores.agregar(new ErrorCompilacion(TipoError.SINTACTICO, actual.getLexema(),
                "Se esperaba " + esperado + " pero se encontro " + encontrado,
                actual.getFila(), actual.getColumna()));
    }

    /** Registra el error y crea la excepcion que lleva al punto de recuperacion mas cercano. */
    private ErrorSintactico errorSintactico(String esperado) {
        reportarError(esperado);
        return new ErrorSintactico();
    }

    /** Quita las comillas de un lexema de cadena: "hola" -> hola */
    private static String sinComillas(String lexema) {
        if (lexema.length() >= 2 && lexema.startsWith("\"") && lexema.endsWith("\"")) {
            return lexema.substring(1, lexema.length() - 1);
        }
        return lexema;
    }

    /** Excepcion interna que salta desde el error hasta su punto de recuperacion. */
    private static class ErrorSintactico extends RuntimeException {
    }
}