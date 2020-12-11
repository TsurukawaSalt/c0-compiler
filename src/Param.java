public class Param {
    private String name;            // 参数名
    private ValType valType;        // 参数类型
    private boolean isConstant;     // 是否是常量

    public Param(String name, ValType valType, boolean isConstant) {
        this.name = name;
        this.valType = valType;
        this.isConstant = isConstant;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ValType getValType() {
        return valType;
    }

    public void setValType(ValType valType) {
        this.valType = valType;
    }

    public boolean isConstant() {
        return isConstant;
    }

    public void setConstant(boolean constant) {
        isConstant = constant;
    }
}
