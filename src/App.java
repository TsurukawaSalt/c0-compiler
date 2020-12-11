import java.io.*;
import java.util.List;
import java.util.Scanner;

/**
 * c0-compiler
 */
public class App{
    public static void main(String[] args) {
        try {
            String filepath = args[0];
            InputStream input = new FileInputStream(filepath);
            Scanner scanner = new Scanner(input);
            StringIter it = new StringIter(scanner);
            Tokenizer tokenizer = new Tokenizer(it);
            GrammarAnalyser.AnalyseGrammar(tokenizer);

            // for test
            System.out.println("全局符号表大小：" + GrammarAnalyser.getGlobalTable().size());
            System.out.println("全局符号表：");
            for (Global global: GrammarAnalyser.getGlobalTable()){
                System.out.print("isConst: " + global.getIs_const() + ", ");
                System.out.print("count: " + global.getValueCount() + ", ");
                System.out.println(global.getValueItems());
            }
            System.out.println("起始函数：");
            System.out.println("index: " + GrammarAnalyser.getStartFunction().getNameIndex() + ", ");
            System.out.println("paramSlot: " + GrammarAnalyser.getStartFunction().getParamSlots() + " ,");
            System.out.println("locSlot: " + GrammarAnalyser.getStartFunction().getLocSlots() + " ,");
            System.out.println("returnSot: " + GrammarAnalyser.getStartFunction().getReturnSlots() + ", ");
            int j=0;
            for (Instruction ins:GrammarAnalyser.getStartFunction().getBody()){
                System.out.println(j + ". " + ins.insType + "(" + ins.getParam() + ")");
                j++;
            }
            System.out.println("函数：");
            for (FunctionDef functionDef: GrammarAnalyser.getFunctionDefTable()){
                System.out.println("index: " + functionDef.getNameIndex() + ", ");
                System.out.println("paramSlot: " + functionDef.getParamSlots() + ", ");
                System.out.println("locSlot: " + functionDef.getLocSlots() + ", ");
                System.out.println("returnSot: " + functionDef.getReturnSlots() + ", ");
                int i=0;
                for (Instruction ins:functionDef.getBody()){
                    System.out.println(i + ". " + ins.insType + "(" + ins.getParam() + ")");
                    i++;
                }
            }

            // transfer
            Transfer binary = new Transfer(GrammarAnalyser.getGlobalTable(), GrammarAnalyser.getStartFunction(), GrammarAnalyser.getFunctionDefTable());
            List<Byte> bytes = binary.generate();
            byte[] resultBytes = new byte[bytes.size()];
            for (int i = 0; i < bytes.size(); ++i) {
                resultBytes[i] = bytes.get(i);
//                System.out.println(bytes.get(i));
            }

            // out
            DataOutputStream out = new DataOutputStream(new FileOutputStream(new File(args[1])));
            out.write(resultBytes);

        }catch (Exception e){
            e.printStackTrace();
            System.exit(-1);
        }
    }
}
