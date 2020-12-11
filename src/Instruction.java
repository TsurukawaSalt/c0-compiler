/**
 * 记录一个指令的具体信息
 */
public class Instruction {
    InstructionType insType;    // 指令类型
    Long param;             // 参数

    public Instruction(InstructionType insType, Long param) {
        this.insType = insType;
        this.param = param;
    }

    public Instruction(InstructionType insType, Integer param) {
        this.insType = insType;
        this.param = (long)param;
    }

    public Instruction(InstructionType insType) {
        this.insType = insType;
        this.param = null;
    }

    public InstructionType getInsType() {
        return insType;
    }

    public void setInsType(InstructionType insType) {
        this.insType = insType;
    }

    public Long getParam() {
        return param;
    }

    public void setParam(Long param) {
        this.param = param;
    }

    public void setParam(Integer param) {
        this.param = (long)param;
    }
}