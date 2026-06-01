package br.edu.easybot.lexer;

/**
 * Categorias de tokens reconhecidas pelo scanner.
 * Os literais "PROGRAMA", "INICIO" etc. sao tratados como palavras reservadas.
 */
public enum TipoToken {

    // palavras reservadas
    PROGRAMA, INICIO, FIM,
    VAR, NUMERO_TIPO, LOGICO_TIPO,
    MOVER, VEL,
    ENQUANTO, FACA, FIM_ENQUANTO,
    SE, ENTAO, SENAO, FIM_SE,
    VERDADEIRO, FALSO,

    // literais e identificadores
    ID, NUMERO_LIT,

    // simbolos
    ATRIB,        // =
    PONTO_VIRG,   // ;
    DOIS_PONTOS,  // :
    VIRGULA,      // ,
    ABRE_PAR,     // (
    FECHA_PAR,    // )

    // operadores aritmeticos
    MAIS, MENOS, MULT, DIV,

    // operadores relacionais
    IGUAL,        // ==
    DIFERENTE,    // !=
    MAIOR,        // >
    MENOR,        // <

    // fim de arquivo
    EOF
}
