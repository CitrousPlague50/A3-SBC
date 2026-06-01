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

PS C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main> mvn package
[INFO] Scanning for projects...
[INFO]
[INFO] ------------------< br.edu.easybot:easybot-compiler >-------------------
[INFO] Building EasyBot Compiler 1.0.0
[INFO]   from pom.xml
[INFO] --------------------------------[ jar ]---------------------------------
[INFO]
[INFO] --- resources:3.4.0:resources (default-resources) @ easybot-compiler ---
[INFO] skip non existing resourceDirectory C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main\src\main\resources
[INFO]
[INFO] --- compiler:3.15.0:compile (default-compile) @ easybot-compiler ---
[INFO] Nothing to compile - all classes are up to date.
[INFO]
[INFO] --- resources:3.4.0:testResources (default-testResources) @ easybot-compiler ---
[INFO] skip non existing resourceDirectory C:\Users\gabri\Downloads\A3-SBC-main (1)\A3-SBC-main\src\test\resources
[INFO]
[INFO] --- compiler:3.15.0:testCompile (default-testCompile) @ easybot-compiler ---
[INFO] No sources to compile
[INFO]
[INFO] --- surefire:3.5.4:test (default-test) @ easybot-compiler ---
[INFO] No tests to run.
[INFO]
[INFO] --- jar:3.3.0:jar (default-jar) @ easybot-compiler ---
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  1.108 s
[INFO] Finished at: 2026-06-01T10:45:47-03:00
[INFO] ------------------------------------------------------------------------
