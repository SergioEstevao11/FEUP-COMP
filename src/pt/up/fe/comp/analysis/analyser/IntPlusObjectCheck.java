package pt.up.fe.comp.analysis.analyser;

import pt.up.fe.comp.analysis.SymbolTableBuilder;
import pt.up.fe.comp.jmm.ast.JmmNode;
import pt.up.fe.comp.jmm.ast.PreorderJmmVisitor;
import pt.up.fe.comp.jmm.report.Report;
import pt.up.fe.comp.jmm.report.Stage;

import java.util.ArrayList;
import java.util.List;

public class IntPlusObjectCheck extends PreorderJmmVisitor<Integer, Integer> {
    private final SymbolTableBuilder symbolTable;
    private final List<Report> reports;

    public IntPlusObjectCheck(SymbolTableBuilder symbolTable, List<Report> reports) {
        this.reports = reports;
        this.symbolTable = symbolTable;

        addVisit("Plus", this::visitIntPlusObject);

        setDefaultVisit((node, oi) -> 0);
    }
    public Integer visitIntPlusObject(JmmNode plusNode, Integer ret){
        String method_name = plusNode.getAncestor("MethodDeclaration").get().getJmmChild(0).getJmmChild(1).get("name");
        String left_node_name = plusNode.getJmmChild(0).get("name");
        String left_node_type = symbolTable.getVariableType(method_name,left_node_name).getName();
        String right_node_name = plusNode.getJmmChild(1).get("name");
        String right_node_type = symbolTable.getVariableType(method_name,right_node_name).getName();

        JmmNode rightNode = plusNode.getJmmChild(1);
        JmmNode leftNode = plusNode.getJmmChild(0);

        if (symbolTable.isObject(method_name,left_node_name) && rightNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (symbolTable.isObject(method_name,right_node_name) && leftNode.getKind().equals("Number")){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + leftNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (left_node_type.equals("int") && symbolTable.isObject(method_name,right_node_name)){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        else if (right_node_type.equals("int") && symbolTable.isObject(method_name,left_node_name)){
            reports.add(Report.newError(Stage.SEMANTIC, -1, -1, "\"" + rightNode + "\" invalid type: expecting an boolean.", null));
        }
        return 0;
    }

}
