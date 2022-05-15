package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.jmm.analysis.table.SymbolTable;
import pt.up.fe.comp.jmm.ast.JmmNode;

import java.util.List;
public class UtilsAnalyser {

    public static boolean hasImport(String checkImport, SymbolTable symbolTable){
        for(String importName : symbolTable.getImports()) {
            String[] splitImport = importName.split("\\.");
            if (splitImport[splitImport.length - 1].equals(checkImport)) {
                System.out.println("AQUIIIIIIIIIIIIII");
                return true;
            }
        }
        System.out.println("false :(");
        return false;
    }

    public static String getParentMethodName(JmmNode node) {
        JmmNode currentNode = node;
        while (!currentNode.getKind().equals("MethodGeneric") && !currentNode.getKind().equals("MethodMain")) {
            currentNode = currentNode.getJmmParent();
        }
        if (currentNode.getKind().equals("MethodGeneric"))
            return currentNode.getChildren().get(1).get("name");
        return "main";
    }
}
