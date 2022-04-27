import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class SymbolTable_Visitor extends PreorderJmmVisitor<SymbolTable_OUR,Boolean> {

    public SymbolTable_Visitor(){
        addVisit("ClassDeclaration", this::visitClassDeclaration);
        addVisit("ImportNames", this::visitImportNames);
    }

    Boolean visitImportNames(JmmNode jmmNode, SymbolTable_OUR symbolTable){
        for(JmmNode node: jmmNode.getChildren()) {
            symbolTable.addImport(node.get("name"));
        }
        return true;
    }

    Boolean visitClassDeclaration(JmmNode jmmNode, SymbolTable_OUR symbolTable){
        symbolTable.setClassName(jmmNode.getChildren().get(0).get("name"));
        return true;
    }
}