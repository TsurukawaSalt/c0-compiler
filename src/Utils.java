import java.util.ArrayList;
import java.util.List;

enum SymbolType{
    FUNCTION,
    CONSTANT,
    VARIABLE,
    PARAM,
    CONSTPARAM,
}

enum ValType{
    VOID("void"),
    INT("int"),
    STRING("string"),
    // 扩展
    DOUBLE("double"),
    CHAR("char");

    private String value;

    ValType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

enum RtnType {
    VOID("void"),
    INT("int"),
    // 扩展
    DOUBLE("double");

    String value;

    RtnType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}

public class Utils {

    public static long toTen(String a){
        long aws = 0;
        long xi = 1;
        for(int i=a.length()-1; i>=0; i--){
            if(a.charAt(i) == '1')
                aws += xi;
            xi *=2;
        }
        return aws;
    }

    public static boolean isRelateOp(TokenType ty){
        return ty == TokenType.LT ||
                ty == TokenType.GT ||
                ty == TokenType.LE ||
                ty == TokenType.GE ||
                ty == TokenType.EQ ||
                ty == TokenType.NEQ;
    }

    public static boolean isPlusOp(TokenType ty){
        return ty == TokenType.PLUS ||
                ty == TokenType.MINUS;
    }

    public static boolean isMulOp(TokenType ty ){
        return ty == TokenType.MUL ||
                ty == TokenType.DIV;
    }

    public static boolean hasMainReturn(List<SymbolEntry> symbols){
        for(SymbolEntry symbolEntry : symbols){
            if (symbolEntry.getName().equals("main")){
                RtnType rtnType =  symbolEntry.getRtnType();
                if (rtnType != RtnType.VOID){
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean hasReturn(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry: symbols){
            if (symbolEntry.getName().equals(name) && symbolEntry.getSymbolType() == SymbolType.FUNCTION){
                return symbolEntry.getRtnType() != RtnType.VOID;
            }
        }
        return false;
    }

    public static List<Instruction> generateInstruction(String type, TokenType tokenType){
        List<Instruction> instructions = new ArrayList<>();
        if (tokenType == TokenType.EQ){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            Instruction instruction2 = new Instruction(InstructionType.Not);
            instructions.add(instruction1);
            instructions.add(instruction2);
        } else if (tokenType == TokenType.NEQ){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            instructions.add(instruction1);
        } else if (tokenType == TokenType.LT){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            Instruction instruction2 = new Instruction(InstructionType.SetLT);
            instructions.add(instruction1);
            instructions.add(instruction2);
        } else if (tokenType == TokenType.GT){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            Instruction instruction2 = new Instruction(InstructionType.SetGT);
            instructions.add(instruction1);
            instructions.add(instruction2);
        } else if (tokenType == TokenType.LE){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            Instruction instruction2 = new Instruction(InstructionType.SetGT);
            Instruction instruction3 = new Instruction(InstructionType.Not);
            instructions.add(instruction1);
            instructions.add(instruction2);
            instructions.add(instruction3);
        } else if (tokenType == TokenType.GE){
            Instruction instruction1 = null;
            if (type.equals("int")){
                instruction1 = new Instruction(InstructionType.CmpI);
            } else if (type.equals("double")){
                instruction1 = new Instruction(InstructionType.CmpF);

            }
            Instruction instruction2 = new Instruction(InstructionType.SetLT);
            Instruction instruction3 = new Instruction(InstructionType.Not);
            instructions.add(instruction1);
            instructions.add(instruction2);
            instructions.add(instruction3);
        }
        return instructions;
    }

    public static SymbolEntry getVar(String name, int level, List<SymbolEntry> symbols){
        SymbolEntry mostMatch = null;
        int nowLevel = 0;
        for (SymbolEntry symbolEntry: symbols){
            if (symbolEntry.getName().equals(name) && symbolEntry.getLevel() <= level){
                if (symbolEntry.getLevel() > nowLevel){
                    mostMatch = symbolEntry;
                }
            }
        }
        return mostMatch;
    }

    /* 在同层数查找是否已有使用改name声明的符号 */
    public static boolean isDeclaredVar(String name, int level, List<SymbolEntry> symbols){
        for (SymbolEntry se: symbols){
            if (se.getName().equals(name) && se.getSymbolType() != SymbolType.FUNCTION && se.getLevel()==level){
                return true;
            }
        }
        return false;
    }

    /* 指可以被使用的变量or常量 满足存在于符号表且所在层数小于等于当前层数 */
    public static boolean canBeUsedVar(String name, int level, List<SymbolEntry> symbols){
        for (SymbolEntry se: symbols){
            if (se.getName().equals(name) && se.getSymbolType() != SymbolType.FUNCTION && se.getLevel() <= level){
                return true;
            }
        }
        return false;
    }

    /* 指可以被赋值的变量（非常量or常量参数） 满足存在于符号表且所在层数小于等于当前层数 */
    public static boolean canBeAssignSymbol(String name, int level, List<SymbolEntry> symbols){
        for (SymbolEntry se : symbols){
            if (se.getName().equals(name)){
                if ((se.getSymbolType() == SymbolType.VARIABLE || se.getSymbolType() == SymbolType.PARAM) && se.getLevel() <= level){
                    return true;
                }
            }
        }
        return false;
    }

    /* 判断是否是当前函数的局部变量 */
    public static boolean isLoc(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry : symbols){
            if (symbolEntry.getName().equals(name) && (symbolEntry.getSymbolType() == SymbolType.CONSTANT || symbolEntry.getSymbolType() == SymbolType.VARIABLE) &&symbolEntry.getLevel() > 1) {
                return true;
            }
        }
        return false;
    }

    /* 根据name获得当前函数的certain局部变量 */
    public static SymbolEntry getLoc(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry : symbols){
            if (symbolEntry.getName().equals(name) && (symbolEntry.getSymbolType() == SymbolType.CONSTANT || symbolEntry.getSymbolType() == SymbolType.VARIABLE) &&symbolEntry.getLevel() > 1) {
                return symbolEntry;
            }
        }
        return null;
    }

    /* 判断是否是当前函数的参数 */
    public static boolean isParam(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry : symbols){
            if (symbolEntry.getName().equals(name) && (symbolEntry.getSymbolType() == SymbolType.PARAM || symbolEntry.getSymbolType() == SymbolType.CONSTPARAM)){
                return true;
            }
        }
        return false;
    }

    /* 根据name获得当前函数的certain参数 */
    public static SymbolEntry getParam(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry : symbols){
            if (symbolEntry.getName().equals(name) && symbolEntry.getSymbolType() == SymbolType.PARAM){
                return symbolEntry;
            }
        }
        return null;
    }
    /* 判断是否是常量 */
    public static boolean isConstant(String name, List<SymbolEntry> symbol){
        for (SymbolEntry se : symbol){
            if (se.getName().equals(name)){
                if (se.getSymbolType() == SymbolType.CONSTANT || se.getSymbolType() == SymbolType.CONSTPARAM){
                    return true;
                }
            }
        }
        return false;
    }
    /* 清除某层数的所有符号 退出block时调用 */
    public static void clearCertainLevelLoc(int level, List<SymbolEntry> symbols){
        symbols.removeIf(se -> se.getSymbolType() != SymbolType.FUNCTION && se.getLevel() >= level);
    }

    /* 判断是否有该函数 */
    public static boolean hasFunction(String name, List<SymbolEntry> symbolEntries) {
        for (SymbolEntry symbolEntry: symbolEntries){
            if (symbolEntry.getName().equals(name) && symbolEntry.getSymbolType() == SymbolType.FUNCTION){
                return true;
            }
        }
        return false;
    }

    /* 根据name获得某函数 */
    public static SymbolEntry getFunction(String name, List<SymbolEntry> symbols){
        for (SymbolEntry symbolEntry: symbols){
            if (symbolEntry.getName().equals(name) && symbolEntry.getSymbolType() == SymbolType.FUNCTION){
                return symbolEntry;
            }
        }
        return null;
    }

    /* 判断是标准库函数 */
    public static boolean isSTLFunction(String name){
        return name.equals("getint") || name.equals("getdouble") || name.equals("getchar") ||
                name.equals("putint") || name.equals("putdouble") || name.equals("putchar") ||
                name.equals("putstr") || name.equals("putln");
    }

    /* 根据name从STLTable获得标准库函数 */
    public static SymbolEntry getSTLFunction(String name, List<SymbolEntry> STLTable){
        for (SymbolEntry se : STLTable){
            if (se.getName().equals(name)){
                return se;
            }
        }
        return null;
    }

    /* 获得返回值类型 */
    public static RtnType getRtnType(Token tk){
        String val = tk.getStringValue();
        switch (val){
            case "int":
                return RtnType.INT;
            case "double":
                return RtnType.DOUBLE;
            case "void":
                return RtnType.VOID;
            default:
                return null;
        }
    }

    /* 获得数据类型 */
    public static ValType getValType(Token tk) {
        String val = tk.getStringValue();
        switch (val){
            case "int":
                return ValType.INT;
            case "double":
                return ValType.DOUBLE;
            case "char":
                return ValType.CHAR;
            case "string":
                return ValType.STRING;
            default:
                return null;
        }
    }
}
