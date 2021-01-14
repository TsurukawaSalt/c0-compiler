import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Transfer {
    private List<Global> globalTable;
    private FunctionDef start;
    private List<FunctionDef> functionDefTable;
    private List<Byte> output;

    int magic = 0x72303b3e;
    int version = 0x00000001;

    public Transfer(List<Global> globalTable, FunctionDef start, List<FunctionDef> functionDefTable) throws FileNotFoundException {
        this.globalTable = globalTable;
        this.start = start;
        this.functionDefTable = functionDefTable;
        output = new ArrayList<>();
    }

    public List<Byte> generate() throws IOException{
        // magic
        transAddInt(4, magic);

        // version
        transAddInt(4, version);

        // globals.count
        transAddInt(4, globalTable.size());

        for (Global global: globalTable){
            // is_const
            transAddInt(1, global.getIs_const());
            // value.count
            // value.items
            if (global.getValueItems() == null){
                transAddInt(4, 8);
                transAddLong(8, 0L);
            } else {
                transAddInt(4, global.getValueItems().length());
                transAddString(global.getValueItems());
            }
        }

        // functions.count
        transAddInt(4, functionDefTable.size() + 1);

        // start
        generateFunction(start);

        // other functions
        for (FunctionDef functionDef: functionDefTable){
            generateFunction(functionDef);
        }

        return output;
    }

    private void generateFunction(FunctionDef functionDef) throws IOException{
        // name
        transAddInt(4, functionDef.getNameIndex());

        // ret_slots
        transAddInt(4, functionDef.getReturnSlots());

        // params_slots;
        transAddInt(4, functionDef.getParamSlots());

        // loc_slots;
        transAddInt(4, functionDef.getLocSlots());

        // body.count
        transAddInt(4, functionDef.getBody().size());

        // body.items
        List<Instruction> instructions = functionDef.getBody();
        for(Instruction instruction : instructions){
            transAddInt(1, instruction.getInsType().getByteVal());
            if(instruction.getParam() != null){
                if(instruction.getInsType() == InstructionType.Push){
                    transAddLong(8, instruction.getParam());
                }
                else{
                    transAddInt(4,instruction.getParam().intValue());
                }
            }
        }
    }

//    private List<Byte> Char2bytes(char value) {
//        List<Byte>  AB=new ArrayList<>();
//        AB.add((byte)(value&0xff));
//        return AB;
//    }
//
//    private List<Byte> String2bytes(String valueString) {
//        List<Byte>  AB=new ArrayList<>();
//        for (int i=0;i<valueString.length();i++){
//            char ch=valueString.charAt(i);
//            AB.add((byte)(ch&0xff));
//        }
//        return AB;
//    }
//
//    private List<Byte> long2bytes(int length, long target) {
//        ArrayList<Byte> bytes = new ArrayList<>();
//        int start = 8 * (length-1);
//        for(int i = 0 ; i < length; i++){
//            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
//        }
//        return bytes;
//    }
//
//    private ArrayList<Byte> int2bytes(int length,int target){
//        ArrayList<Byte> bytes = new ArrayList<>();
//        int start = 8 * (length-1);
//        for(int i = 0 ; i < length; i++){
//            bytes.add((byte) (( target >> ( start - i * 8 )) & 0xFF ));
//        }
//        return bytes;
//    }
    
    private void transAddString(String s){
        for (int i = 0; i < s.length(); i++){
            char c = s.charAt(i);
            output.add((byte)c);
        }
    }

    private void transAddInt(int length, int x){
        int start = 8 * (length - 1);
        for(int i = 0; i < length; i++){
            output.add((byte) (( x >> ( start - i * 8 )) & 0xFF ));
        }
    }

    private void transAddLong(int length, long x){
        int start = 8 * (length-1);
        for(int i = 0 ; i < length; i++){
            output.add((byte) (( x >> ( start - i * 8 )) & 0xFF ));
        }
    }
}