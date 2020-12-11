public enum InstructionType {
    Nop(0x00),
    Push(0x01),
    Pop(0x02),
    PopN(0x03),
    Dup(0x04),
    LocA(0x0a),
    ArgA(0x0b),
    GlobA(0x0c),
    Load8, Load16, Load32, Load64(0x13),
    Store8, Store16, Store32, Store64(0x17),
    Alloc(0x18),
    Free(0x19),
    StackAlloc(0x1a),
    AddI(0x20), SubI(0x21), MulI(0x22), DivI(0x23),
    AddF(0x24), SubF(0x25), MulF(0x26), DivF(0x27),
    DivU(0x28),
    Shl(0x29),
    Shr(0x2a),
    And(0x2b), Or(0x2c), Xor(0x2d), Not(0x2e),
    CmpI(0x30), CmpU(0x31), CmpF(0x32),
    NegI(0x34), NegF(0x35),
    IToF(0x36), FToI(0x37),
    Shrl(0x38),
    SetLT(0x39),
    SetGT(0x3a),
    Br(0x41),
    BrFalse(0x42),
    BrTrue(0x43),
    Call(0x48),
    Ret(0x49),
    CallName(0x4a),
    ScanI(0x50), ScanC(0x51), ScanF(0x52),
    PrintI(0x54), PrintC(0x55), PrintF(0x56), PrintS(0x57),
    PrintLN(0x58),
    Panic(0xfe);

    private String value;
    private byte byteVal;

    InstructionType() {}

    InstructionType(int byteVal) {
        this.byteVal = (byte) byteVal;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public byte getByteVal() {
        return byteVal;
    }

    public void setByteVal(byte byteVal) {
        this.byteVal = byteVal;
    }
}


