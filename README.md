# EasyBot Compiler

Protótipo de compilador para a linguagem **EasyBot**, uma DSL para
descrever rotinas de robôs industriais (movimentos, leitura de sensores
lógicos e laços de controle). Implementado em Java 17 com Maven.

O compilador faz as quatro fases clássicas:

1. **Análise léxica** – `Scanner` baseado em um autômato finito
   determinístico que descarta espaços e comentários (`//` e `/* */`)
   e produz tokens com tipo, lexema, linha e coluna.
2. **Análise sintática** – `Parser` descendente recursivo que segue a
   gramática EBNF descrita abaixo e constrói uma AST.
3. **Análise semântica** – `AnalisadorSemantico` usa uma `TabelaSimbolos`
   para verificar declarações, compatibilidade de tipos e a regra
   exigida pelo edital: *uma variável `LOGICO` não pode aparecer em uma
   operação aritmética*.
4. **Geração de código** – `GeradorMCode` traduz a AST para um arquivo
   intermediário `.mcode` (máquina de pilha simples).

Há ainda um simulador (`SimuladorMCode`) que executa o `.mcode` e
imprime cada comando `MOVE` no console — útil para conferir a saída.

---

## Compilando

Pré-requisitos: **Java 17** e **Maven 3.8+**.

```powershell
mvn package
```

Isso gera `target/easybotc.jar`.

## Usando

```powershell
# compila um programa e grava o .mcode ao lado dele
java -jar target/easybotc.jar exemplos/patrulha.eb

# mostra a lista de tokens
java -jar target/easybotc.jar exemplos/patrulha.eb --tokens

# compila e executa no simulador
java -jar target/easybotc.jar exemplos/patrulha.eb --run

# define caminho de saida
java -jar target/easybotc.jar exemplos/obstaculo.eb -o build/obstaculo.mcode
```

Para ver as mensagens de erro semântico:

```powershell
java -jar target/easybotc.jar exemplos/erros.eb
```

---

## Gramática EBNF

```
<programa>         ::= "PROGRAMA" <id> "INICIO" <declaracoes> <bloco> "FIM"
<declaracoes>      ::= ( "VAR" <id> ":" <tipo> ";" )*
<tipo>             ::= "NUMERO" | "LOGICO"
<bloco>            ::= ( <comando> )*
<comando>          ::= <atribuicao> | <movimento> | <repeticao> | <condicional>
<atribuicao>       ::= <id> "=" <expressao> ";"
<movimento>        ::= "MOVER" "(" <expressao> "," <expressao> ")" "VEL" <expressao> ";"
<repeticao>        ::= "ENQUANTO" <expressao_logica> "FACA" <bloco> "FIM_ENQUANTO"
<condicional>      ::= "SE" <expressao_logica> "ENTAO" <bloco> ( "SENAO" <bloco> )? "FIM_SE"
<expressao>        ::= <termo> ( ( "+" | "-" ) <termo> )*
<termo>            ::= <fator> ( ( "*" | "/" ) <fator> )*
<fator>            ::= <numero> | <id> | "(" <expressao> ")" | "VERDADEIRO" | "FALSO"
<expressao_logica> ::= <expressao> ( "==" | "!=" | ">" | "<" ) <expressao>
<id>               ::= [a-zA-Z][a-zA-Z0-9_]*
<numero>           ::= [0-9]+ ("." [0-9]+)?
```

Palavras reservadas: `PROGRAMA INICIO FIM VAR NUMERO LOGICO MOVER VEL
ENQUANTO FACA FIM_ENQUANTO SE ENTAO SENAO FIM_SE VERDADEIRO FALSO`.

---

## Autômato finito do scanner (visão geral)

```
              [letra|_]            [letra|digito|_]
        ─────────────────►(S1)─────────────────────►(F_ID/palavra reservada)
                                           ▲          │
                                           └──────────┘

              [digito]               [digito]                  '.'   [digito]
        ─────────────────►(S2)─────────────────────►(F_NUM)──────►(S3)──────►(F_NUM)

        '=' ───► (S4) ─── '=' ───► (F_EQ "==")
                         └─ outro ► (F_ATRIB "=")

        '!' ───► (S5) ─── '=' ───► (F_NEQ "!=")
                         └─ outro ► ERRO

        '+' '-' '*' '/' ';' ':' ',' '(' ')' '>' '<' ─► aceita imediato

        '/' '/' ──► consome ate '\n'   (comentario de linha)
        '/' '*' ──► consome ate "*/"   (comentario de bloco)
```

---

## Saída intermediária (`.mcode`)

Cada linha é uma instrução de máquina de pilha:

| Instr.     | Efeito                                           |
|------------|--------------------------------------------------|
| `PROGRAM n`| cabeçalho                                        |
| `DECL x T` | declara variável `x` do tipo `T`                 |
| `PUSHN v`  | empilha número `v`                               |
| `PUSHB b`  | empilha booleano                                 |
| `LOAD x`   | empilha valor de `x`                             |
| `STORE x`  | desempilha topo e grava em `x`                   |
| `ADD/SUB/MUL/DIV` | operações aritméticas sobre os 2 topos    |
| `EQ/NEQ/GT/LT`    | comparações                               |
| `LABEL n`  | rótulo                                           |
| `JZ n`     | salta se topo for falso/zero                     |
| `JMP n`    | salto incondicional                              |
| `MOVE`     | consome `vel`, `y`, `x` e executa o movimento    |
| `HALT`     | encerra                                          |

---

## Estrutura do projeto

```
src/main/java/br/edu/easybot/
├── Main.java                        # CLI
├── lexer/    (Scanner, Token, TipoToken)
├── parser/   (Parser)
├── ast/      (No - AST + Visitor)
├── semantico/(TabelaSimbolos, AnalisadorSemantico)
├── codegen/  (GeradorMCode)
└── runtime/  (SimuladorMCode)
exemplos/
├── patrulha.eb
├── obstaculo.eb
└── erros.eb
```

---

## Equipe

Trabalho A3 – disciplina de Compiladores.

java -jar target\easybotc.jar exemplos\patrulha.eb --run
java -jar target\easybotc.jar exemplos\patrulha.eb --tokens
java -jar target\easybotc.jar exemplos\erros.eb

PS C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main> java -jar target\easybotc.jar exemplos\patrulha.eb --run
[ok] codigo gerado em C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main\exemplos\patrulha.mcode

=== EXECUCAO ===
[INIT] programa Patrulha
[MOVE] destino=(10,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 0,00) vel=2,00
[HALT]
PS C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main> java -jar target\easybotc.jar exemplos\patrulha.eb --tokens
=== TOKENS ===
<PROGRAMA, 'PROGRAMA', l2:c1>
<ID, 'Patrulha', l2:c10>
<INICIO, 'INICIO', l3:c1>
<VAR, 'VAR', l4:c5>
<ID, 'pos_x', l4:c9>
<DOIS_PONTOS, ':', l4:c15>
<NUMERO_TIPO, 'NUMERO', l4:c17>
<PONTO_VIRG, ';', l4:c23>
<VAR, 'VAR', l5:c5>
<ID, 'pos_y', l5:c9>
<DOIS_PONTOS, ':', l5:c15>
<NUMERO_TIPO, 'NUMERO', l5:c17>
<PONTO_VIRG, ';', l5:c23>
<VAR, 'VAR', l6:c5>
<ID, 'velocidade', l6:c9>
<DOIS_PONTOS, ':', l6:c20>
<NUMERO_TIPO, 'NUMERO', l6:c22>
<PONTO_VIRG, ';', l6:c28>
<VAR, 'VAR', l7:c5>
<ID, 'voltas', l7:c9>
<DOIS_PONTOS, ':', l7:c16>
<NUMERO_TIPO, 'NUMERO', l7:c18>
<PONTO_VIRG, ';', l7:c24>
<ID, 'pos_x', l9:c5>
<ATRIB, '=', l9:c11>
<NUMERO_LIT, '0', l9:c13>
<PONTO_VIRG, ';', l9:c14>
<ID, 'pos_y', l10:c5>
<ATRIB, '=', l10:c11>
<NUMERO_LIT, '0', l10:c13>
<PONTO_VIRG, ';', l10:c14>
<ID, 'velocidade', l11:c5>
<ATRIB, '=', l11:c16>
<NUMERO_LIT, '2', l11:c18>
<PONTO_VIRG, ';', l11:c19>
<ID, 'voltas', l12:c5>
<ATRIB, '=', l12:c12>
<NUMERO_LIT, '3', l12:c14>
<PONTO_VIRG, ';', l12:c15>
<ENQUANTO, 'ENQUANTO', l14:c5>
<ID, 'voltas', l14:c14>
<MAIOR, '>', l14:c21>
<NUMERO_LIT, '0', l14:c23>
<FACA, 'FACA', l14:c25>
<MOVER, 'MOVER', l15:c9>
<ABRE_PAR, '(', l15:c14>
<NUMERO_LIT, '10', l15:c15>
<VIRGULA, ',', l15:c17>
<NUMERO_LIT, '0', l15:c19>
<FECHA_PAR, ')', l15:c20>
<VEL, 'VEL', l15:c22>
<ID, 'velocidade', l15:c26>
<PONTO_VIRG, ';', l15:c36>
<MOVER, 'MOVER', l16:c9>
<ABRE_PAR, '(', l16:c14>
<NUMERO_LIT, '10', l16:c15>
<VIRGULA, ',', l16:c17>
<NUMERO_LIT, '10', l16:c19>
<FECHA_PAR, ')', l16:c21>
<VEL, 'VEL', l16:c23>
<ID, 'velocidade', l16:c27>
<PONTO_VIRG, ';', l16:c37>
<MOVER, 'MOVER', l17:c9>
<ABRE_PAR, '(', l17:c14>
<NUMERO_LIT, '0', l17:c15>
<VIRGULA, ',', l17:c16>
<NUMERO_LIT, '10', l17:c18>
<FECHA_PAR, ')', l17:c20>
<VEL, 'VEL', l17:c22>
<ID, 'velocidade', l17:c26>
<PONTO_VIRG, ';', l17:c36>
<MOVER, 'MOVER', l18:c9>
<ABRE_PAR, '(', l18:c14>
<NUMERO_LIT, '0', l18:c15>
<VIRGULA, ',', l18:c16>
<NUMERO_LIT, '0', l18:c18>
<FECHA_PAR, ')', l18:c19>
<VEL, 'VEL', l18:c21>
<ID, 'velocidade', l18:c25>
<PONTO_VIRG, ';', l18:c35>
<ID, 'voltas', l19:c9>
<ATRIB, '=', l19:c16>
<ID, 'voltas', l19:c18>
<MENOS, '-', l19:c25>
<NUMERO_LIT, '1', l19:c27>
<PONTO_VIRG, ';', l19:c28>
<FIM_ENQUANTO, 'FIM_ENQUANTO', l20:c5>
<FIM, 'FIM', l21:c1>
<EOF, '', l22:c1>

[ok] codigo gerado em C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main\exemplos\patrulha.mcode
PS C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main> java -jar target\easybotc.jar exemplos\erros.eb
Foram encontrados 4 erro(s) semantico(s):
 - linha 9: operando esquerdo de '+' deve ser NUMERO (achou LOGICO)
 - linha 10: variavel 'distancia' nao declarada
 - linha 12: condicao de SE deve ser LOGICO (achou NUMERO)
 - linha 13: coordenada X de MOVER deve ser NUMERO (achou LOGICO)

