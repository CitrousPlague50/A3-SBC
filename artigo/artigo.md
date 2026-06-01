# Abstração de Complexidade: Desenvolvimento de Compiladores para Sistemas de Automação e Robótica Industrial

**Autores:** *(preencher nomes da equipe, e-mails e instituição)*
**Curso:** *(preencher)* — **Disciplina:** Compiladores
**Orientador:** *(preencher)*
**Data:** *(preencher)*

---

## Resumo

A automação industrial moderna depende fortemente de linguagens
específicas de domínio (DSLs) capazes de descrever, com clareza, a
rotina de robôs e o tratamento de sensores. Este trabalho apresenta o
projeto e a implementação de um compilador para a linguagem **EasyBot**,
uma DSL didática voltada à descrição de movimentos de robôs e ao
controle baseado em sensores lógicos. O compilador foi desenvolvido em
Java 17 com Maven e cobre as quatro fases clássicas — análise léxica,
análise sintática, análise semântica e geração de código intermediário
(`.mcode`). Um simulador de máquina de pilha foi implementado para
executar o código gerado e validar a corretude do processo. Os
resultados mostram que o compilador detecta corretamente erros léxicos,
sintáticos e semânticos (em especial o uso indevido de variáveis do
tipo `LOGICO` em expressões aritméticas) e gera código executável
coerente com a especificação.

**Palavras-chave:** Compiladores. Linguagens de Domínio Específico.
Automação Industrial. Análise Sintática. Robótica.

---

## Abstract

Modern industrial automation strongly relies on domain-specific
languages (DSLs) capable of clearly describing robot routines and
sensor handling. This work presents the design and implementation of a
compiler for **EasyBot**, an educational DSL for describing robot
movements and control based on logical sensors. The compiler was
developed in Java 17 with Maven and covers the four classical phases —
lexical, syntactic, semantic analysis and intermediate code generation
(`.mcode`). A stack-machine simulator was implemented to execute the
generated code and validate the correctness of the process. Results
show that the compiler correctly detects lexical, syntactic and
semantic errors (particularly the misuse of `LOGICO` variables in
arithmetic expressions) and produces executable code consistent with
the specification.

**Keywords:** Compilers. Domain-Specific Languages. Industrial
Automation. Syntax Analysis. Robotics.

---

## 1 Introdução

A Indústria 4.0 ampliou o uso de robôs colaborativos, células de
manufatura flexível e sensores embarcados. Programar esses
equipamentos diretamente em linguagens de uso geral (C, Java, Python)
exige conhecimento técnico amplo e produz código pouco legível para os
profissionais de chão de fábrica. Uma alternativa consolidada é o uso
de **linguagens de domínio específico** (DSLs), capazes de oferecer
uma sintaxe mais próxima do vocabulário do processo industrial
(AHO et al., 2008).

Este trabalho descreve o desenvolvimento de um compilador para a
linguagem **EasyBot**, uma DSL didática proposta no edital da
disciplina de Compiladores. A linguagem oferece comandos de movimento
(`MOVER ... VEL ...`), declaração de variáveis tipadas (`NUMERO` e
`LOGICO`), estruturas condicionais (`SE/SENAO`) e laços de repetição
(`ENQUANTO`). O compilador, escrito em Java 17, executa as quatro
fases clássicas e gera um código intermediário do tipo máquina de
pilha (`.mcode`), executado por um simulador integrado.

O restante do artigo está organizado da seguinte forma: a Seção 2
revisa os fundamentos de compiladores empregados; a Seção 3 detalha a
arquitetura do EasyBot; a Seção 4 apresenta a implementação; a Seção 5
discute resultados; e a Seção 6 conclui o trabalho.

---

## 2 Fundamentação Teórica

### 2.1 Linguagens Formais e Autômatos

A análise léxica de uma linguagem regular é tradicionalmente
implementada por um **autômato finito determinístico** (AFD), que
reconhece padrões como identificadores, números e operadores
(HOPCROFT; ULLMAN, 2002). Já a análise sintática de uma linguagem
livre de contexto pode ser implementada por **parsers descendentes
recursivos**, em que cada não-terminal da gramática é representado por
uma função (AHO et al., 2008).

### 2.2 Gramáticas EBNF

A *Extended Backus–Naur Form* (EBNF) é a notação padrão para a
especificação formal de linguagens de programação. Permite descrever,
de forma concisa, alternativas, repetições e opcionais — recursos
indispensáveis ao se projetar uma DSL.

### 2.3 Análise Semântica e Tabela de Símbolos

A **tabela de símbolos** registra os identificadores declarados, seus
tipos e escopos. É consultada na análise semântica para detectar usos
de variáveis não declaradas, incompatibilidades de tipo e outras
violações que escapam à gramática (LOUDEN, 2004).

### 2.4 Geração de Código Intermediário

A geração de código intermediário desacopla o *front-end* do
*back-end*. Representações comuns incluem código de três endereços e
**código de máquina de pilha**. Esta última foi adotada no EasyBot por
sua simplicidade de geração e por ser facilmente simulada.

---

## 3 A Linguagem EasyBot

### 3.1 Gramática EBNF

```ebnf
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

### 3.2 Palavras Reservadas

`PROGRAMA`, `INICIO`, `FIM`, `VAR`, `NUMERO`, `LOGICO`, `MOVER`, `VEL`,
`ENQUANTO`, `FACA`, `FIM_ENQUANTO`, `SE`, `ENTAO`, `SENAO`, `FIM_SE`,
`VERDADEIRO`, `FALSO`.

### 3.3 Tipos

A linguagem oferece dois tipos primitivos. `NUMERO` corresponde a
valores reais de dupla precisão, suficientes para representar
coordenadas e velocidades. `LOGICO` representa estados booleanos
provenientes de sensores. Uma das regras semânticas centrais
**proíbe o uso de variáveis `LOGICO` em expressões aritméticas**, a
fim de evitar comportamentos indefinidos em tempo de execução.

---

## 4 Implementação

### 4.1 Arquitetura

O compilador é organizado em pacotes Java independentes, refletindo
as quatro fases:

```
br.edu.easybot
├── lexer       → Scanner, Token, TipoToken
├── parser      → Parser (descendente recursivo)
├── ast         → No (AST + padrão Visitor)
├── semantico   → TabelaSimbolos, AnalisadorSemantico
├── codegen     → GeradorMCode
└── runtime     → SimuladorMCode
```

A classe `Main` integra as fases e implementa a interface de linha de
comando.

### 4.2 Análise Léxica

O scanner foi implementado a partir de um autômato finito
determinístico simplificado, exposto na Figura 1. Espaços em branco
e comentários (`//` e `/* */`) são descartados; identificadores são
comparados a uma tabela de palavras reservadas; números podem conter
parte fracionária.

**Figura 1 — Autômato Finito do Scanner (resumido)**

```
                [letra|_]            [letra|digito|_]
       ──────────────────►(S1)───────────────────────►(F_ID / palavra reservada)
                                          ▲          │
                                          └──────────┘

                [digito]             [digito]                 '.'    [digito]
       ──────────────────►(S2)───────────────────────►(F_NUM)──────►(S3)──────►(F_NUM)

       '=' ──► (S4) ── '=' ──► (F_EQ "==")
                     └─ outro ► (F_ATRIB "=")

       '!' ──► (S5) ── '=' ──► (F_NEQ "!=")
                     └─ outro ► ERRO

       '+' '-' '*' '/' ';' ':' ',' '(' ')' '>' '<' ──► aceita imediato
```

Cada `Token` carrega tipo, lexema, linha e coluna, permitindo
mensagens de erro precisas nas fases posteriores.

### 4.3 Análise Sintática

O parser é descendente recursivo e segue diretamente a EBNF: cada não
terminal vira um método (`parsePrograma`, `parseBloco`,
`parseComando` etc.). A construção é incremental e resulta em uma
**Árvore Sintática Abstrata** (AST) cujos nós implementam o padrão
*Visitor*, simplificando as fases posteriores.

### 4.4 Análise Semântica

A `TabelaSimbolos` armazena, para cada identificador declarado, nome,
tipo e linha. O `AnalisadorSemantico` percorre a AST acumulando
erros em uma lista, lançando uma única exceção ao final com todas as
inconsistências detectadas. As regras verificadas incluem:

- declaração obrigatória antes do uso;
- compatibilidade de tipo em atribuições;
- operandos de `+`, `-`, `*` e `/` devem ser `NUMERO`;
- coordenadas e velocidade do `MOVER` devem ser `NUMERO`;
- condições de `SE` e `ENQUANTO` devem ser `LOGICO`;
- comparações exigem tipos compatíveis (e operandos numéricos para
  `>` e `<`).

### 4.5 Geração de Código

O `GeradorMCode` traduz a AST para um código intermediário de
**máquina de pilha**, escrito em arquivo `.mcode`. As instruções
seguem o conjunto exibido no Quadro 1.

**Quadro 1 — Conjunto de Instruções da Máquina Virtual EasyBot**

| Instrução          | Efeito                                   |
|--------------------|------------------------------------------|
| `PROGRAM nome`     | Cabeçalho do programa                    |
| `DECL x TIPO`      | Declara variável `x` do tipo `TIPO`      |
| `PUSHN v`          | Empilha número `v`                       |
| `PUSHB true/false` | Empilha booleano                         |
| `LOAD x`           | Empilha o valor de `x`                   |
| `STORE x`          | Desempilha topo e grava em `x`           |
| `ADD/SUB/MUL/DIV`  | Operações aritméticas (2 topos)          |
| `EQ/NEQ/GT/LT`     | Operações relacionais (2 topos)          |
| `LABEL n`          | Marca rótulo                             |
| `JZ n`             | Salta para `n` se topo for falso/zero    |
| `JMP n`            | Salto incondicional                      |
| `MOVE`             | Consome `vel, y, x` e executa movimento  |
| `HALT`             | Encerra execução                         |

### 4.6 Simulador

O `SimuladorMCode` interpreta as instruções acima e mantém uma pilha
de execução e um mapa de variáveis. Cada comando `MOVE` produz uma
linha de *log* descrevendo o destino e a velocidade, simulando o
comportamento do robô.

---

## 5 Resultados

Foram testados três programas-exemplo:

### 5.1 `patrulha.eb`

Programa que descreve três voltas em uma trajetória retangular. A
saída do simulador exibe os doze movimentos esperados (quatro por
volta), confirmando o funcionamento da instrução de repetição e do
gerador de código:

```
[INIT] programa Patrulha
[MOVE] destino=(10,00, 0,00) vel=2,00
[MOVE] destino=(10,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 10,00) vel=2,00
[MOVE] destino=(0,00, 0,00) vel=2,00
... (repetido 3 vezes)
[HALT]
```

### 5.2 `obstaculo.eb`

Demonstra a reação a um sensor lógico (`sensor_obstaculo`),
combinando `SE/SENAO` e `ENQUANTO`. O compilador aceita o programa
sem erros e o simulador executa o trajeto previsto.

### 5.3 `erros.eb`

Programa propositalmente incorreto, utilizado para validar a análise
semântica. A execução produziu a lista exata de erros esperada:

```
Foram encontrados 4 erro(s) semantico(s):
 - linha 9: operando esquerdo de '+' deve ser NUMERO (achou LOGICO)
 - linha 10: variavel 'distancia' nao declarada
 - linha 12: condicao de SE deve ser LOGICO (achou NUMERO)
 - linha 13: coordenada X de MOVER deve ser NUMERO (achou LOGICO)
```

Os resultados demonstram que todas as fases do compilador estão
operacionais e que as regras semânticas exigidas pelo edital são
aplicadas corretamente.

---

## 6 Considerações Finais

O trabalho atingiu o objetivo de projetar e implementar um compilador
funcional para uma DSL voltada à automação industrial. A escolha por
uma arquitetura modular (léxico, sintático, semântico, geração e
simulação) facilitou tanto o desenvolvimento quanto a validação. A
geração de código intermediário em forma de máquina de pilha mostrou-
se adequada como abstração executável.

Como trabalhos futuros, propõe-se: (i) suporte a procedimentos e
parâmetros; (ii) integração com bibliotecas de simulação robótica
reais (por exemplo, ROS ou *PyBullet*); (iii) geração de código para
controladores lógicos programáveis (CLPs); e (iv) inclusão de testes
automatizados com JUnit cobrindo cada fase do compilador.

---

## Referências

AHO, Alfred V. et al. **Compiladores: princípios, técnicas e
ferramentas**. 2. ed. São Paulo: Pearson, 2008.

HOPCROFT, John E.; ULLMAN, Jeffrey D. **Introdução à Teoria de
Autômatos, Linguagens e Computação**. 2. ed. Rio de Janeiro: Campus,
2002.

LOUDEN, Kenneth C. **Compiladores: princípios e práticas**. São
Paulo: Cengage Learning, 2004.

PRICE, Ana M. A.; TOSCANI, Simão S. **Implementação de Linguagens
de Programação: Compiladores**. 4. ed. Porto Alegre: Bookman, 2008.

SEBESTA, Robert W. **Conceitos de Linguagens de Programação**. 11.
ed. Porto Alegre: Bookman, 2018.
