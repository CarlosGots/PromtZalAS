package com.mycompany.promtzalp2.lexer;

import com.mycompany.promtzalp2.errores.ErrorCompilacion;
import com.mycompany.promtzalp2.errores.ManejadorErrores;
import com.mycompany.promtzalp2.errores.TipoError;

%%

%class Lexer
%public
%unicode
%line
%column
%char
%type Token

%state COMENTARIO_BLOQUE

%{
    private ManejadorErrores errores;
    private boolean modoResaltado;

    private final StringBuilder textoComentario = new StringBuilder();
    private int comentarioFila;
    private int comentarioColumna;
    private int comentarioOffset;

    public Lexer(java.io.Reader entrada, ManejadorErrores errores, boolean modoResaltado) {
        this(entrada);
        this.errores = errores;
        this.modoResaltado = modoResaltado;
    }

    private Token token(TipoToken tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1, (int) yychar, yylength());
    }

    private void registrarError(String lexema, String descripcion, int fila, int columna) {
        if (errores != null) {
            errores.agregar(new ErrorCompilacion(TipoError.LEXICO, lexema, descripcion, fila, columna));
        }
    }

    private Token error(String descripcion) {
        registrarError(yytext(), descripcion, yyline + 1, yycolumn + 1);
        return modoResaltado ? token(TipoToken.ERROR) : null;
    }
%}

%eofval{
    return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1, (int) yychar, 0);
%eofval}

LETRA            = [a-zA-Z_]
DIGITO           = [0-9]
ID               = {LETRA}({LETRA}|{DIGITO})*
ENTERO           = {DIGITO}+
DECIMAL          = {DIGITO}+ "." {DIGITO}+
CADENA           = \" [^\"\r\n]* \"
CADENA_ABIERTA   = \" [^\"\r\n]*
COMENTARIO_LINEA = "//" [^\r\n]*
SALTO            = \r | \n | \r\n
ESPACIO          = [ \t\f]

%%

<YYINITIAL> {

    /* ---------- Comentarios ---------- */
    {COMENTARIO_LINEA}      { if (modoResaltado) return token(TipoToken.COMENTARIO); }

    "/*"                    {
                                textoComentario.setLength(0);
                                textoComentario.append(yytext());
                                comentarioFila = yyline + 1;
                                comentarioColumna = yycolumn + 1;
                                comentarioOffset = (int) yychar;
                                yybegin(COMENTARIO_BLOQUE);
                            }

    /* ---------- Directivas ---------- */
    "@modelo" | "@rol" | "@formato"     { return token(TipoToken.DIRECTIVA); }
    "@" {ID}                            { Token t = error("Directiva no reconocida"); if (t != null) return t; }

    /* ---------- Palabras reservadas ---------- */
    "AGENTE"                { return token(TipoToken.AGENTE); }
    "contexto"              { return token(TipoToken.CONTEXTO); }
    "variable"              { return token(TipoToken.VARIABLE); }
    "EJECUTAR"              { return token(TipoToken.EJECUTAR); }
    "EXPORTAR"              { return token(TipoToken.EXPORTAR); }

    /* ---------- Comandos de IA ---------- */
    "PREGUNTAR" | "GENERAR" | "RESUMIR" | "ANALIZAR" |
    "TRADUCIR"  | "CLASIFICAR" | "EXTRAER"          { return token(TipoToken.COMANDO_IA); }

    /* ---------- Funcion del sistema ---------- */
    "CARGAR"                { return token(TipoToken.CARGAR); }

    /* ---------- Conectores ---------- */
    "SOBRE" | "DESDE" | "EN" | "COMO"   { return token(TipoToken.CONECTOR); }

    /* ---------- Operadores y delimitadores ---------- */
    "->"                    { return token(TipoToken.FLECHA); }
    "="                     { return token(TipoToken.IGUAL); }
    "+"                     { return token(TipoToken.MAS); }
    "{"                     { return token(TipoToken.LLAVE_A); }
    "}"                     { return token(TipoToken.LLAVE_C); }
    "("                     { return token(TipoToken.PAR_A); }
    ")"                     { return token(TipoToken.PAR_C); }
    ","                     { return token(TipoToken.COMA); }

    /* ---------- Literales ---------- */
    {DECIMAL} | {ENTERO}    { return token(TipoToken.NUMERO); }
    {CADENA}                { return token(TipoToken.CADENA); }
    {CADENA_ABIERTA}        { Token t = error("Cadena sin cerrar"); if (t != null) return t; }

    /* ---------- Identificadores ---------- */
    {ID}                    { return token(TipoToken.ID); }

    /* ---------- Espacios (se ignoran) ---------- */
    {ESPACIO} | {SALTO}     { }

    /* ---------- Cualquier otro caracter es error ---------- */
    [^]                     { Token t = error("Caracter no reconocido"); if (t != null) return t; }
}

<COMENTARIO_BLOQUE> {

    "*/"                    {
                                textoComentario.append(yytext());
                                yybegin(YYINITIAL);
                                if (modoResaltado) {
                                    return new Token(TipoToken.COMENTARIO, textoComentario.toString(),
                                            comentarioFila, comentarioColumna, comentarioOffset, textoComentario.length());
                                }
                            }

    [^*]+ | "*"             { textoComentario.append(yytext()); }

    <<EOF>>                 {
                                yybegin(YYINITIAL);
                                registrarError("/*", "Comentario de bloque sin cerrar", comentarioFila, comentarioColumna);
                                if (modoResaltado) {
                                    return new Token(TipoToken.ERROR, textoComentario.toString(),
                                            comentarioFila, comentarioColumna, comentarioOffset, textoComentario.length());
                                }
                                return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1, (int) yychar, 0);
                            }
}