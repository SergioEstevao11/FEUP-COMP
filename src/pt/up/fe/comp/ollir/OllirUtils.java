package pt.up.fe.comp.ollir;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

public class OllirUtils {

    public static String getCode(Symbol symbol){

        return symbol.getName() + "." + getOllirType(symbol.getType());
    }

    public static String getOllirType(Type type){
        StringBuilder code = new StringBuilder();
        code.append(".");

        if (type.isArray())
            code.append("array.");

        String jmmType = type.getName();

        switch(jmmType){
            case "int":
                code.append("i32");
                break;
            case "boolean":
                code.append("bool");
                break;
            case "void":
                code.append("V");
                break;
            default:
                code.append(jmmType);
                break;
        }

        return code.toString();
    }

    public static String getOllirVar(String jmmVar, Type type){
        return jmmVar + getOllirType(type);
    }

    public static String getOllirParameter(int position, String jmmParameter){
        return "$" + position + "." + jmmParameter;
    }

    public static String getOllirOperator(JmmNode jmmOperator){
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
                return "OPERATOR";
        }
    }

}