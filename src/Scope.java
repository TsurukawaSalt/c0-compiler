public class Scope {
    Instruction instruction;
    int pos;
    int loopNum;

    public Scope(Instruction instruction, int pos, int loopNum) {
        this.instruction = instruction;
        this.pos = pos;
        this.loopNum = loopNum;
    }

    public Instruction getInstruction() {
        return instruction;
    }

    public void setInstruction(Instruction instruction) {
        this.instruction = instruction;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getLoopNum() {
        return loopNum;
    }

    public void setLoopNum(int loopNum) {
        this.loopNum = loopNum;
    }
}
