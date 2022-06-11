package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;

public class OllirUtils {

    public static String getCode(Symbol symbol){

        return symbol.getName() + "." + getOllirType(symbol.getType());
    }

    public static Symbol getSymbol(String name, List<Symbol> fields, List<Symbol> localVars){

        Symbol var = null;
        boolean isField = false;
        for (Symbol symbol : fields) {
            if (symbol.getName().equals(name)) {
                isField = true;
                var = symbol;
                break;
            }
        }
        if (!isField){
            for (Symbol localVar:localVars){
                if (localVar.getName().equals(name)){
                    var = localVar;
                    break;
                }
            }
            //for (Symbol param: parameters){
            //    if (param.getName().equals(name)){
            //        var = param;
            //        break;
            //    }
            //}
        }

        return var;
    }


    public static String getOllirType(Type type){
        StringBuilder code = new StringBuilder();
        code.append(".");

        if (type.isArray())
            return "array." + type.getName();

        String jmmType = type.getName();

        switch(jmmType){
            case "int":
                return "i32";
            case "boolean":
                return "bool";
            case "void":
                return "V";
        }

        return jmmType;
    }

    public static boolean isOperation(JmmNode operation) {
        return operation.getKind().equals("Plus") || operation.getKind().equals("Minus") ||
                operation.getKind().equals("Times") || operation.getKind().equals("Divide") ||
                operation.getKind().equals("Less") || operation.getKind().equals("And") || operation.getKind().equals("Not");
    }

    public static String getOllirVar(String jmmVar, Type type){
        return jmmVar + getOllirType(type);
    }

    public static String getOllirParameter(int position, String jmmParameter){
        return "$" + position + "." + jmmParameter;
    }

    public static String getOllirOperator(JmmNode jmmOperator, StringBuilder code){
        if (jmmOperator.getKind().equals("Assignment")){
            String substring = code.substring(code.lastIndexOf(":=."), code.length() - 1);
            substring = substring.substring(0, substring.indexOf(" "));

        }

        switch (jmmOperator.getKind()){
            case "Add":
                return " +.i32 ";
            case "Less":
                return " <.i32 ";
            case "Sub":
                return " -.i32 ";
            case "Mult":
                return " *.i32 ";
            case "Div":
                return " /.i32 ";
            case "And":
                return " &&.bool ";
            case "Not":
                return " !.bool ";
            default:
                return ".V";
        }
    }

}