package com.mycompany.promtzalp2.gui;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.lexer.TipoToken;
import com.mycompany.promtzalp2.lexer.Token;
import java.io.IOException;
import java.io.StringReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

/**
 * Calcula los estilos (colores) del editor.
 * Usa el MISMO Lexer del compilador en modo resaltado, por lo que cada color
 * corresponde exactamente a un token que reconoce el lenguaje.
 * RichTextFX solo pinta; quien decide el color de cada fragmento es el Lexer.
 */
public final class ResaltadorSintaxis {

    private ResaltadorSintaxis() {
    }

    /** Estilos de todo el texto: colores de tokens + subrayado de errores. */
    public static StyleSpans<Collection<String>> calcular(String texto, List<ErrorCompilacion> errores) {
        StyleSpans<Collection<String>> tokens = estilosDeTokens(texto);
        if (errores == null || errores.isEmpty()) {
            return tokens;
        }
        // Se superponen las marcas de error sobre los colores de los tokens.
        return tokens.overlay(estilosDeErrores(texto, errores), ResaltadorSintaxis::unir);
    }

    /** Recorre el texto con el Lexer y asigna una clase CSS a cada token. */
    private static StyleSpans<Collection<String>> estilosDeTokens(String texto) {
        StyleSpansBuilder<Collection<String>> spans = new StyleSpansBuilder<>();
        Lexer lexer = new Lexer(new StringReader(texto), null, true);
        int ultimo = 0;
        try {
            Token t = lexer.yylex();
            while (t.getTipo() != TipoToken.EOF) {
                int inicio = t.getOffset();
                int fin = Math.min(inicio + t.getLongitud(), texto.length());
                if (inicio >= ultimo && fin > inicio) {
                    spans.add(Collections.emptyList(), inicio - ultimo);   // espacio sin estilo
                    spans.add(Collections.singleton(claseCss(t.getTipo())), fin - inicio);
                    ultimo = fin;
                }
                t = lexer.yylex();
            }
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        spans.add(Collections.emptyList(), Math.max(0, texto.length() - ultimo));
        return spans.create();
    }

    /** Rangos subrayados en rojo segun la fila, columna y lexema de cada error. */
    private static StyleSpans<Collection<String>> estilosDeErrores(String texto, List<ErrorCompilacion> errores) {
        List<int[]> rangos = new ArrayList<>();
        for (ErrorCompilacion e : errores) {
            int inicio = offset(texto, e.getFila(), e.getColumna());
            if (inicio < 0 || inicio >= texto.length()) {
                continue;
            }
            int largo = Math.max(1, e.getLexema() == null ? 1 : e.getLexema().length());
            rangos.add(new int[]{inicio, Math.min(texto.length(), inicio + largo)});
        }
        rangos.sort(Comparator.comparingInt(r -> r[0]));

        StyleSpansBuilder<Collection<String>> spans = new StyleSpansBuilder<>();
        int ultimo = 0;
        for (int[] r : rangos) {
            int inicio = Math.max(r[0], ultimo);   // evita rangos encimados
            if (r[1] <= inicio) {
                continue;
            }
            spans.add(Collections.emptyList(), inicio - ultimo);
            spans.add(Collections.singleton("marca-error"), r[1] - inicio);
            ultimo = r[1];
        }
        spans.add(Collections.emptyList(), Math.max(0, texto.length() - ultimo));
        return spans.create();
    }

    /** Clase CSS de cada tipo de token. Los colores estan en estilos.css. */
    private static String claseCss(TipoToken tipo) {
        return switch (tipo) {
            case DIRECTIVA -> "tk-directiva";
            case AGENTE, CONTEXTO, VARIABLE, EJECUTAR, EXPORTAR -> "tk-reservada";
            case COMANDO_IA, CARGAR -> "tk-comando";
            case CONECTOR, FLECHA -> "tk-conector";
            case CADENA -> "tk-cadena";
            case NUMERO -> "tk-numero";
            case COMENTARIO -> "tk-comentario";
            case ERROR -> "tk-error";
            case ID -> "tk-id";
            default -> "tk-simbolo";
        };
    }

    /** Convierte fila y columna (desde 1) en posicion absoluta dentro del texto. */
    private static int offset(String texto, int fila, int columna) {
        int linea = 1;
        int i = 0;
        while (linea < fila && i < texto.length()) {
            if (texto.charAt(i) == '\n') {
                linea++;
            }
            i++;
        }
        return i + columna - 1;
    }

    /** Une dos listas de clases CSS (color del token + marca de error). */
    private static Collection<String> unir(Collection<String> a, Collection<String> b) {
        if (b.isEmpty()) {
            return a;
        }
        List<String> union = new ArrayList<>(a);
        union.addAll(b);
        return union;
    }
}