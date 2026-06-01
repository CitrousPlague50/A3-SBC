package br.edu.easybot.runtime;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Simulador da maquina de pilha que executa arquivos .mcode.
 * E util para demonstrar que o codigo gerado faz sentido (e tambem
 * para inspecionar a trajetoria do robo passo a passo).
 */
public class SimuladorMCode {

    private final Map<String, Integer> rotulos = new HashMap<>();
    private final List<String[]> instr = new ArrayList<>();
    private final Map<String, Object> mem = new HashMap<>();
    private final Deque<Object> pilha = new ArrayDeque<>();
    private final StringBuilder log = new StringBuilder();

    public String executar(String mcode) {
        carregar(mcode);

        // ponto de partida = primeira instrucao apos PROGRAM/DECL
        int pc = 0;
        while (pc < instr.size()) {
            String[] ins = instr.get(pc);
            String op = ins[0];
            switch (op) {
                case "PROGRAM": log.append("[INIT] programa ").append(ins[1]).append('\n'); pc++; break;
                case "DECL":    mem.put(ins[1], ins[2].equals("LOGICO") ? Boolean.FALSE : Double.valueOf(0)); pc++; break;
                case "PUSHN":   pilha.push(Double.parseDouble(ins[1])); pc++; break;
                case "PUSHB":   pilha.push(Boolean.parseBoolean(ins[1])); pc++; break;
                case "LOAD":    pilha.push(mem.get(ins[1])); pc++; break;
                case "STORE":   mem.put(ins[1], pilha.pop()); pc++; break;
                case "ADD":     bin((a, b) -> a + b); pc++; break;
                case "SUB":     bin((a, b) -> a - b); pc++; break;
                case "MUL":     bin((a, b) -> a * b); pc++; break;
                case "DIV":     bin((a, b) -> a / b); pc++; break;
                case "EQ":      cmp((a, b) -> a.equals(b)); pc++; break;
                case "NEQ":     cmp((a, b) -> !a.equals(b)); pc++; break;
                case "GT":      cmpNum((a, b) -> a > b); pc++; break;
                case "LT":      cmpNum((a, b) -> a < b); pc++; break;
                case "LABEL":   pc++; break;
                case "JZ": {
                    Object v = pilha.pop();
                    boolean falso = (v instanceof Boolean) ? !((Boolean) v)
                                  : (v instanceof Number) && ((Number) v).doubleValue() == 0;
                    pc = falso ? rotulos.get(ins[1]) : pc + 1;
                    break;
                }
                case "JMP":     pc = rotulos.get(ins[1]); break;
                case "MOVE": {
                    double vel = ((Number) pilha.pop()).doubleValue();
                    double y   = ((Number) pilha.pop()).doubleValue();
                    double x   = ((Number) pilha.pop()).doubleValue();
                    log.append(String.format("[MOVE] destino=(%.2f, %.2f) vel=%.2f%n", x, y, vel));
                    pc++;
                    break;
                }
                case "HALT":    log.append("[HALT]\n"); return log.toString();
                default:        throw new IllegalStateException("instrucao desconhecida: " + op);
            }
        }
        return log.toString();
    }

    private void carregar(String mcode) {
        int idx = 0;
        for (String linha : mcode.split("\\r?\\n")) {
            String l = linha.trim();
            if (l.isEmpty()) continue;
            String[] partes = l.split("\\s+");
            if (partes[0].equals("LABEL")) rotulos.put(partes[1], idx);
            instr.add(partes);
            idx++;
        }
    }

    @FunctionalInterface private interface OpBin  { double aplicar(double a, double b); }
    @FunctionalInterface private interface OpCmp  { boolean aplicar(Object a, Object b); }
    @FunctionalInterface private interface OpCmpN { boolean aplicar(double a, double b); }

    private void bin(OpBin op) {
        double b = ((Number) pilha.pop()).doubleValue();
        double a = ((Number) pilha.pop()).doubleValue();
        pilha.push(op.aplicar(a, b));
    }

    private void cmp(OpCmp op) {
        Object b = pilha.pop();
        Object a = pilha.pop();
        pilha.push(op.aplicar(a, b));
    }

    private void cmpNum(OpCmpN op) {
        double b = ((Number) pilha.pop()).doubleValue();
        double a = ((Number) pilha.pop()).doubleValue();
        pilha.push(op.aplicar(a, b));
    }
}
