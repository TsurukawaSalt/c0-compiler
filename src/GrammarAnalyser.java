import java.util.ArrayList;
import java.util.List;

public class GrammarAnalyser {
    /* token列表 */
    private static List<Token> tkList = new ArrayList<>();

    /* 当前token的序号 */
    private static int tokenIndex = -1;

    /* tkList的长度 */
    private static int tkListLength = 0;

    /* 当前读取的token */
    private static Token tk;

    /* 层数 */
    private static int level = 1;

    /* 符号表: 常量/变量/函数 */
    private static List<SymbolEntry> symbolTable = new ArrayList<>();

    /* 全局符号表 */
    private static List<Global> globalTable = new ArrayList<>();

    /* 全局符号个数 */
    private static int globalCount = 0;

    /* 输出函数 */
    private static List<FunctionDef> functionDefTable = new ArrayList<>();

    /* 函数个数 */
    private static int functionCount = 0;

    /* 局部变量个数 */
    private static int locSlots = 0;

    /* 参数列表 */
    private static List<Param> params = new ArrayList<>();

    /* 返回值个数 */
    private static int returnSlots = 0;

    /* 返回类型 */
    private static RtnType rtnType;

    /* 指令 */
    private static List<Instruction> instructions = new ArrayList<>();

    /* 是否返回 */
    private static boolean isReturn = false;

    /* 是否有main函数 */
    private static boolean hasMain = false;

    /* _start函数 */
    private static FunctionDef startFunction;

    /* STL符号表 */
    private static List<SymbolEntry> STLTable = new ArrayList<>();

    /* 记录loop的层数 */
    private static int loopNum = 0;

    /* Continue 记录栈 */
    private static List<Scope> continueIns = new ArrayList<>();

    /* Break 记录栈 */
    private static List<Scope> breakIns = new ArrayList<>();

    /* 入口 */
    public static void AnalyseGrammar(Tokenizer tokenizer) throws GrammarError{
        while (true){
            Token token = tokenizer.nextToken();
            if (token.getTokenType() == TokenType.EOF){
                break;
            } else {
                tkList.add(token);
            }
        }
        tokenIndex = -1;
        tkListLength = tkList.size();
        level = 1;
        initSTLFunction();
        analyseProgram();
    }

    /**
     * item -> function | decl_stmt
     * program -> item*
     */
    private static void analyseProgram() throws GrammarError{
        /* 用来保存_start函数的指令 */
        List<Instruction> startIns = new ArrayList<>();
        while (!isEof()){
            tk = next();
            if (tk.getTokenType() == TokenType.FN_KW){
                /* 清空指令序列 */
                instructions = new ArrayList<>();
                /* 准备工作 */
                level = 1;
                returnSlots = 0;
                params = new ArrayList<>();
                locSlots = 0;
                rtnType = null;
                isReturn = false;
                tk = unread();
                analyseFunction();
                functionCount++;
                globalCount++;
            }
            else if (tk.getTokenType() == TokenType.CONST_KW || tk.getTokenType() == TokenType.LET_KW){
                /* 是全局符号 层数 = 1 */
                level = 1;
                instructions = new ArrayList<>();
                tk = unread();
                analyseDeclStmt();
                /* 将指令保存到_start函数里 */
                startIns.addAll(instructions);
            }
            else{
                throw new GrammarError(ErrorType.ExpectedToken, "fn or const or let");
            }
        }
        /* 判断是否有main函数 */
        if (!hasMain()){
            throw new GrammarError(ErrorType.NoMainFunction);
        }

        /* 添加_start到全局符号表 */
        Global global = new Global(1, 6, "_start");
        globalTable.add(global);

        /* add stackAlloc & call main */
        System.out.println(Utils.hasMainReturn(symbolTable));
        if (Utils.hasMainReturn(symbolTable)){
            /* main有返回值 */
            startIns.add(new Instruction(InstructionType.StackAlloc, 1));
            startIns.add(new Instruction(InstructionType.Call, functionCount));
            startIns.add(new Instruction(InstructionType.PopN, 1));
        } else {
            /* main没有有返回值 */
            startIns.add(new Instruction(InstructionType.StackAlloc, 0));
            startIns.add(new Instruction(InstructionType.Call, functionCount));
        }
        /* _start添加到函数输出表 */
        startFunction = new FunctionDef(globalCount, 0, 0, 0,startIns);
        globalCount++;

        System.out.println("语法分析结束");
    }

    /**
     * function -> 'fn' IDENT '(' function_param_list? ')' '->' ty block_stmt
     */
    private static void analyseFunction() throws GrammarError{
        /* fn */
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.FN_KW){
            throw new GrammarError(ErrorType.ExpectedToken, "fn");
        }
        /* ident */
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.IDENT){
            throw new GrammarError(ErrorType.ExpectedToken, "IDENT");
        }
        /* 函数名 */
        String funcName = tk.getStringValue();
        if (Utils.hasFunction(funcName, symbolTable)){
            throw new GrammarError(ErrorType.DuplicateDeclaration);
        }
        System.out.println("进入" + funcName + "函数");
        /* 左括号 */
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.L_PAREN){
            throw new GrammarError(ErrorType.ExpectedToken, "(");
        }
        // 参数列表 或 右括号
        tk = next();
        if (tk == null || (tk.getTokenType() != TokenType.R_PAREN && tk.getTokenType() != TokenType.CONST_KW && tk.getTokenType() != TokenType.IDENT)){
            throw new GrammarError(ErrorType.ExpectedToken, "function_param_list or )");
        }
        // 参数列表
        if (tk.getTokenType() != TokenType.R_PAREN){
            /* 参数列表 */
            unread();
            analyseFunctionParamList();
            tk = next();
        }
        // )
        if (tk == null || tk.getTokenType() != TokenType.R_PAREN){
            throw new GrammarError(ErrorType.ExpectedToken, ")");
        }
        // ->
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.ARROW){
            throw new GrammarError(ErrorType.ExpectedToken, "->");
        }
        // ty
        tk = next();
        if (tk == null){
            throw new GrammarError(ErrorType.ExpectedToken, "ty");
        }
        /* 返回类型 */
        rtnType = Utils.getRtnType(tk);
        if (rtnType == RtnType.INT){
            returnSlots = 1;
        } else if (rtnType == RtnType.DOUBLE){
            returnSlots = 1;
        } else if (rtnType == RtnType.VOID){
            returnSlots = 0;
            // 由于无返回 直接将isReturn置true
            isReturn = true;
        } else {
            throw new GrammarError(ErrorType.ExpectedToken, "ty");
        }
        /* 函数添加到符号表 */
        symbolTable.add(new SymbolEntry(funcName, SymbolType.FUNCTION, functionCount, rtnType, params));

        // block_stmt
        analyseBlockStmt();


        /* 判断是否返回 */
        if (!isReturn) {
            throw new GrammarError(ErrorType.NoReturn);
        }
        if (rtnType == RtnType.VOID){
            //ret
            int len = instructions.size();
            if (len == 0){
                instructions.add(new Instruction(InstructionType.Ret));
            } else {
                int last = instructions.size()-1;
                if (instructions.get(last).getInsType() != InstructionType.Ret){
                    instructions.add(new Instruction(InstructionType.Ret));
                }
            }

        }
        /* 看该函数是否是main函数 */
        if (funcName.equals("main")){
            hasMain = true;
        }
        /* 添加到全局变量 */
        Global global = new Global(1, funcName.length(), funcName);
        globalTable.add(global);
        /* 添加到函数输出表 */
        FunctionDef functionDef = new FunctionDef(globalCount, returnSlots, params.size(), locSlots, instructions);
        functionDefTable.add(functionDef);

        /* for test*/
        int count = 0;
        for (Instruction ins : instructions){
            System.out.print(count+ ". "+ins.insType + "   ");
            System.out.println(ins.getParam()==null?"" : ins.getParam());
            count++;
        }

    }

    /**
     * function_param_list -> function_param (',' function_param)*
     */
    private static void analyseFunctionParamList() throws GrammarError {
        analyseFunctionParam();
        tk = next();
        while (true){
            // param
            if (tk.getTokenType() == TokenType.R_PAREN){
                unread();
                break;
            }
            analyseFunctionParam();
            tk = next();
        }
    }

    /**
     * function_param -> 'const'? IDENT ':' ty
     */
    private static void analyseFunctionParam() throws GrammarError {
        /* 判断是否是常量参数 */
        boolean isConst = false;
        // const or IDENT
        tk = next();
        if (tk == null || (tk.getTokenType() != TokenType.CONST_KW && tk.getTokenType() != TokenType.IDENT)){
            throw new GrammarError(ErrorType.ExpectedToken, "const or IDENT");
        }
        // const
        if (tk.getTokenType() == TokenType.CONST_KW){
            isConst = true;
            tk = next();
        }
        // IDENT
        if (tk == null || tk.getTokenType() != TokenType.IDENT){
            throw new GrammarError(ErrorType.ExpectedToken, "IDENT");
        }
        /* 参数名 */
        String paramName = tk.getStringValue();
        // :
        tk = next();
        if (tk == null|| tk.getTokenType() != TokenType.COLON){
            throw new GrammarError(ErrorType.ExpectedToken, ":");
        }
        // ty
        tk = next();
        if (tk == null){
            throw new GrammarError(ErrorType.ExpectedToken, "ty");
        }
        /* 参数类型 */
        ValType valType = Utils.getValType(tk);
        if (valType == null){
            throw new GrammarError(ErrorType.InvalidToken);
        }
        /* 添加到函数的参数列表 */
        Param param = new Param(paramName, valType, isConst);
        params.add(param);

        // 添加到符号表
        // level 为函数所在的层数+1
        // offset 为returnSlot-1 + params.size
        SymbolEntry symbolEntry;
        int offset = params.size();
        if (isConst){
            symbolEntry = new SymbolEntry(paramName, SymbolType.CONSTPARAM, valType, level + 1, offset, 1);
        }else{
            symbolEntry = new SymbolEntry(paramName, SymbolType.PARAM, valType, level + 1, offset, 1);
        }
        symbolTable.add(symbolEntry);

    }


    private static void analyseDeclStmt() throws GrammarError{
        tk = next();
        if (tk.getTokenType() == TokenType.LET_KW){
            tk = unread();
            analyseLetDeclStmt();
        } else {
            tk = unread();
            analyseConstDeclStmt();
        }
        if (level == 1){
            globalCount++;
        } else {
            locSlots++;
        }
    }

    /**
     * const_decl_stmt -> 'const' IDENT ':' ty '=' expr ';'
     */
    private static void analyseConstDeclStmt() throws GrammarError {
        /* 记录expr的返回类型 继而判断是否合法*/
        String exprRtnType = "";

        // const
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.CONST_KW ){
            throw new GrammarError(ErrorType.NoConstKeyWord);
        }
        // IDENT
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.IDENT){
            throw new GrammarError(ErrorType.ExpectedToken,"IDENT");
        }
        String constName = tk.getStringValue();
        if (Utils.isDeclaredVar(constName, level, symbolTable)){
            /* 同层次有同名的 */
            throw new GrammarError(ErrorType.DuplicateDeclaration);
        }
        if (level == 1){
            // 加载全局变量地址
            instructions.add(new Instruction(InstructionType.GlobA, globalCount));
        } else {
            // 加载局部变量地址
            instructions.add(new Instruction(InstructionType.LocA, locSlots));
        }
        // :
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.COLON){
            throw new GrammarError(ErrorType.ExpectedToken, ":");
        }
        // ty
        tk = next();
        if (tk == null){
            throw new GrammarError(ErrorType.ExpectedToken, "ty");
        }
        ValType valType = Utils.getValType(tk);
        // 保存到列表
        if (level == 1){
            /* 全局符号表 */
            globalTable.add(new Global(1));
            symbolTable.add(new SymbolEntry(constName, SymbolType.CONSTANT, valType, level, globalCount));
        } else {
            symbolTable.add(new SymbolEntry(constName, SymbolType.CONSTANT, valType, level, locSlots));
        }
        // =
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.ASSIGN){
            throw new GrammarError(ErrorType.ExpectedToken, "=");
        }

        // expr
        exprRtnType = analyseA();

        // ;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }

        System.out.println("need: " + valType.getValue());
        System.out.println("actual: " + exprRtnType);

        if (!exprRtnType.equals(valType.getValue())){
            throw new GrammarError(ErrorType.InvalidAssignment);
        }

        /* 值在栈顶, 地址在次栈顶, 指令store */
        instructions.add(new Instruction(InstructionType.Store64));
    }

    /**
     * let_decl_stmt -> 'let' IDENT ':' ty ('=' expr)? ';'
     */
    private static void analyseLetDeclStmt() throws GrammarError {
        String exprRtnType = "";
        int isInitialed = 0;
        // let
        tk = next();
        // IDENT
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.IDENT){
            throw new GrammarError(ErrorType.ExpectedToken, "IDENT");
        }
        /* 变量名 */
        String varName = tk.getStringValue();
        if (Utils.isDeclaredVar(varName, level, symbolTable)){
            throw new GrammarError(ErrorType.DuplicateDeclaration);
        }
        // :
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.COLON){
            throw new GrammarError(ErrorType.ExpectedToken, ":");
        }
        // ty
        tk = next();
        if (tk == null || (!tk.getStringValue().equals("int") && !tk.getStringValue().equals("double"))){
            throw new GrammarError(ErrorType.ExpectedToken, "ty");
        }
        /* 类型 */
        ValType valType = Utils.getValType(tk);
        // = or ;
        tk = next();
        if (tk == null || (tk.getTokenType() != TokenType.ASSIGN && tk.getTokenType() != TokenType.SEMICOLON)) {
            throw new GrammarError(ErrorType.ExpectedToken, "= or ;");
        }
        // 如果是=
        if (tk.getTokenType() == TokenType.ASSIGN){
            /* 加载地址 用于之后expr的值保存到该地址*/
            if (level == 1){
                instructions.add(new Instruction(InstructionType.GlobA, globalCount));
            } else {
                instructions.add(new Instruction(InstructionType.LocA, locSlots));
            }

            // expr
            exprRtnType = analyseA();
            isInitialed = 1;

            System.out.println("need: " + valType.getValue());
            System.out.println("actual: " + exprRtnType);

            if (!exprRtnType.equals(valType.getValue())){
                throw new GrammarError(ErrorType.InvalidAssignment);
            }

            /* store */
            instructions.add(new Instruction(InstructionType.Store64));
            tk = next();
        }
        // ;
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
        /* 未赋值 无指令操作 只需加到符号表 */
        /* 加到符号表 */
        if (level == 1){
            globalTable.add(new Global(0));
            symbolTable.add(new SymbolEntry(varName, SymbolType.VARIABLE, valType, level, globalCount, isInitialed));
        } else {
            symbolTable.add(new SymbolEntry(varName, SymbolType.VARIABLE, valType, level, locSlots, isInitialed));
        }
    }


    /**
     * ....stmt....
     *    ↑
     * 在stmt的第一个token之前
     * // # 语句
     * stmt ->
     *       expr_stmt			expr
     *     | decl_stmt			let/const
     *     | if_stmt			if
     *     | while_stmt			while
     *     | break_stmt			break
     *     | continue_stmt		continue
     *     | return_stmt		return
     *     | block_stmt			{
     *     | empty_stmt			;
     */
    private static void analyseStmt() throws GrammarError{
        System.out.println("进入stmt");
        // 预读
        Token ptk = peek();
        if (ptk == null){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        } else {
            TokenType type = ptk.getTokenType();
            switch (type) {
                case LET_KW:
                case CONST_KW:
                    analyseDeclStmt();
                    break;
                case IF_KW:
                    analyseIfStmt();
                    break;
                case WHILE_KW:
                    analyseWhileStmt();
                    break;
                case BREAK_KW:
                    analyseBreakStmt();
                    break;
                case CONTINUE_KW:
                    analyseContinueStmt();
                    break;
                case RETURN_KW:
                    analyseReturnStmt();
                    break;
                case L_BRACE:
                    analyseBlockStmt();
                    break;
                case SEMICOLON:
                    analyseEmptyStmt();
                    break;
                default:
                    analyseExprStmt();
            }
        }
        System.out.println("离开stmt");
    }

    /**
     * if_stmt -> 'if' expr block_stmt ('else' 'if' expr block_stmt)* ('else' block_stmt)?
     */
    private static void analyseIfStmt() throws GrammarError {
        System.out.println("开始分析IfStmt");
        // if
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.IF_KW) {
            throw new GrammarError(ErrorType.NoIfKeyWord);
        }

        // expr
        analyseA();

        /* BrTure */
        instructions.add(new Instruction(InstructionType.BrTrue, 1));
        /* Br */
        Instruction jump = new Instruction(InstructionType.Br, 0);
        instructions.add(jump);

        /* 当前指令位置 */
        int blockStart = instructions.size();

        // block_stmt
        analyseBlockStmt();

        /* block结束地址 */
        int blockEnd = instructions.size();

        /* 这个if-block里有返回 */
        if (instructions.get(blockEnd-1).getInsType() == InstructionType.Ret){
            /* 跳过该block */
            int distance = blockEnd - blockStart;
            jump.setParam(distance);

            tk = next();
            if (tk.getTokenType() == TokenType.ELSE_KW){
                tk = next();
                /* else语句 */
                if (tk.getTokenType() == TokenType.L_BRACE){
                    tk = unread();
                    analyseBlockStmt();
                    if (!(instructions.get(blockEnd - 1).getInsType()==InstructionType.Ret)){
                        instructions.add(new Instruction(InstructionType.Br, 0));
                    }
                }
                /* if语句*/
                else if (tk.getTokenType() == TokenType.IF_KW){
                    tk = unread();
                    analyseIfStmt();
                }
            } else {
                tk = unread();
            }
        }
        /* 这个if-block里没有返回 */
        else {
            /* 跳转到整个if块结束 */
            Instruction jumpInstruction = new Instruction(InstructionType.Br, -1);
            instructions.add(jumpInstruction);

            /* 跳转到第二个else */
            int nextElse = instructions.size();
            int distance = nextElse - blockStart;
            jump.setParam(distance);

            tk = next();
            if (tk.getTokenType() == TokenType.ELSE_KW){
                tk = next();
                /* else语句 */
                if (tk.getTokenType() == TokenType.L_BRACE){
                    tk = unread();
                    analyseBlockStmt();
                    instructions.add(new Instruction(InstructionType.Br, 0));
                }
                /* if语句*/
                else if (tk.getTokenType() == TokenType.IF_KW){
                    tk = unread();
                    analyseIfStmt();
                }
            } else {
                tk = unread();
            }
            distance = instructions.size() - nextElse;
            jumpInstruction.setParam(distance);
        }
    }

    /**
     * 'while' expr block_stmt
     */
    private static void analyseWhileStmt() throws GrammarError {
        System.out.println("开始分析while_stmt");

        // while
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.WHILE_KW){
            throw new GrammarError(ErrorType.NoWhileKeyWord);
        }

        /* 指令br */
        instructions.add(new Instruction(InstructionType.Br, 0));

        /* while的起始指令 */
        int whileStart = instructions.size();

        /* expr */
        analyseA();

        /* BrTure */
        instructions.add(new Instruction(InstructionType.BrTrue, 1));

        /* Br 跳过block块 */
        Instruction jumpOutIns = new Instruction(InstructionType.Br, 0);
        instructions.add(jumpOutIns);

        /* block的起始地址 */
        int blockStart = instructions.size();

        loopNum++;
        // block_stmt
        analyseBlockStmt();
        if (loopNum>0){
            loopNum--;
        }

        Instruction jumpBackIns = new Instruction(InstructionType.Br, 0);
        instructions.add(jumpBackIns);

        /* while结束地址 */
        int whileEnd = instructions.size();

        /* 返回whileStart */
        jumpBackIns.setParam(whileStart - whileEnd);

        /* 跳转至whileEnd */
        jumpOutIns.setParam(whileEnd - blockStart);

        /* 更新参数 */
        if (breakIns.size()!=0){
            for (Scope b : breakIns){
                if (b.getLoopNum() == loopNum + 1){
                    b.getInstruction().setParam(whileEnd - b.getPos()); // 跳转到 while 结束
                }
            }
        }
        if (continueIns.size()!=0){
            for (Scope c : continueIns){
                if (c.getLoopNum() == loopNum + 1){
//                    c.getInstruction().setParam(whileEnd - c.getPos() - 1);
                    c.getInstruction().setParam(whileStart - c.getPos()); // 跳转到 while 开始
                }
            }
        }

        if (loopNum == 0){
            breakIns = new ArrayList<>();
            continueIns = new ArrayList<>();
        }
    }

    /**
     * 'break' ';'
     */
    private static void analyseBreakStmt() throws GrammarError {
        System.out.println("开始分析break_stmt");
        // break
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.BREAK_KW){
            throw new GrammarError(ErrorType.NoBreakKeyWord);
        }

        /* 是否合法 */
        if (loopNum == 0){
            throw new GrammarError(ErrorType.InvalidBreak);
        }
        Instruction instruction = new Instruction(InstructionType.Br);
        breakIns.add(new Scope(instruction, instructions.size()+1, loopNum));
        instructions.add(instruction);

        // ;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
    }

    /**
     * 'continue' ';'
     */
    private static void analyseContinueStmt() throws GrammarError {
        System.out.println("开始分析continue");
        // continue
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.CONTINUE_KW){
            throw new GrammarError(ErrorType.NoContinueKeyWord);
        }
        /* 是否合法 */
        if (loopNum == 0){
            throw new GrammarError(ErrorType.InvalidContinue);
        }
        Instruction instruction = new Instruction(InstructionType.Br);
        continueIns.add(new Scope(instruction, instructions.size() + 1, loopNum));
        instructions.add(instruction);

        // ;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
    }

    /**
     * 'return' expr? ';'
     */
    private static void analyseReturnStmt() throws GrammarError {
        String exprRtnType = null;
        // return
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.RETURN_KW){
            throw new GrammarError(ErrorType.NoReturnKeyWord);
        }
        Token ptk = peek();
        if (ptk == null){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
        // expr
        if (ptk.getTokenType() != TokenType.SEMICOLON){
            if (rtnType == RtnType.INT){
                // 加载返回地址
                instructions.add(new Instruction(InstructionType.ArgA, 0));
                // expr
                exprRtnType = analyseA();
                if (!exprRtnType.equals("int")){
                    throw new GrammarError(ErrorType.InvalidReturn);
                }
                instructions.add(new Instruction(InstructionType.Store64));
                isReturn = true;
            }
            else if (rtnType == RtnType.DOUBLE){
                // 加载返回地址
                instructions.add(new Instruction(InstructionType.ArgA, 0));
                // expr
                exprRtnType = analyseA();
                if (!exprRtnType.equals("double")){
                    throw new GrammarError(ErrorType.InvalidReturn);
                }
                instructions.add(new Instruction(InstructionType.Store64));
                isReturn = true;
            }
            else if (rtnType == RtnType.VOID){
                throw new GrammarError(ErrorType.InvalidReturn);
            }

        }
        // ;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
        instructions.add(new Instruction(InstructionType.Ret));
    }

    /**
     * '{' stmt* '}'
     */
    private static void analyseBlockStmt() throws GrammarError {
        System.out.println("进入block");
        /* 进入花括号 层数加一 */
        level++;
        // {
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.L_BRACE){
            throw new GrammarError(ErrorType.ExpectedToken, "{");
        }
        // stmt*
        tk = next();
        if(tk.getTokenType() == null){
            throw new GrammarError(ErrorType.ExpectedToken, "}");
        }
        while (true){
            if (tk.getTokenType() == TokenType.R_BRACE){
                break;
            }
            tk = unread();
            analyseStmt();
            tk = next();
        }
        // }
        if (tk == null || tk.getTokenType() != TokenType.R_BRACE){
            throw new GrammarError(ErrorType.ExpectedToken, "}");
        }
        /* 清除本层及以内的局部变量 */
        Utils.clearCertainLevelLoc(level, symbolTable);
        /* 退出花括号层数减一 */
        level--;
        System.out.println("离开block");
    }

    /**
     * ';'
     */
    private static void analyseEmptyStmt() throws GrammarError {
        //;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
    }

    /**
     * expr ';'
     */
    private static void analyseExprStmt() throws GrammarError {
        // expr
        analyseA();

        // ;
        tk = next();
        if (tk == null || tk.getTokenType() != TokenType.SEMICOLON){
            throw new GrammarError(ErrorType.ExpectedToken, ";");
        }
    }

    /**
     *    表达式
     *    文法改写: A即Expr
     *     A -> ident = A | B               look ahead 2 tokens
     *     B -> C { < C | > C | <= C | >= C | == C | != C}
     *     C -> D { + D | - D }
     *     D -> E { * E | / E }
     *     E -> F { as ty}
     *     F -> -F | G
     *     G -> ident() | ident(H) | I      look ahead 2 tokens
     *     H -> A, H | A                    call_param_list
     *     I -> (A) | ident | literal
     */
    private static String analyseA() throws GrammarError {
        tk = next();
        tk = next();
        if (tk != null && tk.getTokenType() == TokenType.ASSIGN){
            tk = unread();
            tk = unread();
            analyseAssignExpr();
            return "";
        } else {
            String resultType = "";
            tk = unread();
            tk = unread();
            resultType = analyseB();
            return resultType;
        }
    }

    /* ident = A */
    private static void analyseAssignExpr() throws GrammarError{
        String resultType = "";
        // ident
        tk = next();
        String name = tk.getStringValue();
        // fortest
        for (SymbolEntry se:symbolTable){
            System.out.println(se.getName());
        }
        if (!Utils.canBeUsedVar(name, level, symbolTable)){
            throw new GrammarError(ErrorType.NotDeclared);
        }
        if (!Utils.canBeAssignSymbol(name, level, symbolTable)){
            throw new GrammarError(ErrorType.InvalidAssignment);
        }
        /* 获得ident的类型 */
        SymbolEntry sb = Utils.getVar(name, level, symbolTable);
        String identType = sb.getValType().getValue();
        int offset = sb.getStackOffset();
        /* 加载地址 */
        if (Utils.isLoc(name, symbolTable)){
            instructions.add(new Instruction(InstructionType.LocA, offset));
        } else if (Utils.isParam(name, symbolTable)){
            instructions.add(new Instruction(InstructionType.ArgA, returnSlots - 1 + offset));
        } else {
            SymbolEntry gsb = Utils.getVar(name, 1, symbolTable);
            int gOffset = gsb.getStackOffset();
            instructions.add(new Instruction(InstructionType.GlobA, gOffset));
        }


        // =
        tk = next();

        // A
        resultType = analyseA();

        /* 检查计算结果是否符合ident的类型 */
        if (!resultType.equals(identType)){
            System.out.println("identType: " + identType);
            System.out.println("resultType: " + resultType);
            throw new GrammarError(ErrorType.InvalidAssignment);
        }
        /* 值在栈顶 */
        instructions.add(new Instruction(InstructionType.Store64));
    }

    /* B -> C { < C | > C | <= C | >= C | == C | != C} */
    private static String analyseB() throws GrammarError{
        String resultType = "";
        String rightType = "";
        resultType = analyseC();
        tk = next();
        while (true){
            if (!Utils.isRelateOp(tk.getTokenType())){
                tk = unread();
                break;
            }
            TokenType opType = tk.getTokenType();
            rightType = analyseC();
            if (!resultType.equals(rightType)){
                throw new GrammarError(ErrorType.InvalidCalculation);
            }
            /* 两边的值都加载到了栈顶 */
            instructions.addAll(Utils.generateInstruction(resultType, opType));
            tk = next();
        }
        return resultType;
    }

    /* C -> D { + D | - D } */
    private static String analyseC() throws GrammarError{
        String leftType = "";
        String rightType = "";
        leftType = analyseD();
        tk = next();
        while (true){
            if(!Utils.isPlusOp(tk.getTokenType())){
                tk = unread();
                break;
            }
            TokenType opType = tk.getTokenType();
            rightType = analyseD();
            /* 指令 */
            if (!leftType.equals(rightType)){
                throw new GrammarError(ErrorType.InvalidCalculation);
            }
            if (leftType.equals("int")){
                if (opType == TokenType.PLUS){
                    instructions.add(new Instruction(InstructionType.AddI));
                } else {
                    instructions.add(new Instruction(InstructionType.SubI));
                }
            } else if (leftType.equals("double")){
                if (opType == TokenType.PLUS){
                    instructions.add(new Instruction(InstructionType.AddF));
                } else {
                    instructions.add(new Instruction(InstructionType.SubF));
                }
            }
            tk = next();
        }
        return leftType;
    }

    /* D -> E { * E | / E } */
    private static String analyseD() throws GrammarError{
        String leftType = "";
        String rightType = "";
        leftType = analyseE();
        tk = next();
        while (true){
            if (!Utils.isMulOp(tk.getTokenType())){
                tk = unread();
                break;
            }
            TokenType opType = tk.getTokenType();
            rightType = analyseE();
            /* 指令 */
            if (!leftType.equals(rightType)){
                throw new GrammarError(ErrorType.InvalidCalculation);
            }
            if (leftType.equals("int")){
                if (opType == TokenType.MUL){
                    instructions.add(new Instruction(InstructionType.MulI));
                } else {
                    instructions.add(new Instruction(InstructionType.DivI));
                }
            } else if (leftType.equals("double")){
                if (opType == TokenType.MUL){
                    instructions.add(new Instruction(InstructionType.MulF));
                } else {
                    instructions.add(new Instruction(InstructionType.DivF));
                }
            }
            tk = next();
        }
        return leftType;
    }

    /* E -> F { as ty } */
    private static String analyseE() throws GrammarError{
        String resultType = "";
        resultType = analyseF();
        tk = next();
        while(true){
            if (tk.getTokenType() != TokenType.AS_KW){
                tk = unread();
                break;
            }
            // ty
            tk = next();
            String newType = tk.getStringValue();
            if (resultType.equals("int") && newType.equals("double")){
                /* 指令 */
                instructions.add(new Instruction(InstructionType.IToF));
                resultType = "double";
            } else if (resultType.equals("double") && newType.equals("int")){
                instructions.add(new Instruction(InstructionType.FToI));
                resultType = "int";
            } else if(!resultType.equals(newType)){
                throw new GrammarError(ErrorType.InvalidAs);
            }
            tk = next();
        }
        return resultType;
    }

    /* F -> -F | G */
    private static String analyseF() throws GrammarError{
        String resultType = "";
        tk = next();
        if (tk.getTokenType() == TokenType.MINUS){
            resultType = analyseF();
            if (resultType.equals("int")){
                instructions.add(new Instruction(InstructionType.NegI));
            } else if (resultType.equals("double")){
                instructions.add(new Instruction(InstructionType.NegF));
            }
        } else {
            unread();
            resultType = analyseG();
        }
        return resultType;
    }

    /* G -> ident() | ident(H) | I      look ahead 2 tokens */
    private static String analyseG() throws GrammarError{
        boolean isSTLFunc = false;
        String resultType = "";
        Token tk1 = next();
        Token tk2 = next();
        /* 调用函数 */
        if (tk1.getTokenType() == TokenType.IDENT && tk2.getTokenType() == TokenType.L_PAREN){
            String funcName = tk1.getStringValue();
            Instruction callIns;
            int needParamNum = 0;
            int realParamNum = 0;

            /* 调用的函数 */
            SymbolEntry func = null;

            /* 标准库函数 */
            if (Utils.isSTLFunction(funcName)){
                isSTLFunc = true;

                /* 获得函数 */
                func = Utils.getSTLFunction(funcName, STLTable);
                needParamNum = func.getParams().size();

                /* 加入全局符号表 标准库函数每调用一次 就添加到全局符号表一次 */
                Global global = new Global(1, funcName.length(), funcName);
                globalTable.add(global);

                /* 指令 */
                callIns = new Instruction(InstructionType.CallName, globalCount);
                globalCount++;
            }

            /* 自定义函数 */
            else if (Utils.hasFunction(funcName, symbolTable)){
                /* 获得函数 */
                func = Utils.getFunction(funcName, symbolTable);
                needParamNum = func.getParams().size();

                /* 指令 */
                int offset = func.getStackOffset();
                callIns = new Instruction(InstructionType.Call, offset + 1);
            }

            /* 未声明函数 */
            else {
                throw new GrammarError(ErrorType.NotDeclared);
            }

            /* 根据函数的返回值确定 stackAlloc */
            String callRtnType = func.getRtnType().getValue();
            int retSlot = 0;
            if (callRtnType.equals("void")){
                resultType = "void";
                retSlot = 0;
            } else if (callRtnType.equals("int")){
                resultType = "int";
                retSlot = 1;
            } else if (callRtnType.equals("double")){
                resultType = "double";
                retSlot = 1;
            }
            /* 指令 */
            instructions.add(new Instruction(InstructionType.StackAlloc, retSlot));

            /* 分析参数 */
            tk = next();
            /* 无参数 */
            if (tk.getTokenType() == TokenType.R_PAREN){
                realParamNum = 0;
                /* 若参数不匹配 */
                if (needParamNum != realParamNum){
                    throw new GrammarError(ErrorType.InvalidParams);
                }
                /* 参数个数正确 */
                else {
                    instructions.add(callIns);
                }
            }
            /* 有参数 */
            else {
                tk = unread();
                analyseH(func, isSTLFunc);
                tk = next();
                instructions.add(callIns);
            }

            return resultType;
        }
        /* I -> (A) | ident | literal */
        else {
            tk = unread();
            tk = unread();
            resultType = analyseI();
        }
        return resultType;
    }

    /* H -> A, H | A                    call_param_list */
    private static void analyseH(SymbolEntry func, boolean isSTL) throws GrammarError{
        if (isSTL){
            analyseA();
            tk = next();
            if (tk.getTokenType() == TokenType.COMMA){
                throw new GrammarError(ErrorType.InvalidParams);
            } else {
                tk = unread();
            }
        } else {
            List<Param> needParams = func.getParams();
            int needCount = needParams.size();
            int realCount = 0;
            String resultType = "";
            while (true){
                resultType = analyseA();
                realCount++;
                if (realCount > needCount || !needParams.get(realCount-1).getValType().getValue().equals(resultType)){
                    throw new GrammarError(ErrorType.InvalidParams);
                }
                tk = next();
                if (tk.getTokenType() != TokenType.COMMA){
                    tk = unread();
                    break;
                }
            }
        }

    }

    /* I -> (A) | ident | literal */
    private static String analyseI() throws GrammarError{
        String resultType = "";
        tk = next();
        // (A)
        if (tk.getTokenType() == TokenType.L_PAREN){
            resultType = analyseA();
            tk = next();
            if (tk.getTokenType() != TokenType.R_PAREN){
                throw new GrammarError(ErrorType.ExpectedToken, ")");
            }
        }
        // ident
        else if (tk.getTokenType() == TokenType.IDENT){
            String name = tk.getStringValue();
            System.out.println("---------------------------");
            System.out.println(name);
            if (!Utils.canBeUsedVar(name, level, symbolTable)){
                throw new GrammarError(ErrorType.NotDeclared);
            }
            SymbolEntry sb = Utils.getVar(name, level, symbolTable);
            String identType = sb.getValType().getValue();
            resultType = identType;
            int offset = sb.getStackOffset();
            // 局部变量
            if (Utils.isLoc(name, symbolTable)){
                instructions.add(new Instruction(InstructionType.LocA, offset));
                instructions.add(new Instruction(InstructionType.Load64));
            }
            // 参数
            else if (Utils.isParam(name, symbolTable)){
                instructions.add(new Instruction(InstructionType.ArgA, returnSlots-1+offset));
                instructions.add(new Instruction(InstructionType.Load64));
            }
            // 全局符号
            else {
                SymbolEntry gsb = Utils.getVar(name, 1, symbolTable);
                int gOffset = gsb.getStackOffset();
                instructions.add(new Instruction(InstructionType.GlobA, gOffset));
                instructions.add(new Instruction(InstructionType.Load64));
            }
        }
        // int_literal
        else if (tk.getTokenType() == TokenType.INT_LITERAL) {
            instructions.add(new Instruction(InstructionType.Push, (long)tk.getValue()));
            resultType = "int";
        }
        // double_literal
        else if (tk.getTokenType() == TokenType.DOUBLE_LITERAL){
            String binary = Long.toBinaryString(Double.doubleToLongBits((double)tk.getValue()));
            instructions.add(new Instruction(InstructionType.Push, Utils.toTen(binary)));
            resultType = "double";
        }
        // string_literal
        else if (tk.getTokenType() == TokenType.STRING_LITERAL){
            String str = tk.getStringValue();
            Global global = new Global(1, str.length(), str);
            globalTable.add(global);

            instructions.add(new Instruction(InstructionType.Push, globalCount));
            globalCount++;
            resultType = "string";
        }
        // char_literal;
        else if (tk.getTokenType() == TokenType.CHAR_LITERAL){
            instructions.add(new Instruction(InstructionType.Push, (Integer) tk.getValue()));
            resultType = "int";
        }
        return resultType;
    }





    // 读取下一个token
    private static Token next(){
        if (tokenIndex < tkListLength){
            return tkList.get(++tokenIndex);
        } else {
            // 已经到最后一个token了
            return null;
        }
    }

    // 回退一个token
    private static Token unread(){
        if (tokenIndex >= 0){
            tokenIndex--;
            if (tokenIndex == -1){
                return null;
            } else {
                return tkList.get(tokenIndex);
            }
        } else {
            return null;
        }
    }

    // 预读一个token
    private static Token peek(){
        if (tokenIndex < tkListLength){
            return tkList.get(tokenIndex + 1);
        } else {
            // 已经到最后一个token了
            return null;
        }
    }

    private static boolean isEof(){
        return tokenIndex == tkListLength - 1;
    }

    private static void initSTLFunction(){
        STLTable.add(new SymbolEntry("getint", SymbolType.FUNCTION, 1, RtnType.INT, new ArrayList<>()));
        STLTable.add(new SymbolEntry("getdouble", SymbolType.FUNCTION, 2, RtnType.DOUBLE, new ArrayList<>()));
        STLTable.add(new SymbolEntry("getchar", SymbolType.FUNCTION, 3, RtnType.INT, new ArrayList<>()));
        List<Param> params1 = new ArrayList<>();
        params1.add(new Param("", ValType.INT, false));
        STLTable.add(new SymbolEntry("putint", SymbolType.FUNCTION, 4, RtnType.VOID, params1));
        List<Param> params2 = new ArrayList<>();
        params1.add(new Param("", ValType.DOUBLE, false));
        STLTable.add(new SymbolEntry("putdouble", SymbolType.FUNCTION, 5, RtnType.VOID, params2));
        List<Param> params3 = new ArrayList<>();
        params1.add(new Param("", ValType.INT, false));
        STLTable.add(new SymbolEntry("putchar", SymbolType.FUNCTION, 6, RtnType.VOID, params3));
        List<Param> params4 = new ArrayList<>();
        params1.add(new Param("", ValType.INT, false));
        STLTable.add(new SymbolEntry("putstr", SymbolType.FUNCTION, 7, RtnType.VOID, params4));
        STLTable.add(new SymbolEntry("putln", SymbolType.FUNCTION, 8, RtnType.VOID, new ArrayList<>()));
    }

    private static boolean hasMain(){
        return hasMain;
    }

    public static List<Global> getGlobalTable() {
        return globalTable;
    }

    public static List<FunctionDef> getFunctionDefTable() {
        return functionDefTable;
    }

    public static FunctionDef getStartFunction() {
        return startFunction;
    }
}
