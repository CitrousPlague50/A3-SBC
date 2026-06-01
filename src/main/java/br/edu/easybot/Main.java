package br.edu.easybot;

import br.edu.easybot.ast.No;
import br.edu.easybot.codegen.GeradorMCode;
import br.edu.easybot.lexer.Scanner;
import br.edu.easybot.lexer.Token;
import br.edu.easybot.parser.Parser;
import br.edu.easybot.runtime.SimuladorMCode;
import br.edu.easybot.semantico.AnalisadorSemantico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

/**
 * Entrada da linha de comando do compilador EasyBot.
 *
 *   easybotc <arquivo.eb> [--tokens] [--run] [--saida arquivo.mcode]
 */
public class Main {

    public static void main(String[] args) {
        if (args.length == 0) {
            ajuda();
            System.exit(1);
        }

        String entrada = null;
        String saida   = null;
        boolean mostrarTokens = false;
        boolean executar      = false;

        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            switch (a) {
                case "-h": case "--help":   ajuda(); return;
                case "--tokens":            mostrarTokens = true; break;
                case "--run":               executar = true;      break;
                case "-o": case "--saida":  saida = args[++i];    break;
                default:
                    if (entrada == null) entrada = a;
                    else { System.err.println("argumento inesperado: " + a); System.exit(1); }
            }
        }

        if (entrada == null) {
            System.err.println("arquivo .eb obrigatorio");
            System.exit(1);
        }

        try {
            String fonte = Files.readString(Paths.get(entrada));

            // 1) lexico
            List<Token> tokens = new Scanner(fonte).tokenizar();
            if (mostrarTokens) {
                System.out.println("=== TOKENS ===");
                tokens.forEach(System.out::println);
                System.out.println();
            }

            // 2) sintatico
            No.Programa prog = new Parser(tokens).parsePrograma();

            // 3) semantico
            AnalisadorSemantico sem = new AnalisadorSemantico();
            sem.analisar(prog);

            // 4) geracao
            String mcode = new GeradorMCode().gerar(prog, sem.getTabela());

            Path destino = (saida != null)
                    ? Paths.get(saida)
                    : trocarExtensao(Paths.get(entrada), ".mcode");
            Files.writeString(destino, mcode);
            System.out.println("[ok] codigo gerado em " + destino.toAbsolutePath());

            // 5) opcional: executar
            if (executar) {
                System.out.println("\n=== EXECUCAO ===");
                System.out.print(new SimuladorMCode().executar(mcode));
            }
        } catch (IOException e) {
            System.err.println("erro ao ler arquivo: " + e.getMessage());
            System.exit(2);
        } catch (RuntimeException e) {
            System.err.println(e.getMessage());
            System.exit(3);
        }
    }

    private static Path trocarExtensao(Path p, String nova) {
        String nome = p.getFileName().toString();
        int i = nome.lastIndexOf('.');
        String base = (i < 0) ? nome : nome.substring(0, i);
        Path pai = p.getParent();
        return (pai == null) ? Paths.get(base + nova) : pai.resolve(base + nova);
    }

    private static void ajuda() {
        System.out.println("EasyBot Compiler");
        System.out.println("uso: easybotc <arquivo.eb> [--tokens] [--run] [-o saida.mcode]");
    }
}
