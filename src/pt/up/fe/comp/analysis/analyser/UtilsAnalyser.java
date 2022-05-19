package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
public class UtilsAnalyser {

    public static String getVariableType(JmmNode node, SymbolTable symbolTable) {
        //System.out.println(symbolTable.getLocalVariables(node.getJmmParent()));
        if (node.getKind().equals("Number")) return "int";
        else if (node.getKind().equals("True") || node.getKind().equals("False")) return "boolean";
        else if (node.getKind().equals("This")) return symbolTable.getClassName();

        /*List<Symbol> localVariables = symbolTable.getLocalVariables(parentMethodName);
        List<Symbol> fields = symbolTable.getFields();
        List<Symbol> parameters = symbolTable.getParameters(parentMethodName);

        // Verifies if the element is in the symbol and table. And if it is, return the type.
        for (Symbol symb : localVariables) {
            String varName = symb.getName();
            if (varName.equals(node.get("name")))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }

        for (Symbol symb : fields) {
            String varName = symb.getName();
            if (varName.equals(node.get("name")))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }

        for (Symbol symb : parameters) {
            String varName = symb.getName();
            if (varName.equals(node.get("name")))
                return symb.getType().getName() + (symb.getType().isArray() ? "[]" : "");
        }*/
        return "undefined";
    }

    public static String getNodeType(JmmNode node, SymbolTable analysis){
        String kind = node.getKind();

        if(isMathExpression(kind)) return "int";
        if(isBooleanExpression(kind)) return "boolean";

        switch (kind){
            case "Dot":
                return getReturnValueMethod(node,analysis);
            case "ArrayAccess":
                return "int";
            case "NewObject":
                return node.getChildren().get(0).get("name");
            case "NewIntArray":
                return "int[]";
            default:
                // Identifier
                return getVariableType(node,analysis);
        }

    }




    public static boolean isMathExpression(String kind) {
        return kind.equals("Mult") || kind.equals("Add") || kind.equals("Sub") || kind.equals("Div");
    }

    public static boolean isBooleanExpression(String kind) {
        return kind.equals("Less") || kind.equals("And") || kind.equals("Not");
    }

    public static Boolean isOperator(String kind) {
        return kind.equals("Add") ||
                kind.equals("Mult") ||
                kind.equals("Sub") ||
                kind.equals("Div") ||
                kind.equals("Less") ||
                kind.equals("And") ||
                kind.equals("ArrayAccess")||
                kind.equals("ArrayExpression")||
                kind.equals("ArrayAssignment");
    }

    public static String getReturnValueMethod(JmmNode dotNode, SymbolTable symbolTable) {
        JmmNode leftNode = dotNode.getChildren().get(0);
        JmmNode rigthNode = dotNode.getChildren().get(1);

        String typeName = UtilsAnalyser.getNodeType(leftNode, symbolTable);
        String className = symbolTable.getClassName();

        if(rigthNode.getKind().equals("Length")) return "int";

        String methodName = dotNode.getChildren().get(1).getChildren().get(0).get("name");
        boolean containsMethodName = symbolTable.getMethods().contains(methodName);

        if (containsMethodName && (typeName.equals(className) || dotNode.getKind().equals("This"))) {
            Type returnType = symbolTable.getReturnType(methodName);
            return returnType.getName() + (returnType.isArray() ? "[]" : "");
        }

        return "undefined";
    }


    public static boolean hasImport(String checkImport, SymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) return true;
        }
        return false;
    }
}
