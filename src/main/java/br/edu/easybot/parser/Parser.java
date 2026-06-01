package br.edu.easybot.parser;

import br.edu.easybot.ast.No;
import br.edu.easybot.lexer.Token;
import br.edu.easybot.lexer.TipoToken;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser descendente recursivo para EasyBot.
 *
 * Cada metodo {@code parseX()} corresponde a uma producao da EBNF
 * documentada no README.
 */
public class Parser {

    private final List<Token> tokens;
    private int p; // posicao atual

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
        this.p = 0;
    }

    public No.Programa parsePrograma() {
        consumir(TipoToken.PROGRAMA);
        Token id = consumir(TipoToken.ID);
        consumir(TipoToken.INICIO);

        List<No.Declaracao> decls = parseDeclaracoes();
        List<No.Comando> bloco = parseBloco(TipoToken.FIM);

        consumir(TipoToken.FIM);
        if (atual().getTipo() != TipoToken.EOF) {
            throw erro("conteudo apos 'FIM' nao e permitido");
        }
        return new No.Programa(id.getLexema(), decls, bloco);
    }

    // <declaracoes> ::= ( "VAR" <id> ":" <tipo> ";" )*
    private List<No.Declaracao> parseDeclaracoes() {
        List<No.Declaracao> lista = new ArrayList<>();
        while (atual().getTipo() == TipoToken.VAR) {
            consumir(TipoToken.VAR);
            Token nome = consumir(TipoToken.ID);
            consumir(TipoToken.DOIS_PONTOS);
            No.Tipo t;
            if (atual().getTipo() == TipoToken.NUMERO_TIPO) {
                consumir(TipoToken.NUMERO_TIPO);
                t = No.Tipo.NUMERO;
            } else if (atual().getTipo() == TipoToken.LOGICO_TIPO) {
                consumir(TipoToken.LOGICO_TIPO);
                t = No.Tipo.LOGICO;
            } else {
                throw erro("tipo esperado (NUMERO ou LOGICO)");
            }
            consumir(TipoToken.PONTO_VIRG);
            lista.add(new No.Declaracao(nome.getLexema(), t, nome.getLinha()));
        }
        return lista;
    }

    private List<No.Comando> parseBloco(TipoToken... terminadores) {
        List<No.Comando> cmds = new ArrayList<>();
        while (!ehTerminador(terminadores) && atual().getTipo() != TipoToken.EOF) {
            cmds.add(parseComando());
        }
        return cmds;
    }

    private boolean ehTerminador(TipoToken... ts) {
        TipoToken at = atual().getTipo();
        for (TipoToken t : ts) if (t == at) return true;
        return false;
    }

    private No.Comando parseComando() {
        TipoToken t = atual().getTipo();
        switch (t) {
            case ID:        return parseAtribuicao();
            case MOVER:     return parseMover();
            case ENQUANTO:  return parseEnquanto();
            case SE:        return parseSe();
            default:        throw erro("comando esperado, encontrado '" + atual().getLexema() + "'");
        }
    }

    private No.Comando parseAtribuicao() {
        Token id = consumir(TipoToken.ID);
        consumir(TipoToken.ATRIB);
        No.Expr e = parseExpressao();
        consumir(TipoToken.PONTO_VIRG);
        return new No.Atribuicao(id.getLexema(), e, id.getLinha());
    }

    private No.Comando parseMover() {
        Token tk = consumir(TipoToken.MOVER);
        consumir(TipoToken.ABRE_PAR);
        No.Expr x = parseExpressao();
        consumir(TipoToken.VIRGULA);
        No.Expr y = parseExpressao();
        consumir(TipoToken.FECHA_PAR);
        consumir(TipoToken.VEL);
        No.Expr v = parseExpressao();
        consumir(TipoToken.PONTO_VIRG);
        return new No.Mover(x, y, v, tk.getLinha());
    }

    private No.Comando parseEnquanto() {
        Token tk = consumir(TipoToken.ENQUANTO);
        No.Expr cond = parseExpressaoLogica();
        consumir(TipoToken.FACA);
        List<No.Comando> corpo = parseBloco(TipoToken.FIM_ENQUANTO);
        consumir(TipoToken.FIM_ENQUANTO);
        return new No.Enquanto(cond, corpo, tk.getLinha());
    }

    private No.Comando parseSe() {
        Token tk = consumir(TipoToken.SE);
        No.Expr cond = parseExpressaoLogica();
        consumir(TipoToken.ENTAO);
        List<No.Comando> entao = parseBloco(TipoToken.SENAO, TipoToken.FIM_SE);
        List<No.Comando> senao = null;
        if (atual().getTipo() == TipoToken.SENAO) {
            consumir(TipoToken.SENAO);
            senao = parseBloco(TipoToken.FIM_SE);
        }
        consumir(TipoToken.FIM_SE);
        return new No.Se(cond, entao, senao, tk.getLinha());
    }

    // <expressao_logica> ::= <expressao> ( "==" | "!=" | ">" | "<" ) <expressao>
    private No.Expr parseExpressaoLogica() {
        No.Expr esq = parseExpressao();
        TipoToken t = atual().getTipo();
        if (t == TipoToken.IGUAL || t == TipoToken.DIFERENTE
         || t == TipoToken.MAIOR || t == TipoToken.MENOR) {
            Token op = consumir(t);
            No.Expr dir = parseExpressao();
            return new No.Comparacao(op.getLexema(), esq, dir, op.getLinha());
        }
        return esq; // permite condicoes ja booleanas (ex: variavel LOGICO)
    }

    // <expressao> ::= <termo> ( ( "+" | "-" ) <termo> )*
    private No.Expr parseExpressao() {
        No.Expr e = parseTermo();
        while (atual().getTipo() == TipoToken.MAIS || atual().getTipo() == TipoToken.MENOS) {
            Token op = consumir(atual().getTipo());
            No.Expr d = parseTermo();
            e = new No.Binaria(op.getLexema(), e, d, op.getLinha());
        }
        return e;
    }

    // <termo> ::= <fator> ( ( "*" | "/" ) <fator> )*
    private No.Expr parseTermo() {
        No.Expr e = parseFator();
        while (atual().getTipo() == TipoToken.MULT || atual().getTipo() == TipoToken.DIV) {
            Token op = consumir(atual().getTipo());
            No.Expr d = parseFator();
            e = new No.Binaria(op.getLexema(), e, d, op.getLinha());
        }
        return e;
    }

    private No.Expr parseFator() {
        Token t = atual();
        switch (t.getTipo()) {
            case NUMERO_LIT:
                avancar();
                return new No.LiteralNumero(Double.parseDouble(t.getLexema()), t.getLinha());
            case VERDADEIRO:
                avancar();
                return new No.LiteralLogico(true, t.getLinha());
            case FALSO:
                avancar();
                return new No.LiteralLogico(false, t.getLinha());
            case ID:
                avancar();
                return new No.Identificador(t.getLexema(), t.getLinha());
            case ABRE_PAR: {
                consumir(TipoToken.ABRE_PAR);
                No.Expr e = parseExpressao();
                consumir(TipoToken.FECHA_PAR);
                return e;
            }
            default:
                throw erro("fator esperado (numero, identificador ou '('), achou '" + t.getLexema() + "'");
        }
    }

    // ---------- utilitarios ----------

    private Token atual() { return tokens.get(p); }
    private void avancar() { p++; }

    private Token consumir(TipoToken esperado) {
        Token t = atual();
        if (t.getTipo() != esperado) {
            throw erro("esperado " + esperado + " mas veio '" + t.getLexema() + "' (" + t.getTipo() + ")");
        }
        avancar();
        return t;
    }

    private RuntimeException erro(String msg) {
        Token t = atual();
        return new RuntimeException("[Sintatico] linha " + t.getLinha() + ", coluna " + t.getColuna() + ": " + msg);
    }
}
