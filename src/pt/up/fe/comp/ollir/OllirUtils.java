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
                return " <.bool ";
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