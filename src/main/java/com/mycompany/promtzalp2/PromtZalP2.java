package com.mycompany.promtzalp2;

import com.mycompany.promtzalp2.lexer.Lexer;
import com.mycompany.promtzalp2.lexer.TipoToken;
import com.mycompany.promtzalp2.lexer.Token;
import java.io.StringReader;

public class PromtZalP2 {

    public static void main(String[] args) throws Exception {
        String entrada = "AGENTE analista\n  ventas $ AGENTE";
        Lexer lexer = new Lexer(new StringReader(entrada));

        Token t;
        do {
            t = lexer.yylex();
            System.out.println(t);
        } while (t.getTipo() != TipoToken.EOF);
    }
}
