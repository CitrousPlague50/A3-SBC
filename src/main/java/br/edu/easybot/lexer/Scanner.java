package br.edu.easybot.lexer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Scanner (analisador lexico) da linguagem EasyBot.
 *
 * Faz a varredura caractere a caractere, descarta espacos em branco e
 * comentarios e produz uma lista de {@link Token}.
 *
 * Comentarios suportados:
 *   // ate o fim da linha
 *   /* bloco *\/   (sem aninhamento)
 *
 * O automato esta descrito no relatorio tecnico (README.md).
 */
public class Scanner {

    private static final Map<String, TipoToken> PALAVRAS = new HashMap<>();
    static {
        PALAVRAS.put("PROGRAMA",     TipoToken.PROGRAMA);
        PALAVRAS.put("INICIO",       TipoToken.INICIO);
        PALAVRAS.put("FIM",          TipoToken.FIM);
        PALAVRAS.put("VAR",          TipoToken.VAR);
        PALAVRAS.put("NUMERO",       TipoToken.NUMERO_TIPO);
        PALAVRAS.put("LOGICO",       TipoToken.LOGICO_TIPO);
        PALAVRAS.put("MOVER",        TipoToken.MOVER);
        PALAVRAS.put("VEL",          TipoToken.VEL);
        PALAVRAS.put("ENQUANTO",     TipoToken.ENQUANTO);
        PALAVRAS.put("FACA",         TipoToken.FACA);
        PALAVRAS.put("FIM_ENQUANTO", TipoToken.FIM_ENQUANTO);
        PALAVRAS.put("SE",           TipoToken.SE);
        PALAVRAS.put("ENTAO",        TipoToken.ENTAO);
        PALAVRAS.put("SENAO",        TipoToken.SENAO);
        PALAVRAS.put("FIM_SE",       TipoToken.FIM_SE);
        PALAVRAS.put("VERDADEIRO",   TipoToken.VERDADEIRO);
        PALAVRAS.put("FALSO",        TipoToken.FALSO);
    }

    private final String src;
    private int pos;
    private int linha;
    private int coluna;

    public Scanner(String fonte) {
        this.src = fonte;
        this.pos = 0;
        this.linha = 1;
        this.coluna = 1;
    }

    public List<Token> tokenizar() {
        List<Token> saida = new ArrayList<>();
        Token t;
        while ((t = proximo()) != null) {
            saida.add(t);
            if (t.getTipo() == TipoToken.EOF) break;
        }
        return saida;
    }

    // -------------------------------------------------------------------
    // nucleo do scanner
    // -------------------------------------------------------------------

    private Token proximo() {
        pularEspacosEComentarios();

        if (pos >= src.length()) {
            return new Token(TipoToken.EOF, "", linha, coluna);
        }

        int linhaTok = linha;
        int colunaTok = coluna;
        char c = src.charAt(pos);

        // identificadores e palavras reservadas
        if (Character.isLetter(c) || c == '_') {
            String lex = lerEnquanto(ch -> Character.isLetterOrDigit(ch) || ch == '_');
            TipoToken tipo = PALAVRAS.getOrDefault(lex, TipoToken.ID);
            return new Token(tipo, lex, linhaTok, colunaTok);
        }

        // numeros
        if (Character.isDigit(c)) {
            StringBuilder sb = new StringBuilder();
            while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                sb.append(consumir());
            }
            if (pos < src.length() && src.charAt(pos) == '.') {
                sb.append(consumir()); // o ponto
                if (pos >= src.length() || !Character.isDigit(src.charAt(pos))) {
                    throw erro("digito esperado apos '.'", linhaTok, colunaTok);
                }
                while (pos < src.length() && Character.isDigit(src.charAt(pos))) {
                    sb.append(consumir());
                }
            }
            return new Token(TipoToken.NUMERO_LIT, sb.toString(), linhaTok, colunaTok);
        }

        // simbolos e operadores
        switch (c) {
            case '+': consumir(); return new Token(TipoToken.MAIS,       "+", linhaTok, colunaTok);
            case '-': consumir(); return new Token(TipoToken.MENOS,      "-", linhaTok, colunaTok);
            case '*': consumir(); return new Token(TipoToken.MULT,       "*", linhaTok, colunaTok);
            case '/': consumir(); return new Token(TipoToken.DIV,        "/", linhaTok, colunaTok);
            case ';': consumir(); return new Token(TipoToken.PONTO_VIRG, ";", linhaTok, colunaTok);
            case ':': consumir(); return new Token(TipoToken.DOIS_PONTOS,":", linhaTok, colunaTok);
            case ',': consumir(); return new Token(TipoToken.VIRGULA,    ",", linhaTok, colunaTok);
            case '(': consumir(); return new Token(TipoToken.ABRE_PAR,   "(", linhaTok, colunaTok);
            case ')': consumir(); return new Token(TipoToken.FECHA_PAR,  ")", linhaTok, colunaTok);
            case '>': consumir(); return new Token(TipoToken.MAIOR,      ">", linhaTok, colunaTok);
            case '<': consumir(); return new Token(TipoToken.MENOR,      "<", linhaTok, colunaTok);
            case '=':
                consumir();
                if (pos < src.length() && src.charAt(pos) == '=') {
                    consumir();
                    return new Token(TipoToken.IGUAL, "==", linhaTok, colunaTok);
                }
                return new Token(TipoToken.ATRIB, "=", linhaTok, colunaTok);
            case '!':
                consumir();
                if (pos < src.length() && src.charAt(pos) == '=') {
                    consumir();
                    return new Token(TipoToken.DIFERENTE, "!=", linhaTok, colunaTok);
                }
                throw erro("caractere inesperado '!' (esperado '!=')", linhaTok, colunaTok);
            default:
                throw erro("caractere desconhecido '" + c + "'", linhaTok, colunaTok);
        }
    }

    // -------------------------------------------------------------------
    // utilitarios
    // -------------------------------------------------------------------

    @FunctionalInterface
    private interface Predicado { boolean teste(char c); }

    private String lerEnquanto(Predicado p) {
        StringBuilder sb = new StringBuilder();
        while (pos < src.length() && p.teste(src.charAt(pos))) {
            sb.append(consumir());
        }
        return sb.toString();
    }

    private char consumir() {
        char c = src.charAt(pos++);
        if (c == '\n') { linha++; coluna = 1; }
        else           { coluna++; }
        return c;
    }

    private void pularEspacosEComentarios() {
        while (pos < src.length()) {
            char c = src.charAt(pos);
            if (Character.isWhitespace(c)) {
                consumir();
            } else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '/') {
                while (pos < src.length() && src.charAt(pos) != '\n') consumir();
            } else if (c == '/' && pos + 1 < src.length() && src.charAt(pos + 1) == '*') {
                consumir(); consumir();
                while (pos < src.length() &&
                       !(src.charAt(pos) == '*' && pos + 1 < src.length() && src.charAt(pos + 1) == '/')) {
                    consumir();
                }
                if (pos >= src.length()) {
                    throw erro("comentario de bloco nao fechado", linha, coluna);
                }
                consumir(); consumir(); // consome */
            } else {
                break;
            }
        }
    }

    private RuntimeException erro(String msg, int l, int c) {
        return new RuntimeException("[Lexico] linha " + l + ", coluna " + c + ": " + msg);
    }
}
