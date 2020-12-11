import java.util.List;

public class SymbolEntry {
    private String name;
    private SymbolType symbolType;
    private ValType valType;
    private int level;
    private int stackOffset;            // 即id
    // 变量独有
    private int isInitialed;
    // 函数独有
    private RtnType rtnType;
    private List<Param> params;

    // 常量
    public SymbolEntry(String name, SymbolType symbolType, ValType valType, int level, int stackOffset) {
        this.name = name;
        this.symbolType = symbolType;
        this.valType = valType;
        this.level = level;
        this.stackOffset = stackOffset;
    }

    // 变量
    public SymbolEntry(String name, SymbolType symbolType, ValType valType, int level, int stackOffset, int isInitialed) {
        this.name = name;
        this.symbolType = symbolType;
        this.valType = valType;
        this.level = level;
        this.stackOffset = stackOffset;
        this.isInitialed = isInitialed;
    }

    // 函数
    public SymbolEntry(String name, SymbolType symbolType, int stackOffset, RtnType rtnType, List<Param> params) {
        this.name = name;
        this.symbolType = symbolType;
        this.stackOffset = stackOffset;
        this.rtnType = rtnType;
        this.params = params;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public ValType getValType() {
        return valType;
    }

    public void setValType(ValType valType) {
        this.valType = valType;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public int getStackOffset() {
        return stackOffset;
    }

    public void setStackOffset(int stackOffset) {
        this.stackOffset = stackOffset;
    }

    public int getIsInitialed() {
        return isInitialed;
    }

    public void setIsInitialed(int isInitialed) {
        this.isInitialed = isInitialed;
    }

    public RtnType getRtnType() {
        return rtnType;
    }

    public void setRtnType(RtnType rtnType) {
        this.rtnType = rtnType;
    }

    public List<Param> getParams() {
        return params;
    }

    public void setParams(List<Param> params) {
        this.params = params;
    }
}
