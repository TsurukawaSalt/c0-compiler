import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Transfer {
    private List<Global> globals;
    private FunctionDef start;
    private List<FunctionDef> functionDefs;
    private List<Byte> output;

    int magic = 0x72303b3e;
    int version = 0x00000001;

    public Transfer(List<Global> globals, FunctionDef start, List<FunctionDef> functionDefs) throws FileNotFoundException {
        this.globals = globals;
        this.start = start;
        this.functionDefs = functionDefs;
        output = new ArrayList<>();
    }

    public List<Byte> generate() throws IOException{
        //magic
        List<Byte> magic=int2bytes(4,this.magic);
        output.addAll(magic);
        //version
        List<Byte> version=int2bytes(4,this.version);
        output.addAll(version);

        //globals.count
        List<Byte> globalCount = int2bytes(4, globals.size());
        output.addAll(globalCount);

        for (Global global: globals){
            // isConstant
            List<Byte> isConst = int2bytes(1, global.getIs_const());
            output.addAll(isConst);

            // value.count
            List<Byte> globalValueCount;

            // value items
            List<Byte> globalValue;
            if (global.getValueItems() == null){
                globalValueCount = int2bytes(4, 8);
                globalValue = long2bytes(8,0L);
            } else {
                globalValue = String2bytes(global.getValueItems());
                globalValueCount = int2bytes(4, globalValue.size());
            }

            output.addAll(globalValueCount);
            output.addAll(globalValue);
        }

        // function.count
        List<Byte> functionsCount = int2bytes(4, functionDefs.size() + 1);
        output.addAll(functionsCount);

        generateFunction(start);

        for (FunctionDef functionDef: functionDefs){
            generateFunction(functionDef);
        }
        return output;
    }

    private void generateFunction(FunctionDef functionDef) throws IOException{
        // name
        List<Byte> name = int2bytes(4, functionDef.getNameIndex());
        output.addAll(name);

        //retSlots
        List<Byte> retSlots = int2bytes(4,functionDef.getReturnSlots());
        output.addAll(retSlots);

        //paramsSlots;
        List<Byte> paramsSlots=int2bytes(4,functionDef.getParamSlots());
        output.addAll(paramsSlots);

        //locSlots;
        List<Byte> locSlots=int2bytes(4,functionDef.getLocSlots());
        output.addAll(locSlots);

        List<Instruction> instructions = functionDef.getBody();

        //bodyCount
        List<Byte> bodyCount=int2bytes(4, instructions.size());
        output.addAll(bodyCount);

        //instructions
        for(Instruction instruction : instructions){
            //type
            List<Byte> type = int2bytes(1, instruction.getInsType().getByteVal());
            output.addAll(type);
            //out.writeBytes(type.toString());

            if(instruction.getParam() != null){
                List<Byte>  x;
                if(instruction.getInsType() == InstructionType.Push)
                    x = long2bytes(8,instruction.getParam());
                else
                    x = int2bytes(4,instruction.getParam().intValue());
                output.addAll(x);
            }
        }
    }

    private List<Byte> Char2bytes(char value) {
        List<Byte>  AB=new ArrayList<>();
        AB.add((byte)(value&0xff));
        return AB;
    }

    private List<Byte> String2bytes(String valueString) {
        List<Byte>  AB=new ArrayList<>();
        for (int i=0;i<valueString.length();i++){
            char ch=valueString.charAt(i);
            AB.add((byte)(ch&0xff));
        }
        return AB;
    }

    private List<Byte> long2bytes(int length, long target) {
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }

    private ArrayList<Byte> int2bytes(int length,int target){
        ArrayList<Byte> bytes = new ArrayList<>();
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
        }
        return bytes;
    }
}