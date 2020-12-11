public enum TokenType {
    // 关键字
    FN_KW("fn"),
    LET_KW("let"),
    CONST_KW("const"),
    AS_KW("as"),
    WHILE_KW("while"),
    IF_KW("if"),
    ELSE_KW("else"),
    RETURN_KW("return"),
    BREAK_KW("break"),
    CONTINUE_KW("continue"),

    // 字面量
    INT_LITERAL("0"),
    STRING_LITERAL("0"),
    DOUBLE_LITERAL("0"),
    CHAR_LITERAL("0"),

    // 标识符
    IDENT("0"),

    // 运算符
    PLUS("+"),
    MINUS("-"),
    MUL("*"),
    DIV("/"),
    ASSIGN("="),
    EQ("=="),
    NEQ("!="),
    LT("<"),
    GT(">"),
    LE("<="),
    GE(">="),
    L_PAREN("("),
    R_PAREN(")"),
    L_BRACE("{"),
    R_BRACE("}"),
    ARROW("->"),
    COMMA(","),
    COLON(":"),
    SEMICOLON(";"),

    // 注释
    COMMENT("//"),

    // 文字结尾
    EOF("0eof");

    private String value;


    TokenType(String value){
        this.value = value;
    }

    public String getValue(){
        return value;
    }
}
