package br.edu.easybot.ast;

import java.util.List;

/**
 * Definicoes da AST da linguagem EasyBot.
 *
 * Mantemos as classes como records/POJOs simples e expomos um metodo
 * {@code aceitar(Visitante)} para o padrao Visitor usado pelo analisador
 * semantico e pelo gerador de codigo.
 */
public final class No {

    private No() { /* container */ }

    // ---------- nó base ----------
    public interface Comando { <T> T aceitar(Visitante<T> v); }
    public interface Expr    { <T> T aceitar(Visitante<T> v); }

    // ---------- programa ----------
    public static final class Programa {
        public final String nome;
        public final List<Declaracao> declaracoes;
        public final List<Comando> bloco;
        public Programa(String nome, List<Declaracao> declaracoes, List<Comando> bloco) {
            this.nome = nome;
            this.declaracoes = declaracoes;
            this.bloco = bloco;
        }
    }

    public static final class Declaracao {
        public final String nome;
        public final Tipo tipo;
        public final int linha;
        public Declaracao(String nome, Tipo tipo, int linha) {
            this.nome = nome; this.tipo = tipo; this.linha = linha;
        }
    }

    public enum Tipo { NUMERO, LOGICO }

    // ---------- comandos ----------
    public static final class Atribuicao implements Comando {
        public final String alvo;
        public final Expr valor;
        public final int linha;
        public Atribuicao(String alvo, Expr valor, int linha) {
            this.alvo = alvo; this.valor = valor; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class Mover implements Comando {
        public final Expr x, y, vel;
        public final int linha;
        public Mover(Expr x, Expr y, Expr vel, int linha) {
            this.x = x; this.y = y; this.vel = vel; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class Enquanto implements Comando {
        public final Expr condicao;
        public final List<Comando> corpo;
        public final int linha;
        public Enquanto(Expr condicao, List<Comando> corpo, int linha) {
            this.condicao = condicao; this.corpo = corpo; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class Se implements Comando {
        public final Expr condicao;
        public final List<Comando> entao;
        public final List<Comando> senao; // pode ser null
        public final int linha;
        public Se(Expr condicao, List<Comando> entao, List<Comando> senao, int linha) {
            this.condicao = condicao; this.entao = entao; this.senao = senao; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    // ---------- expressoes ----------
    public static final class Binaria implements Expr {
        public final String op;       // + - * /
        public final Expr esq, dir;
        public final int linha;
        public Binaria(String op, Expr esq, Expr dir, int linha) {
            this.op = op; this.esq = esq; this.dir = dir; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class Comparacao implements Expr {
        public final String op;       // == != > <
        public final Expr esq, dir;
        public final int linha;
        public Comparacao(String op, Expr esq, Expr dir, int linha) {
            this.op = op; this.esq = esq; this.dir = dir; this.linha = linha;
        }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class LiteralNumero implements Expr {
        public final double valor;
        public final int linha;
        public LiteralNumero(double valor, int linha) { this.valor = valor; this.linha = linha; }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class LiteralLogico implements Expr {
        public final boolean valor;
        public final int linha;
        public LiteralLogico(boolean valor, int linha) { this.valor = valor; this.linha = linha; }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    public static final class Identificador implements Expr {
        public final String nome;
        public final int linha;
        public Identificador(String nome, int linha) { this.nome = nome; this.linha = linha; }
        @Override public <T> T aceitar(Visitante<T> v) { return v.visitar(this); }
    }

    // ---------- visitor ----------
    public interface Visitante<T> {
        T visitar(Atribuicao c);
        T visitar(Mover c);
        T visitar(Enquanto c);
        T visitar(Se c);
        T visitar(Binaria e);
        T visitar(Comparacao e);
        T visitar(LiteralNumero e);
        T visitar(LiteralLogico e);
        T visitar(Identificador e);
    }
}
