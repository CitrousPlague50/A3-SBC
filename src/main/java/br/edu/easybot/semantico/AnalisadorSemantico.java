package br.edu.easybot.semantico;

import br.edu.easybot.ast.No;

import java.util.ArrayList;
import java.util.List;

/**
 * Analise semantica.
 *
 * Verificacoes principais:
 *  - todas as variaveis usadas precisam ter sido declaradas;
 *  - tipos dos dois lados de uma atribuicao devem ser compativeis;
 *  - operacoes aritmeticas (+ - * /) so aceitam NUMERO;
 *  - sensores/variaveis LOGICO nao podem aparecer em expressao aritmetica
 *    (regra exigida pelo edital);
 *  - condicoes de SE e ENQUANTO devem produzir LOGICO;
 *  - comparacoes precisam comparar valores do mesmo tipo.
 *
 * Os erros sao acumulados em uma lista antes de serem lancados, para que
 * o programador veja varios problemas de uma vez.
 */
public class AnalisadorSemantico implements No.Visitante<No.Tipo> {

    private final TabelaSimbolos tabela = new TabelaSimbolos();
    private final List<String> erros = new ArrayList<>();

    public TabelaSimbolos getTabela() { return tabela; }

    public void analisar(No.Programa prog) {
        for (No.Declaracao d : prog.declaracoes) {
            try {
                tabela.declarar(d.nome, d.tipo, d.linha);
            } catch (RuntimeException ex) {
                erros.add(ex.getMessage());
            }
        }
        for (No.Comando c : prog.bloco) c.aceitar(this);

        if (!erros.isEmpty()) {
            StringBuilder sb = new StringBuilder("Foram encontrados ")
                    .append(erros.size()).append(" erro(s) semantico(s):\n");
            for (String e : erros) sb.append(" - ").append(e).append('\n');
            throw new RuntimeException(sb.toString());
        }
    }

    // ---------- comandos ----------

    @Override public No.Tipo visitar(No.Atribuicao c) {
        TabelaSimbolos.Simbolo s = tabela.buscar(c.alvo);
        if (s == null) {
            erros.add("linha " + c.linha + ": variavel '" + c.alvo + "' nao declarada");
            return null;
        }
        No.Tipo tValor = c.valor.aceitar(this);
        if (tValor != null && tValor != s.tipo) {
            erros.add("linha " + c.linha + ": atribuicao de tipo " + tValor
                    + " a variavel '" + c.alvo + "' do tipo " + s.tipo);
        }
        return null;
    }

    @Override public No.Tipo visitar(No.Mover c) {
        exigirNumero(c.x, "coordenada X de MOVER", c.linha);
        exigirNumero(c.y, "coordenada Y de MOVER", c.linha);
        exigirNumero(c.vel, "velocidade de MOVER", c.linha);
        return null;
    }

    @Override public No.Tipo visitar(No.Enquanto c) {
        No.Tipo t = c.condicao.aceitar(this);
        if (t != null && t != No.Tipo.LOGICO) {
            erros.add("linha " + c.linha + ": condicao de ENQUANTO deve ser LOGICO (achou " + t + ")");
        }
        for (No.Comando filho : c.corpo) filho.aceitar(this);
        return null;
    }

    @Override public No.Tipo visitar(No.Se c) {
        No.Tipo t = c.condicao.aceitar(this);
        if (t != null && t != No.Tipo.LOGICO) {
            erros.add("linha " + c.linha + ": condicao de SE deve ser LOGICO (achou " + t + ")");
        }
        for (No.Comando filho : c.entao) filho.aceitar(this);
        if (c.senao != null) for (No.Comando filho : c.senao) filho.aceitar(this);
        return null;
    }

    // ---------- expressoes ----------

    @Override public No.Tipo visitar(No.Binaria e) {
        No.Tipo a = e.esq.aceitar(this);
        No.Tipo b = e.dir.aceitar(this);
        if (a != null && a != No.Tipo.NUMERO) {
            erros.add("linha " + e.linha + ": operando esquerdo de '" + e.op
                    + "' deve ser NUMERO (achou " + a + ")");
        }
        if (b != null && b != No.Tipo.NUMERO) {
            erros.add("linha " + e.linha + ": operando direito de '" + e.op
                    + "' deve ser NUMERO (achou " + b + ")");
        }
        return No.Tipo.NUMERO;
    }

    @Override public No.Tipo visitar(No.Comparacao e) {
        No.Tipo a = e.esq.aceitar(this);
        No.Tipo b = e.dir.aceitar(this);
        if (a != null && b != null && a != b) {
            erros.add("linha " + e.linha + ": comparacao entre tipos diferentes ("
                    + a + " " + e.op + " " + b + ")");
        }
        // > e < so fazem sentido para NUMERO
        if ((e.op.equals(">") || e.op.equals("<")) && a != null && a != No.Tipo.NUMERO) {
            erros.add("linha " + e.linha + ": operador '" + e.op + "' requer NUMERO");
        }
        return No.Tipo.LOGICO;
    }

    @Override public No.Tipo visitar(No.LiteralNumero e) { return No.Tipo.NUMERO; }
    @Override public No.Tipo visitar(No.LiteralLogico e) { return No.Tipo.LOGICO; }

    @Override public No.Tipo visitar(No.Identificador e) {
        TabelaSimbolos.Simbolo s = tabela.buscar(e.nome);
        if (s == null) {
            erros.add("linha " + e.linha + ": identificador '" + e.nome + "' nao declarado");
            return null;
        }
        return s.tipo;
    }

    // ---------- auxiliar ----------

    private void exigirNumero(No.Expr e, String contexto, int linha) {
        No.Tipo t = e.aceitar(this);
        if (t != null && t != No.Tipo.NUMERO) {
            erros.add("linha " + linha + ": " + contexto + " deve ser NUMERO (achou " + t + ")");
        }
    }
}
