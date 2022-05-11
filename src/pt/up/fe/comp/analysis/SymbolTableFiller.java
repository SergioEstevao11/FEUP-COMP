package pt.up.fe.comp.analysis;

import pt.up.fe.comp.jmm.analysis.table.Symbol;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;

public class SymbolTableFiller extends PreorderJmmVisitor<SymbolTableBuilder,Integer> {

    public SymbolTableFiller(){
        // addVisit(kind, method);
    }

    private Integer importDeclVisit(JmmNode importDecl, SymbolTableBuilder symbolTable){
        return 0;
    }
    
}
