import java.util.List;

/**
 * function的具体输出信息，属性参考C0指导书
 */
public class FunctionDef {
    /* 函数名称在全局变量中的位置 */
    private Integer nameIndex;
    /* 返回值占据的 slot 数 */
    private Integer returnSlots;
    /* 参数占据的 slot 数 */
    private Integer paramSlots;
    /* 局部变量占据的 slot 数 */
    private Integer locSlots;
    /* 函数体：指令序列 */
    private List<Instruction> body;

    public FunctionDef(Integer nameIndex, Integer returnSlots, Integer paramSlots, Integer locSlots, List<Instruction> body) {
        this.nameIndex = nameIndex;
        this.returnSlots = returnSlots;
        this.paramSlots = paramSlots;
        this.locSlots = locSlots;
        this.body = body;
    }

    public Integer getNameIndex() {
        return nameIndex;
    }

    public void setNameIndex(Integer nameIndex) {
        this.nameIndex = nameIndex;
    }

    public Integer getReturnSlots() {
        return returnSlots;
    }

    public void setReturnSlots(Integer returnSlots) {
        this.returnSlots = returnSlots;
    }

    public Integer getParamSlots() {
        return paramSlots;
    }

    public void setParamSlots(Integer paramSlots) {
        this.paramSlots = paramSlots;
    }

    public Integer getLocSlots() {
        return locSlots;
    }

    public void setLocSlots(Integer locSlots) {
        this.locSlots = locSlots;
    }

    public List<Instruction> getBody() {
        return body;
    }

    public void setBody(List<Instruction> body) {
        this.body = body;
    }
}
