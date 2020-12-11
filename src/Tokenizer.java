import java.util.regex.Pattern;

public class Tokenizer {

    private StringIter it;

    public Tokenizer(StringIter it) {
        this.it = it;
    }

    // 这里本来是想实现 Iterator<Token> 的，但是 Iterator 不允许抛异常，于是就这样了

    // 获取下一个 Token
    public Token nextToken() throws TokenError {
        it.readAll();

        // 跳过之前的所有空白字符
        skipSpaceCharacters();

        if (it.isEOF()) {
            return new Token(TokenType.EOF, "", it.currentPos(), it.currentPos());
        }

        char peek = it.peekChar();
        // 标识符 or 关键字
        if (Character.isAlphabetic(peek)) {
            return lexIdentOrKeyword();
        }
        // 无符号整数 or 浮点数常量
        else if (Character.isDigit(peek)) {
            return lexUIntOrDouble();
        }
        // 字符串常量
        else if (peek == '"') {
            return lexString();
        }
        // 标识符
        else if (peek == '_') {
            return lexIdent();
        }
        // 字符常量
        else if (peek == '\'') {
            return lexChar();
        }
        // 运算符 or 注释
        else {
            return lexOperatorOrAnnotation();
        }
    }

    // 无符号整数 or 浮点数
    private Token lexUIntOrDouble() throws TokenError {
        StringBuilder sb = new StringBuilder();
        while (Character.isDigit(it.peekChar()) || it.peekChar()=='.' || it.peekChar() == 'e' || it.peekChar() == 'E' || it.peekChar() == '+' || it.peekChar() == '-') {
            sb.append(it.nextChar());
        }

        String doubleRegex = "[0-9]+.[0-9]+([eE][-+]?[0-9]+)?";
        String uintRegex = "[0-9]+";
        if(Pattern.matches(uintRegex, sb.toString()))
            return new Token(TokenType.INT_LITERAL, Long.parseLong(sb.toString()), it.previousPos(), it.currentPos());
        else if(Pattern.matches(doubleRegex, sb.toString()))
            return new Token(TokenType.DOUBLE_LITERAL, Double.valueOf(sb.toString()), it.previousPos(), it.currentPos());
        else
            throw new TokenError(ErrorType.IllegalInput, it.previousPos());
    }

    private Token lexIdentOrKeyword() throws TokenError {
        StringBuilder sb = new StringBuilder();
        while (Character.isLetterOrDigit(it.peekChar()) || it.peekChar() == '_') {
            sb.append(it.nextChar());
        }

        switch (sb.toString()) {
            case "fn":
                return new Token(TokenType.FN_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "let":
                return new Token(TokenType.LET_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "const":
                return new Token(TokenType.CONST_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "as":
                return new Token(TokenType.AS_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "while":
                return new Token(TokenType.WHILE_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "if":
                return new Token(TokenType.IF_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "else":
                return new Token(TokenType.ELSE_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "return":
                return new Token(TokenType.RETURN_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "break":
                return new Token(TokenType.BREAK_KW, sb.toString(), it.previousPos(), it.currentPos());
            case "continue":
                return new Token(TokenType.CONTINUE_KW, sb.toString(), it.previousPos(), it.currentPos());
            default:
                return new Token(TokenType.IDENT, sb.toString(), it.previousPos(), it.currentPos());
        }
    }

    private Token lexOperatorOrAnnotation() throws TokenError {
        switch (it.nextChar()) {
            // +
            case '+':
                return new Token(TokenType.PLUS, '+', it.previousPos(), it.currentPos());

            // - or ->
            case '-':
                if (it.peekChar() == '>') {
                    it.nextChar();
                    return new Token(TokenType.ARROW, "->", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.MINUS, '-', it.previousPos(), it.currentPos());

            case '*':
                return new Token(TokenType.MUL, '*', it.previousPos(), it.currentPos());

            // 注释 or / or //
            case '/':
                if(it.peekChar() == '/'){
                    it.nextChar();
                    char now = it.nextChar();
                    while (now != '\n') {
                        now = it.nextChar();
                    }
                    return nextToken();
                }
                else
                    return new Token(TokenType.DIV, '/', it.previousPos(), it.currentPos());

            // = or ==
            case '=':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.EQ, "==", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.ASSIGN, '=', it.previousPos(), it.currentPos());

            // !=
            case '!':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.NEQ, "!=", it.previousPos(), it.currentPos());
                }
                throw new TokenError(ErrorType.IllegalInput, it.previousPos());

            // < or <=
            case '<':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.LE, "<=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.LT, '<', it.previousPos(), it.currentPos());

            // > or >=
            case '>':
                if (it.peekChar() == '=') {
                    it.nextChar();
                    return new Token(TokenType.GE, ">=", it.previousPos(), it.currentPos());
                }
                return new Token(TokenType.GT, '>', it.previousPos(), it.currentPos());

            case '(':
                return new Token(TokenType.L_PAREN, '(', it.previousPos(), it.currentPos());

            case ')':
                return new Token(TokenType.R_PAREN, ')', it.previousPos(), it.currentPos());

            case '{':
                return new Token(TokenType.L_BRACE, '{', it.previousPos(), it.currentPos());

            case '}':
                return new Token(TokenType.R_BRACE, '}', it.previousPos(), it.currentPos());

            case ',':
                return new Token(TokenType.COMMA, ',', it.previousPos(), it.currentPos());

            case ':':
                return new Token(TokenType.COLON, ':', it.previousPos(), it.currentPos());

            case ';':
                return new Token(TokenType.SEMICOLON, ';', it.previousPos(), it.currentPos());

            default:
                throw new TokenError(ErrorType.IllegalInput, it.previousPos());
        }
    }

    private void skipSpaceCharacters() {
        while (!it.isEOF() && Character.isWhitespace(it.peekChar())) {
            it.nextChar();
        }
    }

    // 标识符
    private Token lexIdent() throws TokenError {
        StringBuilder sb = new StringBuilder();
        int i = 100;
        while (Character.isLetterOrDigit(it.peekChar()) && i > 0) {
            sb.append(it.nextChar());
            i--;
        }
        return new Token(TokenType.IDENT, sb.toString(), it.previousPos(), it.currentPos());
    }

    // 字符串常量
    private Token lexString() throws TokenError {
        StringBuilder sb = new StringBuilder();
        char pre = it.nextChar();
        int i = 65535;
        char now;
        while (i > 0) {
            now = it.nextChar();
            if (pre == '\\') {
                if (now == '\\') {
                    sb.append('\\');
                    pre = ' ';
                    i--;
                }
                else if (now == 'n') {
                    sb.append('\n');
                    pre = 'n';
                    i--;
                }
                else if (now == '"') {
                    sb.append('"');
                    pre = '"';
                    i--;
                }
                else if(now == '\''){
                    sb.append('\'');
                    pre = '\'';
                    i--;
                }
            }
            else {
                if (now == '"') break;
                else if (now != '\\') sb.append(now);
                pre = now;
                i--;
            }
        }
        return new Token(TokenType.STRING_LITERAL, sb.toString(), it.previousPos(), it.currentPos());
    }

    // 字符常量
    private Token lexChar() throws TokenError{
        char c = it.nextChar();
        if(c == '\''){
            c = it.nextChar();
            if(c == '\'')
                throw new TokenError(ErrorType.IllegalInput, it.previousPos());
            else if(c == '\\'){
                c = it.nextChar();
                char cc = it.nextChar();
                if(cc != '\'')
                    throw new TokenError(ErrorType.IllegalInput, it.previousPos());
                if(c == '\'')
                    return new Token(TokenType.CHAR_LITERAL, (int) '\'', it.previousPos(), it.currentPos());
                else if(c == '"')
                    return new Token(TokenType.CHAR_LITERAL, (int) '"', it.previousPos(), it.currentPos());
                else if(c == '\\')
                    return new Token(TokenType.CHAR_LITERAL, (int) '\\', it.previousPos(), it.currentPos());
                else if(c == 't')
                    return new Token(TokenType.CHAR_LITERAL, (int) '\t', it.previousPos(), it.currentPos());
                else if(c == 'r')
                    return new Token(TokenType.CHAR_LITERAL, (int) '\r', it.previousPos(), it.currentPos());
                else if(c == 'n')
                    return new Token(TokenType.CHAR_LITERAL, (int) '\n', it.previousPos(), it.currentPos());
                else
                    throw new TokenError(ErrorType.IllegalInput, it.previousPos());
            }
            else{
                if(it.nextChar() == '\'')
                    return new Token(TokenType.CHAR_LITERAL, (int) c, it.previousPos(), it.currentPos());
                else
                    throw new TokenError(ErrorType.IllegalInput, it.previousPos());
            }
        }
        throw new TokenError(ErrorType.IllegalInput, it.previousPos());
    }
}
