package br.edu.easybot.codegen;

import br.edu.easybot.ast.No;
import br.edu.easybot.semantico.TabelaSimbolos;

/**
 * Gerador de codigo intermediario (.mcode).
 *
 * O formato e uma maquina de pilha simples, suficiente para simular
 * a execucao do programa em um runtime externo:
 *
 *   PROGRAM <nome>
 *   DECL  <id> <tipo>
 *   PUSHN <numero>
 *   PUSHB <true|false>
 *   LOAD  <id>
 *   STORE <id>
 *   ADD | SUB | MUL | DIV
 *   EQ  | NEQ | GT  | LT
 *   LABEL <n>
 *   JZ <n>      ; salta se topo da pilha = falso/0
 *   JMP <n>
 *   MOVE        ; consome 3 valores: vel, y, x
 *   HALT
 */
public class GeradorMCode implements No.Visitante<Void> {

    private final StringBuilder saida = new StringBuilder();
    private int proximoRotulo = 0;

    public String gerar(No.Programa prog, TabelaSimbolos tab) {
        saida.append("PROGRAM ").append(prog.nome).append('\n');
        for (TabelaSimbolos.Simbolo s : tab.todos()) {
            saida.append("DECL ").append(s.nome).append(' ').append(s.tipo).append('\n');
        }
        for (No.Comando c : prog.bloco) c.aceitar(this);
        saida.append("HALT\n");
        return saida.toString();
    }

    private int novoRotulo() { return proximoRotulo++; }

    private void emitir(String linha) { saida.append(linha).append('\n'); }

    // ---------- comandos ----------

    @Override public Void visitar(No.Atribuicao c) {
        c.valor.aceitar(this);
        emitir("STORE " + c.alvo);
        return null;
    }

    @Override public Void visitar(No.Mover c) {
        c.x.aceitar(this);
        c.y.aceitar(this);
        c.vel.aceitar(this);
        emitir("MOVE");
        return null;
    }

    @Override public Void visitar(No.Enquanto c) {
        int inicio = novoRotulo();
        int fim    = novoRotulo();
        emitir("LABEL " + inicio);
        c.condicao.aceitar(this);
        emitir("JZ " + fim);
        for (No.Comando filho : c.corpo) filho.aceitar(this);
        emitir("JMP " + inicio);
        emitir("LABEL " + fim);
        return null;
    }

    @Override public Void visitar(No.Se c) {
        int rotuloSenao = novoRotulo();
        int rotuloFim   = novoRotulo();
        c.condicao.aceitar(this);
        emitir("JZ " + rotuloSenao);
        for (No.Comando filho : c.entao) filho.aceitar(this);
        emitir("JMP " + rotuloFim);
        emitir("LABEL " + rotuloSenao);
        if (c.senao != null) {
            for (No.Comando filho : c.senao) filho.aceitar(this);
        }
        emitir("LABEL " + rotuloFim);
        return null;
    }

    // ---------- expressoes ----------

    @Override public Void visitar(No.Binaria e) {
        e.esq.aceitar(this);
        e.dir.aceitar(this);
        switch (e.op) {
            case "+": emitir("ADD"); break;
            case "-": emitir("SUB"); break;
            case "*": emitir("MUL"); break;
            case "/": emitir("DIV"); break;
            default:  throw new IllegalStateException("op aritmetica desconhecida: " + e.op);
        }
        return null;
    }

    @Override public Void visitar(No.Comparacao e) {
        e.esq.aceitar(this);
        e.dir.aceitar(this);
        switch (e.op) {
            case "==": emitir("EQ");  break;
            case "!=": emitir("NEQ"); break;
            case ">":  emitir("GT");  break;
            case "<":  emitir("LT");  break;
            default:   throw new IllegalStateException("op relacional desconhecida: " + e.op);
        }
        return null;
    }

    @Override public Void visitar(No.LiteralNumero e) {
        // remove ".0" supérfluo quando o numero for inteiro
        if (e.valor == Math.floor(e.valor) && !Double.isInfinite(e.valor)) {
            emitir("PUSHN " + (long) e.valor);
        } else {
            emitir("PUSHN " + e.valor);
        }
        return null;
    }

    @Override public Void visitar(No.LiteralLogico e) {
        emitir("PUSHB " + (e.valor ? "true" : "false"));
        return null;
    }

    @Override public Void visitar(No.Identificador e) {
        emitir("LOAD " + e.nome);
        return null;
    }
}
