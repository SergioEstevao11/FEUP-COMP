package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.analysis.table.Type;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class ArrayIndexNotIntCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;


    public ArrayIndexNotIntCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;
        addVisit("ArrayAccess", this::visitArrayAccess);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitArrayAccess(JmmNode arrayAccessNode, Integer ret){
        JmmNode left_node = arrayAccessNode.getJmmChild(0);
        JmmNode right_node = arrayAccessNode.getJmmChild(1);

        String method_name = null;
        if( arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getKind().equals("MainMethodHeader")) method_name = arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(0).get("name");
        else method_name = arrayAccessNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = left_node.get("name");
        String right_node_name = right_node.get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();

        if(symbolTable.isArray(method_name, left_node_name)){
            if(!right_node_type.equals("int")) reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + right_node_type + "\" invalid type: can't access an array without an int", null));
        }
        return 0;
    }

    public List<Report> getReports(){
        return reports;
    }
}
