package br.edu.easybot.semantico;

import br.edu.easybot.ast.No;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Tabela de simbolos simples (escopo unico).
 * A gramatica de EasyBot nao introduz escopos aninhados, entao um mapa
 * global ja resolve. Mesmo assim deixamos a porta aberta para herdar
 * em versoes futuras.
 */
public class TabelaSimbolos {

    public static final class Simbolo {
        public final String nome;
        public final No.Tipo tipo;
        public final int linhaDeclaracao;
        public Simbolo(String nome, No.Tipo tipo, int linha) {
            this.nome = nome; this.tipo = tipo; this.linhaDeclaracao = linha;
        }
    }

    private final Map<String, Simbolo> simbolos = new LinkedHashMap<>();

    public void declarar(String nome, No.Tipo tipo, int linha) {
        if (simbolos.containsKey(nome)) {
            Simbolo j = simbolos.get(nome);
            throw new RuntimeException("[Semantico] linha " + linha
                + ": variavel '" + nome + "' ja declarada na linha " + j.linhaDeclaracao);
        }
        simbolos.put(nome, new Simbolo(nome, tipo, linha));
    }

    public Simbolo buscar(String nome) {
        return simbolos.get(nome);
    }

    public boolean existe(String nome) {
        return simbolos.containsKey(nome);
    }

    public Iterable<Simbolo> todos() {
        return simbolos.values();
    }
}
