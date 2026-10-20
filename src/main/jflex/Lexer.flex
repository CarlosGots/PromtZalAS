package com.mycompany.promtzalp2.lexer;

%%

%class Lexer
%public
%unicode
%line
%column
%type Token

%{
    private Token token(TipoToken tipo) {
        return new Token(tipo, yytext(), yyline + 1, yycolumn + 1);
    }
%}

%eofval{
    return new Token(TipoToken.EOF, "", yyline + 1, yycolumn + 1);
%eofval}

LETRA   = [a-zA-Z_]
DIGITO  = [0-9]
ID      = {LETRA}({LETRA}|{DIGITO})*
ESPACIO = [ \t\r\n\f]+

%%

"AGENTE"    { return token(TipoToken.AGENTE); }
{ID}        { return token(TipoToken.ID); }
{ESPACIO}   { /* se ignora */ }
[^]         { return token(TipoToken.ERROR); }
