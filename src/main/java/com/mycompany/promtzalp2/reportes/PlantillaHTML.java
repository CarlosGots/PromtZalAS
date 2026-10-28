package com.mycompany.promtzalp2.reportes;

import com.mycompany.promtzalp2.lexer.TipoToken;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Plantilla comun de todos los reportes: estructura HTML, estilos,
 * colores por categoria, escape de caracteres y guardado en "reportes".
 */
public final class PlantillaHTML {

    /** Carpeta (en la raiz del proyecto) donde se guardan los reportes. */
    public static final String CARPETA = "reportes";

    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    private PlantillaHTML() {
    }

    /** Arma el documento HTML completo con encabezado, cuerpo y pie. */
    public static String documento(String titulo, String archivoFuente, String cuerpo) {
        return "<!DOCTYPE html>\n<html lang=\"es\">\n<head>\n"
                + "<meta charset=\"UTF-8\">\n"
                + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n"
                + "<title>" + escapar(titulo) + " - PromptZal</title>\n"
                + "<style>" + ESTILOS + "</style>\n"
                + "</head>\n<body>\n"
                + "<header><h1>" + escapar(titulo) + "</h1>"
                + "<p>Archivo: <strong>" + escapar(archivoFuente) + "</strong> &middot; Generado: "
                + LocalDateTime.now().format(FECHA) + "</p></header>\n"
                + "<main>\n" + cuerpo + "\n</main>\n"
                + "<footer>PromptZal IDE &middot; Laboratorio de Lenguajes Formales y de Programaci&oacute;n "
                + "&middot; USAC CUNOC 2026</footer>\n"
                + "</body>\n</html>\n";
    }

    /** Tarjeta de resumen con un numero (ej. "Lexicos: 3"). */
    public static String tarjeta(String etiqueta, int valor, String clase) {
        return "<div class=\"tarjeta " + clase + "\"><div class=\"valor\">" + valor
                + "</div><div class=\"etiqueta\">" + escapar(etiqueta) + "</div></div>";
    }

    /**
     * Clase CSS de la categoria de un token. Usa las mismas categorias
     * y colores que el resaltado del editor.
     */
    public static String claseToken(TipoToken tipo) {
        return switch (tipo) {
            case DIRECTIVA -> "tk-directiva";
            case AGENTE, CONTEXTO, VARIABLE, EJECUTAR, EXPORTAR -> "tk-reservada";
            case COMANDO_IA, CARGAR -> "tk-comando";
            case CONECTOR, FLECHA -> "tk-conector";
            case CADENA -> "tk-cadena";
            case NUMERO -> "tk-numero";
            case ID -> "tk-id";
            default -> "tk-simbolo";
        };
    }

    /** Evita que el texto del usuario rompa el HTML (<, >, &, comillas). */
    public static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("&", "&amp;")
                    .replace("<", "&lt;")
                    .replace(">", "&gt;")
                    .replace("\"", "&quot;");
    }

    /** Guarda el reporte en la carpeta "reportes" y devuelve el archivo creado. */
    public static File guardar(String nombreArchivo, String html) throws IOException {
        File carpeta = new File(CARPETA);
        if (!carpeta.exists() && !carpeta.mkdirs()) {
            throw new IOException("No se pudo crear la carpeta " + carpeta.getAbsolutePath());
        }
        File destino = new File(carpeta, nombreArchivo);
        Files.writeString(destino.toPath(), html, StandardCharsets.UTF_8);
        return destino;
    }

    private static final String ESTILOS = """
            body { font-family: 'Segoe UI', Roboto, sans-serif; margin: 0; background: #f4f6fa; color: #1f2430; }
            header { background: linear-gradient(135deg, #1e1e2e, #2b3a55); color: #fff; padding: 28px 40px; }
            header h1 { margin: 0; font-size: 26px; }
            header p { margin: 6px 0 0; opacity: .85; font-size: 14px; }
            main { padding: 28px 40px; }
            footer { padding: 16px 40px 28px; color: #889; font-size: 12px; }

            /* ---------- Tarjetas de resumen ---------- */
            .tarjetas { display: flex; gap: 16px; flex-wrap: wrap; margin-bottom: 24px; }
            .tarjeta { background: #fff; border-radius: 10px; padding: 14px 22px; min-width: 130px;
                       box-shadow: 0 1px 4px rgba(0,0,0,.08); border-left: 5px solid #2ec4b6; }
            .tarjeta .valor { font-size: 26px; font-weight: 700; }
            .tarjeta .etiqueta { font-size: 12px; color: #667; text-transform: uppercase; letter-spacing: .5px; }
            .tarjeta.lexico { border-color: #e08a3c; }
            .tarjeta.sintactico { border-color: #d64545; }
            .tarjeta.semantico { border-color: #b04fc0; }
            .tarjeta.agente { border-color: #3f7fc4; }
            .tarjeta.variable { border-color: #2a9d8f; }
            .tarjeta.resultado { border-color: #6a8f3a; }

            /* ---------- Tablas ---------- */
            table { width: 100%; border-collapse: collapse; background: #fff; border-radius: 10px;
                    overflow: hidden; box-shadow: 0 1px 4px rgba(0,0,0,.08); }
            th { background: #2b3a55; color: #fff; text-align: left; padding: 10px 12px; font-size: 13px; }
            td { padding: 9px 12px; border-bottom: 1px solid #eceff4; font-size: 14px; vertical-align: top; }
            tr:nth-child(even) td { background: #fafbfd; }
            code { font-family: Consolas, monospace; background: #eef1f6; padding: 2px 6px; border-radius: 4px; }

            /* ---------- Etiquetas de tipo (errores y simbolos) ---------- */
            .tipo { padding: 3px 9px; border-radius: 12px; font-size: 12px; font-weight: 600; color: #fff; white-space: nowrap; }
            .LEXICO { background: #e08a3c; } .SINTACTICO { background: #d64545; } .SEMANTICO { background: #b04fc0; }
            .AGENTE { background: #3f7fc4; } .VARIABLE { background: #2a9d8f; } .RESULTADO { background: #6a8f3a; }
            .vacio { background: #e9f7ef; color: #1e7d4a; padding: 18px 22px; border-radius: 10px; font-weight: 600; }

            /* ---------- Colores por categoria de token (mismos que el editor) ---------- */
            .tk-directiva { --c: #1a9e93; }   /* teal */
            .tk-reservada { --c: #3f7fc4; }   /* azul */
            .tk-comando   { --c: #b04fc0; }   /* magenta */
            .tk-conector  { --c: #e08a3c; }   /* naranja */
            .tk-cadena    { --c: #d64545; }   /* rojo */
            .tk-numero    { --c: #4f9a2f; }   /* verde */
            .tk-id        { --c: #5c6b80; }   /* gris azulado */
            .tk-simbolo   { --c: #8a94a6; }   /* gris */
            .tipo.tk { background: var(--c); }
            code.tk { color: var(--c); font-weight: 600; }
            .tarjeta.tk { border-color: var(--c); }

            /* ---------- Colores por tipo de nodo del AST ---------- */
            .n-programa  { --c: #2b3a55; }
            .n-directiva { --c: #1a9e93; }
            .n-agente    { --c: #3f7fc4; }
            .n-variable  { --c: #2a9d8f; }
            .n-comando   { --c: #b04fc0; }
            .n-conector  { --c: #e08a3c; }
            .n-expresion { --c: #7a8699; }
            .n-referencia { --c: #6a8f3a; }
            .n-termino-cadena { --c: #d64545; }
            .n-termino-numero { --c: #4f9a2f; }
            .n-termino-id     { --c: #5c6b80; }
            .n-termino-cargar { --c: #8e44ad; }

            /* ---------- Arbol del AST ---------- */
            .arbol, .arbol ul { list-style: none; margin: 0; padding-left: 26px; }
            .arbol { padding-left: 0; }
            .arbol li { position: relative; padding: 4px 0; }
            .arbol ul li::before { content: ''; position: absolute; left: -16px; top: 0; bottom: 0; border-left: 1px solid #b8c2d3; }
            .arbol ul li::after { content: ''; position: absolute; left: -16px; top: 16px; width: 12px; border-top: 1px solid #b8c2d3; }
            .arbol ul li:last-child::before { height: 16px; }
            .nodo { display: inline-block; background: #fff; border: 1px solid #d6dde8; border-left: 4px solid var(--c, #d6dde8);
                    border-radius: 6px; padding: 3px 10px; font-family: Consolas, monospace; font-size: 13px; }
            .clase { font-family: 'Segoe UI', sans-serif; font-size: 11px; color: #fff; background: var(--c, #2b3a55);
                     border-radius: 4px; padding: 1px 6px; margin-right: 8px; }
            .leyenda { display: flex; flex-wrap: wrap; gap: 8px; margin-bottom: 22px; }
            """;
}