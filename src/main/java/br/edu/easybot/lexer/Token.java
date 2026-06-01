package br.edu.easybot.lexer;

public class Token {

    private final TipoToken tipo;
    private final String lexema;
    private final int linha;
    private final int coluna;

    public Token(TipoToken tipo, String lexema, int linha, int coluna) {
        this.tipo = tipo;
        this.lexema = lexema;
        this.linha = linha;
        this.coluna = coluna;
    }

    public TipoToken getTipo()  { return tipo; }
    public String getLexema()   { return lexema; }
    public int getLinha()       { return linha; }
    public int getColuna()      { return coluna; }

    @Override
    public String toString() {
        return String.format("<%s, '%s', l%d:c%d>", tipo, lexema, linha, coluna);
    }
}
